package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CloneEntity
import com.example.sandbox.SandboxLog
import com.example.ui.CloneSpaceViewModel
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.GrayText
import com.example.ui.theme.LightText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlatePrimary
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTertiary
import com.example.ui.theme.SoftRed
import com.example.ui.theme.TerminalCyan
import com.example.utils.AppInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CloneSpaceApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloneSpaceApp() {
    val viewModel: CloneSpaceViewModel = viewModel()
    
    val clones by viewModel.allClones.collectAsStateWithLifecycle()
    val cloneCount by viewModel.cloneCount.collectAsStateWithLifecycle()
    val scannedApps by viewModel.scannedApps.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    
    val sandboxLogs by viewModel.sandboxLogs.collectAsStateWithLifecycle()
    val isSandboxConnected by viewModel.isSandboxConnected.collectAsStateWithLifecycle()
    val activeRunningClone by viewModel.activeRunningClone.collectAsStateWithLifecycle()
    
    var currentTab by remember { mutableStateOf(0) }
    var showCloneDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root"),
        topBar = {
            Column(
                modifier = Modifier
                    .background(SlateBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CLONE SPACE",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp,
                            color = LightText
                        )
                        Text(
                            text = "Multi-Process Sandbox Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlatePrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    // Host to Sandbox link status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SlateSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSandboxConnected) ActiveGreen else GrayText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSandboxConnected) "ISOLATION ACTIVE" else "DETACHED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSandboxConnected) ActiveGreen else GrayText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Navigation Tabs
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = Color.Transparent,
                    contentColor = SlatePrimary,
                    divider = {}
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Virtual Space")
                            }
                        },
                        selectedContentColor = LightText,
                        unselectedContentColor = GrayText
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Architecture")
                            }
                        },
                        selectedContentColor = LightText,
                        unselectedContentColor = GrayText
                    )
                    Tab(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Code Specs")
                            }
                        },
                        selectedContentColor = LightText,
                        unselectedContentColor = GrayText
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0 && activeRunningClone == null) {
                FloatingActionButton(
                    onClick = { showCloneDialog = true },
                    containerColor = SlatePrimary,
                    contentColor = Color(0xFF003258),
                    modifier = Modifier.testTag("add_clone_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Clone")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clone App", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = SlateBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> VirtualSpaceDashboard(
                    clones = clones,
                    cloneCount = cloneCount,
                    activeRunningClone = activeRunningClone,
                    sandboxLogs = sandboxLogs,
                    isPlaying = isSandboxConnected,
                    onLaunch = { clone -> viewModel.launchClone(clone) },
                    onDelete = { clone -> viewModel.deleteClone(clone) },
                    onKillProcess = { viewModel.stopActiveSandbox() },
                    onClearConsole = { viewModel.clearConsole() }
                )
                1 -> ArchitectureView()
                2 -> SourceShowcaseView()
            }
            
            if (showCloneDialog) {
                CloneAppDialog(
                    scannedApps = scannedApps,
                    isScanning = isScanning,
                    onDismiss = { showCloneDialog = false },
                    onRefreshList = { viewModel.scanInstalledApps() },
                    onConfirmClone = { appInfo, customName, colorHex, isIsolated, fakeDevice, fakeAndroid ->
                        viewModel.cloneApp(
                            app = appInfo,
                            customName = customName,
                            customColorHex = colorHex,
                            isIsolated = isIsolated,
                            fakeDeviceId = fakeDevice,
                            fakeAndroidId = fakeAndroid,
                            onLimitReached = {
                                Toast.makeText(context, "Clone limit reached! Maximum 100 profiles allowed.", Toast.LENGTH_LONG).show()
                            },
                            onSuccess = {
                                Toast.makeText(context, "App cloned successfully to environment profile!", Toast.LENGTH_SHORT).show()
                                showCloneDialog = false
                            }
                        )
                    }
                )
            }
        }
    }
}

// TAB 1: Dashboard with app lists and terminal emulator log
@Composable
fun VirtualSpaceDashboard(
    clones: List<CloneEntity>,
    cloneCount: Int,
    activeRunningClone: CloneEntity?,
    sandboxLogs: List<SandboxLog>,
    isPlaying: Boolean,
    onLaunch: (CloneEntity) -> Unit,
    onDelete: (CloneEntity) -> Unit,
    onKillProcess: () -> Unit,
    onClearConsole: () -> Unit
) {
    if (activeRunningClone != null) {
        // Run Sandbox Terminal Console Screen
        SandboxTerminalConsole(
            clone = activeRunningClone,
            logs = sandboxLogs,
            onKill = onKillProcess,
            onClear = onClearConsole
        )
    } else {
        // App clone profiles overview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Memory & stats bar
            PerformanceMonitorBar(cloneCount = cloneCount)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (clones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty profiles",
                            tint = GrayText,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No cloned profiles configured",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Click 'Clone App' below to spawn your first isolated parallel workspace. Supports up to 100 clones completely ad-free.",
                            fontSize = 12.sp,
                            color = GrayText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "SANDBOX CLONES (${clones.size} / 100)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clone_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(clones, key = { it.id }) { clone ->
                        CloneProfileItemCard(
                            clone = clone,
                            onLaunch = { onLaunch(clone) },
                            onDelete = { onDelete(clone) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceMonitorBar(cloneCount: Int) {
    Card(
        shape = RoundedCornerShape(24.dp), // rounded-3xl from CSS
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant), // bg-[#2D2F31]
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Active Instances count with elegant blue accent
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ACTIVE INSTANCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$cloneCount ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlatePrimary
                    )
                    Text(
                        text = "/ 100",
                        fontSize = 13.sp,
                        color = GrayText
                    )
                }
            }
            
            // Right Column: RAM loading progress
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RAM USAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val usedRamFraction = (1.2 + cloneCount * 0.15).coerceIn(1.2, 7.9)
                val percentUsed = (usedRamFraction / 8.0).toFloat().coerceIn(0f, 1f)
                Text(
                    text = "${String.format(Locale.US, "%.1f", usedRamFraction)} GB / 8.0 GB",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TerminalCyan
                )
                
                // M3 Loader bar
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OutlineBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentUsed)
                            .background(SlatePrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceMetricBlock(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun CloneProfileItemCard(
    clone: CloneEntity,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = remember(clone.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(clone.colorHex))
        } catch (e: Exception) {
            SlatePrimary
        }
    }
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated premium squircle colored app icon Tinted (rounded-xl from design guidelines)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(2.dp, accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Get application initial or clone badge
                Text(
                    text = clone.name.take(2).uppercase(),
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                
                // Index label badge at bottom-right of the squircle
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clone.cloneNumber.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Name metadata details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clone.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LightText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = clone.packageName,
                    fontSize = 11.sp,
                    color = GrayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sandbox Process Status tags
                    Box(
                        modifier = Modifier
                            .background(SlateSurfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "INDEX #${clone.cloneNumber}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlatePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (clone.isIsolated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(SlateSurfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ISOLATED PID",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Interaction Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Play Launch
                IconButton(
                    onClick = onLaunch,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SlatePrimary.copy(alpha = 0.15f),
                        contentColor = SlatePrimary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run App Instance",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Delete Profile
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SoftRed.copy(alpha = 0.1f),
                        contentColor = SoftRed
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete App Instance",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// SUB-SCREEN 1B: Retro Sandboxed Exec Console
@Composable
fun SandboxTerminalConsole(
    clone: CloneEntity,
    logs: List<SandboxLog>,
    onKill: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Auto scroll bottom when logs update
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Controller banner
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = clone.name.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Text(
                            text = "Sandboxed package: ${clone.packageName}",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Clear terminal button
                    OutlinedButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayText),
                        border = BorderStroke(1.dp, SlateSurfaceVariant)
                    ) {
                        Text("Clear Logs", fontSize = 11.sp)
                    }
                    
                    // Kill process button
                    Button(
                        onClick = onKill,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftRed, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kill Sandbox", fontSize = 11.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Terminal Window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF030712)) // Pure terminal dark
                .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header prompt style
                Text(
                    text = "CLONESPACE SANDBOX CONSOLE SYSTEM INIT V3.5",
                    fontSize = 11.sp,
                    color = TerminalCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target Engine Module: ${clone.packageName}",
                    fontSize = 10.sp,
                    color = GrayText,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "=========================================",
                    fontSize = 10.sp,
                    color = SlateSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                if (logs.isEmpty()) {
                    Text(
                        text = "⏳ Spawning OS context & loading proxy instances...",
                        fontSize = 12.sp,
                        color = ActiveGreen,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    logs.forEach { log ->
                        val formatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
                        val timeStr = formatter.format(Date(log.timestamp))
                        
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "[$timeStr] ",
                                fontSize = 11.sp,
                                color = SlatePrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log.message,
                                fontSize = 11.sp,
                                color = if (log.message.contains("❌") || log.message.contains("🔴") || log.message.contains("failed")) SoftRed 
                                         else if (log.message.contains("✅") || log.message.contains("Security Sandbox Verified")) ActiveGreen
                                         else LightText,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                
                // Infinite blink cursor simulation
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "clonespace@sandbox_proc_${android.os.Process.myPid()}:$ ",
                        fontSize = 11.sp,
                        color = GrayText,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 6.dp, height = 12.dp)
                            .background(ActiveGreen)
                    )
                }
            }
        }
    }
}

// TAB 2: Educational interactive diagram screen
@Composable
fun ArchitectureView() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, OutlineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SANDBOX GRAPH & HOW IT WORKS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SlatePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Virtualization in Android bypasses OS limits on dynamic profile registration. Here is how engines like Clone Space deploy secure sandboxes:",
                    fontSize = 12.sp,
                    color = GrayText,
                    lineHeight = 18.sp
                )
            }
        }

        // Section 1: Dynamic Class Loader
        SystemArchitectureBlock(
            title = "1. Class Loader & Proxy Hook Injection",
            description = "At runtime, Android refuses to install multiple instances of an app due to unique ApplicationID constraints. We solve this by compiling custom DexClassLoaders that load the target application binary code dynamically. Our host process masquerades as a 'stub template' dynamically registering activities, services, & receivers to content indices in the OS.",
            icon = Icons.Default.Cached,
            iconColor = SlatePrimary
        )

        // Section 2: Binder API Hooking
        SystemArchitectureBlock(
            title = "2. System Service IPC Interception",
            description = "The cloned binary app queries Android system services (like ActivityManager, PackageManager, Telemetry Services). The custom proxy overrides these default Binder connections. System requests like getPackageInfo() are intercepted at the Binder layer to return the virtual identity index of the cloned profile, preventing collisions.",
            icon = Icons.Default.FilterList,
            iconColor = SlateSecondary
        )

        // Section 3: Storage Sandboxing File Redirection
        SystemArchitectureBlock(
            title = "3. Isolated File System Redirection",
            description = "When a sandbox app attempts to write database credentials (like Telegram Sqlite file), they try to use default Paths on /data/data/org.telegram.messenger. The sandbox intercepts and redirects standard I/O paths to virtual isolated subfolders: e.g. /data/data/com.example/virtual/clone_31/. Access is redirected seamlessly.",
            icon = Icons.Default.FolderOpen,
            iconColor = SlateTertiary
        )
        
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun SystemArchitectureBlock(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LightText
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OutlineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = GrayText,
                lineHeight = 18.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

// TAB 3: Code specification viewer
@Composable
fun SourceShowcaseView() {
    var selectedFileIndex by remember { mutableStateOf(0) }
    val files = listOf("Isolated Service", "Dynamic Loader", "Binder Intercept")
    
    val selectedCodeContent = when(selectedFileIndex) {
        0 -> """
// 📝 File: Manifest & Service Implementation
// Declares the isolated sandboxed process inside the host app
// File: AndroidManifest.xml:
// <service
//     android:name=".sandbox.IsolatedSandboxService"
//     android:isolatedProcess="true"
//     android:process=":isolated_sandbox_service" />

package com.example.sandbox

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process

class IsolatedSandboxService : Service() {
    // Declared with android:isolatedProcess="true"
    // Runs under a distinct Linux UID, separate from the host app
    
    override fun onBind(intent: Intent?): IBinder? {
         // Establish standard Binder IPC communication
         // Prevents colliding database or session reads
         val childPid = Process.myPid()
         val childUid = Process.myUid() 
         
         // Secure Sandbox contains files and processes tightly:
         // System UID >= 99000 limits data permissions
         return mSandboxBinder
    }
}
        """.trimIndent()
        
        1 -> """
// 📝 File: DynamicClassLoader.kt
// Bypasses hardcoded system limits to load dynamic APKs
package com.example.loader

import dalvik.system.DexClassLoader
import java.io.File

class SandboxClassLoader(
    dexPath: String,
    optimizedDir: String,
    librarySearchPath: String,
    parent: ClassLoader
) : DexClassLoader(
    dexPath,          // Source APK path loaded from virtual folder
    optimizedDir,     // Cache directory for secondary DEX optimizations
    librarySearchPath,// Redirection hook for target native library directories (.so)
    parent            // Host class fallback loader
) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // Core hooking: Intercept activity loader instantiation
        if (name.startsWith("com.google.android")) {
            return super.loadClass(name, resolve)
        }
        return findClass(name) ?: super.loadClass(name, resolve)
    }
}
        """.trimIndent()
        
        else -> """
// 📝 File: BinderHookEngine.kt
// Intercepts Service requests from target cloned apps
package com.example.hook

import android.os.IBinder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class BinderProxyHook(
    val baseService: IBinder,
    val clonedPackage: String,
    val cloneNumber: Int
) : InvocationHandler {

    // Overrides standard API call responses with Spoofed configurations
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        val name = method.name
        
        // Intercept Package manager inquiries to isolate package identifiers
        if (name == "getPackageInfo") {
            val originalPackage = args?.get(0) as? String
            if (originalPackage == clonedPackage) {
                // Return proxy manifest definitions customized for index
                return getSandboxPackageManifest(clonedPackage, cloneNumber)
            }
        }
        return method.invoke(baseService, *(args ?: emptyArray()))
    }
}
        """.trimIndent()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, OutlineBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "REFERENCE CODE INJECTOR ENGINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SlatePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Observe direct functional templates of hooks executing inside the Sandbox system.",
                    fontSize = 12.sp,
                    color = GrayText
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            files.forEachIndexed { index, name ->
                val selected = selectedFileIndex == index
                FilterChip(
                    selected = selected,
                    onClick = { selectedFileIndex = index },
                    label = { Text(name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlatePrimary,
                        selectedLabelColor = Color(0xFF003258),
                        containerColor = SlateSurface,
                        labelColor = GrayText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selected) SlatePrimary else OutlineBorder,
                        selectedBorderColor = SlatePrimary,
                        enabled = true,
                        selected = selected
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Monospace code display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121315))
                .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = selectedCodeContent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TerminalCyan,
                lineHeight = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(60.dp))
    }
}

// DIALOG: Interactive Creation Form (supports up to 100 clones list)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneAppDialog(
    scannedApps: List<AppInfo>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onRefreshList: () -> Unit,
    onConfirmClone: (
        app: AppInfo,
        customName: String,
        colorHex: String,
        isIsolated: Boolean,
        fakeDeviceId: String,
        fakeAndroidId: String
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    
    var customLabel by remember { mutableStateOf("") }
    var isIsolated by remember { mutableStateOf(true) }
    var mockDeviceId by remember { mutableStateOf("") }
    var mockAndroidId by remember { mutableStateOf("") }
    
    // Accents lists
    val colorPalettes = listOf("#5865F2", "#24A1DE", "#25D366", "#E1306C", "#FF0000", "#1877F2", "#0077B5", "#F59E0B", "#10B981")
    var selectedColor by remember { mutableStateOf(colorPalettes[0]) }

    val filteredApps = remember(scannedApps, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            scannedApps
        } else {
            scannedApps.filter { it.name.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, OutlineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header back navigation inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedApp != null) {
                        IconButton(
                            onClick = { selectedApp = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = LightText
                            )
                        }
                    }
                    Text(
                        text = if (selectedApp == null) "CHOOSE INSTALLED TARGET APP" else "CONFIGURE SANDBOX CONTAINER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlatePrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = if (selectedApp == null) TextAlign.Start else TextAlign.Center
                    )
                    
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = LightText)
                    }
                }
                
                HorizontalDivider(color = OutlineBorder, modifier = Modifier.padding(vertical = 12.dp))

                if (selectedApp == null) {
                    // STEP 1: App Selection List
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search system or package database", color = GrayText, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                        modifier = Modifier.fillMaxWidth().testTag("app_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlatePrimary,
                            unfocusedBorderColor = OutlineBorder,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (isScanning) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SlatePrimary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredApps) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateSurfaceVariant)
                                        .border(BorderStroke(1.dp, OutlineBorder), RoundedCornerShape(12.dp))
                                        .clickable { 
                                            selectedApp = app 
                                            customLabel = "${app.name} Clone"
                                            // Mock default IDs automatically
                                            mockDeviceId = "IMEI-" + (100000000000000..999999999999999).random().toString().take(15)
                                            mockAndroidId = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Colored Tinted Indicator Block
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SlatePrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = app.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = SlatePrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.name, fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                        Text(app.packageName, color = GrayText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(SlateSurface.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (app.isSystem) "System" else app.category,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (app.isSystem) SlateTertiary else SlateSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // STEP 2: Configure Sandbox properties
                    val app = selectedApp!!
                    val keyboardController = LocalSoftwareKeyboardController.current
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Display selected package
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceVariant, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, OutlineBorder), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(selectedColor)).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(app.name.take(1).uppercase(), color = Color(android.graphics.Color.parseColor(selectedColor)), fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(app.name, fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                Text(app.packageName, fontSize = 11.sp, color = GrayText)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Form Name
                        Text("PROFILE LABEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customLabel,
                            onValueChange = { customLabel = it },
                            placeholder = { Text("E.g. Parallel Tele 1", color = GrayText) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SlatePrimary,
                                unfocusedBorderColor = OutlineBorder,
                                focusedTextColor = LightText
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Select accent tint
                        Text("WORKSPACE THEME ACCENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            colorPalettes.forEach { itemHex ->
                                val colorParsed = Color(android.graphics.Color.parseColor(itemHex))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(colorParsed)
                                        .border(
                                            width = if (selectedColor == itemHex) 3.dp else 1.dp,
                                            color = if (selectedColor == itemHex) LightText else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = itemHex }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Container level isolations
                        Text("CONTAINER ISOLATION SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Toggle 1: Isolated Process
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Isolated Native Process", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightText)
                                Text("Fork dedicated Service with android:isolatedProcess=true and security constraints.", fontSize = 10.sp, color = GrayText)
                            }
                            Switch(
                                checked = isIsolated, 
                                onCheckedChange = { isIsolated = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LightText,
                                    checkedTrackColor = SlatePrimary
                                )
                            )
                        }
                        
                        HorizontalDivider(color = OutlineBorder, modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Toggle 2: Storage redirs info
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Storage Sandboxing Redirect", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LightText)
                                Text("Redirect standard /data/data filesystem queries to sandbox database subpaths.", fontSize = 10.sp, color = GrayText)
                            }
                            IconButton(onClick = {}, colors = IconButtonDefaults.iconButtonColors(contentColor = SlateSecondary)) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Always Enabled")
                            }
                        }
                        
                        HorizontalDivider(color = OutlineBorder, modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Spoof device properties config
                        Text("SPOOF TELEMETRY PROPERTIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mockDeviceId,
                                onValueChange = { mockDeviceId = it },
                                label = { Text("Mock IMEI ID", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SlatePrimary,
                                    unfocusedBorderColor = OutlineBorder,
                                    focusedTextColor = LightText
                                )
                            )
                            OutlinedTextField(
                                value = mockAndroidId,
                                onValueChange = { mockAndroidId = it },
                                label = { Text("Mock Android ID", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SlatePrimary,
                                    unfocusedBorderColor = OutlineBorder,
                                    focusedTextColor = LightText
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Save button
                        Button(
                            onClick = {
                                onConfirmClone(
                                    app,
                                    customLabel,
                                    selectedColor,
                                    isIsolated,
                                    mockDeviceId,
                                    mockAndroidId
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary, contentColor = Color(0xFF003258)),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_clone_button")
                        ) {
                            Text("Spawn Isolated Profile Workspace", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
