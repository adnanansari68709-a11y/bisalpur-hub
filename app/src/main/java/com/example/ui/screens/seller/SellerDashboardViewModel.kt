package com.example.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class SellerDashboardViewModel(
    private val shopRepository: ShopRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val shopFlow = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                shopRepository.getShopBySellerIdFlow(userId)
            } else {
                MutableStateFlow(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
        
    val ordersFlow = shopFlow.flatMapLatest { shop ->
        if (shop != null) {
            orderRepository.getShopOrders(shop.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val productsFlow = shopFlow.flatMapLatest { shop ->
        if (shop != null) {
            shopRepository.getProductsByShop(shop.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
