package com.example.ui.premium

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.SubscriptionPlan
import com.example.domain.model.SubscriptionStatus
import com.example.ui.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.YEARLY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aura Premium", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aura Plan Premium'a Yükseltin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gelişmiş kisiselleştirme, istatistikler ve otomatik senkronizasyon.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Plan comparison: Monthly vs Yearly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlanCard(
                    title = "Aylık Plan",
                    price = "₺49.99 / ay",
                    badge = null,
                    isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                    onClick = { selectedPlan = SubscriptionPlan.MONTHLY },
                    modifier = Modifier.weight(1f)
                )

                PlanCard(
                    title = "Yıllık Plan",
                    price = "₺29.99 / ay",
                    badge = "%40 İNDİRİM",
                    isSelected = selectedPlan == SubscriptionPlan.YEARLY,
                    onClick = { selectedPlan = SubscriptionPlan.YEARLY },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features list
            Column(modifier = Modifier.fillMaxWidth()) {
                FeatureRow("Gelişmiş Dashboard Özelleştirme Şablonları")
                FeatureRow("Sınırsız Alışkanlık & Hedef Takibi")
                FeatureRow("Bulut İki Yönlü Anlık Senkronizasyon")
                FeatureRow("Detaylı Verimlilik İstatistikleri & Dışa Aktarma")
                FeatureRow("Öncelikli Müşteri Desteği")
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (uiState.subscription.status == SubscriptionStatus.FREE && !uiState.subscription.trialUsed) {
                OutlinedButton(
                    onClick = { viewModel.startFreeTrial() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("7 Gün Ücretsiz Denemeyi Başlat")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.purchaseSubscription(selectedPlan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("buy_premium_btn")
            ) {
                Text("Aura Premium'a Katıl", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text("Satın Alımları Geri Yükle")
            }
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
