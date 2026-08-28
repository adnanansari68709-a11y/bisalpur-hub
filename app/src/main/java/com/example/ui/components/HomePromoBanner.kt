package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class PromoBannerItem(
    val title: String,
    val subtitle: String,
    val tag: String,
    val actionText: String,
    val gradientColors: List<Color>
)

@Composable
fun HomePromoBanner(
    onBannerClick: (PromoBannerItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bannerItems = remember {
        listOf(
            PromoBannerItem(
                title = "Bisalpur Festival Sale",
                subtitle = "Up to 60% Off on Handlooms & Sarees",
                tag = "LIMITED TIME DEALS",
                actionText = "Shop Deals",
                gradientColors = listOf(Color(0xFF0F2444), Color(0xFF1E60FF))
            ),
            PromoBannerItem(
                title = "Pure Desi Ghee Sweets",
                subtitle = "Authentic Bisalpur Pedas & Kaju Katli",
                tag = "LOCAL SPECIALITIES",
                actionText = "Explore Sweets",
                gradientColors = listOf(Color(0xFF854D0E), Color(0xFFD97706))
            ),
            PromoBannerItem(
                title = "Same-Day Delivery",
                subtitle = "Free delivery on orders above ₹499",
                tag = "LIGHTNING FAST",
                actionText = "Order Now",
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF059669))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { bannerItems.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % bannerItems.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) { page ->
            val item = bannerItems[page]
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onBannerClick(item) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(item.gradientColors))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.75f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tag Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LuxuryGold,
                            contentColor = RoyalNavyDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = RoyalNavyDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Title & Subtitle
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = SurfaceWhite.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                        }

                        // Action Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SurfaceWhite, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = item.actionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalNavyPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = RoyalNavyPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Indicator Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(bannerItems.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 6.dp,
                    label = "indicatorWidth"
                )
                val color = if (isSelected) ElectricBluePrimary else SurfaceBorder

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .background(color, CircleShape)
                )
            }
        }
    }
}
