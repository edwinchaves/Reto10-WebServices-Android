package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FavoriteCombination
import com.example.data.model.LotteryDrawResult

@Entity(tableName = "lottery_results")
data class LotteryResultEntity(
  @PrimaryKey val uniqueId: String,
  val year: Int?,
  val month: Int?,
  val drawDate: String,
  val lotteryName: String,
  val drawNumber: String,
  val winningTicket: String,
  val winningSeries: String,
  val prizeType: String,
)

@Entity(tableName = "favorite_combinations")
data class FavoriteCombinationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val number: String,
  val digitsCount: Int,
  val strategyName: String,
  val referenceLottery: String,
  val justification: String,
  val digitFrequencySummary: String,
  val createdAtMillis: Long,
)

fun LotteryResultEntity.toDomain(): LotteryDrawResult =
  LotteryDrawResult(
    uniqueId = uniqueId,
    a_o_del_sorteo = year,
    mes_del_sorteo = month,
    fecha_del_sorteo = drawDate,
    loter_a = lotteryName,
    n_mero_del_sorteo = drawNumber,
    numero_billete_ganador = winningTicket,
    numero_serie_ganadora = winningSeries,
    tipo_de_premio = prizeType,
  )

fun LotteryDrawResult.toEntity(): LotteryResultEntity =
  LotteryResultEntity(
    uniqueId = uniqueId,
    year = a_o_del_sorteo,
    month = mes_del_sorteo,
    drawDate = fecha_del_sorteo,
    lotteryName = loter_a,
    drawNumber = n_mero_del_sorteo,
    winningTicket = numero_billete_ganador,
    winningSeries = numero_serie_ganadora,
    prizeType = tipo_de_premio,
  )

fun FavoriteCombinationEntity.toDomain(): FavoriteCombination =
  FavoriteCombination(
    id = id,
    number = number,
    digitsCount = digitsCount,
    strategyName = strategyName,
    referenceLottery = referenceLottery,
    justification = justification,
    digitFrequencySummary = digitFrequencySummary,
    createdAtMillis = createdAtMillis,
  )

fun FavoriteCombination.toEntity(): FavoriteCombinationEntity =
  FavoriteCombinationEntity(
    id = id,
    number = number,
    digitsCount = digitsCount,
    strategyName = strategyName,
    referenceLottery = referenceLottery,
    justification = justification,
    digitFrequencySummary = digitFrequencySummary,
    createdAtMillis = createdAtMillis,
  )
