package com.example.data.model

/**
 * Representa un registro del conjunto de datos i3kx-3zps de datos.gov.co.
 * Los nombres reflejan los 8 campos oficiales de la API Socrata.
 * El campo numero_billete_ganador se conserva como String para preservar ceros iniciales.
 */
data class LotteryDrawResult(
  val uniqueId: String,
  val a_o_del_sorteo: Int?,
  val mes_del_sorteo: Int?,
  val fecha_del_sorteo: String,
  val loter_a: String,
  val n_mero_del_sorteo: String,
  val numero_billete_ganador: String,
  val numero_serie_ganadora: String,
  val tipo_de_premio: String,
) {
  val formattedDate: String
    get() = fecha_del_sorteo.take(10).ifBlank {
      val y = a_o_del_sorteo?.toString() ?: "----"
      val m = mes_del_sorteo?.toString()?.padStart(2, '0') ?: "--"
      "$y-$m"
    }

  val numericTicketValue: Int?
    get() = digitsOnly.toIntOrNull()

  val digitsOnly: String
    get() = numero_billete_ganador.filter { it.isDigit() }

  /**
   * Normaliza a 4 cifras conservando ceros iniciales cuando corresponde al formato estándar de billete/chance.
   */
  val fourDigitTicket: String
    get() {
      val d = digitsOnly
      return when {
        d.isEmpty() -> ""
        d.length >= 4 -> d.takeLast(4)
        else -> d.padStart(4, '0')
      }
    }

  val threeDigitChance: String
    get() {
      val d = fourDigitTicket
      return if (d.length >= 3) d.takeLast(3) else d
    }

  val twoDigitEnding: String
    get() {
      val d = fourDigitTicket
      return if (d.length >= 2) d.takeLast(2) else d
    }
}

data class ParseReport(
  val rawCount: Int,
  val validRecords: List<LotteryDrawResult>,
  val discardedIncomplete: Int,
  val duplicatesRemoved: Int,
)

data class ApiQueryParams(
  val limit: Int = 50,
  val offset: Int = 0,
  val year: Int? = null,
  val month: Int? = null,
  val lotteryName: String? = null,
  val prizeType: String? = null,
  val dateFilter: String? = null,
  val searchQuery: String? = null,
  val sortDescending: Boolean = true,
)

data class FavoriteCombination(
  val id: Long = 0,
  val number: String,
  val digitsCount: Int,
  val strategyName: String,
  val referenceLottery: String,
  val justification: String,
  val digitFrequencySummary: String,
  val createdAtMillis: Long,
)

sealed interface SyncState {
  data object Idle : SyncState
  data class Loading(val message: String) : SyncState
  data class Success(val recordsCount: Int, val timestampMillis: Long) : SyncState
  data class Error(val message: String) : SyncState
}

sealed interface RemoteQueryState<out T> {
  data object Idle : RemoteQueryState<Nothing>
  data object Loading : RemoteQueryState<Nothing>
  data class Success<T>(val data: T, val hasMore: Boolean) : RemoteQueryState<T>
  data class Error(val message: String) : RemoteQueryState<Nothing>
}
