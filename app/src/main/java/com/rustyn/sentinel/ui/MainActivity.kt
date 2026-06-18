package com.rustyn.sentinel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

                if (hasScreeningRole && hasContactsPermission) {
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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
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
                                        modifier = Modifier.size(24.dp)
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
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
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
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) }
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Protection Requires Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sentinel needs these permissions to actively intercept spam calls and protect you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Role Card
        GlassmorphicCard {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasScreeningRole) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Call Screening", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("Allows the app to detect and block spam silently.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contacts Card
        GlassmorphicCard {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasContactsPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Contacts Bypass", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("Ensures your saved contacts are never accidentally blocked.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (!hasScreeningRole && roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                } else if (!hasContactsPermission) {
                    contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            val buttonText = if (!hasScreeningRole) "Grant Call Screening" else "Grant Contacts Access"
            Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onExit) {
            Text("Exit App", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

