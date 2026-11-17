package com.example.data.remote

import com.example.data.model.ApiQueryParams
import com.example.data.model.ParseReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/**
 * Servicio HTTP nativo basado en HttpURLConnection para consultar el endpoint Socrata
 * oficial de datos.gov.co (conjunto de datos i3kx-3zps).
 */
class DatosGovLotteryService {

  companion object {
    const val BASE_URL = "https://www.datos.gov.co/resource/i3kx-3zps.json"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 25_000
  }

  suspend fun fetchResults(params: ApiQueryParams): Result<ParseReport> = withContext(Dispatchers.IO) {
    val urlString = buildSocrataUrl(params)
    var connection: HttpURLConnection? = null
    try {
      val url = URL(urlString)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "LotoAnalyticsColombia-Android/1.0")
      }

      val responseCode = connection.responseCode
      val stream = if (responseCode in 200..299) {
        connection.inputStream
      } else {
        connection.errorStream
      }

      val body = stream?.use { input ->
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
      }.orEmpty()

      if (responseCode !in 200..299) {
        val errorDetail = when (responseCode) {
          400 -> "Parámetros de consulta inválidos (HTTP 400)."
          404 -> "Conjunto de datos no encontrado en datos.gov.co (HTTP 404)."
          429 -> "Límite de peticiones excedido temporalmente en datos.gov.co (HTTP 429)."
          in 500..599 -> "El servidor de datos.gov.co presentó un error temporal (HTTP $responseCode)."
          else -> "Error HTTP $responseCode al consultar datos.gov.co."
        }
        return@withContext Result.failure(IOException(errorDetail))
      }

      LotteryJsonParser.parseResponse(body)
    } catch (_: UnknownHostException) {
      Result.failure(IOException("Sin conexión a Internet. Verifica tu red para consultar datos.gov.co."))
    } catch (_: SocketTimeoutException) {
      Result.failure(IOException("Tiempo de espera agotado al conectar con datos.gov.co."))
    } catch (e: IOException) {
      Result.failure(IOException("Error de comunicación de red: ${e.localizedMessage ?: "desconocido"}"))
    } catch (e: Exception) {
      Result.failure(IllegalStateException("Fallo inesperado procesando datos: ${e.localizedMessage}"))
    } finally {
      connection?.disconnect()
    }
  }

  internal fun buildSocrataUrl(params: ApiQueryParams): String {
    val queryParts = mutableListOf<String>()
    queryParts.add("\$limit=${params.limit.coerceIn(1, 2000)}")
    queryParts.add("\$offset=${params.offset.coerceAtLeast(0)}")

    val orderDir = if (params.sortDescending) "DESC" else "ASC"
    queryParts.add("\$order=" + encode("fecha_del_sorteo $orderDir, a_o_del_sorteo $orderDir"))

    val whereClauses = mutableListOf<String>()
    params.year?.let { year ->
      whereClauses.add("a_o_del_sorteo = $year")
    }
    params.month?.let { month ->
      whereClauses.add("mes_del_sorteo = $month")
    }
    params.lotteryName?.trim()?.takeIf { it.isNotEmpty() }?.let { lottery ->
      val escaped = escapeSoql(lottery)
      whereClauses.add("upper(loter_a) like upper('%$escaped%')")
    }
    params.prizeType?.trim()?.takeIf { it.isNotEmpty() }?.let { prize ->
      val escaped = escapeSoql(prize)
      whereClauses.add("upper(tipo_de_premio) like upper('%$escaped%')")
    }
    params.dateFilter?.trim()?.takeIf { it.isNotEmpty() }?.let { dateText ->
      val escaped = escapeSoql(dateText)
      whereClauses.add("fecha_del_sorteo like '%$escaped%'")
    }
    params.searchQuery?.trim()?.takeIf { it.isNotEmpty() }?.let { search ->
      val escaped = escapeSoql(search)
      whereClauses.add(
        "(upper(loter_a) like upper('%$escaped%') " +
          "OR numero_billete_ganador like '%$escaped%' " +
          "OR n_mero_del_sorteo like '%$escaped%' " +
          "OR upper(tipo_de_premio) like upper('%$escaped%'))"
      )
    }

    if (whereClauses.isNotEmpty()) {
      queryParts.add("\$where=" + encode(whereClauses.joinToString(" AND ")))
    }

    return "$BASE_URL?${queryParts.joinToString("&")}"
  }

  private fun escapeSoql(value: String): String = value.replace("'", "''")

  private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
