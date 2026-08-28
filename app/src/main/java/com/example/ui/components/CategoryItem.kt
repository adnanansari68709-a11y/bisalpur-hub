package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class CategoryData(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconTint: Color,
    val bgTint: Color
)

val defaultCategories = listOf(
    CategoryData("all", "All", Icons.Filled.GridView, RoyalNavyPrimary, SurfaceVariantLight),
    CategoryData("Fashion", "Fashion", Icons.Filled.Checkroom, ElectricBluePrimary, ElectricBlueLight),
    CategoryData("Sweets & Food", "Sweets & Food", Icons.Filled.Restaurant, WarningOrange, WarningOrangeLight),
    CategoryData("Footwear", "Footwear", Icons.Filled.DirectionsWalk, Color(0xFF7C3AED), Color(0xFFF3E8FF)),
    CategoryData("Groceries", "Groceries", Icons.Filled.ShoppingBasket, SuccessGreen, SuccessGreenLight),
    CategoryData("Home & Living", "Home & Living", Icons.Filled.Home, Color(0xFF0284C7), Color(0xFFE0F2FE))
)

@Composable
fun CategoryItem(
    category: CategoryData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) RoyalNavyPrimary else category.bgTint,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) SurfaceWhite else category.iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) RoyalNavyPrimary else TextMedium,
            maxLines = 1
        )
    }
}
