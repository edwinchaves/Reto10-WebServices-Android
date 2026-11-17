package com.example.data.remote

import com.example.data.model.LotteryDrawResult
import com.example.data.model.ParseReport
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Procesador de respuestas JSON utilizando exclusivamente org.json.JSONObject y org.json.JSONArray
 * del SDK de Android, cumpliendo el requisito académico obligatorio.
 */
object LotteryJsonParser {

  fun parseResponse(rawJson: String): Result<ParseReport> {
    val trimmed = rawJson.trim()
    if (trimmed.isEmpty()) {
      return Result.failure(IllegalStateException("La respuesta del servidor llegó vacía."))
    }

    val jsonArray: JSONArray = try {
      if (trimmed.startsWith("[")) {
        JSONArray(trimmed)
      } else if (trimmed.startsWith("{")) {
        val obj = JSONObject(trimmed)
        if (obj.has("error") || obj.has("message")) {
          val msg = obj.optString("message", obj.optString("error", "Error inesperado del API"))
          return Result.failure(IllegalStateException("Error del servicio Socrata: $msg"))
        }
        return Result.failure(IllegalStateException("Estructura JSON inesperada: se esperaba un arreglo."))
      } else {
        return Result.failure(IllegalStateException("La respuesta no contiene un arreglo JSON válido."))
      }
    } catch (e: JSONException) {
      return Result.failure(IllegalStateException("Error al interpretar el JSON recibido: ${e.localizedMessage}"))
    }

    val seenKeys = HashSet<String>(jsonArray.length())
    val validList = ArrayList<LotteryDrawResult>(jsonArray.length())
    var discarded = 0
    var duplicates = 0

    for (index in 0 until jsonArray.length()) {
      val itemObj = jsonArray.optJSONObject(index)
      if (itemObj == null) {
        discarded++
        continue
      }

      val parsed = parseSingleObject(itemObj)
      if (parsed == null) {
        discarded++
        continue
      }

      if (!seenKeys.add(parsed.uniqueId)) {
        duplicates++
        continue
      }

      validList.add(parsed)
    }

    return Result.success(
      ParseReport(
        rawCount = jsonArray.length(),
        validRecords = validList,
        discardedIncomplete = discarded,
        duplicatesRemoved = duplicates,
      )
    )
  }

  fun parseSingleObject(obj: JSONObject): LotteryDrawResult? {
    val fechaRaw = optCleanString(obj, "fecha_del_sorteo")
    val loteriaRaw = optCleanString(obj, "loter_a")
    val sorteoNumRaw = optCleanString(obj, "n_mero_del_sorteo")
    val billeteRaw = optWinningTicket(obj, "numero_billete_ganador")
    val serieRaw = optCleanString(obj, "numero_serie_ganadora")
    val premioRaw = optCleanString(obj, "tipo_de_premio")

    var year = optFlexibleInt(obj, "a_o_del_sorteo")
    var month = optFlexibleInt(obj, "mes_del_sorteo")

    // Inferir año y mes desde fecha_del_sorteo si vienen nulos en el registro
    if ((year == null || month == null) && fechaRaw.length >= 7) {
      val parts = fechaRaw.take(10).split("-")
      if (parts.size >= 2) {
        if (year == null) year = parts[0].toIntOrNull()
        if (month == null) month = parts[1].toIntOrNull()?.takeIf { it in 1..12 }
      }
    }

    // Validar que el registro tenga información mínima usable:
    // Lotería no vacía y número ganador con al menos un dígito.
    val digits = billeteRaw.filter { it.isDigit() }
    if (loteriaRaw.isBlank() || digits.isEmpty()) {
      return null
    }

    val normalizedTicket = if (billeteRaw.all { it.isDigit() }) {
      billeteRaw
    } else {
      digits
    }

    val normalizedLottery = loteriaRaw.trim().replace(Regex("\\s+"), " ")
    val normalizedPrize = premioRaw.ifBlank { "Sin especificar" }
    val normalizedDraw = sorteoNumRaw.ifBlank { "S/N" }
    val normalizedSeries = serieRaw.ifBlank { "N/A" }

    val compositeKey = buildUniqueId(
      lottery = normalizedLottery,
      date = fechaRaw.take(10),
      drawNumber = normalizedDraw,
      ticket = normalizedTicket,
      series = normalizedSeries,
      prize = normalizedPrize,
    )

    return LotteryDrawResult(
      uniqueId = compositeKey,
      a_o_del_sorteo = year,
      mes_del_sorteo = month,
      fecha_del_sorteo = fechaRaw,
      loter_a = normalizedLottery,
      n_mero_del_sorteo = normalizedDraw,
      numero_billete_ganador = normalizedTicket,
      numero_serie_ganadora = normalizedSeries,
      tipo_de_premio = normalizedPrize,
    )
  }

  private fun optCleanString(obj: JSONObject, key: String): String {
    if (!obj.has(key) || obj.isNull(key)) return ""
    val value = obj.optString(key, "").trim()
    return if (value.equals("null", ignoreCase = true) || value.equals("undefined", ignoreCase = true)) {
      ""
    } else {
      value
    }
  }

  /**
   * Extrae numero_billete_ganador como cadena conservando estrictamente ceros iniciales.
   */
  private fun optWinningTicket(obj: JSONObject, key: String): String {
    if (!obj.has(key) || obj.isNull(key)) return ""
    val rawAny = obj.opt(key) ?: return ""
    val asText = rawAny.toString().trim()
    if (asText.equals("null", ignoreCase = true)) return ""
    return asText
  }

  private fun optFlexibleInt(obj: JSONObject, key: String): Int? {
    if (!obj.has(key) || obj.isNull(key)) return null
    val raw = obj.opt(key) ?: return null
    return when (raw) {
      is Number -> raw.toInt()
      is String -> raw.trim().toDoubleOrNull()?.toInt()
      else -> raw.toString().trim().toDoubleOrNull()?.toInt()
    }
  }

  private fun buildUniqueId(
    lottery: String,
    date: String,
    drawNumber: String,
    ticket: String,
    series: String,
    prize: String,
  ): String {
    val raw = "${lottery.uppercase()}|$date|$drawNumber|$ticket|$series|${prize.uppercase()}"
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
    return digest.take(12).joinToString("") { "%02x".format(it) }
  }
}
