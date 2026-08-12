package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.SyncUiState
import com.example.domain.model.TaskPriority

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SyncStatusBanner(syncState: SyncUiState) {
    AnimatedVisibility(
        visible = syncState !is SyncUiState.Synced,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val (bgColor, icon, text) = when (syncState) {
            is SyncUiState.Syncing -> Triple(
                MaterialTheme.colorScheme.primaryContainer,
                Icons.Default.Sync,
                "Bulut ile senkronize ediliyor (${syncState.pendingCount} öge)..."
            )
            is SyncUiState.PendingOffline -> Triple(
                MaterialTheme.colorScheme.tertiaryContainer,
                Icons.Default.CloudOff,
                "Çevrimdışı mod. ${syncState.pendingCount} değişiklik yerelde saklandı."
            )
            is SyncUiState.Error -> Triple(
                MaterialTheme.colorScheme.errorContainer,
                Icons.Default.ErrorOutline,
                "Senkronizasyon hatası: ${syncState.message}"
            )
            else -> Triple(Color.Transparent, Icons.Default.Check, "")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Sync",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    val (bgColor, textColor, label) = when (priority) {
        TaskPriority.URGENT -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Acil")
        TaskPriority.HIGH -> Triple(Color(0xFFFFEDD5), Color(0xFFEA580C), "Yüksek")
        TaskPriority.MEDIUM -> Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), "Orta")
        TaskPriority.LOW -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), "Düşük")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.testTag("empty_state_action_btn")
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun SkeletonShimmerCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    ) {}
}
