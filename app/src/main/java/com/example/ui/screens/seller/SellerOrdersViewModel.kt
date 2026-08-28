package com.example.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.OrderEntity
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SellerOrdersViewModel(
    private val shopRepository: ShopRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val orders: StateFlow<List<OrderEntity>> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                shopRepository.getShopBySellerIdFlow(userId).flatMapLatest { shop ->
                    if (shop != null) {
                        orderRepository.getShopOrders(shop.id)
                    } else {
                        MutableStateFlow(emptyList())
                    }
                }
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateOrderStatus(order: OrderEntity, newStatus: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(order, newStatus)
        }
    }
}
