package com.example.domain.repository

import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(
    private val orderDao: OrderDao,
    private val productDao: ProductDao
) {
    suspend fun placeOrder(
        userId: Long,
        shopId: Long,
        items: List<OrderItemEntity>,
        address: String,
        total: Double,
        paymentMethod: String = "Cash on Delivery"
    ): Result<Long> {
        // Simple stock check
        for (item in items) {
            val product = productDao.getProductById(item.productId) ?: return Result.failure(Exception("Product not found"))
            if (product.stock < item.quantity) {
                return Result.failure(Exception("Insufficient stock for ${product.name}"))
            }
        }
        
        // Deduct stock
        for (item in items) {
            val product = productDao.getProductById(item.productId)!!
            productDao.updateProduct(product.copy(stock = product.stock - item.quantity))
        }

        val order = OrderEntity(
            orderNumber = "BH-${(System.currentTimeMillis() % 900000 + 100000)}",
            userId = userId,
            shopId = shopId,
            totalAmount = total,
            deliveryAddress = address,
            status = "Order Placed",
            paymentMethod = paymentMethod
        )
        val orderId = orderDao.insertOrder(order)
        
        val itemsWithOrderId = items.map { it.copy(id = 0, orderId = orderId) }
        for (item in itemsWithOrderId) {
            orderDao.insertOrderItem(item)
        }
        
        return Result.success(orderId)
    }

    fun getUserOrders(userId: Long): Flow<List<OrderEntity>> {
        return orderDao.getOrdersForUser(userId)
    }
    
    fun getShopOrders(shopId: Long): Flow<List<OrderEntity>> {
        return orderDao.getOrdersForShop(shopId)
    }

    suspend fun getOrderById(orderId: Long): OrderEntity? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun updateOrderStatus(order: OrderEntity, newStatus: String): Result<Unit> {
        val currentOrder = orderDao.getOrderById(order.id) ?: order
        // Prevent cancelled orders from moving back into active stages
        if (currentOrder.status.equals("Cancelled", ignoreCase = true)) {
            return Result.failure(IllegalStateException("Cannot modify status of a cancelled order."))
        }
        orderDao.updateOrder(currentOrder.copy(status = newStatus))
        return Result.success(Unit)
    }
    
    suspend fun cancelOrder(orderId: Long, userId: Long, reason: String): Result<OrderEntity> {
        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(IllegalArgumentException("Order not found"))

        // Security check: only the authenticated owner can cancel their order
        if (order.userId != userId) {
            return Result.failure(SecurityException("Unauthorized: You can only cancel your own orders."))
        }

        // State check: only cancellable if in eligible stages
        if (!isOrderCancellable(order.status)) {
            return Result.failure(IllegalStateException("Order is in '${order.status}' stage and cannot be cancelled."))
        }

        val isPrepaid = isPrepaidPayment(order.paymentMethod)
        val refundStatus = if (isPrepaid) "Refund Pending" else null

        val cancelledOrder = order.copy(
            status = "Cancelled",
            cancelReason = reason.trim().ifBlank { "Ordered by mistake" },
            cancelledAt = System.currentTimeMillis(),
            refundStatus = refundStatus
        )

        orderDao.updateOrder(cancelledOrder)

        // Restock inventory for items in this cancelled order
        val items = orderDao.getOrderItems(orderId)
        for (item in items) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                productDao.updateProduct(product.copy(stock = product.stock + item.quantity))
            }
        }

        return Result.success(cancelledOrder)
    }

    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity> {
        return orderDao.getOrderItems(orderId)
    }

    companion object {
        fun isOrderCancellable(status: String): Boolean {
            val s = status.trim().lowercase()
            return when (s) {
                "order placed", "placed", "pending", "confirmed", "preparing" -> true
                "ready for delivery", "out for delivery", "delivered", "cancelled" -> false
                else -> false
            }
        }

        fun isPrepaidPayment(paymentMethod: String): Boolean {
            val p = paymentMethod.trim().lowercase()
            return p.contains("online") || p.contains("upi") || p.contains("card") || p.contains("prepaid") || p.contains("net banking")
        }
    }
}

