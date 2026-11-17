package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.LotoAppContainer
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.LotoAnalyticsTheme
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseBright
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.GeneratorViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.ResultsViewModel
import com.example.ui.viewmodel.StatisticsViewModel

enum class AppTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  INICIO("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
  RESULTADOS("Resultados", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
  ESTADISTICAS("Estadísticas", Icons.Filled.Analytics, Icons.Outlined.Analytics),
  GENERADOR("Generador", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
  HISTORIAL("Historial", Icons.Filled.History, Icons.Outlined.History),
}

class MainActivity : ComponentActivity() {

  private val appContainer: LotoAppContainer by lazy { LotoAppContainer(applicationContext) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(DeepNavyBg.toArgb()),
      navigationBarStyle = SystemBarStyle.dark(SurfaceNavy.toArgb()),
    )
    setContent {
      LotoAnalyticsTheme {
        LotoAnalyticsApp(appContainer = appContainer)
      }
    }
  }
}

@Composable
fun LotoAnalyticsApp(appContainer: LotoAppContainer) {
  val repository = appContainer.lotteryRepository

  val dashboardVm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
  val resultsVm: ResultsViewModel = viewModel(factory = ResultsViewModel.Factory(repository))
  val statisticsVm: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory(repository))
  val generatorVm: GeneratorViewModel = viewModel(factory = GeneratorViewModel.Factory(repository))
  val historyVm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(repository))

  val dashboardState by dashboardVm.uiState.collectAsStateWithLifecycle()
  val resultsState by resultsVm.uiState.collectAsStateWithLifecycle()
  val statisticsState by statisticsVm.uiState.collectAsStateWithLifecycle()
  val generatorState by generatorVm.uiState.collectAsStateWithLifecycle()
  val historyState by historyVm.uiState.collectAsStateWithLifecycle()

  var currentTab by rememberSaveable { mutableStateOf(AppTab.INICIO) }
  val snackbarHostState = remember { SnackbarHostState() }

  // Mostrar mensajes de confirmación de guardado/eliminación de favoritas
  LaunchedEffect(generatorState.feedbackMessage) {
    generatorState.feedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      generatorVm.clearFeedback()
    }
  }
  LaunchedEffect(historyState.feedbackMessage) {
    historyState.feedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      historyVm.clearFeedback()
    }
  }

  // BackHandler para volver a Inicio desde cualquier pestaña secundaria
  BackHandler(enabled = currentTab != AppTab.INICIO) {
    currentTab = AppTab.INICIO
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = DeepNavyBg,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(
            containerColor = SurfaceNavy,
            contentColor = TextPrimary,
          ) {
            AppTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = TurquoiseBright,
                  selectedTextColor = TurquoiseBright,
                  indicatorColor = ElectricBlueContainer,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary,
                ),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (useNavigationRail) {
          NavigationRail(
            containerColor = SurfaceNavy,
            contentColor = TextPrimary,
          ) {
            AppTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                modifier = Modifier.testTag("rail_${tab.name.lowercase()}"),
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = TurquoiseBright,
                  selectedTextColor = TurquoiseBright,
                  indicatorColor = ElectricBlueContainer,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary,
                ),
              )
            }
          }
        }

        when (currentTab) {
          AppTab.INICIO -> {
            DashboardScreen(
              uiState = dashboardState,
              onSyncClick = { dashboardVm.synchronizeData(fetchMorePages = false) },
              onLoadMoreHistory = { dashboardVm.synchronizeData(fetchMorePages = true) },
              onLotterySelected = { lottery ->
                resultsVm.updateLottery(lottery)
                currentTab = AppTab.RESULTADOS
              },
            )
          }

          AppTab.RESULTADOS -> {
            ResultsScreen(
              uiState = resultsState,
              onSearchChange = resultsVm::updateSearchQuery,
              onDateFilterChange = resultsVm::updateDateFilter,
              onYearSelected = resultsVm::updateYear,
              onMonthSelected = resultsVm::updateMonth,
              onLotterySelected = resultsVm::updateLottery,
              onPrizeTypeSelected = resultsVm::updatePrizeType,
              onToggleSort = resultsVm::toggleSortOrder,
              onApplyQuery = resultsVm::executeQuery,
              onClearFilters = resultsVm::clearFilters,
              onNextPage = resultsVm::nextPage,
              onPreviousPage = resultsVm::previousPage,
            )
          }

          AppTab.ESTADISTICAS -> {
            StatisticsScreen(
              uiState = statisticsState,
              onSelectSection = statisticsVm::selectSection,
              onSelectTarget = statisticsVm::selectTarget,
              onSelectLottery = statisticsVm::selectLottery,
              onSelectComparisonYears = statisticsVm::selectComparisonYears,
              onNavigateToSync = { currentTab = AppTab.INICIO },
            )
          }

          AppTab.GENERADOR -> {
            GeneratorScreen(
              uiState = generatorState,
              onModalitySelected = generatorVm::setModality,
              onStrategySelected = generatorVm::setStrategy,
              onLotterySelected = generatorVm::setReferenceLottery,
              onWindowSelected = generatorVm::setHistoricalWindow,
              onQuantityChanged = generatorVm::setQuantity,
              onGenerateClick = generatorVm::generate,
              onSaveFavoriteClick = generatorVm::saveToFavorites,
            )
          }

          AppTab.HISTORIAL -> {
            HistoryScreen(
              uiState = historyState,
              onSelectTab = historyVm::selectTab,
              onSearchQueryChange = historyVm::updateSearchQuery,
              onDeleteFavorite = historyVm::deleteFavorite,
              onNavigateToGenerator = { currentTab = AppTab.GENERADOR },
            )
          }
        }
      }
    }
  }
}
