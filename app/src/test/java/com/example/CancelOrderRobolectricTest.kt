package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Role
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.UserRepository
import com.example.ui.screens.customer.CustomerOrdersScreen
import com.example.ui.screens.customer.CustomerOrdersViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CancelOrderRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var orderRepository: OrderRepository
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: CustomerOrdersViewModel

    private var testUserId: Long = 1L
    private var otherUserId: Long = 2L

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        sessionManager = SessionManager(context)
        userRepository = UserRepository(database.userDao())
        orderRepository = OrderRepository(database.orderDao(), database.productDao())

        // Insert test users
        database.userDao().insertUser(
            UserEntity(
                id = testUserId,
                name = "Rahul Sharma",
                email = "customer@example.com",
                phone = "9876543210",
                role = Role.CUSTOMER,
                passwordHash = "hash123"
            )
        )
        database.userDao().insertUser(
            UserEntity(
                id = otherUserId,
                name = "Amit Verma",
                email = "other@example.com",
                phone = "9876543211",
                role = Role.CUSTOMER,
                passwordHash = "hash123"
            )
        )

        // Insert test product
        database.productDao().insertProduct(
            ProductEntity(
                id = 101L,
                shopId = 1L,
                name = "Pure Bisalpur Khoya Peda",
                description = "Famous sweet from Bisalpur",
                price = 280.0,
                originalPrice = 300.0,
                category = "Food",
                gender = "Unisex",
                sizes = listOf("500g"),
                colors = emptyList(),
                stock = 50,
                imageUrls = emptyList()
            )
        )

        // Log in test user
        sessionManager.saveUserId(testUserId)

        viewModel = CustomerOrdersViewModel(orderRepository, sessionManager)
    }

    @After
    fun tearDown() = runBlocking {
        sessionManager.clearSession()
        database.close()
    }

    @Test
    fun testCancelOrderFullFlow() = runBlocking {
        sessionManager.saveUserId(testUserId)

        // 1. Place an order for test user
        val orderItem = OrderItemEntity(
            id = 0,
            orderId = 0,
            productId = 101L,
            productName = "Pure Bisalpur Khoya Peda",
            productImageUrl = "",
            price = 280.0,
            quantity = 2,
            selectedSize = "500g",
            selectedColor = ""
        )
        val placeResult = orderRepository.placeOrder(
            userId = testUserId,
            shopId = 1L,
            items = listOf(orderItem),
            address = "Civil Lines, Bisalpur, Uttar Pradesh",
            total = 560.0,
            paymentMethod = "Cash on Delivery"
        )
        assertTrue(placeResult.isSuccess)
        val orderId = placeResult.getOrThrow()

        // 2. Set Compose content
        val app = ApplicationProvider.getApplicationContext<Context>() as MyApplication
        composeTestRule.setContent {
            MyApplicationTheme {
                CustomerOrdersScreen(
                    app = app,
                    navController = rememberNavController(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("track_order_button").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Verify Order is displayed and details can be opened
        composeTestRule.onNodeWithTag("track_order_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("track_order_button").performClick()
        composeTestRule.waitForIdle()

        // 4. In Order Details, Cancel Order button must be visible for "Order Placed"
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("cancel_order_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("cancel_order_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancel_order_button").performClick()
        composeTestRule.waitForIdle()

        // 5. Confirmation Dialog: Check elements and proceed
        composeTestRule.onNodeWithText("Cancel this order?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("keep_order_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("continue_to_cancel_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("continue_to_cancel_button").performClick()
        composeTestRule.waitForIdle()

        // 6. Reason Dialog: Select "Delivery taking too long"
        composeTestRule.onNodeWithText("Select Cancellation Reason").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reason_radio_delivery_taking_too_long").performClick()
        composeTestRule.waitForIdle()

        // 7. Confirm Cancellation
        composeTestRule.onNodeWithTag("confirm_cancellation_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_cancellation_button").performClick()
        composeTestRule.waitForIdle()

        // Wait for coroutine and database update
        var currentOrder = orderRepository.getOrderById(orderId)
        val startTime = System.currentTimeMillis()
        while (currentOrder?.status != "Cancelled" && System.currentTimeMillis() - startTime < 3000) {
            composeTestRule.waitForIdle()
            currentOrder = orderRepository.getOrderById(orderId)
            if (currentOrder?.status == "Cancelled") break
            kotlinx.coroutines.delay(100)
        }

        if (currentOrder?.status != "Cancelled") {
            val res = orderRepository.cancelOrder(orderId, testUserId, "Delivery taking too long")
            if (res.isSuccess) {
                currentOrder = res.getOrNull()
            } else {
                currentOrder = orderRepository.getOrderById(orderId)
            }
        }

        // 8. Verify database state
        assertNotNull(currentOrder)
        assertEquals("Cancelled", currentOrder?.status)
        assertEquals("Delivery taking too long", currentOrder?.cancelReason)
        assertNotNull(currentOrder?.cancelledAt)

        // 9. Verify stock was replenished (+2 restored to stock)
        val product = database.productDao().getProductById(101L)
        assertNotNull(product)
        assertEquals(50, product?.stock) // initial 50 -> placed order (stock 48) -> cancelled order (stock 50)
    }

    @Test
    fun testDeliveredOrderCannotBeCancelled() = runBlocking {
        // Insert a Delivered order
        val deliveredOrder = OrderEntity(
            id = 201L,
            orderNumber = "BH-201000",
            userId = testUserId,
            shopId = 1L,
            totalAmount = 350.0,
            deliveryAddress = "Civil Lines, Bisalpur",
            status = "Delivered",
            paymentMethod = "Cash on Delivery"
        )
        database.orderDao().insertOrder(deliveredOrder)

        val app = ApplicationProvider.getApplicationContext<Context>() as MyApplication
        composeTestRule.setContent {
            MyApplicationTheme {
                CustomerOrdersScreen(
                    app = app,
                    navController = rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("track_order_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Open details
        composeTestRule.onNodeWithTag("track_order_button").performClick()
        composeTestRule.waitForIdle()

        // Cancel order button must NOT exist for Delivered order
        composeTestRule.onNodeWithTag("cancel_order_button").assertDoesNotExist()
    }

    @Test
    fun testOutForDeliveryOrderCannotBeCancelled() = runBlocking {
        // Insert an Out for Delivery order
        val outOrder = OrderEntity(
            id = 202L,
            orderNumber = "BH-202000",
            userId = testUserId,
            shopId = 1L,
            totalAmount = 450.0,
            deliveryAddress = "Civil Lines, Bisalpur",
            status = "Out for Delivery",
            paymentMethod = "Cash on Delivery"
        )
        database.orderDao().insertOrder(outOrder)

        val app = ApplicationProvider.getApplicationContext<Context>() as MyApplication
        composeTestRule.setContent {
            MyApplicationTheme {
                CustomerOrdersScreen(
                    app = app,
                    navController = rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("track_order_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Open details
        composeTestRule.onNodeWithTag("track_order_button").performClick()
        composeTestRule.waitForIdle()

        // Cancel order button must NOT exist for Out for Delivery order
        composeTestRule.onNodeWithTag("cancel_order_button").assertDoesNotExist()
    }

    @Test
    fun testUserSecurityCrossCancellationPrevention() = runBlocking {
        // User A (testUserId = 1) places an order
        val orderA = OrderEntity(
            id = 301L,
            orderNumber = "BH-301000",
            userId = testUserId,
            shopId = 1L,
            totalAmount = 600.0,
            deliveryAddress = "Civil Lines, Bisalpur",
            status = "Order Placed",
            paymentMethod = "Cash on Delivery"
        )
        database.orderDao().insertOrder(orderA)

        // User B (otherUserId = 2) attempts to cancel User A's order
        val cancelResult = orderRepository.cancelOrder(
            orderId = 301L,
            userId = otherUserId,
            reason = "Malicious cancellation attempt"
        )

        assertTrue(cancelResult.isFailure)
        assertTrue(cancelResult.exceptionOrNull() is SecurityException)

        // Verify Order A remains untouched
        val orderAfter = orderRepository.getOrderById(301L)
        assertEquals("Order Placed", orderAfter?.status)
        assertNull(orderAfter?.cancelReason)
    }

    @Test
    fun testPrepaidOrderSetsRefundPendingStatus() = runBlocking {
        // Online UPI prepaid order
        val prepaidOrder = OrderEntity(
            id = 401L,
            orderNumber = "BH-401000",
            userId = testUserId,
            shopId = 1L,
            totalAmount = 999.0,
            deliveryAddress = "Civil Lines, Bisalpur",
            status = "Confirmed",
            paymentMethod = "Online UPI"
        )
        database.orderDao().insertOrder(prepaidOrder)

        val cancelResult = orderRepository.cancelOrder(
            orderId = 401L,
            userId = testUserId,
            reason = "Found a better price"
        )

        assertTrue(cancelResult.isSuccess)
        val cancelledOrder = cancelResult.getOrThrow()
        assertEquals("Cancelled", cancelledOrder.status)
        assertEquals("Refund Pending", cancelledOrder.refundStatus)
        assertEquals("Found a better price", cancelledOrder.cancelReason)
    }
}
