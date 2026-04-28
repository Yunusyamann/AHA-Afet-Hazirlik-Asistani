package com.aha.afethazirlikasistani

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val Navy = Color(0xFF071B4D)
private val Blue = Color(0xFF0B76F6)
private val SoftBlue = Color(0xFFEAF4FF)
private val SoftGreen = Color(0xFFEAFBF2)
private val SoftOrange = Color(0xFFFFF5E8)
private val SoftRed = Color(0xFFFFEEEE)
private val TextDark = Color(0xFF162033)
private val TextMuted = Color(0xFF667085)
private val BorderLight = Color(0xFFE6EAF0)
private val PageBg = Color(0xFFFFFFFF)

enum class Screen {
    HOME, BAG, CHECKLIST, RISK, EMERGENCY, PROFILE, MAP, GUIDE
}

data class CheckItem(
    val title: String,
    val description: String,
    val weight: Int,
    val defaultChecked: Boolean
)

data class FeatureCardData(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val target: Screen,
    val color: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        setContent {
            AhaTheme {
                AhaApp()
            }
        }
    }
}

@Composable
fun AhaTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Blue,
        secondary = Navy,
        background = PageBg,
        surface = Color.White,
        onPrimary = Color.White,
        onSurface = TextDark
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@Composable
fun AhaApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            AhaBottomBar(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBg)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(onNavigate = { currentScreen = it })
                Screen.BAG -> DisasterBagScreen()
                Screen.CHECKLIST -> HomeChecklistScreen()
                Screen.RISK -> VisualRiskScreen()
                Screen.EMERGENCY -> EmergencyScreen(onNavigate = { currentScreen = it })
                Screen.PROFILE -> PersonalInfoScreen()
                Screen.MAP -> MapScreen()
                Screen.GUIDE -> OfflineGuideScreen()
            }
        }
    }
}

@Composable
fun AhaBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        Triple(Screen.HOME, "Ana Sayfa", "⌂"),
        Triple(Screen.BAG, "Çanta", "▣"),
        Triple(Screen.CHECKLIST, "Ev", "✓"),
        Triple(Screen.RISK, "Risk", "◇"),
        Triple(Screen.EMERGENCY, "Acil", "!")
    )

    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.first
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clipLike()
                        .background(if (selected) SoftBlue else Color.Transparent)
                        .clickable { onNavigate(item.first) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.third,
                        color = if (selected) Blue else TextMuted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.second,
                        color = if (selected) Blue else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun Modifier.clipLike(): Modifier {
    return this.then(
        Modifier
            .border(0.dp, Color.Transparent, RoundedCornerShape(18.dp))
    )
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    val features = listOf(
        FeatureCardData(
            title = "Afet Çantası Kontrolü",
            subtitle = "Fotoğraf yükle, eksikleri yapay zekâ analiz etsin.",
            emoji = "🎒",
            target = Screen.BAG,
            color = SoftBlue
        ),
        FeatureCardData(
            title = "Ev Hazırlık Skoru",
            subtitle = "Dolap, TV, çıkış yolu ve aile planını kontrol et.",
            emoji = "🏠",
            target = Screen.CHECKLIST,
            color = SoftGreen
        ),
        FeatureCardData(
            title = "Yapı Görsel Risk Analizi",
            subtitle = "Duvar, kolon ve çatlak fotoğrafları için ön rapor al.",
            emoji = "🔎",
            target = Screen.RISK,
            color = SoftOrange
        ),
        FeatureCardData(
            title = "Acil Durum Modu",
            subtitle = "Toplanma alanı, güven bildirimi ve yol haritası.",
            emoji = "🚨",
            target = Screen.EMERGENCY,
            color = SoftRed
        ),
        FeatureCardData(
            title = "Kişisel Ev Bilgileri",
            subtitle = "Evdeki kişi sayısı ve özel ihtiyaçları tanımla.",
            emoji = "👥",
            target = Screen.PROFILE,
            color = Color(0xFFF4F2FF)
        ),
        FeatureCardData(
            title = "Canlı Afet Haritası",
            subtitle = "Toplanma alanı ve demo yol durumu verilerini gör.",
            emoji = "🗺️",
            target = Screen.MAP,
            color = Color(0xFFEFFFFB)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Header(
                title = "AHA",
                subtitle = "Akıllı Afet Hazırlık Asistanı"
            )

            Spacer(Modifier.height(18.dp))

            HeroScoreCard(
                score = 76,
                title = "Genel Hazırlık Skorunuz",
                description = "Hazırlık seviyeniz iyi. Afet çantası ve ev içi sabitleme kontrollerini tamamlayarak skoru artırabilirsiniz."
            )

            Spacer(Modifier.height(18.dp))

            QuickEmergencyCard(onClick = { onNavigate(Screen.EMERGENCY) })

            Spacer(Modifier.height(22.dp))

            SectionTitle("Modüller")
        }

        items(features) { feature ->
            FeatureCard(feature = feature, onClick = { onNavigate(feature.target) })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun Header(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = Navy,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HeroScoreCard(
    score: Int,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Navy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.18f)
            )
        }
    }
}

@Composable
fun QuickEmergencyCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SoftRed),
        border = BorderStroke(1.dp, Color(0xFFFFD2D2))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🚨", fontSize = 32.sp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Acil Durum Modu",
                    color = Color(0xFFB42318),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Deprem anında hızlı yönlendirme ve güven bildirimi.",
                    color = Color(0xFF8A1F17),
                    fontSize = 13.sp
                )
            }
            Text(text = "›", color = Color(0xFFB42318), fontSize = 30.sp)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextDark,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
fun FeatureCard(feature: FeatureCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = feature.color),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = feature.emoji, fontSize = 26.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = feature.subtitle,
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = "›",
                color = TextMuted,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DisasterBagScreen() {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var analyzed by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
        analyzed = uri != null
    }

    val detectedItems = listOf("Su", "Fener", "İlk yardım çantası", "Maske", "Battaniye")
    val missingItems = listOf("Düdük", "Powerbank", "Yedek pil", "Konserve gıda", "Hijyen seti")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Header(
                title = "Afet Çantası",
                subtitle = "Fotoğraf yükleyerek çanta hazırlık durumunu kontrol et."
            )

            Spacer(Modifier.height(18.dp))

            DemoNotice("Bu demo sürümde fotoğraf yüklendiğinde yapay zekâ bağlıymış gibi örnek analiz sonucu gösterilir.")

            Spacer(Modifier.height(16.dp))

            UploadCard(
                title = "Afet çantası fotoğrafı yükle",
                description = "Çantanın içindeki malzemeler görünür olacak şekilde fotoğraf seç.",
                buttonText = if (selectedImage == null) "Fotoğraf Seç" else "Fotoğrafı Değiştir",
                emoji = "🎒",
                onClick = { launcher.launch("image/*") }
            )

            if (analyzed) {
                Spacer(Modifier.height(18.dp))

                HeroScoreCard(
                    score = 68,
                    title = "Çanta Hazırlık Skoru",
                    description = "Temel malzemeler mevcut ancak haberleşme, enerji ve gıda desteği için bazı eksikler tespit edildi."
                )

                Spacer(Modifier.height(18.dp))

                ResultListCard(
                    title = "Tespit Edilen Malzemeler",
                    items = detectedItems,
                    color = SoftGreen
                )

                Spacer(Modifier.height(12.dp))

                ResultListCard(
                    title = "Eksik Malzemeler",
                    items = missingItems,
                    color = SoftOrange
                )

                Spacer(Modifier.height(12.dp))

                RecommendationCard(
                    title = "Öneri",
                    text = "Afet çantanız temel seviyede hazırdır. Düdük, powerbank, yedek pil ve uzun ömürlü gıda ekleyerek çanta hazırlığınızı güçlendirin."
                )
            }
        }
    }
}

@Composable
fun HomeChecklistScreen() {
    val items = remember {
        mutableStateListOf(
            CheckItem("Dolaplar duvara sabitlendi mi?", "Büyük dolap ve kitaplıkların devrilmesini önler.", 10, true),
            CheckItem("Televizyon sabitlendi mi?", "TV ve ekranların düşme riskini azaltır.", 8, false),
            CheckItem("Avize ve ağır aydınlatmalar güvenli mi?", "Tavan bağlantıları kontrol edilmelidir.", 8, true),
            CheckItem("Yatak yanında devrilebilecek ağır eşya var mı?", "Uyku sırasında yaralanma riskini azaltır.", 8, false),
            CheckItem("Acil çıkış yolu açık mı?", "Kapı önü ve koridorlar boş bırakılmalıdır.", 10, true),
            CheckItem("Doğalgaz vanası erişilebilir mi?", "Afet sonrası hızlı kapatma için erişilebilir olmalıdır.", 10, true),
            CheckItem("Yangın söndürücü var mı?", "Yangın başlangıcında hızlı müdahale sağlar.", 8, false),
            CheckItem("Aile afet planı oluşturuldu mu?", "Aile bireylerinin nerede buluşacağını belirler.", 10, false),
            CheckItem("Toplanma alanı biliniyor mu?", "En yakın güvenli alan önceden öğrenilmelidir.", 8, true),
            CheckItem("Özel ihtiyaç planı hazır mı?", "Yaşlı, çocuk, engelli veya evcil hayvan için planlama.", 10, false),
            CheckItem("Apartman acil çıkış planı biliniyor mu?", "Site ve apartman tahliye planı öğrenilmelidir.", 10, true)
        )
    }

    val checkedStates = remember {
        mutableStateListOf<Boolean>().apply {
            addAll(items.map { it.defaultChecked })
        }
    }

    val totalWeight = items.sumOf { it.weight }
    val currentScore = items.indices.sumOf { index ->
        if (checkedStates[index]) items[index].weight else 0
    } * 100 / totalWeight

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Header(
                title = "Ev Hazırlığı",
                subtitle = "Deprem öncesi ev içi riskleri azalt."
            )

            Spacer(Modifier.height(18.dp))

            HeroScoreCard(
                score = currentScore,
                title = "Ev Hazırlık Skoru",
                description = when {
                    currentScore >= 80 -> "Ev içi hazırlık seviyeniz güçlü. Düzenli kontrol etmeyi unutmayın."
                    currentScore >= 60 -> "Hazırlık seviyeniz orta-iyi düzeyde. Eksik maddeleri tamamlayarak daha güvenli hale gelebilirsiniz."
                    else -> "Ev içi hazırlık seviyeniz düşük. Özellikle sabitleme, çıkış yolu ve aile planı başlıklarını tamamlamanız önerilir."
                }
            )

            Spacer(Modifier.height(18.dp))
            SectionTitle("Kontrol Listesi")
        }

        items(items.indices.toList()) { index ->
            ChecklistCard(
                item = items[index],
                checked = checkedStates[index],
                onCheckedChange = { checkedStates[index] = it }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun VisualRiskScreen() {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var analyzed by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
        analyzed = uri != null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Header(
                title = "Görsel Risk",
                subtitle = "Duvar, kolon, kiriş ve tavan fotoğrafları için ön analiz."
            )

            Spacer(Modifier.height(18.dp))

            DemoNotice("Bu analiz kesin yapı güvenliği raporu değildir. Yalnızca yapay zekâ destekli görsel ön değerlendirme olarak düşünülmelidir.")

            Spacer(Modifier.height(16.dp))

            UploadCard(
                title = "Yapı fotoğrafı yükle",
                description = "Duvar, kolon çevresi, kiriş, tavan veya merdiven boşluğu fotoğrafı seç.",
                buttonText = if (selectedImage == null) "Fotoğraf Seç" else "Yeni Fotoğraf Seç",
                emoji = "🔎",
                onClick = { launcher.launch("image/*") }
            )

            if (analyzed) {
                Spacer(Modifier.height(18.dp))

                RiskReportCard()

                Spacer(Modifier.height(12.dp))

                RecommendationCard(
                    title = "Uzman Kontrol Önerisi",
                    text = "Fotoğrafta orta düzey görsel risk belirtileri tespit edildi. Bu sonuç kesin rapor değildir. Yetkili inşaat mühendisi tarafından yerinde inceleme önerilir."
                )
            }
        }
    }
}

@Composable
fun EmergencyScreen(onNavigate: (Screen) -> Unit) {
    var status by remember { mutableStateOf("Henüz bildirim gönderilmedi.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "ACİL DURUM MODU",
                color = Color(0xFFB42318),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Deprem algılandığında veya manuel başlatıldığında kullanıcıyı hızlıca yönlendirir.",
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SoftRed),
                border = BorderStroke(1.dp, Color(0xFFFFD2D2))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Deprem Senaryosu Aktif",
                        color = Color(0xFFB42318),
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "En yakın toplanma alanı, güven bildirimi, çevrimdışı rehber ve yol durumu haritası hazır.",
                        color = Color(0xFF8A1F17),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            EmergencyButton(
                text = "Güvendeyim",
                subText = "Aile bireylerine güven bildirimi gönder.",
                color = Color(0xFF12B76A),
                onClick = { status = "Güvendeyim bildirimi aile bireylerine gönderildi. Demo SMS hazırlandı." }
            )

            Spacer(Modifier.height(10.dp))

            EmergencyButton(
                text = "Yardıma İhtiyacım Var",
                subText = "Yetkililere konum ve ihtiyaç bildirimi ilet.",
                color = Color(0xFFF04438),
                onClick = { status = "Yardım ihtiyacı bildirimi yetkili paneline iletildi. Demo kayıt oluşturuldu." }
            )

            Spacer(Modifier.height(10.dp))

            EmergencyButton(
                text = "Konumumu Paylaş",
                subText = "Kayıtlı acil kişilere konum bağlantısı gönder.",
                color = Blue,
                onClick = { status = "Konum paylaşımı demo olarak hazırlandı." }
            )

            Spacer(Modifier.height(16.dp))

            StatusCard(status)

            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SecondaryActionCard(
                    title = "Toplanma Alanı",
                    emoji = "📍",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.MAP) }
                )

                Spacer(Modifier.width(12.dp))

                SecondaryActionCard(
                    title = "Afet Rehberi",
                    emoji = "📘",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.GUIDE) }
                )
            }

            Spacer(Modifier.height(12.dp))

            SecondaryActionCard(
                title = "Güncel Yol Durumu Haritası",
                emoji = "🗺️",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(Screen.MAP) }
            )
        }
    }
}

@Composable
fun PersonalInfoScreen() {
    var personCount by remember { mutableStateOf("4") }
    var address by remember { mutableStateOf("Kahramanmaraş / Onikişubat") }
    var floor by remember { mutableStateOf("5") }
    var specialNeed by remember { mutableStateOf("1 yaşlı birey, 1 şeker hastası birey") }
    var emergencyContact by remember { mutableStateOf("05XX XXX XX XX") }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Header(
            title = "Ev Bilgileri",
            subtitle = "Afet anında yetkililere operasyonel bilgi sağlar."
        )

        Spacer(Modifier.height(18.dp))

        DemoNotice("Bu bilgiler demo sürümde cihaz içinde tutuluyormuş gibi gösterilir. Gerçek sistemde açık kullanıcı onayıyla yetkili paneline aktarılır.")

        Spacer(Modifier.height(16.dp))

        InputField("Adres / Bölge", address) { address = it }
        InputField("Evdeki kişi sayısı", personCount, KeyboardType.Number) { personCount = it }
        InputField("Bulunduğu kat", floor, KeyboardType.Number) { floor = it }
        InputField("Özel ihtiyaç / sağlık bilgisi", specialNeed) { specialNeed = it }
        InputField("Acil durumda aranacak kişi", emergencyContact, KeyboardType.Phone) { emergencyContact = it }

        Spacer(Modifier.height(12.dp))

        PrimaryButton(
            text = "Bilgileri Kaydet",
            onClick = { saved = true }
        )

        if (saved) {
            Spacer(Modifier.height(16.dp))

            RecommendationCard(
                title = "Yetkili Paneli Demo Çıktısı",
                text = "Adres: $address\nEvde $personCount kişi var. Kat: $floor. Özel durum: $specialNeed. Tahliye ve sağlık desteği gerekebilir."
            )
        }
    }
}

@Composable
fun MapScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Header(
            title = "Afet Haritası",
            subtitle = "OpenStreetMap tabanlı demo toplanma alanı ve yol durumu."
        )

        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MapLegend("Açık yol", Color(0xFF12B76A), Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            MapLegend("Riskli", Color(0xFFF79009), Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            MapLegend("Kapalı", Color(0xFFF04438), Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxSize()
        ) {
            OsmMapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun OfflineGuideScreen() {
    val guideItems = listOf(
        "Deprem sırasında sakin kal, panikle merdiven veya asansöre yönelme.",
        "Çök-Kapan-Tutun pozisyonunu al.",
        "Sarsıntı bittikten sonra gaz, elektrik ve su vanalarını kontrol et.",
        "Ayakkabı giy, afet çantanı al ve güvenli çıkış yolunu kullan.",
        "En yakın toplanma alanına git.",
        "Telefon hatlarını gereksiz meşgul etme, kısa mesaj kullan.",
        "Hasarlı binalara tekrar girme.",
        "Yetkililerin duyurularını takip et."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
    ) {
        item {
            Header(
                title = "Afet Rehberi",
                subtitle = "İnternet olmasa bile erişilebilecek temel bilgiler."
            )

            Spacer(Modifier.height(18.dp))
        }

        items(guideItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = Blue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = item,
                        color = TextDark,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun DemoNotice(text: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SoftBlue),
        border = BorderStroke(1.dp, Color(0xFFD6EAFF))
    ) {
        Text(
            text = text,
            color = Color(0xFF175CD3),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun UploadCard(
    title: String,
    description: String,
    buttonText: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(SoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 36.sp)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = title,
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = buttonText,
                onClick = onClick
            )
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun ResultListCard(title: String, items: List<String>, color: Color) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            items.forEach {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Blue, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = it, color = TextDark, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(title: String, text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = text,
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun ChecklistCard(
    item: CheckItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (checked) Color(0xFFB7F0CF) else BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = item.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF12B76A),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD0D5DD)
                )
            )
        }
    }
}

@Composable
fun RiskReportCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SoftOrange),
        border = BorderStroke(1.dp, Color(0xFFFFE1B3)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚠️", fontSize = 28.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Görsel Yapı Risk Ön Analizi",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Genel Risk Seviyesi: Orta",
                        color = Color(0xFFB54708),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val findings = listOf(
                "Duvar yüzeyinde çatlak benzeri izler algılandı.",
                "Sıva ayrılması ihtimali orta seviyede.",
                "Rutubet/su hasarı belirtisi düşük seviyede.",
                "Kolon-kiriş birleşim bölgesi için ek fotoğraf önerilir."
            )

            findings.forEach {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "•", color = Color(0xFFB54708), fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = TextDark,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyButton(
    text: String,
    subText: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subText,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Text(
                text = "›",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusCard(status: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bildirim Durumu",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = status,
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SecondaryActionCard(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
            .height(112.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = BorderLight,
            focusedLabelColor = Blue,
            cursorColor = Blue
        )
    )
}

@Composable
fun MapLegend(text: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(7.dp))
            Text(text = text, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun OsmMapView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.5)
            controller.setCenter(GeoPoint(37.5753, 36.9228))

            val assemblyArea = Marker(this)
            assemblyArea.position = GeoPoint(37.5753, 36.9228)
            assemblyArea.title = "Toplanma Alanı"
            assemblyArea.subDescription = "Demo güvenli toplanma noktası"
            assemblyArea.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            overlays.add(assemblyArea)

            val hospital = Marker(this)
            hospital.position = GeoPoint(37.5865, 36.9295)
            hospital.title = "Sağlık Destek Noktası"
            hospital.subDescription = "Demo sağlık müdahale alanı"
            hospital.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            overlays.add(hospital)

            val openRoad = Polyline()
            openRoad.setPoints(
                listOf(
                    GeoPoint(37.5720, 36.9160),
                    GeoPoint(37.5753, 36.9228),
                    GeoPoint(37.5800, 36.9270)
                )
            )
            openRoad.outlinePaint.color = AndroidColor.rgb(18, 183, 106)
            openRoad.outlinePaint.strokeWidth = 10f
            overlays.add(openRoad)

            val riskyRoad = Polyline()
            riskyRoad.setPoints(
                listOf(
                    GeoPoint(37.5680, 36.9200),
                    GeoPoint(37.5710, 36.9275),
                    GeoPoint(37.5730, 36.9340)
                )
            )
            riskyRoad.outlinePaint.color = AndroidColor.rgb(247, 144, 9)
            riskyRoad.outlinePaint.strokeWidth = 10f
            overlays.add(riskyRoad)

            val closedRoad = Polyline()
            closedRoad.setPoints(
                listOf(
                    GeoPoint(37.5810, 36.9140),
                    GeoPoint(37.5840, 36.9200),
                    GeoPoint(37.5865, 36.9295)
                )
            )
            closedRoad.outlinePaint.color = AndroidColor.rgb(240, 68, 56)
            closedRoad.outlinePaint.strokeWidth = 10f
            overlays.add(closedRoad)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}