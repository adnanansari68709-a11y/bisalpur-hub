package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserEntity
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.WishlistRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerHomeViewModel(
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val allProducts = shopRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProfile: StateFlow<UserEntity?> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                userRepository.getUserFlow(userId)
            } else {
                MutableStateFlow(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val wishlistedIds: StateFlow<Set<Long>> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                wishlistRepository.getWishlistedProductIds(userId)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun toggleWishlist(productId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != null) {
                val isAdded = wishlistRepository.toggleWishlist(userId, productId)
                onResult(isAdded)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}
