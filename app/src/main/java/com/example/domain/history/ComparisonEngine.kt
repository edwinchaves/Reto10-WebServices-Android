package com.example.domain.history

import com.example.data.model.FavoriteCombination
import com.example.data.model.LotteryDrawResult

enum class MatchType(val label: String) {
  EXACT_4("Coincidencia exacta 4 cifras"),
  EXACT_3("Coincidencia 3 últimas cifras"),
  EXACT_2("Coincidencia 2 últimas cifras"),
}

data class HistoricalMatch(
  val draw: LotteryDrawResult,
  val matchType: MatchType,
)

data class FavoriteComparisonSummary(
  val favorite: FavoriteCombination,
  val exactFourMatches: Int,
  val exactThreeMatches: Int,
  val exactTwoMatches: Int,
  val lastDigitMatches: Int,
  val matchedDraws: List<HistoricalMatch>,
)

object ComparisonEngine {

  fun compareFavoriteWithHistory(
    favorite: FavoriteCombination,
    draws: List<LotteryDrawResult>,
  ): FavoriteComparisonSummary {
    val target = favorite.number.filter { it.isDigit() }
    if (target.isEmpty() || draws.isEmpty()) {
      return FavoriteComparisonSummary(
        favorite = favorite,
        exactFourMatches = 0,
        exactThreeMatches = 0,
        exactTwoMatches = 0,
        lastDigitMatches = 0,
        matchedDraws = emptyList(),
      )
    }

    var exact4 = 0
    var exact3 = 0
    var exact2 = 0
    var last1 = 0
    val matchedList = ArrayList<HistoricalMatch>()

    val target3 = target.takeLast(3)
    val target2 = target.takeLast(2)
    val target1 = target.takeLast(1)

    for (draw in draws) {
      val ticket4 = draw.fourDigitTicket
      val ticket3 = draw.threeDigitChance
      val ticket2 = draw.twoDigitEnding
      val ticket1 = ticket4.takeLast(1)

      if (ticket1 == target1) {
        last1++
      }

      if (favorite.digitsCount == 4 && ticket4 == target) {
        exact4++
        matchedList.add(HistoricalMatch(draw, MatchType.EXACT_4))
      } else if (ticket3 == target3 && target3.length == 3) {
        exact3++
        matchedList.add(HistoricalMatch(draw, MatchType.EXACT_3))
      } else if (ticket2 == target2 && target2.length == 2) {
        exact2++
        if (matchedList.size < 40) {
          matchedList.add(HistoricalMatch(draw, MatchType.EXACT_2))
        }
      }
    }

    // Ordenar matches priorizando coincidencias más exactas y luego fecha reciente
    val sortedMatches = matchedList.sortedWith(
      compareBy<HistoricalMatch> { it.matchType.ordinal }
        .thenByDescending { it.draw.formattedDate }
    )

    return FavoriteComparisonSummary(
      favorite = favorite,
      exactFourMatches = exact4,
      exactThreeMatches = exact3,
      exactTwoMatches = exact2,
      lastDigitMatches = last1,
      matchedDraws = sortedMatches,
    )
  }
}
