package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.domain.repository.OrderRepository
import com.example.util.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CancelOrderUiState {
    object Idle : CancelOrderUiState()
    object Submitting : CancelOrderUiState()
    data class Success(val message: String = "Your order has been cancelled successfully.", val orderId: Long) : CancelOrderUiState()
    data class Error(val message: String) : CancelOrderUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val orders: StateFlow<List<OrderEntity>> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                orderRepository.getUserOrders(userId)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _cancelState = MutableStateFlow<CancelOrderUiState>(CancelOrderUiState.Idle)
    val cancelState: StateFlow<CancelOrderUiState> = _cancelState.asStateFlow()

    private val _orderItemsMap = MutableStateFlow<Map<Long, List<OrderItemEntity>>>(emptyMap())
    val orderItemsMap: StateFlow<Map<Long, List<OrderItemEntity>>> = _orderItemsMap.asStateFlow()

    fun loadOrderItems(orderId: Long) {
        if (_orderItemsMap.value.containsKey(orderId)) return
        viewModelScope.launch {
            val items = orderRepository.getOrderItems(orderId)
            _orderItemsMap.value = _orderItemsMap.value + (orderId to items)
        }
    }

    fun cancelOrder(orderId: Long, reason: String, onCompleted: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _cancelState.value = CancelOrderUiState.Submitting
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _cancelState.value = CancelOrderUiState.Error("User session not found. Please log in again.")
                onCompleted?.invoke(false)
                return@launch
            }

            val result = orderRepository.cancelOrder(orderId, userId, reason)
            result.onSuccess { cancelledOrder ->
                _cancelState.value = CancelOrderUiState.Success(
                    message = "Your order has been cancelled successfully.",
                    orderId = cancelledOrder.id
                )
                onCompleted?.invoke(true)
            }.onFailure { ex ->
                _cancelState.value = CancelOrderUiState.Error(
                    ex.message ?: "Failed to cancel order. Please try again."
                )
                onCompleted?.invoke(false)
            }
        }
    }

    fun resetCancelState() {
        _cancelState.value = CancelOrderUiState.Idle
    }
}

