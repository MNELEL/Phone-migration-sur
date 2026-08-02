package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ProTipArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val readTimeMinutes: Int,
    val date: String,
    val icon: ImageVector,
    val fullContent: String,
    val keyTakeaways: List<String>,
    val googleSearchQuery: String,
    val isFeatured: Boolean = false
)

object ProTipsRepository {
    fun getArticles(): List<ProTipArticle> {
        return listOf(
            ProTipArticle(
                id = "tip_authenticator",
                title = "העברה בטוחה של אפליקציות אימות (2FA) ובנקאות",
                summary = "כיצד להעביר מפתחות Google Authenticator, Authy ואפליקציות בנקאיות ללא אובדן גישה לחשבונות.",
                category = "אבטחה ואימות",
                readTimeMinutes = 4,
                date = "2026-08-01",
                icon = Icons.Default.Security,
                isFeatured = true,
                googleSearchQuery = "Android 2FA authenticator app migration best practices 2026",
                keyTakeaways = listOf(
                    "ייצוא קוד QR מרוכז מתוך Google Authenticator במכשיר הישן לפני הפרמוט",
                    "ביצוע גיבוי ענן מוצפן ב-Authy או 1Password במידה ונעשה בהם שימוש",
                    "התחברות ראשונית לאפליקציות בנקאיות במכשיר החדש בעוד המכשיר הישן פעיל לקבלת SMS אימות"
                ),
                fullContent = """
                    מעבר בין מכשירים ניידים כרוך בסיכון ממשי של נעילת גישה לחשבונות מאובטחים אם לא מבצעים העברה מסודרת של מפתחות ה-2FA.
                    
                    שלבים מומלצים למעבר בטוח:
                    1. פתח את אפליקציית Google Authenticator במכשיר הישן, לחץ על תפריט -> 'ייצוא חשבונות'.
                    2. ייווצר קוד QR מרוכז המכיל את כל המפתחות.
                    3. סרוק את קוד ה-QR באמצעות אפליקציית Authenticator במכשיר החדש.
                    4. ודא כי כל החשבונות הופיעו בהצלחה במכשיר החדש בטרם תבצע איפוס להגדרות יצרן במכשיר הישן.
                    5. באפליקציות בנקאות וכרטיסי אשראי - בצע הפעלה (Activation) ראשונית במכשיר החדש בעוד המכשיר הישן זמין לקבלת אימות דו-שלבי.
                """.trimIndent()
            ),
            ProTipArticle(
                id = "tip_whatsapp_cloud",
                title = "מדריך שלם לגיבוי והעברת היסטוריית WhatsApp ו-Media",
                summary = "שיטות מעבר מהירות של שיחות ותמונות WhatsApp בחיבור Wi-Fi ישיר או Google Drive.",
                category = "גיבוי וענן",
                readTimeMinutes = 3,
                date = "2026-08-01",
                icon = Icons.Default.CloudSync,
                isFeatured = false,
                googleSearchQuery = "WhatsApp chat transfer direct wifi Android 2026",
                keyTakeaways = listOf(
                    "שימוש ברכיב ה-Direct Chat Transfer המובנה ב-WhatsApp להעברה מהירה ללא תלויות ענן",
                    "וידוא קיומו של גיבוי Google Drive מוצפן מקצה לקצה לפני תחילת התהליך",
                    "בדיקת נפח האחסון הזמין ב-Google Drive בטרם תחילת העלאת קובצי מדיה כבדים"
                ),
                fullContent = """
                    העברת היסטוריית WhatsApp בין מכשירי Android שודרגה משמעותית בשנים האחרונות ומאפשרת העברה ישירה באמצעות Wi-Fi Local.
                    
                    כיצד להעביר בשיטה המהירה:
                    1. חבר את שני המכשירים לאותה רשת Wi-Fi.
                    2. במכשיר הישן: היכנס ל-WhatsApp -> הגדרות -> צ'אטים -> העברת צ'אטים (Transfer Chats).
                    3. סרוק את קוד ה-QR שיופיע במכשיר החדש.
                    4. כל הצ'אטים והמדיה יועברו במהירות גבוהה ללא צורך בהורדה ממושכת משרתי הענן.
                """.trimIndent()
            ),
            ProTipArticle(
                id = "tip_google_one_backup",
                title = "מיטוב גיבוי המערכת ב-Google One וניהול נפח אחסון",
                summary = "כיצד לוודא שגיבוי המכשיר האוטומטי ב-Google One כולל אנשי קשר, הודעות SMS והגדרות אפליקציה.",
                category = "גיבוי וענן",
                readTimeMinutes = 5,
                date = "2026-07-30",
                icon = Icons.Default.Backup,
                isFeatured = false,
                googleSearchQuery = "Google One Android system backup tips storage optimization 2026",
                keyTakeaways = listOf(
                    "בדיקת הסטטוס ב-הגדרות המכשיר -> Google -> גיבוי",
                    "וידוא שגיבוי תמונות ב-Google Photos מוגדר באיכות Storage Saver במידת הצורך",
                    "סנכרון ידני של SMS והיסטוריית שיחות ממש לפני תחילת המעבר"
                ),
                fullContent = """
                    גיבוי Google One הוא מעטפת הגיבוי המרכזית עבור מכשירי Android. הוא מורכב מגיבוי נתוני אפליקציות, SMS, אנשי קשר, הגדרות מכשיר והיסטוריית שיחות.
                    
                    טיפים לביצוע מושלם:
                    - כנס להגדרות -> Google -> גיבוי ולחץ על 'גבה כעת' (Back up now) כשהמכשיר מחובר למטען ול-Wi-Fi.
                    - ודא שאפליקציית Google Photos סיימה לסנכרן את כל האלבומים החשובים.
                """.trimIndent()
            ),
            ProTipArticle(
                id = "tip_google_play_publishing",
                title = "דגשי אבטחה ופרסום אפליקציות ב-Google Play Console",
                summary = "דרישות חובה עבור מניפסט, Target SDK 34, Keystore וקונפיגורציית App Signing.",
                category = "Google Play & SDKs",
                readTimeMinutes = 6,
                date = "2026-07-28",
                icon = Icons.Default.Shop,
                isFeatured = false,
                googleSearchQuery = "Google Play Store publishing requirements target sdk 34 app signing 2026",
                keyTakeaways = listOf(
                    "חובת הגדרת android:exported מפורשת במניפסט לכל Activity/Service",
                    "שמירה מאובטחת של קובץ ה-Release Keystore ושימוש ב-Play App Signing",
                    "כיווץ ואופטימיזציה של AAB בעזרת R8 ו-ProGuard"
                ),
                fullContent = """
                    בעת הכנת אפליקציה לפרסום ב-Google Play Console, יש לשמור על תאימות מלאה לדרישות האבטחה והמניפסט העדכניות.
                    
                    דגשים מרכזיים:
                    - Target SDK: יש לוודא ש-targetSdk מוגדר ל-34 ומעלה.
                    - App Signing: מומלץ להפעיל Google Play App Signing המבטיח שמפתח החתימה הראשי נשמר באופן מוצפן בשרתי Google.
                    - Form Data Safety: חובה להצהיר ב-Console על אופן איסוף והצפנת הנתונים באפליקציה.
                """.trimIndent()
            ),
            ProTipArticle(
                id = "tip_media_sdcard_transfer",
                title = "העברה מהירה של קובצי וידאו כבדים ומשאבי אחסון",
                summary = "שימוש בטכנולוגיית Wi-Fi Direct ו-USB-C OTG כפול להעברת מאות גיגה-בייט בדקות.",
                category = "אפליקציות ומדיה",
                readTimeMinutes = 3,
                date = "2026-07-25",
                icon = Icons.Default.Folder,
                isFeatured = false,
                googleSearchQuery = "Fastest way transfer large files between Android phones USB OTG 2026",
                keyTakeaways = listOf(
                    "שימוש בחיבור כבל USB-C ל-USB-C להעברות בקצב של עד 10Gbps",
                    "שימוש ב-Quick Share (Near Share) להעברת תיקיות ללא חיבור אינטרנט",
                    "אימות שלמות קבצים באמצעות גודל תיקייה ומספר פריטים"
                ),
                fullContent = """
                    כאשר מדובר בהעברת נפחי מדיה גדולים (4K Videos, אלבומי תמונות כבדים), הסתמכות על העלאה והורדה מהענן עלולה לקחת שעות רבות.
                    
                    פתרונות מומלצים:
                    1. חיבור כבל ישיר USB-C ל-USB-C בין המכשירים ובחירת מצב 'File Transfer' (MTP).
                    2. שימוש ב-Quick Share המובנה ב-Android לקצב העברה אלחוטי גבוה במיוחד ב-Wi-Fi Direct.
                """.trimIndent()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProTipsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var articles by remember { mutableStateOf(ProTipsRepository.getArticles()) }
    var selectedCategory by remember { mutableStateOf("הכל") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<ProTipArticle?>(null) }

    val categories = remember {
        listOf("הכל", "גיבוי וענן", "אבטחה ואימות", "אפליקציות ומדיה", "Google Play & SDKs")
    }

    val filteredArticles = remember(selectedCategory, searchQuery, articles) {
        articles.filter { article ->
            val matchesCategory = (selectedCategory == "הכל" || article.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    article.title.contains(searchQuery, ignoreCase = true) ||
                    article.summary.contains(searchQuery, ignoreCase = true) ||
                    article.fullContent.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val featuredArticle = remember(articles) { articles.firstOrNull { it.isFeatured } ?: articles.firstOrNull() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "טיפים ומאמרי מומחים",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "כלים, מדריכים ועדכוני Google Search להעברת מידע",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "חזרה")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Box & Live Google Search Trigger
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("חפש במאמרים או הקלד נושא לחיפוש בגוגל...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "רענן")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "חפש מידע מעודכן ברשת:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val queryToSearch = if (searchQuery.isNotBlank()) {
                                    "Android data migration $searchQuery 2026"
                                } else {
                                    "Android data migration backup best practices 2026"
                                }
                                val googleSearchUrl = "https://www.google.com/search?q=${Uri.encode(queryToSearch)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleSearchUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("חפש ב-Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Featured Article Banner if no search active
                if (searchQuery.isBlank() && selectedCategory == "הכל" && featuredArticle != null) {
                    item {
                        FeaturedTipCard(
                            article = featuredArticle,
                            onClick = { selectedArticle = featuredArticle },
                            onGoogleSearch = { query ->
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "תוצאות חיפוש (${filteredArticles.size})" else "כל המאמרים והמדריכים",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (filteredArticles.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("לא נמצאו מאמרים תואמים במאגר המקומי.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.google.com/search?q=${Uri.encode("Android migration $searchQuery")}")
                                        )
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("חפש '$searchQuery' בגוגל")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredArticles, key = { it.id }) { article ->
                        ProTipArticleRow(
                            article = article,
                            onClick = { selectedArticle = article },
                            onGoogleSearch = { query ->
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    // Article Details Dialog
    selectedArticle?.let { article ->
        ProTipDetailDialog(
            article = article,
            onDismiss = { selectedArticle = null },
            onGoogleSearch = { query ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                )
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun FeaturedTipCard(
    article: ProTipArticle,
    onClick: () -> Unit,
    onGoogleSearch: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "מומלץ היום",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${article.readTimeMinutes} דק' קריאה",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick) {
                    Text("קרא מדריך מלא", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = { onGoogleSearch(article.googleSearchQuery) }) {
                    Icon(
                        Icons.Default.TravelExplore,
                        contentDescription = "חפש בגוגל",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProTipArticleRow(
    article: ProTipArticle,
    onClick: () -> Unit,
    onGoogleSearch: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = article.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = article.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${article.readTimeMinutes} דק'",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { onGoogleSearch(article.googleSearchQuery) }) {
                Icon(
                    Icons.Default.TravelExplore,
                    contentDescription = "חפש בגוגל",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProTipDetailDialog(
    article: ProTipArticle,
    onDismiss: () -> Unit,
    onGoogleSearch: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(article.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "קטגוריה: ${article.category} | ${article.readTimeMinutes} דקות קריאה",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                item {
                    Text(
                        text = article.fullContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (article.keyTakeaways.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "נקודות מפתח לביצוע:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(article.keyTakeaways) { takeaway ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("✓ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(takeaway, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGoogleSearch(article.googleSearchQuery) }
            ) {
                Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("חפש עוד בגוגל")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("סגור")
            }
        }
    )
}
