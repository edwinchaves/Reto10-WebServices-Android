package com.example

import com.example.data.model.FavoriteCombination
import com.example.data.model.LotteryDrawResult
import com.example.data.remote.DatosGovLotteryService
import com.example.data.remote.LotteryJsonParser
import com.example.domain.generator.ChanceGeneratorEngine
import com.example.domain.generator.ChanceModality
import com.example.domain.generator.GenerationStrategy
import com.example.domain.generator.HistoricalWindow
import com.example.domain.history.ComparisonEngine
import com.example.domain.statistics.AnalysisTarget
import com.example.domain.statistics.StatisticalEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoAnalyticsUnitTest {

  @Test
  fun `json parser preserves leading zeros removes duplicates and discards incomplete records`() {
    val sampleJson = """
      [
        {
          "a_o_del_sorteo": "2024",
          "mes_del_sorteo": "5",
          "fecha_del_sorteo": "2024-05-16T00:00:00.000",
          "loter_a": "LOTERIA DE BOGOTA",
          "n_mero_del_sorteo": "2742",
          "numero_billete_ganador": "0042",
          "numero_serie_ganadora": "089",
          "tipo_de_premio": "Premio Mayor"
        },
        {
          "a_o_del_sorteo": 2024,
          "mes_del_sorteo": 5,
          "fecha_del_sorteo": "2024-05-16T00:00:00.000",
          "loter_a": "LOTERIA DE BOGOTA",
          "n_mero_del_sorteo": "2742",
          "numero_billete_ganador": "0042",
          "numero_serie_ganadora": "089",
          "tipo_de_premio": "Premio Mayor"
        },
        {
          "a_o_del_sorteo": null,
          "loter_a": "",
          "numero_billete_ganador": ""
        },
        {
          "a_o_del_sorteo": 2023,
          "mes_del_sorteo": 12,
          "fecha_del_sorteo": "2023-12-21T00:00:00.000",
          "loter_a": "LOTERIA DE MEDELLIN",
          "n_mero_del_sorteo": "4709",
          "numero_billete_ganador": "0915",
          "numero_serie_ganadora": null,
          "tipo_de_premio": "Premio Mayor"
        }
      ]
    """.trimIndent()

    val report = LotteryJsonParser.parseResponse(sampleJson).getOrThrow()
    assertEquals(4, report.rawCount)
    assertEquals(2, report.validRecords.size)
    assertEquals(1, report.duplicatesRemoved)
    assertEquals(1, report.discardedIncomplete)
    assertEquals("0042", report.validRecords[0].numero_billete_ganador)
    assertEquals("0915", report.validRecords[1].numero_billete_ganador)
    assertEquals(2024, report.validRecords[0].a_o_del_sorteo)
  }

  @Test
  fun `statistical engine computes descriptive metrics and warns on discrete digits`() {
    val draws = (0 until 40).map { idx ->
      val ticket = (idx * 250).toString().padStart(4, '0')
      LotteryDrawResult(
        uniqueId = "id_$idx",
        a_o_del_sorteo = if (idx < 20) 2023 else 2024,
        mes_del_sorteo = (idx % 12) + 1,
        fecha_del_sorteo = if (idx < 20) "2023-06-10" else "2024-06-10",
        loter_a = "LOTERIA DE PRUEBA",
        n_mero_del_sorteo = "${1000 + idx}",
        numero_billete_ganador = ticket,
        numero_serie_ganadora = "010",
        tipo_de_premio = "Premio Mayor",
      )
    }

    val wholeAnalysis = StatisticalEngine.analyzeBellCurve(draws, AnalysisTarget.WHOLE_NUMBER)
    assertTrue(wholeAnalysis.isNormalFitAppropriate)
    assertEquals(40, wholeAnalysis.stats.sampleSize)
    assertTrue(wholeAnalysis.stats.stdDev > 0.0)

    val digitAnalysis = StatisticalEngine.analyzeBellCurve(draws, AnalysisTarget.ALL_DIGITS)
    assertFalse(digitAnalysis.isNormalFitAppropriate)
    assertEquals(160, digitAnalysis.stats.sampleSize)
  }

  @Test
  fun `generator creates exact digit lengths for all four strategies and comparison finds matches`() {
    val draws = listOf(
      LotteryDrawResult("1", 2024, 5, "2024-05-10", "LOTERIA A", "100", "0482", "01", "Mayor"),
      LotteryDrawResult("2", 2024, 5, "2024-05-17", "LOTERIA A", "101", "9482", "02", "Mayor"),
    )

    GenerationStrategy.entries.forEach { strategy ->
      val threeDigit = ChanceGeneratorEngine.generateCombinations(
        draws = draws,
        modality = ChanceModality.THREE_DIGITS,
        strategy = strategy,
        referenceLottery = "Todas",
        window = HistoricalWindow.ALL,
        quantity = 3,
      )
      assertEquals(3, threeDigit.size)
      assertTrue(threeDigit.all { it.number.length == 3 && it.number.all(Char::isDigit) })
    }

    val fav = FavoriteCombination(
      id = 1L,
      number = "0482",
      digitsCount = 4,
      strategyName = "Frec. Histórica",
      referenceLottery = "Todas",
      justification = "Test",
      digitFrequencySummary = "0|4|8|2",
      createdAtMillis = 0L,
    )
    val comparison = ComparisonEngine.compareFavoriteWithHistory(fav, draws)
    assertEquals(1, comparison.exactFourMatches)
    assertEquals(1, comparison.exactThreeMatches)
  }

  @Test
  fun `socrata url builder encodes filters cleanly`() {
    val service = DatosGovLotteryService()
    val url = service.buildSocrataUrl(
      com.example.data.model.ApiQueryParams(
        limit = 30,
        offset = 60,
        year = 2024,
        month = 5,
        lotteryName = "BOGOTA",
      )
    )
    assertTrue(url.contains("\$limit=30"))
    assertTrue(url.contains("\$offset=60"))
    assertTrue(url.contains("a_o_del_sorteo"))
    assertTrue(url.contains("mes_del_sorteo"))
  }
}
