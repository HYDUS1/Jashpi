package com.jashpi.ai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
            isDeveloper = isDeveloper,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onDeveloperCodeSubmit = { input ->
                // 🔑 SECRET DEVELOPER CHEAT CODE 🔑
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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Jashpi AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E5CE6))
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("What is your name?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Select Language:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("English", "Hindi", "Hinglish").forEach { lang ->
                FilterChip(
                    selected = language == lang,
                    onClick = { language = lang },
                    label = { Text(lang) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { if (name.isNotBlank()) onComplete(name, language) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = name.isNotBlank()
        ) {
            Text("Start Jashpi AI", fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    isDeveloper: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onDeveloperCodeSubmit: (String) -> Unit
) {
    val tabs = listOf("Chat", "Discover", "Library", "Settings")

    Scaffold(
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
                0 -> ChatScreen(userName)
                1 -> CenterText("Discover Screen Coming Soon")
                2 -> CenterText("Library Screen Coming Soon")
                3 -> SettingsScreen(isDeveloper, onDeveloperCodeSubmit)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(userName: String) {
    var inputText by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Jashpi AI", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            actions = {
                IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                IconButton(onClick = { }) { Icon(Icons.Default.Notifications, contentDescription = "Notifications") }
            }
        )

        Column(
            modifier = Modifier.weight(1f).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Star, contentDescription = "Logo", modifier = Modifier.size(64.dp), tint = Color(0xFF5E5CE6))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hello $userName, how can I help?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) { Icon(Icons.Default.Add, contentDescription = "Attach") }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Message AI Chat...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent, unfocusedContainerColor = Color(0xFFF0F0F0), focusedContainerColor = Color(0xFFF0F0F0))
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { }) { Icon(Icons.Default.Mic, contentDescription = "Voice") }
            IconButton(onClick = { }) { Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF5E5CE6)) }
        }
    }
}

@Composable
fun SettingsScreen(isDeveloper: Boolean, onDeveloperCodeSubmit: (String) -> Unit) {
    var cheatCode by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isDeveloper) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚀 DEVELOPER MODE ACTIVE", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text("API Keys and Telegram Setup available here.", fontSize = 12.sp)
                }
            }
        } else {
            OutlinedTextField(
                value = cheatCode,
                onValueChange = { 
                    cheatCode = it
                    onDeveloperCodeSubmit(it)
                },
                label = { Text("Enter Code") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, color = Color.Gray)
    }
}
