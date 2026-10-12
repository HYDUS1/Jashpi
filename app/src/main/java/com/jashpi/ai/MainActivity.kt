package com.jashpi.ai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF5E5CE6))) {
                JashpiApp()
            }
        }
    }
}

@Composable
fun JashpiApp() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("JashpiPrefs", Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf(prefs.getString("userName", "") ?: "") }
    var isDeveloper by remember { mutableStateOf(prefs.getBoolean("isDeveloper", false)) }
    var selectedTab by remember { mutableStateOf(0) }

    if (userName.isEmpty()) {
        FirstTimeSetup(onComplete = { name, lang ->
            prefs.edit().putString("userName", name).putString("language", lang).apply()
            userName = name
        })
    } else {
        MainScreen(
            userName = userName,
            prefs = prefs,
            isDeveloper = isDeveloper,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onDeveloperCodeSubmit = { input ->
                if (input == "J@shpi_Dev_2024!") {
                    prefs.edit().putBoolean("isDeveloper", true).apply()
                    isDeveloper = true
                }
            }
        )
    }
}

@Composable
fun FirstTimeSetup(onComplete: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Welcome to Jashpi AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E5CE6))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("What is your name?") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Text("Select Language:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("English", "Hindi", "Hinglish").forEach { lang ->
                FilterChip(selected = language == lang, onClick = { language = lang }, label = { Text(lang) })
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { if (name.isNotBlank()) onComplete(name, language) }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = name.isNotBlank()) {
            Text("Start Jashpi AI", fontSize = 18.sp)
        }
    }
}

@Composable
fun MainScreen(userName: String, prefs: android.content.SharedPreferences, isDeveloper: Boolean, selectedTab: Int, onTabSelected: (Int) -> Unit, onDeveloperCodeSubmit: (String) -> Unit) {
    var showSosDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Chat", "Discover", "Library", "Settings")

    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text("🚨 SOS ACTIVATED 🚨", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Sending live location and alert to Telegram...") },
            confirmButton = { Button(onClick = { showSosDialog = false }) { Text("Cancel SOS") } }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    showSosDialog = true
                    val token = prefs.getString("telegram_token", "") ?: ""
                    val chatId = prefs.getString("telegram_chat_id", "") ?: ""
                    TelegramHelper.sendSOS(token, chatId, userName)
                },
                containerColor = Color.Red,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Warning, contentDescription = "SOS") },
                text = { Text("SOS") }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF8F8F8))) {
            when (selectedTab) {
                0 -> ChatScreen(userName, prefs)
                1 -> CenterText("Discover: Safety Guides Coming Soon")
                2 -> CenterText("Library: Scanned Links & OTP History")
                3 -> SettingsScreen(isDeveloper, prefs, onDeveloperCodeSubmit)
            }
        }
    }
}

@Composable
fun ChatScreen(userName: String, prefs: android.content.SharedPreferences) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Hello $userName! I am Jashpi AI. How can I help you today?") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Jashpi AI", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))

        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = if (msg.startsWith("You:")) Color(0xFFE0E0FF) else Color.White)
                ) {
                    Text(msg, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.Add, contentDescription = "Attach") }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Jashpi AI...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { }) { Icon(Icons.Default.Mic, contentDescription = "Voice") }
            IconButton(onClick = {
                if (inputText.isNotBlank() && !isLoading) {
                    val userMessage = inputText
                    messages.add("You: $userMessage")
                    inputText = ""
                    isLoading = true
                    scope.launch {
                        val apiKey = prefs.getString("gemini_key", "") ?: ""
                        val response = GeminiApi.getResponse(apiKey, userMessage)
                        messages.add("Jashpi AI: $response")
                        isLoading = false
                    }
                }
            }) { Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF5E5CE6)) }
        }
    }
}

@Composable
fun SettingsScreen(isDeveloper: Boolean, prefs: android.content.SharedPreferences, onDeveloperCodeSubmit: (String) -> Unit) {
    var cheatCode by remember { mutableStateOf("") }
    var geminiKey by remember { mutableStateOf(prefs.getString("gemini_key", "") ?: "") }
    var telegramToken by remember { mutableStateOf(prefs.getString("telegram_token", "") ?: "") }
    var telegramChatId by remember { mutableStateOf(prefs.getString("telegram_chat_id", "") ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isDeveloper) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚀 DEVELOPER MODE ACTIVE", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(value = geminiKey, onValueChange = { geminiKey = it; prefs.edit().putString("gemini_key", it).apply() }, label = { Text("Gemini API Key") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = telegramToken, onValueChange = { telegramToken = it; prefs.edit().putString("telegram_token", it).apply() }, label = { Text("Telegram Bot Token") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = telegramChatId, onValueChange = { telegramChatId = it; prefs.edit().putString("telegram_chat_id", it).apply() }, label = { Text("Telegram Chat ID") }, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            OutlinedTextField(value = cheatCode, onValueChange = { cheatCode = it; onDeveloperCodeSubmit(it) }, label = { Text("Enter Code") }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
