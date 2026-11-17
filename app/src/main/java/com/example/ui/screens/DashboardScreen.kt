package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SyncState
import com.example.ui.components.DigitFrequencyBarChart
import com.example.ui.components.DrawResultCard
import com.example.ui.components.EmptyDataStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.components.HistogramBellCurveChart
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.SurfaceNavyHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseAccent
import com.example.ui.theme.TurquoiseBright
import com.example.ui.theme.VioletLight
import com.example.ui.viewmodel.DashboardUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  uiState: DashboardUiState,
  onSyncClick: () -> Unit,
  onLoadMoreHistory: () -> Unit,
  onLotterySelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Image(
              painter = painterResource(id = R.drawable.img_dashboard_hero_1794877260539),
              contentDescription = "Fondo analítico LotoAnalytics Colombia",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .matchParentSize()
                .alpha(0.32f),
            )
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      DeepNavyBg.copy(alpha = 0.45f),
                      SurfaceNavy.copy(alpha = 0.94f),
                    )
                  )
                ),
            )

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Surface(
                  color = ElectricBlueContainer.copy(alpha = 0.85f),
                  shape = RoundedCornerShape(50),
                  border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                ) {
                  Text(
                    text = "datos.gov.co · i3kx-3zps",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = TurquoiseBright,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Última actualización",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = formatTimestamp(uiState.lastSyncMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                  )
                }
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "LotoAnalytics Colombia",
                  style = MaterialTheme.typography.headlineMedium,
                  color = TextPrimary,
                )
                Text(
                  text = "Análisis estadístico descriptivo de sorteos históricos y exploración de combinaciones para Chance.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                val isLoading = uiState.syncState is SyncState.Loading
                Button(
                  onClick = onSyncClick,
                  enabled = !isLoading,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("sync_data_button"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = TextPrimary,
                  ),
                ) {
                  if (isLoading) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      strokeWidth = 2.dp,
                      color = TextPrimary,
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Outlined.Refresh,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(if (isLoading) "Sincronizando…" else "Sincronizar datos")
                }

                if (uiState.totalRecords > 0) {
                  OutlinedButton(
                    onClick = onLoadMoreHistory,
                    enabled = !isLoading,
                    modifier = Modifier.testTag("load_more_history_button"),
                    border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TurquoiseBright),
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Download,
                      contentDescription = "Ampliar historial",
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ampliar")
                  }
                }
              }
            }
          }
        }
      }

      // Sync Status Feedback
      item {
        AnimatedVisibility(visible = uiState.syncState is SyncState.Error) {
          val err = uiState.syncState as? SyncState.Error
          if (err != null) {
            ErrorBannerCard(
              message = err.message,
              onRetry = onSyncClick,
            )
          }
        }
        AnimatedVisibility(visible = uiState.syncState is SyncState.Success) {
          val ok = uiState.syncState as? SyncState.Success
          if (ok != null) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = EmeraldSuccess.copy(alpha = 0.14f),
              border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Sincronización completada: ${ok.recordsCount} registros válidos procesados (descartados: ${uiState.discardedRecords}, duplicados: ${uiState.duplicatesRemoved}).",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextPrimary,
                )
              }
            }
          }
        }
      }

      if (uiState.totalRecords == 0 && uiState.syncState !is SyncState.Loading) {
        item {
          EmptyDataStateCard(
            title = "Sin registros locales",
            subtitle = "Conéctate al Catálogo de Datos Abiertos (datos.gov.co) para descargar y analizar los sorteos oficiales de loterías colombianas.",
            actionLabel = "Sincronizar ahora",
            onAction = onSyncClick,
          )
        }
      } else if (uiState.totalRecords > 0) {
        // Key KPIs
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            StatMetricCard(
              label = "Registros consultados",
              value = uiState.totalRecords.toString(),
              subtitle = "Base oficial depurada",
              icon = Icons.Outlined.Dataset,
              accentColor = ElectricBlueLight,
              modifier = Modifier.weight(1f),
            )
            StatMetricCard(
              label = "Loterías activas",
              value = uiState.availableLotteries.size.toString(),
              subtitle = "Toca una abajo para filtrar",
              icon = Icons.Outlined.ConfirmationNumber,
              accentColor = TurquoiseBright,
              modifier = Modifier.weight(1f),
            )
          }
        }

        // Latest Draw Spotlight
        uiState.latestDraw?.let { latest ->
          item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Último resultado registrado",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
              DrawResultCard(
                draw = latest,
                badgeText = "Más reciente",
                badgeColor = TurquoiseBright,
              )
            }
          }
        }

        // Available Lotteries Chips
        if (uiState.availableLotteries.isNotEmpty()) {
          item {
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
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = VioletLight,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Loterías disponibles (${uiState.availableLotteries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                  )
                }
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  uiState.availableLotteries.take(18).forEach { lottery ->
                    Surface(
                      modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onLotterySelected(lottery) },
                      color = SurfaceNavyHighlight,
                      shape = RoundedCornerShape(50),
                      border = BorderStroke(1.dp, BorderSubtle),
                    ) {
                      Text(
                        text = lottery,
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlueLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Digit Distribution Bar Chart
        if (uiState.digitDistribution.isNotEmpty()) {
          item {
            DigitFrequencyBarChart(
              title = "Distribución general de dígitos (0–9)",
              items = uiState.digitDistribution,
            )
          }
        }

        // Highest & Lowest Frequency Digits
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            FrequencyRankingCard(
              title = "Mayor frecuencia",
              items = uiState.mostFrequentDigits.map { "Dígito ${it.label}" to "${it.count} (${"%.1f".format(it.percentage)}%)" },
              icon = Icons.Outlined.TrendingUp,
              accent = TurquoiseBright,
              modifier = Modifier.weight(1f),
            )
            FrequencyRankingCard(
              title = "Menor frecuencia",
              items = uiState.leastFrequentDigits.map { "Dígito ${it.label}" to "${it.count} (${"%.1f".format(it.percentage)}%)" },
              icon = Icons.Outlined.TrendingDown,
              accent = VioletLight,
              modifier = Modifier.weight(1f),
            )
          }
        }

        // Whole-number overview chart
        uiState.wholeNumberOverview?.let { bell ->
          item {
            HistogramBellCurveChart(analysis = bell)
          }
        }

        // Top 2-digit endings preview
        if (uiState.topTwoDigitEndings.isNotEmpty()) {
          item {
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
                Text(
                  text = "Terminaciones de 2 cifras más repetidas",
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary,
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  uiState.topTwoDigitEndings.forEach { item ->
                    Surface(
                      modifier = Modifier.weight(1f),
                      color = SurfaceNavyHighlight,
                      shape = RoundedCornerShape(12.dp),
                      border = BorderStroke(1.dp, DeepViolet.copy(alpha = 0.45f)),
                    ) {
                      Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                      ) {
                        Text(
                          text = item.label,
                          fontFamily = JetBrainsMonoFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 18.sp,
                          color = TurquoiseBright,
                        )
                        Text(
                          text = "${item.count}x",
                          style = MaterialTheme.typography.labelSmall,
                          color = TextSecondary,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        item {
          StatisticalDisclaimerBanner()
        }
      }
    }
  }
}

@Composable
private fun FrequencyRankingCard(
  title: String,
  items: List<Pair<String, String>>,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accent: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accent,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = TextPrimary,
        )
      }
      items.forEach { (label, value) ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
          )
          Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent,
          )
        }
      }
    }
  }
}

private fun formatTimestamp(millis: Long?): String {
  if (millis == null) return "Sin sincronizar"
  val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "CO"))
  return sdf.format(Date(millis))
}
