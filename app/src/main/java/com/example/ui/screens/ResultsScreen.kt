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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import com.example.data.model.LotteryDrawResult
import com.example.data.model.RemoteQueryState
import com.example.ui.components.DrawResultCard
import com.example.ui.components.EmptyDataStateCard
import com.example.ui.components.ErrorBannerCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseBright
import com.example.ui.viewmodel.ResultsUiState

private val MONTH_NAMES = listOf(
  1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril",
  5 to "Mayo", 6 to "Junio", 7 to "Julio", 8 to "Agosto",
  9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
  uiState: ResultsUiState,
  onSearchChange: (String) -> Unit,
  onDateFilterChange: (String) -> Unit,
  onYearSelected: (Int?) -> Unit,
  onMonthSelected: (Int?) -> Unit,
  onLotterySelected: (String) -> Unit,
  onPrizeTypeSelected: (String) -> Unit,
  onToggleSort: () -> Unit,
  onApplyQuery: () -> Unit,
  onClearFilters: () -> Unit,
  onNextPage: () -> Unit,
  onPreviousPage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var filtersExpanded by remember { mutableStateOf(true) }
  val filters = uiState.filters

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header + Search
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Resultados Oficiales",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
              )
              Text(
                text = "Consulta directa en datos.gov.co con filtros Socrata",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
              )
            }
            FilledTonalButton(
              onClick = { filtersExpanded = !filtersExpanded },
              modifier = Modifier.testTag("toggle_filters_button"),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = ElectricBlueContainer,
                contentColor = ElectricBlueLight,
              ),
            ) {
              Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filtros",
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (filtersExpanded) "Ocultar" else "Filtros")
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = filters.searchQuery,
              onValueChange = onSearchChange,
              placeholder = { Text("Buscar billete, lotería o sorteo…") },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
              },
              trailingIcon = {
                if (filters.searchQuery.isNotEmpty()) {
                  IconButton(onClick = {
                    onSearchChange("")
                    onApplyQuery()
                  }) {
                    Icon(Icons.Outlined.Clear, contentDescription = "Limpiar búsqueda")
                  }
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("results_search_input"),
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceNavyElevated,
                unfocusedContainerColor = SurfaceNavyElevated,
              ),
            )

            Button(
              onClick = onApplyQuery,
              modifier = Modifier
                .height(54.dp)
                .testTag("results_search_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            ) {
              Text("Consultar")
            }
          }
        }
      }

      // Filter Card
      item {
        AnimatedVisibility(visible = filtersExpanded) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BorderStroke(1.dp, BorderSubtle),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                // Year selector
                SimpleDropdownSelector(
                  label = "Año",
                  selectedLabel = filters.year?.toString() ?: "Todos",
                  options = listOf("Todos") + uiState.availableYears.map { it.toString() },
                  onSelect = { chosen ->
                    onYearSelected(if (chosen == "Todos") null else chosen.toIntOrNull())
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "filter_year_dropdown",
                )

                // Month selector
                SimpleDropdownSelector(
                  label = "Mes",
                  selectedLabel = filters.month?.let { m ->
                    MONTH_NAMES.firstOrNull { it.first == m }?.second ?: m.toString()
                  } ?: "Todos",
                  options = listOf("Todos") + MONTH_NAMES.map { it.second },
                  onSelect = { chosen ->
                    val monthNum = MONTH_NAMES.firstOrNull { it.second == chosen }?.first
                    onMonthSelected(monthNum)
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "filter_month_dropdown",
                )
              }

              // Lottery selector
              SimpleDropdownSelector(
                label = "Lotería",
                selectedLabel = filters.lotteryName.ifBlank { "Todas las loterías" },
                options = listOf("Todas las loterías") + uiState.availableLotteries,
                onSelect = { chosen ->
                  onLotterySelected(if (chosen == "Todas las loterías") "" else chosen)
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "filter_lottery_dropdown",
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                SimpleDropdownSelector(
                  label = "Tipo de premio",
                  selectedLabel = filters.prizeType.ifBlank { "Todos" },
                  options = listOf("Todos") + uiState.availablePrizeTypes,
                  onSelect = { chosen ->
                    onPrizeTypeSelected(if (chosen == "Todos") "" else chosen)
                  },
                  modifier = Modifier.weight(1f),
                  testTag = "filter_prize_dropdown",
                )

                OutlinedTextField(
                  value = filters.dateFilter,
                  onValueChange = onDateFilterChange,
                  label = { Text("Fecha (AAAA-MM-DD)") },
                  placeholder = { Text("2024-05") },
                  singleLine = true,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("filter_date_input"),
                  shape = RoundedCornerShape(12.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderSubtle,
                  ),
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedButton(
                  onClick = onToggleSort,
                  modifier = Modifier.testTag("sort_order_button"),
                  border = BorderStroke(1.dp, BorderSubtle),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = "Cambiar orden",
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (filters.sortDescending) "Fecha: Recientes" else "Fecha: Antiguos")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.testTag("clear_filters_button"),
                  ) {
                    Text("Limpiar", color = TextSecondary)
                  }
                  Button(
                    onClick = onApplyQuery,
                    modifier = Modifier.testTag("apply_filters_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                  ) {
                    Text("Aplicar")
                  }
                }
              }
            }
          }
        }
      }

      // Query state rendering
      when (val state = uiState.queryState) {
        RemoteQueryState.Idle,
        RemoteQueryState.Loading -> {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                CircularProgressIndicator(color = TurquoiseBright)
                Text(
                  text = "Consultando servidor Socrata datos.gov.co…",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary,
                )
              }
            }
          }
        }

        is RemoteQueryState.Error -> {
          item {
            ErrorBannerCard(
              message = state.message,
              onRetry = onApplyQuery,
            )
          }
        }

        is RemoteQueryState.Success<List<LotteryDrawResult>> -> {
          if (state.data.isEmpty()) {
            item {
              EmptyDataStateCard(
                title = "Sin coincidencias",
                subtitle = "No se encontraron sorteos para la combinación de filtros enviada a datos.gov.co.",
                actionLabel = "Limpiar filtros",
                onAction = onClearFilters,
              )
            }
          } else {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Mostrando ${state.data.size} registros",
                  style = MaterialTheme.typography.labelLarge,
                  color = TurquoiseBright,
                )
                Text(
                  text = "Página ${filters.page + 1}",
                  fontFamily = JetBrainsMonoFamily,
                  style = MaterialTheme.typography.labelMedium,
                  color = TextSecondary,
                )
              }
            }

            items(state.data, key = { it.uniqueId }) { draw ->
              DrawResultCard(draw = draw)
            }

            // Pagination Bar
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedButton(
                  onClick = onPreviousPage,
                  enabled = filters.page > 0,
                  modifier = Modifier.testTag("pagination_prev_button"),
                  border = BorderStroke(1.dp, BorderSubtle),
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Página anterior",
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Anterior")
                }

                Text(
                  text = "Página ${filters.page + 1}",
                  fontFamily = JetBrainsMonoFamily,
                  style = MaterialTheme.typography.labelLarge,
                  color = TextPrimary,
                )

                Button(
                  onClick = onNextPage,
                  enabled = state.hasMore,
                  modifier = Modifier.testTag("pagination_next_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                ) {
                  Text("Siguiente")
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Página siguiente",
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdownSelector(
  label: String,
  selectedLabel: String,
  options: List<String>,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "",
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value = selectedLabel,
      onValueChange = {},
      readOnly = true,
      singleLine = true,
      label = { Text(label) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .fillMaxWidth()
        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        .let { if (testTag.isNotEmpty()) it.testTag(testTag) else it },
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElectricBlue,
        unfocusedBorderColor = BorderSubtle,
        focusedContainerColor = SurfaceNavyElevated,
        unfocusedContainerColor = SurfaceNavyElevated,
      ),
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      containerColor = SurfaceNavyElevated,
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option, color = TextPrimary) },
          onClick = {
            expanded = false
            onSelect(option)
          },
        )
      }
    }
  }
}
