package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.history.FavoriteComparisonSummary
import com.example.ui.components.DrawResultCard
import com.example.ui.components.EmptyDataStateCard
import com.example.ui.components.LotteryBallDisplay
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.SurfaceNavyHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseBright
import com.example.ui.theme.VioletLight
import com.example.ui.viewmodel.HistoryTab
import com.example.ui.viewmodel.HistoryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  uiState: HistoryUiState,
  onSelectTab: (HistoryTab) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onDeleteFavorite: (Long) -> Unit,
  onNavigateToGenerator: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Historial y Comparación",
          style = MaterialTheme.typography.headlineSmall,
          color = TextPrimary,
        )
        PrimaryTabRow(
          selectedTabIndex = uiState.activeTab.ordinal,
          containerColor = SurfaceNavy,
          contentColor = TurquoiseBright,
        ) {
          HistoryTab.entries.forEach { tab ->
            Tab(
              selected = uiState.activeTab == tab,
              onClick = { onSelectTab(tab) },
              modifier = Modifier.testTag("history_tab_${tab.name.lowercase()}"),
              text = {
                Text(
                  text = tab.label,
                  style = MaterialTheme.typography.labelLarge,
                  maxLines = 1,
                )
              },
            )
          }
        }
      }

      when (uiState.activeTab) {
        HistoryTab.FAVORITES_COMPARE -> {
          FavoritesCompareTab(
            comparisons = uiState.comparisons,
            onDeleteFavorite = onDeleteFavorite,
            onNavigateToGenerator = onNavigateToGenerator,
          )
        }
        HistoryTab.HISTORICAL_LOG -> {
          HistoricalLogTab(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
          )
        }
      }
    }
  }
}

@Composable
private fun FavoritesCompareTab(
  comparisons: List<FavoriteComparisonSummary>,
  onDeleteFavorite: (Long) -> Unit,
  onNavigateToGenerator: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    if (comparisons.isEmpty()) {
      item {
        EmptyDataStateCard(
          title = "Sin combinaciones favoritas",
          subtitle = "Guarda combinaciones desde el Generador para compararlas automáticamente contra los sorteos históricos descargados.",
          actionLabel = "Ir al Generador",
          onAction = onNavigateToGenerator,
        )
      }
    } else {
      // Summary card for all saved favorites
      item {
        val totalExact4 = comparisons.sumOf { it.exactFourMatches }
        val totalExact3 = comparisons.sumOf { it.exactThreeMatches }
        val totalExact2 = comparisons.sumOf { it.exactTwoMatches }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Outlined.CompareArrows,
                contentDescription = null,
                tint = TurquoiseBright,
              )
              Text(
                text = "Resumen de Combinaciones Guardadas (${comparisons.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              MiniCounter("4 cifras", totalExact4, TurquoiseBright, Modifier.weight(1f))
              MiniCounter("3 últimas", totalExact3, ElectricBlueLight, Modifier.weight(1f))
              MiniCounter("2 últimas", totalExact2, VioletLight, Modifier.weight(1f))
            }
          }
        }
      }

      items(comparisons, key = { it.favorite.id }) { summary ->
        FavoriteComparisonCard(
          summary = summary,
          onDelete = { onDeleteFavorite(summary.favorite.id) },
        )
      }
    }
  }
}

@Composable
private fun FavoriteComparisonCard(
  summary: FavoriteComparisonSummary,
  onDelete: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val fav = summary.favorite

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LotteryBallDisplay(
          number = fav.number,
          ballSize = 38.dp,
          accentColor = ElectricBlue,
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("delete_favorite_${fav.id}"),
        ) {
          Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "Eliminar favorita",
            tint = RoseError,
          )
        }
      }

      Text(
        text = "${fav.strategyName} · ${fav.digitsCount} cifras · Ref: ${fav.referenceLottery}",
        style = MaterialTheme.typography.labelMedium,
        color = TurquoiseBright,
      )

      Text(
        text = "Frecuencias: ${fav.digitFrequencySummary}",
        fontFamily = JetBrainsMonoFamily,
        fontSize = 11.sp,
        color = VioletLight,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MiniCounter("Pleno 4", summary.exactFourMatches, TurquoiseBright, Modifier.weight(1f))
        MiniCounter("3 cifras", summary.exactThreeMatches, ElectricBlueLight, Modifier.weight(1f))
        MiniCounter("2 cifras", summary.exactTwoMatches, VioletLight, Modifier.weight(1f))
        MiniCounter("Últ. 1", summary.lastDigitMatches, TextSecondary, Modifier.weight(1f))
      }

      if (summary.matchedDraws.isNotEmpty()) {
        TextButton(
          onClick = { expanded = !expanded },
          modifier = Modifier.testTag("toggle_matches_${fav.id}"),
        ) {
          Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (expanded) "Ocultar coincidencias (${summary.matchedDraws.size})"
            else "Ver coincidencias históricas (${summary.matchedDraws.size})",
            color = ElectricBlueLight,
          )
        }

        AnimatedVisibility(visible = expanded) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            summary.matchedDraws.take(15).forEach { match ->
              Surface(
                color = SurfaceNavyHighlight,
                shape = RoundedCornerShape(10.dp),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = match.draw.loter_a,
                      style = MaterialTheme.typography.labelMedium,
                      color = TextPrimary,
                    )
                    Text(
                      text = "${match.draw.formattedDate} · ${match.matchType.label}",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary,
                    )
                  }
                  Text(
                    text = match.draw.numero_billete_ganador,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TurquoiseBright,
                  )
                }
              }
            }
          }
        }
      } else {
        Text(
          text = "Sin coincidencias de 2, 3 o 4 cifras en los sorteos descargados.",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
        )
      }
    }
  }
}

@Composable
private fun HistoricalLogTab(
  uiState: HistoryUiState,
  onSearchQueryChange: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      OutlinedTextField(
        value = uiState.searchNumberQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Filtrar historial local por número, lotería o fecha…") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_local_search"),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ElectricBlue,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = SurfaceNavyElevated,
          unfocusedContainerColor = SurfaceNavyElevated,
        ),
      )
    }

    if (uiState.historicalDraws.isEmpty()) {
      item {
        EmptyDataStateCard(
          title = "Sin sorteos en el historial",
          subtitle = "Sincroniza desde Inicio o ajusta tu búsqueda local.",
        )
      }
    } else {
      items(uiState.historicalDraws, key = { it.uniqueId }) { draw ->
        DrawResultCard(draw = draw)
      }
    }
  }
}

@Composable
private fun MiniCounter(
  label: String,
  count: Int,
  color: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = SurfaceNavyHighlight,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = count.toString(),
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = color,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
      )
    }
  }
}
