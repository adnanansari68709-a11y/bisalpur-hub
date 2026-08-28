package com.example.data.local

import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ShopDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedSampleDataIfNeeded(
        userDao: UserDao,
        shopDao: ShopDao,
        productDao: ProductDao
    ) = withContext(Dispatchers.IO) {
        val existingProducts = productDao.getAllProducts().firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            // Seed a local seller user
            val sellerUser = UserEntity(
                id = 0,
                name = "Bisalpur Handloom House",
                email = "seller@bisalpurhub.com",
                phone = "+919876543210",
                passwordHash = com.example.util.SecurityUtils.hashPassword("password123"),
                role = Role.SELLER
            )
            val sellerId = userDao.insertUser(sellerUser)

            // Seed Shop 1: Bisalpur Royal Handlooms
            val shop1 = ShopEntity(
                id = 0,
                sellerId = sellerId,
                name = "Bisalpur Handlooms & Textiles",
                description = "Authentic local handloom silk sarees, Lucknowi chikankari kurtas, and traditional ethnic wear directly from master weavers.",
                logoUrl = ""
            )
            val shopId1 = shopDao.insertShop(shop1)

            // Seed Shop 2: Shree Ram Sweets & Snacks
            val shop2 = ShopEntity(
                id = 0,
                sellerId = sellerId,
                name = "Shree Ram Sweets & Local Delights",
                description = "Pure Desi Ghee traditional sweets, famous Bisalpur pedas, and freshly roasted namkeens.",
                logoUrl = ""
            )
            val shopId2 = shopDao.insertShop(shop2)

            // Seed Shop 3: Kisan Organics & Daily Essentials
            val shop3 = ShopEntity(
                id = 0,
                sellerId = sellerId,
                name = "Kisan Organics & Spices",
                description = "Farm-fresh cold-pressed mustard oil, organic jaggery (gur), and premium ground spices from Terai farms.",
                logoUrl = ""
            )
            val shopId3 = shopDao.insertShop(shop3)

            // Seed Products
            val sampleProducts = listOf(
                ProductEntity(
                    shopId = shopId1,
                    name = "Banarasi Silk Embroidered Saree",
                    description = "Exquisite zari woven royal Banarasi silk saree with matching unstitched blouse piece. Perfect for festive ceremonies and weddings.",
                    price = 1499.0,
                    originalPrice = 2999.0,
                    category = "Fashion",
                    gender = "Women",
                    sizes = listOf("Free Size"),
                    colors = listOf("Royal Maroon", "Midnight Blue", "Emerald Green", "Peacock Teal"),
                    stock = 25,
                    imageUrls = listOf("https://images.unsplash.com/photo-1610030469983-98e550d6193c")
                ),
                ProductEntity(
                    shopId = shopId1,
                    name = "Handcrafted Chikankari Cotton Kurta",
                    description = "Breathable pure cotton kurta featuring intricate hand embroidery done by local Bisalpur artisans. Casual & festive wear.",
                    price = 799.0,
                    originalPrice = 1499.0,
                    category = "Fashion",
                    gender = "Men",
                    sizes = listOf("S", "M", "L", "XL", "XXL"),
                    colors = listOf("Ivory White", "Sky Blue", "Pastel Mint", "Soft Pink"),
                    stock = 40,
                    imageUrls = listOf("https://images.unsplash.com/photo-1597983073493-88cd35cf93b0")
                ),
                ProductEntity(
                    shopId = shopId2,
                    name = "Pure Desi Ghee Bisalpur Special Peda (1 Kg)",
                    description = "The world-famous melt-in-mouth mawa pedas made with 100% pure buffalo milk and desi ghee. Freshly packed daily.",
                    price = 480.0,
                    originalPrice = 600.0,
                    category = "Sweets & Food",
                    gender = "Unisex",
                    sizes = listOf("500g", "1 Kg", "2 Kg Box"),
                    colors = listOf("Classic Golden"),
                    stock = 50,
                    imageUrls = listOf("https://images.unsplash.com/photo-1541781774459-bb2af2f05b55")
                ),
                ProductEntity(
                    shopId = shopId1,
                    name = "Royal Mojari Embroidered Jutti",
                    description = "Handcrafted genuine leather traditional Punjabi jutti with gold zari embroidery and cushioned insole for all-day comfort.",
                    price = 649.0,
                    originalPrice = 1199.0,
                    category = "Footwear",
                    gender = "Men",
                    sizes = listOf("7", "8", "9", "10", "11"),
                    colors = listOf("Royal Tan", "Midnight Black", "Antique Gold"),
                    stock = 30,
                    imageUrls = listOf("https://images.unsplash.com/photo-1549298916-b41d501d3772")
                ),
                ProductEntity(
                    shopId = shopId3,
                    name = "Cold-Pressed Pure Yellow Mustard Oil (5L)",
                    description = "Traditionally wood-pressed (Kachi Ghani) pure yellow mustard oil directly from local fertile farms. Rich pungent aroma and natural pungency.",
                    price = 849.0,
                    originalPrice = 1150.0,
                    category = "Groceries",
                    gender = "Unisex",
                    sizes = listOf("1L Can", "2L Can", "5L Jar"),
                    colors = listOf("Natural Golden"),
                    stock = 60,
                    imageUrls = listOf("https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5")
                ),
                ProductEntity(
                    shopId = shopId1,
                    name = "Embellished Anarkali Festive Kurti Set",
                    description = "Three-piece designer Anarkali suit set with heavy flared dupatta and matching bottom. Rich mirror-work details.",
                    price = 1299.0,
                    originalPrice = 2499.0,
                    category = "Fashion",
                    gender = "Women",
                    sizes = listOf("M", "L", "XL", "XXL"),
                    colors = listOf("Deep Wine", "Mustard Yellow", "Royal Blue"),
                    stock = 20,
                    imageUrls = listOf("https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b")
                ),
                ProductEntity(
                    shopId = shopId3,
                    name = "Organic Sugarcane Jaggery Powder (1 Kg)",
                    description = "100% chemical-free organic jaggery powder made from fresh sugarcane juice in Bisalpur. Ideal healthy sugar substitute.",
                    price = 160.0,
                    originalPrice = 220.0,
                    category = "Groceries",
                    gender = "Unisex",
                    sizes = listOf("500g", "1 Kg", "3 Kg"),
                    colors = listOf("Natural Amber"),
                    stock = 80,
                    imageUrls = listOf("https://images.unsplash.com/photo-1606787366850-de6330128bfc")
                ),
                ProductEntity(
                    shopId = shopId1,
                    name = "Handcrafted Brass Diya & Pooja Thali Set",
                    description = "Traditional 7-piece carved pure brass pooja thali set with bell, agarbatti stand, and decorative oil lamp for home ceremonies.",
                    price = 899.0,
                    originalPrice = 1599.0,
                    category = "Home & Living",
                    gender = "Unisex",
                    sizes = listOf("Standard 10 Inch", "Grand 12 Inch"),
                    colors = listOf("Antique Brass", "Glossy Gold"),
                    stock = 35,
                    imageUrls = listOf("https://images.unsplash.com/photo-1534447677768-be436bb09401")
                ),
                ProductEntity(
                    shopId = shopId2,
                    name = "Kaju Katli Gift Hamper (500g)",
                    description = "Diamond cut silver-leaf covered premium cashew fudge sweets made with selected Goan cashews. Gift-wrapped box.",
                    price = 520.0,
                    originalPrice = 650.0,
                    category = "Sweets & Food",
                    gender = "Unisex",
                    sizes = listOf("500g", "1 Kg"),
                    colors = listOf("Silver Leaf"),
                    stock = 45,
                    imageUrls = listOf("https://images.unsplash.com/photo-1599785209707-a456fc1337bb")
                )
            )

            for (p in sampleProducts) {
                productDao.insertProduct(p)
            }
        }
    }
}
