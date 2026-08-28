package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.WishlistRepository
import com.example.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerSearchViewModel(
    private val shopRepository: ShopRepository,
    private val wishlistRepository: WishlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _searchResults = MutableStateFlow<List<ProductEntity>>(emptyList())
    val searchResults: StateFlow<List<ProductEntity>> = _searchResults
    
    private var searchJob: Job? = null

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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                shopRepository.searchProducts(query).collectLatest { results ->
                    _searchResults.value = results
                }
            }
        }
    }

    fun toggleWishlist(productId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != null) {
                val isAdded = wishlistRepository.toggleWishlist(userId, productId)
                onResult(isAdded)
            }
        }
    }
}
