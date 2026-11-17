package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotteryDrawResult
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.SurfaceNavyHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseAccent
import com.example.ui.theme.TurquoiseBright
import com.example.ui.theme.TurquoiseContainer

@Composable
fun LotteryBallDisplay(
  number: String,
  modifier: Modifier = Modifier,
  ballSize: Dp = 40.dp,
  accentColor: Color = ElectricBlue,
) {
  val chars = number.ifBlank { "----" }.toList()
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    chars.forEach { ch ->
      Box(
        modifier = Modifier
          .size(ballSize)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(
                accentColor.copy(alpha = 0.28f),
                DeepViolet.copy(alpha = 0.22f),
              )
            )
          )
          .border(
            width = 1.2.dp,
            color = accentColor.copy(alpha = 0.7f),
            shape = CircleShape,
          ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = ch.toString(),
          fontFamily = JetBrainsMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = if (ballSize >= 42.dp) 18.sp else 15.sp,
          color = TextPrimary,
        )
      }
    }
  }
}

@Composable
fun StatMetricCard(
  label: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  accentColor: Color = ElectricBlueLight,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = accentColor,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = value,
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = accentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun DrawResultCard(
  draw: LotteryDrawResult,
  modifier: Modifier = Modifier,
  badgeText: String? = null,
  badgeColor: Color = TurquoiseBright,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = draw.loter_a,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${draw.formattedDate} · Sorteo #${draw.n_mero_del_sorteo}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
          )
        }
        if (!badgeText.isNullOrBlank()) {
          Surface(
            color = badgeColor.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              color = badgeColor,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Billete Ganador",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
          )
          LotteryBallDisplay(
            number = draw.numero_billete_ganador,
            ballSize = 36.dp,
            accentColor = ElectricBlue,
          )
        }

        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "Serie",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
          )
          Surface(
            color = SurfaceNavyHighlight,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BorderSubtle),
          ) {
            Text(
              text = draw.numero_serie_ganadora,
              fontFamily = JetBrainsMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TurquoiseBright,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }
      }

      Surface(
        color = ElectricBlueContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = draw.tipo_de_premio,
            style = MaterialTheme.typography.labelMedium,
            color = ElectricBlueLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Text(
            text = "Año ${draw.a_o_del_sorteo ?: "--"}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
          )
        }
      }
    }
  }
}

@Composable
fun StatisticalDisclaimerBanner(
  title: String = "Independencia de sorteos",
  body: String = "Los sorteos de lotería son eventos independientes. El análisis estadístico y las frecuencias históricas son descriptivos y no predicen ni garantizan resultados futuros.",
  isWarningStyle: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val containerColor = if (isWarningStyle) AmberContainer.copy(alpha = 0.55f) else TurquoiseContainer.copy(alpha = 0.45f)
  val borderColor = if (isWarningStyle) AmberWarning.copy(alpha = 0.45f) else TurquoiseAccent.copy(alpha = 0.45f)
  val iconTint = if (isWarningStyle) AmberWarning else TurquoiseBright

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        imageVector = if (isWarningStyle) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
        contentDescription = title,
        tint = iconTint,
        modifier = Modifier.size(20.dp),
      )
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = iconTint,
        )
        Text(
          text = body,
          style = MaterialTheme.typography.bodySmall,
          color = TextPrimary.copy(alpha = 0.9f),
        )
      }
    }
  }
}

@Composable
fun EmptyDataStateCard(
  title: String,
  subtitle: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Outlined.CloudOff,
        contentDescription = title,
        tint = ElectricBlueLight,
        modifier = Modifier.size(40.dp),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onAction,
          modifier = Modifier.testTag("empty_state_action_button"),
          colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
        ) {
          Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(actionLabel)
        }
      }
    }
  }
}

@Composable
fun ErrorBannerCard(
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = RoseContainer,
    border = BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = "Error de conexión o procesamiento",
          style = MaterialTheme.typography.labelLarge,
          color = RoseError,
        )
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall,
          color = TextPrimary,
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onRetry,
        modifier = Modifier.testTag("error_retry_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = RoseError,
          contentColor = TextPrimary,
        ),
      ) {
        Text("Reintentar")
      }
    }
  }
}
