package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.api.GeminiClient
import com.example.data.*
import com.example.ui.theme.WarmCreamBackground
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch

// Chat bubble data model for AI Travel Planner
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NongLuApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Main States
    var currentTab by remember { mutableStateOf(0) } // 0: Accommodations, 1: Travel Tips, 2: AI Planner
    var selectedResort by remember { mutableStateOf<Accommodation?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }

    // Bookmarks state (saved in memory)
    val bookmarkedIds = remember { mutableStateListOf<String>() }

    // AI Tab state
    var chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                "init",
                "ยินดีต้อนรับสู่หนองลูสังขละบุรีครับ! 🌅 ผมคือ AI ผู้รอบรู้และไกด์ท้องถิ่นของสะพานมอญ คุณสามารถให้ผมช่วยวางแผนทริปท่องเที่ยว แนะนำที่กิน สไตล์การตักบาตร หรือสอบถามเส้นทางขับรถทางหลวง 323 เลนเขาชันได้ทันทีครับ!",
                isUser = false,
                "08:00"
            )
        )
    }
    var aiLoading by remember { mutableStateOf(false) }
    var chatInputField by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Action function for quick chatbot prompt
    fun sendChatMessage(text: String) {
        if (text.isBlank() || aiLoading) return
        val userMsgId = "user_${System.currentTimeMillis()}"
        chatMessages.add(ChatMessage(userMsgId, text, isUser = true, "ตอนนี้"))
        aiLoading = true

        coroutineScope.launch {
            try {
                val response = GeminiClient.planTrip(text)
                val aiMsgId = "ai_${System.currentTimeMillis()}"
                chatMessages.add(ChatMessage(aiMsgId, response, isUser = false, "ตอนนี้"))
            } catch (e: Exception) {
                chatMessages.add(ChatMessage(
                    "ai_err_${System.currentTimeMillis()}",
                    "ขออภัยชั่วคราวครับ เกิดความผิดพลาดกรุณาลองกดส่งอีกครั้ง",
                    isUser = false,
                    "ตอนนี้"
                ))
            } finally {
                aiLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (selectedResort == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val tabs = listOf(
                        Triple("คาเฟ่ & ที่พัก", Icons.Default.Home, 0),
                        Triple("คู่มือท้องถิ่น", Icons.Default.Info, 1),
                        Triple("แผนเที่ยวด้วย AI", Icons.Default.Share, 2)
                    )

                    tabs.forEach { (title, icon, index) ->
                        NavigationBarItem(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag("tab_button_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = selectedResort,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { resort ->
                if (resort != null) {
                    // Resort Details Screen
                    ResortDetailScreen(
                        accommodation = resort,
                        isBookmarked = bookmarkedIds.contains(resort.id),
                        onBookmarkToggled = {
                            if (bookmarkedIds.contains(resort.id)) {
                                bookmarkedIds.remove(resort.id)
                                Toast.makeText(context, "นำออกจากรายการที่บันทึกแล้ว", Toast.LENGTH_SHORT).show()
                            } else {
                                bookmarkedIds.add(resort.id)
                                Toast.makeText(context, "บันทึกที่พักแล้ว", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBackClick = { selectedResort = null }
                    )
                } else {
                    // Category list & main tabs
                    when (currentTab) {
                        0 -> {
                            // TAB 0: Accommodations list with glassmorphism header & categories
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Hero Banner Block
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_mon_bridge),
                                        contentDescription = "Mon Bridge Morning Mist",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Gradient overlay for visual aesthetics and contrast
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.3f),
                                                        Color.Black.copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )

                                    // Hero Title text
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "เที่ยวหนองลู สังขละบุรี",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ค้นหาที่พัก ท่าเรือนำเที่ยว และคู่มือขับรถอย่างท้องถิ่น",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                // Search Box Section with styled glassmorphism style container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .offset(y = (-14).dp)
                                        .shadow(6.dp, RoundedCornerShape(12.dp))
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        TextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            placeholder = {
                                                Text(
                                                    "ค้นหาที่พักวิวแม่น้ำ, ใกล้สะพานมอญ...",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                disabledContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            ),
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("search_bar_input")
                                        )
                                    }
                                }

                                // Category Selector - Scrollable Row
                                val categories = listOf("ทั้งหมด", "ติดสะพานมอญ", "วิวแม่น้ำ", "ราคาประหยัด", "สัตว์เลี้ยงเข้าได้")
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(categories) { category ->
                                        val isSelected = selectedCategory == category
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                                .clickable { selectedCategory = category }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                                .testTag("category_pill_$category"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = category,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Filter Accommodations
                                val filteredAccommodations = AccommodationRepository.list.filter { accommodation ->
                                    val matchesQuery = accommodation.name.contains(searchQuery, ignoreCase = true) ||
                                            accommodation.nameEn.contains(searchQuery, ignoreCase = true) ||
                                            accommodation.description.contains(searchQuery, ignoreCase = true)
                                    val matchesCategory = selectedCategory == "ทั้งหมด" ||
                                            accommodation.category == selectedCategory ||
                                            (selectedCategory == "ราคาประหยัด" && (accommodation.priceRange.contains("350") || accommodation.priceRange.contains("600") || accommodation.priceRange.contains("800"))) ||
                                            (selectedCategory == "สัตว์เลี้ยงเข้าได้" && accommodation.features.any { it.contains("สัตว์เลี้ยง") }) ||
                                            (selectedCategory == "ติดสะพานมอญ" && accommodation.distanceKmToBridge <= 0.4f)

                                    matchesQuery && matchesCategory
                                }

                                if (filteredAccommodations.isEmpty()) {
                                    // Empty State view helpfully crafted
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "No accommodation empty state",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "ไม่พบที่พักตรงกับเงื่อนไขสะกดใจคุณ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ลองล้างพารามิเตอร์ช่องค้นหาหรือเปลี่ยนแท็บอื่นแทน",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = {
                                            searchQuery = ""
                                            selectedCategory = "ทั้งหมด"
                                        }) {
                                            Text("รีเซ็ตตัวกรองทั้งหมด")
                                        }
                                    }
                                } else {
                                    // Accommodations list
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        item {
                                            Text(
                                                "แนะนำโรงแรม & โฮมสเตย์ (${filteredAccommodations.size})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }

                                        items(filteredAccommodations, key = { it.id }) { accommodation ->
                                            AccommodationCard(
                                                accommodation = accommodation,
                                                isBookmarked = bookmarkedIds.contains(accommodation.id),
                                                onBookmarkClick = {
                                                    if (bookmarkedIds.contains(accommodation.id)) {
                                                        bookmarkedIds.remove(accommodation.id)
                                                    } else {
                                                        bookmarkedIds.add(accommodation.id)
                                                    }
                                                },
                                                onClick = { selectedResort = accommodation }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: Local Knowledge Guide Hub
                            LocalGuideScreen()
                        }

                        2 -> {
                            // TAB 2: AI Planner Assistant
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(WarmCreamBackground)
                            ) {
                                // AI Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50)) // Green indicator
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "AI อัจฉริยะคุยสด",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "Sangkhla Stay AI Co-Pilot",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "วางแผนทริปแบบวันต่อวัน แนะนำจุดถ่ายภาพ ร้านอร่อย สดลึกระดับหนองลู",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Quick Action Presets inside horizontal scroll
                                val presets = listOf(
                                    "ขอแผนเที่ยวสังขละบุรี 3 วัน 2 คืน แบบสโลว์ไลฟ์",
                                    "ตักบาตรมอญมีข้อห้ามอะไรบ้าง และทำกี่โมง",
                                    "แนะนำร้านอาหารเด็ดใกล้สามประสบและตลาดมอญ",
                                    "แนะนำทางสาย 323 ช่วงไหนลาดชัน โค้งอันตรายบ้าง"
                                )

                                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        "คำถามยอดฮิต (กดเพื่อถามทันที):",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                                    )
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(presets) { preset ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable(enabled = !aiLoading) {
                                                        sendChatMessage(preset)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = preset,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                // Chat Bubble logs
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(chatMessages) { msg ->
                                        ChatBubbleItem(msg)
                                    }

                                    if (aiLoading) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "ไกด์สังขละบุรี AI กำลังขบคิดแผนส่วนตัวของคุณ...",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                }

                                // Input Field Panel at Bottom Row of Tab
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .shadow(8.dp)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextField(
                                            value = chatInputField,
                                            onValueChange = { chatInputField = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("ai_chat_input"),
                                            placeholder = {
                                                Text(
                                                    "พิมพ์คำถามท่องเที่ยวหนองลู-สังขละ...",
                                                    fontSize = 13.sp
                                                )
                                            },
                                            colors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(24.dp),
                                            maxLines = 3,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                            keyboardActions = KeyboardActions(onSend = {
                                                if (chatInputField.isNotBlank()) {
                                                    val text = chatInputField
                                                    chatInputField = ""
                                                    keyboardController?.hide()
                                                    sendChatMessage(text)
                                                }
                                            })
                                        )

                                        FloatingActionButton(
                                            onClick = {
                                                if (chatInputField.isNotBlank()) {
                                                    val text = chatInputField
                                                    chatInputField = ""
                                                    keyboardController?.hide()
                                                    sendChatMessage(text)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(46.dp)
                                                .testTag("ai_send_button"),
                                            shape = CircleShape,
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Send prompt icon",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Resort item representation Card Layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationCard(
    accommodation: Accommodation,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("resort_card_${accommodation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Image displaying the specific resort cottage
                Image(
                    painter = painterResource(id = accommodation.imgResId),
                    contentDescription = accommodation.nameProductDesc(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Category overlay tag
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        accommodation.category,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Distance quick indicator
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Distance",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "ห่างสะพานมอญ ${accommodation.distanceKmToBridge} กม.",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bookmark icon button
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark button",
                        tint = if (isBookmarked) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = accommodation.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = accommodation.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = accommodation.nameEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "“${accommodation.quote}”",
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone icon overlay",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = accommodation.phone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))) {
                                append("เริ่มต้น ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                append(accommodation.priceRange)
                            }
                        },
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// Resort detailed detail representation view
@Composable
fun ResortDetailScreen(
    accommodation: Accommodation,
    isBookmarked: Boolean,
    onBookmarkToggled: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(WarmCreamBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Photo banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Image(
                        painter = painterResource(id = accommodation.imgResId),
                        contentDescription = "Detailed Image of ${accommodation.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top control buttons translucent and floating
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                                .testTag("btn_detail_back")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back button",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = onBookmarkToggled,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark toggle button",
                                tint = if (isBookmarked) Color.Red else Color.White
                            )
                        }
                    }

                    // Lower dynamic content overlays
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = accommodation.category,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = accommodation.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = accommodation.nameEn,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Score",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = accommodation.rating.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quote and detailed descriptions
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "“${accommodation.quote}”",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "ฟีเจอร์และสิ่งอำนวยความสะดวก",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Features styled chips row
                    FlowRow(
                        horizontalGap = 8.dp,
                        verticalGap = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        accommodation.features.forEach { feature ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = feature,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CRITICAL FEATURE REQUIREMENT: The Comparison Map showing hotel position relative to Mon Bridge!
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "แผนวิถีเชิงสังเขป (ระยะห่างสะพานมอญ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            "ห่างกัน ${accommodation.distanceKmToBridge} กม.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "แผนที่จำลองตำแหน่งที่พักในตำบลหนองลู เทียบกับพิกัด 'สะพานมอญ' และแม่น้ำสามสายบจบ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Draw the custom MonBridgeMiniMap canvas!
                    MonBridgeMiniMap(
                        resortLocation = accommodation.location,
                        resortName = accommodation.name,
                        distanceKm = accommodation.distanceKmToBridge
                    )
                }
            }

            // Rooms & Pricing List
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ประเภทห้องพัก & อัตราค่าบริการ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    ) {
                        Column {
                            accommodation.roomTypes.forEachIndexed { i, room ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = room.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "สำหรับเข้าพัก 2 ท่าน (รวมอาหารเช้า)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    Text(
                                        text = room.price,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (i < accommodation.roomTypes.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                }
                            }
                        }
                    }
                }
            }

            // Reviews List
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "รีวิวและความคิดเห็นผู้ใช้บริการ (${accommodation.reviews.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    accommodation.reviews.forEach { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                r.author.take(1),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = r.author,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row {
                                        repeat(r.rating.toInt()) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "star",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "“${r.comment}”",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Reservation & Contacts Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.White)
                    )
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${accommodation.phone}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(26.dp))
                    .testTag("btn_call_resort"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Telephone",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "โทรติดต่อสอบถาม: ${accommodation.phone}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Custom simulated map drawing showing confluence of three rivers & Mon Bridge position
@Composable
fun MonBridgeMiniMap(
    resortLocation: LatLng,
    resortName: String,
    distanceKm: Float
) {
    val riverColor = Color(0xFFBBE5DF)
    val bridgeColor = Color(0xFF8C7355)
    val pinBridgeColor = Color(0xFFD32F2F)
    val pinResortColor = Color(0xFFE65100)
    val pinSamprasopColor = Color(0xFF1976D2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAE7DF))
            .border(1.dp, Color.Black.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
    ) {
        // Compose Canvas for drawing geography shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // --- Draw 3 Rivers ---
            // Songkalia River: Flows from north downward to middle
            val songkaliaPath = Path().apply {
                moveTo(width * 0.35f, 0f)
                quadraticTo(
                    width * 0.33f, height * 0.25f,
                    width * 0.45f, height * 0.5f
                )
            }
            drawPath(
                path = songkaliaPath,
                color = riverColor,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )

            // Bikhli River: Flows from northeast to middle
            val bikhliPath = Path().apply {
                moveTo(width, height * 0.15f)
                quadraticTo(
                    width * 0.7f, height * 0.35f,
                    width * 0.45f, height * 0.5f
                )
            }
            drawPath(
                path = bikhliPath,
                color = riverColor,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )

            // Ranti River: Flows from southeast to middle
            val rantiPath = Path().apply {
                moveTo(width, height * 0.85f)
                quadraticTo(
                    width * 0.7f, height * 0.65f,
                    width * 0.45f, height * 0.5f
                )
            }
            drawPath(
                path = rantiPath,
                color = riverColor,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )

            // Samprasop Confluence Basin (Large lake center where rivers merge)
            val samprasopBasin = Path().apply {
                moveTo(width * 0.45f, height * 0.5f)
                cubicTo(
                    width * 0.2f, height * 0.4f,
                    width * 0.15f, height * 0.7f,
                    0f, height * 0.6f
                )
            }
            drawPath(
                path = samprasopBasin,
                color = riverColor,
                style = Stroke(width = 44.dp.toPx(), cap = StrokeCap.Round)
            )

            // --- Draw WOODEN Mon Bridge ---
            // Crosses the Songkalia River (on the upper part)
            val bridgeStart = Offset(width * 0.30f, height * 0.24f)
            val bridgeEnd = Offset(width * 0.50f, height * 0.14f)
            drawLine(
                color = bridgeColor,
                start = bridgeStart,
                end = bridgeEnd,
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Square
            )

            // Draw timber bridge supports decoration lines
            repeat(5) { step ->
                val fraction = step / 4f
                val pin = Offset(
                    bridgeStart.x + (bridgeEnd.x - bridgeStart.x) * fraction,
                    bridgeStart.y + (bridgeEnd.y - bridgeStart.y) * fraction
                )
                drawLine(
                    color = Color(0xFF6D4C41),
                    start = Offset(pin.x, pin.y - 4.dp.toPx()),
                    end = Offset(pin.x, pin.y + 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // --- Location Markers (Normal coordinates mapped to Canvas aspect ratio) ---
            // Bridge center
            val bridgeCenter = Offset(width * 0.40f, height * 0.19f)
            // Samprasop confluence point center
            val samprasopCenter = Offset(width * 0.45f, height * 0.50f)

            // Determine unique display offsets for each resort relative to Mon Bridge
            val resortNameLowercase = resortName.lowercase()
            val resortMapOffset = when {
                resortNameLowercase.contains("สามประสบ") -> Offset(width * 0.44f, height * 0.35f)
                resortNameLowercase.contains("สะพานรัก") -> Offset(width * 0.54f, height * 0.10f)
                resortNameLowercase.contains("พี") -> Offset(width * 0.24f, height * 0.42f)
                resortNameLowercase.contains("สังขละ") -> Offset(width * 0.20f, height * 0.40f)
                resortNameLowercase.contains("โอดี") -> Offset(width * 0.56f, height * 0.40f)
                else -> Offset(width * 0.65f, height * 0.34f) // Baan Suan
            }

            // Draw line connecting the resort to the Mon Bridge to display distance context
            drawLine(
                color = Color.Black.copy(alpha = 0.4f),
                start = bridgeCenter,
                end = resortMapOffset,
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // --- Draw Markers ---
            // 1. Mon Bridge Pin (Red star)
            drawCircle(
                color = pinBridgeColor,
                radius = 8.dp.toPx(),
                center = bridgeCenter
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = bridgeCenter
            )

            // 2. Sam Prasop Center (Blue Circle)
            drawCircle(
                color = pinSamprasopColor,
                radius = 6.dp.toPx(),
                center = samprasopCenter
            )

            // 3. User Resort Center (Orange Circle)
            drawCircle(
                color = pinResortColor,
                radius = 9.dp.toPx(),
                center = resortMapOffset
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = resortMapOffset
            )
        }

        // Map Legends floated statically
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            MapLegendItem(color = pinBridgeColor, text = "สะพานมอญ (จุดเริ่มตักบาตร)")
            MapLegendItem(color = pinSamprasopColor, text = "สามประสบ (จุดบรรจบน้ำ 3 สาย)")
            MapLegendItem(color = pinResortColor, text = resortName)
        }

        // Compass Rose decorative overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(32.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "N ⬆",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MapLegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}

// TAB 1: Expandable travel guide cards
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalGuideScreen() {
    val itemsList = TravelGuideRepository.guides
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 22.dp)
        ) {
            Column {
                Text(
                    text = "คลังความรู้สะพานมอญ & สังขละ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "ข้อมูลอัพเดทประเพณีท้องถิ่น ที่จอดรถ และคำแนะนำการขับรถ",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsList.forEachIndexed { parentIndex, category ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .testTag("guide_section_$parentIndex"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            // Header Row Click triggers expand
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedIndex = if (expandedIndex == parentIndex) null else parentIndex
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Custom visual asset indicator reflecting category icons
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (category.iconName) {
                                                "commute" -> Icons.Default.Settings // Simulated car commute
                                                "local_parking" -> Icons.Default.LocationOn // Simulated parking
                                                "volunteer_activism" -> Icons.Default.Favorite // Simulated alms volunteering
                                                else -> Icons.Default.Info // Scenic boat tour
                                            },
                                            contentDescription = "Category symbol",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = category.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = category.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (expandedIndex == parentIndex) Icons.Default.ArrowBack else Icons.Default.ArrowBack, // Standard rotation or mock alternate arrow
                                    contentDescription = "Expand Indicator",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Expandable detailed information block
                            AnimatedVisibility(
                                visible = expandedIndex == parentIndex,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                        .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
                                ) {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), modifier = Modifier.padding(bottom = 12.dp))

                                    category.items.forEach { guideItem ->
                                        Text(
                                            text = guideItem.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                        Text(
                                            text = guideItem.subtitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = guideItem.content,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        // Detailed local advice notes
                                        if (guideItem.tips.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    "💡 เกร็ดความรู้เสริม:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFBC9A5C),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                                guideItem.tips.forEach { tip ->
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 2.dp),
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Text("• ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                        Text(tip, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 16.sp)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ChatBubble representations
@Composable
fun ChatBubbleItem(msg: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (msg.isUser) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = "${if (msg.isUser) "คุณ" else "พี่ไกด์หนองลู"} • ${msg.timestamp}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

// Dynamic FlowRow layout to replace missing accompanist libraries
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: androidx.compose.ui.unit.Dp = 8.dp,
    verticalGap: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + horizontalGap.roundToPx()
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        var totalHeight = 0
        rows.forEachIndexed { i, row ->
            val rowHeight = row.maxOf { it.height }
            totalHeight += rowHeight
            if (i < rows.size - 1) {
                totalHeight += verticalGap.roundToPx()
            }
        }

        layout(layoutWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalGap.roundToPx()
                }
                y += rowHeight + verticalGap.roundToPx()
            }
        }
    }
}

// Accommodation extension to generate custom content accessibility description
fun Accommodation.nameProductDesc(): String {
    return "สตรีทวิวมองเห็น $name ที่เป็นสถาปัตยกรรมสวยงาม"
}
