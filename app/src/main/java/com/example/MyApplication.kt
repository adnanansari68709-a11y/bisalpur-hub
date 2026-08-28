package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.domain.repository.UserRepository
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.CartRepository
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.WishlistRepository
import com.example.util.SessionManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MyApplication : Application() {

    lateinit var database: AppDatabase
        private set
        
    lateinit var sessionManager: SessionManager
        private set
        
    lateinit var userRepository: UserRepository
        private set
        
    lateinit var shopRepository: ShopRepository
        private set
        
    lateinit var cartRepository: CartRepository
        private set
        
    lateinit var orderRepository: OrderRepository
        private set

    lateinit var wishlistRepository: WishlistRepository
        private set

    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "bisalpur_hub_db"
        ).fallbackToDestructiveMigration().build()
        
        sessionManager = SessionManager(this)
        userRepository = UserRepository(database.userDao())
        shopRepository = ShopRepository(database.shopDao(), database.productDao())
        cartRepository = CartRepository(database.cartDao(), database.productDao())
        orderRepository = OrderRepository(database.orderDao(), database.productDao())
        wishlistRepository = WishlistRepository(database.favoriteDao(), database.productDao())

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.example.data.local.DatabaseInitializer.seedSampleDataIfNeeded(
                database.userDao(),
                database.shopDao(),
                database.productDao()
            )
        }
    }
}
