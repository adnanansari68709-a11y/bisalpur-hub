package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Role
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.WishlistRepository
import com.example.ui.screens.customer.ProductDetailViewModel
import com.example.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WishlistAndCartRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var cartRepository: CartRepository
    private lateinit var shopRepository: ShopRepository
    private lateinit var wishlistRepository: WishlistRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        cartRepository = CartRepository(database.cartDao(), database.productDao())
        shopRepository = ShopRepository(database.shopDao(), database.productDao())
        wishlistRepository = WishlistRepository(database.favoriteDao(), database.productDao())

        runBlocking {
            database.userDao().insertUser(
                UserEntity(
                    id = 1L,
                    name = "Rahul Sharma",
                    email = "rahul@example.com",
                    phone = "+919876543210",
                    passwordHash = "hash1",
                    role = Role.CUSTOMER
                )
            )
            database.userDao().insertUser(
                UserEntity(
                    id = 2L,
                    name = "Priya Verma",
                    email = "priya@example.com",
                    phone = "+919876543211",
                    passwordHash = "hash2",
                    role = Role.CUSTOMER
                )
            )
            database.productDao().insertProduct(
                ProductEntity(
                    id = 101L,
                    shopId = 1L,
                    name = "Bisalpur Peda Sweets Box",
                    description = "Traditional pure khoya sweets",
                    price = 350.0,
                    originalPrice = 400.0,
                    category = "Sweets & Food",
                    gender = "Unisex",
                    imageUrls = listOf("https://example.com/peda.jpg"),
                    sizes = listOf("500g", "1kg"),
                    colors = listOf("Standard"),
                    stock = 50
                )
            )
            database.productDao().insertProduct(
                ProductEntity(
                    id = 102L,
                    shopId = 1L,
                    name = "Handcrafted Cotton Kurta",
                    description = "Pure cotton ethnic wear",
                    price = 799.0,
                    originalPrice = 999.0,
                    category = "Fashion",
                    gender = "Men",
                    imageUrls = listOf("https://example.com/kurta.jpg"),
                    sizes = listOf("M", "L", "XL"),
                    colors = listOf("Navy Blue", "White"),
                    stock = 20
                )
            )
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            sessionManager.clearSession()
        }
        database.close()
    }

    @Test
    fun testAddToCartDirectRepository() = runBlocking {
        // User 1 adds to cart directly
        cartRepository.addToCart(1L, 101L, 2, "500g", "Standard")

        val cartItems = cartRepository.getCartItems(1L).first()
        assertEquals(1, cartItems.size)
        assertEquals(101L, cartItems[0].productId)
        assertEquals(2, cartItems[0].quantity)
        assertEquals("500g", cartItems[0].selectedSize)
    }

    @Test
    fun testWishlistAddAndToggle() = runBlocking {
        // User 1 logs in
        sessionManager.saveUserId(1L)

        val isWishlistedInitial = wishlistRepository.isWishlisted(1L, 101L).first()
        assertFalse(isWishlistedInitial)

        // Add to wishlist
        val added = wishlistRepository.toggleWishlist(1L, 101L)
        assertTrue(added)

        val isWishlistedAfter = wishlistRepository.isWishlisted(1L, 101L).first()
        assertTrue(isWishlistedAfter)

        val wishlistItems = wishlistRepository.getWishlistProducts(1L).first()
        assertEquals(1, wishlistItems.size)
        assertEquals("Bisalpur Peda Sweets Box", wishlistItems[0].name)

        // Remove from wishlist
        val removed = wishlistRepository.toggleWishlist(1L, 101L)
        assertFalse(removed)

        val wishlistAfterRemoval = wishlistRepository.getWishlistProducts(1L).first()
        assertTrue(wishlistAfterRemoval.isEmpty())
    }

    @Test
    fun testWishlistPerUserIsolation() = runBlocking {
        // User 1 saves product 101
        wishlistRepository.addToWishlist(1L, 101L)

        // User 2 saves product 102
        wishlistRepository.addToWishlist(2L, 102L)

        val user1Wishlist = wishlistRepository.getWishlistProducts(1L).first()
        val user2Wishlist = wishlistRepository.getWishlistProducts(2L).first()

        assertEquals(1, user1Wishlist.size)
        assertEquals(101L, user1Wishlist[0].id)

        assertEquals(1, user2Wishlist.size)
        assertEquals(102L, user2Wishlist[0].id)
    }
}
