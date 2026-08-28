package com.example.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ShopEntity
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SellerShopViewModel(
    private val shopRepository: ShopRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentShop: StateFlow<ShopEntity?> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                shopRepository.getShopBySellerIdFlow(userId)
            } else {
                MutableStateFlow(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun createShop(name: String, description: String) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != null) {
                shopRepository.createShop(
                    ShopEntity(
                        sellerId = userId,
                        name = name,
                        description = description,
                        logoUrl = null
                    )
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}
