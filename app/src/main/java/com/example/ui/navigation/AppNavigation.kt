package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.MyApplication
import com.example.domain.model.Role
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.customer.CustomerMainScreen
import com.example.ui.screens.seller.SellerMainScreen

@Composable
fun AppNavigation(userId: Long?, app: MyApplication) {
    if (userId == null) {
        AuthScreen(onAuthSuccess = { /* Automatically handled by userId state change */ })
    } else {
        // We need to know the user's role
        val userFlow = app.userRepository.getUserFlow(userId)
        val user by userFlow.collectAsState(initial = null)
        
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (user!!.role) {
                Role.CUSTOMER -> CustomerMainScreen(app)
                Role.SELLER -> SellerMainScreen(app)
                Role.ADMIN -> Box {} // Admin flow not implemented yet
            }
        }
    }
}
