package com.example.di

import android.content.Context
import com.example.data.local.LotoDatabase
import com.example.data.local.SyncPreferences
import com.example.data.remote.DatosGovLotteryService
import com.example.data.repository.LotteryRepository

class LotoAppContainer(context: Context) {
  private val database: LotoDatabase by lazy { LotoDatabase.getInstance(context) }
  private val apiService: DatosGovLotteryService by lazy { DatosGovLotteryService() }
  private val syncPreferences: SyncPreferences by lazy { SyncPreferences(context) }

  val lotteryRepository: LotteryRepository by lazy {
    LotteryRepository(
      apiService = apiService,
      lotteryDao = database.lotteryDao(),
      syncPreferences = syncPreferences,
    )
  }
}
