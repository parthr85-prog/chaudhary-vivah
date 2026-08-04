package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.model.Profile
import com.example.ui.screens.*
import com.example.ui.theme.ChaudharyVivahTheme
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.LightRoseContainer
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.viewmodel.MatrimonyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChaudharyVivahTheme {
                val viewModel: MatrimonyViewModel = viewModel()
                val navController = rememberNavController()

                val appLanguage by viewModel.appLanguage.collectAsState()
                val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

                val myProfile by viewModel.myProfile.collectAsState()
                val isAdmin by viewModel.isAdmin.collectAsState()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val pendingProfiles by viewModel.pendingProfiles.collectAsState()

                val isApprovedUser = isAdmin || myProfile.isApproved

                var showFilterSheet by remember { mutableStateOf(false) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide bottom bar on splash, auth, or for unapproved users pending admin approval
                val showBottomBar = isLoggedIn && isApprovedUser && currentRoute in listOf("home", "top_picks", "kundli", "profile_setup", "interests", "admin_dashboard")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text(strings.navHome, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = RoyalMaroon,
                                        selectedTextColor = RoyalMaroon,
                                        indicatorColor = LightRoseContainer
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )

                                if (isAdmin) {
                                    NavigationBarItem(
                                        selected = currentRoute == "admin_dashboard",
                                        onClick = {
                                            navController.navigate("admin_dashboard") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (pendingProfiles.isNotEmpty()) {
                                                        Badge(containerColor = RoyalMaroon) {
                                                            Text("${pendingProfiles.size}", color = Color.White, fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Approval")
                                            }
                                        },
                                        label = { Text(if (appLanguage == "gu") "મંજૂરી પેનલ" else "Approvals", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalMaroon,
                                            selectedTextColor = RoyalMaroon,
                                            indicatorColor = LightRoseContainer
                                        ),
                                        modifier = Modifier.testTag("nav_admin_panel")
                                    )
                                }

                                if (!isAdmin) {
                                    NavigationBarItem(
                                        selected = currentRoute == "top_picks",
                                        onClick = {
                                            navController.navigate("top_picks") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Picks") },
                                        label = { Text(strings.navTopPicks, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalMaroon,
                                            selectedTextColor = RoyalMaroon,
                                            indicatorColor = LightRoseContainer
                                        ),
                                        modifier = Modifier.testTag("nav_top_picks")
                                    )

                                    NavigationBarItem(
                                        selected = currentRoute == "kundli",
                                        onClick = {
                                            navController.navigate("kundli") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Stars, contentDescription = "Kundli") },
                                        label = { Text(strings.navKundli, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalMaroon,
                                            selectedTextColor = RoyalMaroon,
                                            indicatorColor = LightRoseContainer
                                        ),
                                        modifier = Modifier.testTag("nav_kundli")
                                    )

                                    NavigationBarItem(
                                        selected = currentRoute == "interests",
                                        onClick = {
                                            navController.navigate("interests") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "Interests") },
                                        label = { Text(if (appLanguage == "gu") "રસ-પ્રસ્તાવ" else "Interests", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalMaroon,
                                            selectedTextColor = RoyalMaroon,
                                            indicatorColor = LightRoseContainer
                                        ),
                                        modifier = Modifier.testTag("nav_interests")
                                    )

                                    NavigationBarItem(
                                        selected = currentRoute == "profile_setup",
                                        onClick = {
                                            navController.navigate("profile_setup") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "My Bio") },
                                        label = { Text(strings.navMyBio, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = RoyalMaroon,
                                            selectedTextColor = RoyalMaroon,
                                            indicatorColor = LightRoseContainer
                                        ),
                                        modifier = Modifier.testTag("nav_my_bio")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(durationMillis = 350))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(durationMillis = 350))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(durationMillis = 350))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(durationMillis = 350))
                        }
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onSplashComplete = {
                                    viewModel.updateDefaultSearchGender()
                                    if (viewModel.isLoggedIn.value) {
                                        if (viewModel.isAdmin.value || viewModel.myProfile.value.phoneContact.equals("srushtichaudhary11@gmail.com", ignoreCase = true)) {
                                            navController.navigate("admin_dashboard") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        } else if (viewModel.myProfile.value.isApproved) {
                                            navController.navigate("home") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("profile_setup") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate("auth") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("auth") {
                            AuthScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { role, isNewUser ->
                                    viewModel.updateDefaultSearchGender()
                                    viewModel.refreshDashboard()
                                    if (viewModel.isAdmin.value || viewModel.myProfile.value.phoneContact.equals("srushtichaudhary11@gmail.com", ignoreCase = true)) {
                                        navController.navigate("admin_dashboard") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    } else {
                                        if (isNewUser || !viewModel.myProfile.value.isApproved) {
                                            navController.navigate("profile_setup") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("home") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        }
                                    }
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin_dashboard")
                                }
                            )
                        }

                        composable("interests") {
                            com.example.ui.screens.InterestsScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToProfileDetail = { profileId ->
                                    navController.navigate("profile_detail/$profileId")
                                },
                                onNavigateToChat = { profileId ->
                                    navController.navigate("chat/$profileId")
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToProfileDetail = { profileId ->
                                    navController.navigate("profile_detail/$profileId")
                                },
                                onNavigateToKundli = { partner ->
                                    viewModel.selectPartnerForKundli(partner)
                                    navController.navigate("kundli")
                                },
                                onNavigateToChat = { profileId ->
                                    navController.navigate("chat/$profileId")
                                },
                                onNavigateToTopPicks = {
                                    navController.navigate("top_picks")
                                },
                                onOpenFilterSheet = {
                                    showFilterSheet = true
                                },
                                onNavigateToProfileSetup = {
                                    navController.navigate("profile_setup")
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin_dashboard")
                                },
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "profile_detail/{profileId}",
                            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
                            ProfileDetailScreen(
                                profileId = profileId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onKundliClick = { partner ->
                                    viewModel.selectPartnerForKundli(partner)
                                    navController.navigate("kundli")
                                },
                                onChatClick = { pId ->
                                    navController.navigate("chat/$pId")
                                }
                            )
                        }

                        composable("top_picks") {
                            TopPicksScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToProfileDetail = { profileId ->
                                    navController.navigate("profile_detail/$profileId")
                                },
                                onNavigateToChat = { profileId ->
                                    navController.navigate("chat/$profileId")
                                }
                            )
                        }

                        composable("kundli") {
                            KundliMatchScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "chat/{profileId}",
                            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
                            ChatScreen(
                                profileId = profileId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("profile_setup") {
                            ProfileSetupScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("admin_dashboard") {
                            AdminApprovalScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate("home") {
                                            popUpTo("admin_dashboard") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (showFilterSheet) {
                        FilterBottomSheet(
                            viewModel = viewModel,
                            onDismiss = { showFilterSheet = false }
                        )
                    }
                }
            }
        }
    }
}
