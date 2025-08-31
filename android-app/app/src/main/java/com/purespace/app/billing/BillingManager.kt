package com.purespace.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val PREMIUM_MONTHLY_SKU = "premium_monthly"
        const val PREMIUM_YEARLY_SKU = "premium_yearly"
        const val PREMIUM_LIFETIME_SKU = "premium_lifetime"
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()

    private var availableProducts = emptyList<ProductDetails>()

    init {
        startConnection()
    }

    private fun startConnection() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("Billing client connected successfully")
            _billingState.value = _billingState.value.copy(isConnected = true)
            
            // Query available products and existing purchases
            queryProducts()
            queryPurchases()
        } else {
            Timber.e("Billing setup failed: ${billingResult.debugMessage}")
            _billingState.value = _billingState.value.copy(
                isConnected = false,
                error = billingResult.debugMessage
            )
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.w("Billing service disconnected")
        _billingState.value = _billingState.value.copy(isConnected = false)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("Purchase canceled by user")
                _purchaseResult.value = PurchaseResult.Cancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.d("Item already owned")
                _purchaseResult.value = PurchaseResult.AlreadyOwned
            }
            else -> {
                Timber.e("Purchase failed: ${billingResult.debugMessage}")
                _purchaseResult.value = PurchaseResult.Error(billingResult.debugMessage)
            }
        }
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_MONTHLY_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_YEARLY_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_LIFETIME_SKU)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                availableProducts = productDetailsList
                _billingState.value = _billingState.value.copy(
                    availableProducts = productDetailsList.map { it.toPremiumProduct() }
                )
                Timber.d("Found ${productDetailsList.size} available products")
            } else {
                Timber.e("Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    private fun queryPurchases() {
        // Query subscription purchases
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handleExistingPurchases(purchases)
            }
        }

        // Query in-app purchases (lifetime)
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handleExistingPurchases(purchases)
            }
        }
    }

    private fun handleExistingPurchases(purchases: List<Purchase>) {
        val activePurchases = purchases.filter { 
            it.purchaseState == Purchase.PurchaseState.PURCHASED 
        }
        
        val hasPremium = activePurchases.any { purchase ->
            purchase.products.any { productId ->
                productId in listOf(PREMIUM_MONTHLY_SKU, PREMIUM_YEARLY_SKU, PREMIUM_LIFETIME_SKU)
            }
        }

        _billingState.value = _billingState.value.copy(
            hasPremium = hasPremium,
            activePurchases = activePurchases
        )

        // Acknowledge unacknowledged purchases
        activePurchases.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Verify purchase with backend
            verifyPurchaseWithBackend(purchase)
            
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            
            _purchaseResult.value = PurchaseResult.Success(purchase)
            
            // Update premium status
            val hasPremium = purchase.products.any { productId ->
                productId in listOf(PREMIUM_MONTHLY_SKU, PREMIUM_YEARLY_SKU, PREMIUM_LIFETIME_SKU)
            }
            
            if (hasPremium) {
                _billingState.value = _billingState.value.copy(hasPremium = true)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Purchase acknowledged successfully")
            } else {
                Timber.e("Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    private fun verifyPurchaseWithBackend(purchase: Purchase) {
        // TODO: Implement backend verification
        Timber.d("Verifying purchase with backend: ${purchase.orderId}")
    }

    suspend fun launchBillingFlow(activity: Activity, productId: String): BillingResult {
        return suspendCancellableCoroutine { continuation ->
            val product = availableProducts.find { it.productId == productId }
            
            if (product == null) {
                continuation.resume(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
                        .setDebugMessage("Product not found")
                        .build()
                )
                return@suspendCancellableCoroutine
            }

            val offerToken = when (productId) {
                PREMIUM_MONTHLY_SKU, PREMIUM_YEARLY_SKU -> {
                    product.subscriptionOfferDetails?.firstOrNull()?.offerToken
                }
                else -> null
            }

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .apply { 
                        offerToken?.let { setOfferToken(it) }
                    }
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            continuation.resume(billingResult)
        }
    }

    fun clearPurchaseResult() {
        _purchaseResult.value = null
    }

    private fun ProductDetails.toPremiumProduct(): PremiumProduct {
        return PremiumProduct(
            productId = productId,
            title = title,
            description = description,
            price = when (productType) {
                BillingClient.ProductType.SUBS -> {
                    subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""
                }
                BillingClient.ProductType.INAPP -> {
                    oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                }
                else -> ""
            },
            productType = when (productType) {
                BillingClient.ProductType.SUBS -> PremiumProductType.SUBSCRIPTION
                BillingClient.ProductType.INAPP -> PremiumProductType.ONE_TIME
                else -> PremiumProductType.ONE_TIME
            }
        )
    }
}

data class BillingState(
    val isConnected: Boolean = false,
    val hasPremium: Boolean = false,
    val availableProducts: List<PremiumProduct> = emptyList(),
    val activePurchases: List<Purchase> = emptyList(),
    val error: String? = null
)

sealed class PurchaseResult {
    data class Success(val purchase: Purchase) : PurchaseResult()
    object Cancelled : PurchaseResult()
    object AlreadyOwned : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

data class PremiumProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val productType: PremiumProductType
)

enum class PremiumProductType {
    SUBSCRIPTION,
    ONE_TIME
}
