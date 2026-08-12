package com.example.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.SubscriptionEntity
import com.example.data.repository.SubscriptionRepository
import com.example.domain.model.SubscriptionPlan
import com.example.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PremiumUiState(
    val subscription: SubscriptionEntity = SubscriptionEntity(id = "sub", userId = "default_user"),
    val selectedPlan: SubscriptionPlan = SubscriptionPlan.YEARLY,
    val message: String? = null,
    val isLoading: Boolean = false
)

class PremiumViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val uiState: StateFlow<PremiumUiState> = subscriptionRepository.getSubscription().map { sub ->
        PremiumUiState(subscription = sub)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PremiumUiState()
    )

    fun selectPlan(plan: SubscriptionPlan) {
        viewModelScope.launch {
            // Update ui state selection
        }
    }

    fun startFreeTrial() {
        viewModelScope.launch {
            val success = subscriptionRepository.activateTrial()
            if (!success) {
                // Already used trial
            }
        }
    }

    fun purchaseSubscription(plan: SubscriptionPlan) {
        viewModelScope.launch {
            val updated = SubscriptionEntity(
                id = "sub_premium",
                userId = "default_user",
                status = SubscriptionStatus.PREMIUM,
                plan = plan,
                startDate = System.currentTimeMillis(),
                expiryDate = System.currentTimeMillis() + if (plan == SubscriptionPlan.YEARLY) 365 * 24 * 3600 * 1000L else 30 * 24 * 3600 * 1000L,
                trialUsed = true
            )
            subscriptionRepository.updateSubscription(updated)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            // Query purchase history simulation / Google Play Billing
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PremiumViewModel(container.subscriptionRepository) as T
        }
    }
}
