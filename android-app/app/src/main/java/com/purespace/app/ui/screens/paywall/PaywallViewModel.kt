package com.purespace.app.ui.screens.paywall

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.billing.BillingManager
import com.purespace.app.billing.PremiumProduct
import com.purespace.app.billing.PurchaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        // Observe billing state
        viewModelScope.launch {
            billingManager.billingState.collect { billingState ->
                _uiState.value = _uiState.value.copy(
                    availableProducts = billingState.availableProducts,
                    isLoading = false,
                    errorMessage = billingState.error
                )
                
                // Auto-select yearly plan if available
                if (_uiState.value.selectedProductId == null && billingState.availableProducts.isNotEmpty()) {
                    val yearlyProduct = billingState.availableProducts.find { 
                        it.productId == BillingManager.PREMIUM_YEARLY_SKU 
                    }
                    val defaultProduct = yearlyProduct ?: billingState.availableProducts.first()
                    _uiState.value = _uiState.value.copy(selectedProductId = defaultProduct.productId)
                }
            }
        }

        // Observe purchase results
        viewModelScope.launch {
            billingManager.purchaseResult.collect { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        Timber.d("Purchase successful: ${result.purchase.orderId}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            purchaseSuccess = true,
                            errorMessage = null
                        )
                    }
                    is PurchaseResult.Error -> {
                        Timber.e("Purchase failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is PurchaseResult.Cancelled -> {
                        Timber.d("Purchase cancelled by user")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is PurchaseResult.AlreadyOwned -> {
                        Timber.d("Product already owned")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            purchaseSuccess = true,
                            errorMessage = null
                        )
                    }
                    null -> {
                        // No result yet
                    }
                }
            }
        }
    }

    fun selectProduct(productId: String) {
        _uiState.value = _uiState.value.copy(
            selectedProductId = productId,
            errorMessage = null
        )
    }

    fun purchaseSelectedProduct(activity: ComponentActivity) {
        val selectedProductId = _uiState.value.selectedProductId
        if (selectedProductId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a plan first"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val billingResult = billingManager.launchBillingFlow(activity, selectedProductId)
                
                if (billingResult.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to start purchase: ${billingResult.debugMessage}"
                    )
                }
                // Note: Success/failure will be handled by the purchase result flow
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch billing flow")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to start purchase: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearPurchaseResult() {
        billingManager.clearPurchaseResult()
        _uiState.value = _uiState.value.copy(purchaseSuccess = false)
    }
}

data class PaywallUiState(
    val isLoading: Boolean = true,
    val availableProducts: List<PremiumProduct> = emptyList(),
    val selectedProductId: String? = null,
    val purchaseSuccess: Boolean = false,
    val errorMessage: String? = null
)
