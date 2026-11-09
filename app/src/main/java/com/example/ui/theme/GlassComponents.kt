package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ObsidianBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CobaltPrimary.copy(alpha = 0.14f),
                        DeepNavySurface.copy(alpha = 0.45f),
                        ObsidianBlack
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = size.maxDimension * 0.72f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.78f),
                    radius = size.maxDimension * 0.55f
                )
            )
            val step = 48.dp.toPx()
            val gridColor = GlassBorder.copy(alpha = 0.16f)
            var x = 0f
            while (x <= size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentBorder: Boolean = false,
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (accentBorder) {
        Brush.linearGradient(
            listOf(
                NeonCyan.copy(alpha = 0.55f),
                CobaltPrimary.copy(alpha = 0.35f),
                GlassBorder.copy(alpha = 0.45f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                GlassBorder.copy(alpha = 0.65f),
                GlassBorder.copy(alpha = 0.22f)
            )
        )
    }

    val cardModifier = modifier
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    SlateGlass.copy(alpha = 0.86f),
                    DeepNavySurface.copy(alpha = 0.92f)
                )
            )
        )
        .border(width = 1.dp, brush = borderBrush, shape = shape)
        .let { base ->
            if (onClick != null) base.clickable(onClick = onClick) else base
        }

    Column(
        modifier = cardModifier.padding(contentPadding),
        content = content
    )
}

enum class BadgeTone {
    ONLINE,
    PENDING,
    ALERT,
    INFO,
    MUTED
}

@Composable
fun StatusBadge(
    text: String,
    tone: BadgeTone,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        BadgeTone.ONLINE -> EmeraldContainer to EmeraldOnline
        BadgeTone.PENDING -> AmberContainer to AmberPending
        BadgeTone.ALERT -> CrimsonContainer to CrimsonAlert
        BadgeTone.INFO -> CobaltContainer to NeonCyan
        BadgeTone.MUTED -> ElevatedGlass to TextSecondary
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VisibleMonitoringBanner(
    isMonitoringActive: Boolean,
    isStreamActive: Boolean,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "monitor_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    val bannerColor by animateColorAsState(
        targetValue = if (isMonitoringActive || isStreamActive) {
            CobaltContainer.copy(alpha = 0.9f)
        } else {
            DeepNavySurface.copy(alpha = 0.9f)
        },
        label = "banner_bg"
    )
    val dotColor = when {
        isStreamActive -> EmeraldOnline
        isMonitoringActive -> NeonCyan
        else -> TextMuted
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bannerColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = if (isMonitoringActive || isStreamActive) alpha else 0.7f))
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (isMonitoringActive) Icons.Rounded.Visibility else Icons.Rounded.Shield,
                    contentDescription = "Monitoring visibility status",
                    tint = dotColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when {
                        isStreamActive -> "LIVE SCREEN & TELEMETRY INDICATOR ACTIVE"
                        isMonitoringActive -> "CONSENTED TELEMETRY INDICATOR VISIBLE"
                        else -> "STANDBY • ZERO BACKGROUND CAPTURE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isMonitoringActive || isStreamActive) {
                TextButton(
                    onClick = onDisconnectClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("disconnect_monitoring_banner_button")
                ) {
                    Text(
                        text = "Pause",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonAlert,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(NeonCyan, CobaltPrimary)))
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
        if (actionLabel != null && onActionClick != null) {
            OutlinedButton(
                onClick = onActionClick,
                border = BorderStroke(1.dp, GlassBorder),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("section_action_${actionLabel.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    detail: String,
    icon: ImageVector,
    tint: Color = NeonCyan,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = tint.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ConfirmAuthorizationDialog(
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavySurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) CrimsonAlert else CobaltPrimary
                ),
                modifier = Modifier.testTag("confirm_auth_dialog_button")
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_auth_dialog_button")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
