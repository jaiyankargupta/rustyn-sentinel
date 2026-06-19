package com.rustyn.sentinel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rustyn.sentinel.R
import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import com.rustyn.sentinel.ui.components.GlassmorphicCard
import com.rustyn.sentinel.ui.dashboard.DashboardScreen
import com.rustyn.sentinel.ui.dashboard.DashboardViewModel
import com.rustyn.sentinel.ui.history.HistoryScreen
import com.rustyn.sentinel.ui.history.HistoryViewModel
import com.rustyn.sentinel.ui.navigation.Screen
import com.rustyn.sentinel.ui.rules.RulesScreen
import com.rustyn.sentinel.ui.rules.RulesViewModel
import com.rustyn.sentinel.ui.settings.SettingsScreen
import com.rustyn.sentinel.ui.settings.SettingsViewModel
import com.rustyn.sentinel.ui.suggestions.SuggestionsScreen
import com.rustyn.sentinel.ui.suggestions.SuggestionsViewModel
import com.rustyn.sentinel.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RustynSentinelTheme {
                val context = LocalContext.current
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                
                var hasScreeningRole by remember {
                    mutableStateOf(roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true)
                }
                var hasContactsPermission by remember {
                    mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                }
                
                var showSplash by remember { mutableStateOf(true) }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasScreeningRole = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
                            hasContactsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                if (showSplash) {
                    AnimatedSplashScreen {
                        showSplash = false
                    }
                } else if (hasScreeningRole && hasContactsPermission) {
                    SentinelMainContainer()
                } else {
                    PermissionsRequiredScreen(
                        hasScreeningRole = hasScreeningRole,
                        hasContactsPermission = hasContactsPermission,
                        onExit = { finish() }
                    )
                }
            }
        }
    }
}

data class BottomNavTab(
    val screen: Screen,
    val iconRes: Int,
    val label: String
)

@Composable
fun SentinelMainContainer() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Retrieve shared badge counters for suggestions
    val suggestionsViewModel: SuggestionsViewModel = hiltViewModel()
    val suggestionsCount by suggestionsViewModel.suggestions.collectAsState()

    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    val tabs = listOf(
        BottomNavTab(Screen.Dashboard, R.drawable.ic_nav_dashboard, "Dashboard"),
        BottomNavTab(Screen.Rules, R.drawable.ic_nav_rules, "Rules"),
        BottomNavTab(Screen.History, R.drawable.ic_nav_history, "History"),
        BottomNavTab(Screen.Suggestions, R.drawable.ic_nav_suggestions, "Insights")
    )

    val showBottomBar = currentRoute?.startsWith(Screen.Settings.route) != true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val isSelected = currentRoute?.startsWith(tab.screen.route) == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (tab.screen == Screen.Suggestions && suggestionsCount.isNotEmpty()) {
                                            Badge(
                                                containerColor = BlockRed,
                                                contentColor = TextLight
                                            ) {
                                                Text("${suggestionsCount.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = tab.iconRes),
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                unselectedIconColor = TextSubtle,
                                selectedTextColor = PrimarySky,
                                unselectedTextColor = TextSubtle,
                                indicatorColor = PrimarySky
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(250)) }
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToSuggestions = {
                        navController.navigate(Screen.Suggestions.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Rules
            composable(Screen.Rules.route) {
                val rulesViewModel: RulesViewModel = hiltViewModel()
                RulesScreen(viewModel = rulesViewModel)
            }

            // History with deep link argument mapping
            composable(
                route = Screen.History.route + "?highlightedCallId={highlightedCallId}",
                arguments = listOf(
                    navArgument("highlightedCallId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "sentinel://history_detail/{highlightedCallId}"
                    }
                )
            ) { backStackEntry ->
                val highlightedCallId = backStackEntry.arguments?.getInt("highlightedCallId") ?: -1
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(
                    viewModel = historyViewModel,
                    highlightedCallId = if (highlightedCallId != -1) highlightedCallId else null
                )
            }

            // Suggestions
            composable(Screen.Suggestions.route) {
                SuggestionsScreen(viewModel = suggestionsViewModel)
            }

            // Settings
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PermissionsRequiredScreen(
    hasScreeningRole: Boolean,
    hasContactsPermission: Boolean,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
    
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface
                    )
                )
            )
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated shield
        Text("🛡️", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Protection Requires Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Rustyn Sentinel needs these permissions to intercept spam calls and protect you.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(36.dp))

        // Role Card
        PermissionCard(
            icon = "📞",
            title = "Call Screening",
            description = "Allows the app to detect and block spam silently.",
            isGranted = hasScreeningRole
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Contacts Card
        PermissionCard(
            icon = "👤",
            title = "Contacts Bypass",
            description = "Ensures your saved contacts are never blocked.",
            isGranted = hasContactsPermission
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (!hasScreeningRole && roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                } else if (!hasContactsPermission) {
                    contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimarySky,
                contentColor = DarkBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            val buttonText = if (!hasScreeningRole) "Grant Call Screening" else "Grant Contacts Access"
            Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onExit) {
            Text("Exit App", color = TextMuted)
        }
    }
}

@Composable
private fun PermissionCard(
    icon: String,
    title: String,
    description: String,
    isGranted: Boolean
) {
    val borderColor = if (isGranted) SuccessGreen.copy(alpha = 0.3f) else BorderSubtle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isGranted) SuccessGreen.copy(alpha = 0.10f) else PrimarySky.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isGranted) "✓" else icon, fontSize = if (isGranted) 20.sp else 22.sp, color = if (isGranted) SuccessGreen else TextLight)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    title,
                    color = TextLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splashAlpha"
    )
    
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1500) // Hold the splash screen for 1.5 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimarySky.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", fontSize = 48.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rustyn Sentinel",
                color = TextLight,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Call Protection",
                color = PrimarySky,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}
