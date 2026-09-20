package pl.wataha.app.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.wataha.app.WatahaApp
import pl.wataha.app.data.model.PostType
import pl.wataha.app.ui.components.FlagStripe
import pl.wataha.app.ui.components.KotwicaLogo
import pl.wataha.app.ui.components.WatahaDrawerContent
import pl.wataha.app.ui.screens.AssistantScreen
import pl.wataha.app.ui.screens.AuthScreen
import pl.wataha.app.ui.screens.ChatScreen
import pl.wataha.app.ui.screens.ChatThreadScreen
import pl.wataha.app.ui.screens.CreatePostScreen
import pl.wataha.app.ui.screens.CrisisScreen
import pl.wataha.app.ui.screens.ExchangesScreen
import pl.wataha.app.ui.screens.FeedbackScreen
import pl.wataha.app.ui.screens.HelpScreen
import pl.wataha.app.ui.screens.HomeScreen
import pl.wataha.app.ui.screens.MapScreen
import pl.wataha.app.ui.screens.MeshLabScreen
import pl.wataha.app.ui.screens.NetworkScreen
import pl.wataha.app.ui.screens.NotificationsScreen
import pl.wataha.app.ui.screens.PostDetailScreen
import pl.wataha.app.ui.screens.PostsScreen
import pl.wataha.app.ui.screens.ProfileScreen
import pl.wataha.app.ui.screens.ReputationScreen
import pl.wataha.app.ui.screens.SurvivalScreen
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun WatahaRoot(app: WatahaApp, openCrisis: StateFlow<Boolean>, onCrisisConsumed: () -> Unit = {}) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val crisisReq by openCrisis.collectAsState()

    fun go(route: String) {
        scope.launch { drawerState.close() }
        nav.navigate(route)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            WatahaDrawerContent(
                app = app,
                onNavigate = { go(it) },
                onClose = { scope.launch { drawerState.close() } },
                onLogout = {
                    scope.launch { drawerState.close() }
                    app.repo.logout()
                    nav.navigate("auth") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    ) {
        NavHost(
            navController = nav,
            startDestination = "splash",
            enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(260)) { it / 5 } },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(260)) { it / 5 } }
        ) {

            composable("splash") {
                SplashScreen(app = app, onReady = { hasSession ->
                    nav.navigate(if (hasSession) "home" else "auth") { popUpTo("splash") { inclusive = true } }
                })
            }

            composable("auth") {
                AuthScreen(repo = app.repo, onLoggedIn = {
                    nav.navigate("home") { popUpTo("auth") { inclusive = true } }
                })
            }

            composable("home") {
                HomeScreen(
                    repo = app.repo,
                    comm = app.comm,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNav = { nav.navigate(it) }
                )
            }

            composable("posts") {
                PostsScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onOpen = { id -> nav.navigate("post/$id") },
                    onCreate = { nav.navigate("create") }
                )
            }

            composable(
                "post/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { entry ->
                PostDetailScreen(
                    repo = app.repo,
                    postId = entry.arguments?.getString("postId") ?: "",
                    onBack = { nav.popBackStack() },
                    onOpenThread = { uid -> nav.navigate("newchat/$uid") },
                    onNavigateMap = { nav.navigate("map") }
                )
            }

            composable(
                "create/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { entry ->
                val t = entry.arguments?.getString("type")
                val initial = PostType.entries.firstOrNull { it.name == t }
                CreatePostScreen(
                    repo = app.repo,
                    initialType = initial,
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() },
                    onRequestLocation = { nav.navigate("map") }
                )
            }

            composable("create") {
                CreatePostScreen(
                    repo = app.repo,
                    initialType = null,
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() },
                    onRequestLocation = { nav.navigate("map") }
                )
            }

            composable("chat") {
                ChatScreen(repo = app.repo, onBack = { nav.popBackStack() },
                    onOpenThread = { tid -> nav.navigate("thread/$tid") })
            }

            composable(
                "thread/{threadId}",
                arguments = listOf(navArgument("threadId") { type = NavType.StringType })
            ) { entry ->
                ChatThreadScreen(
                    repo = app.repo,
                    threadId = entry.arguments?.getString("threadId") ?: "",
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                "newchat/{uid}",
                arguments = listOf(navArgument("uid") { type = NavType.StringType })
            ) { entry ->
                ChatThreadScreen(
                    repo = app.repo,
                    recipientId = entry.arguments?.getString("uid"),
                    onBack = { nav.popBackStack() }
                )
            }

            composable("exchanges") {
                ExchangesScreen(repo = app.repo, onBack = { nav.popBackStack() })
            }

            composable("help") {
                HelpScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onNeed = { nav.navigate("create/NEED") },
                    onOffer = { nav.navigate("create/OFFER") },
                    onOpen = { id -> nav.navigate("post/$id") }
                )
            }

            composable("map") {
                MapScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onWriteTo = { uid -> nav.navigate("newchat/$uid") }
                )
            }

            composable("reputation") {
                ReputationScreen(repo = app.repo, onBack = { nav.popBackStack() })
            }

            composable("profile") {
                ProfileScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onLoggedOut = {
                        nav.navigate("auth") { popUpTo(0) { inclusive = true } }
                    },
                    appVersion = "0.2.0"
                )
            }

            composable("crisis") {
                CrisisScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onMap = { nav.navigate("map") },
                    onNetwork = { nav.navigate("network") }
                )
            }

            composable("network") {
                NetworkScreen(
                    repo = app.repo, comm = app.comm,
                    onBack = { nav.popBackStack() },
                    onMesh = { nav.navigate("mesh") }
                )
            }

            composable("mesh") {
                MeshLabScreen(comm = app.comm, onBack = { nav.popBackStack() })
            }

            composable("notification-test") {
                NotificationsScreen(onBack = { nav.popBackStack() })
            }

            composable("assistant") {
                AssistantScreen(
                    repo = app.repo,
                    onBack = { nav.popBackStack() },
                    onCrisis = { nav.navigate("crisis") }
                )
            }

            composable("survival") {
                SurvivalScreen(repo = app.repo, onBack = { nav.popBackStack() })
            }

            composable("feedback") {
                FeedbackScreen(repo = app.repo, onBack = { nav.popBackStack() })
            }
        }
    }

    LaunchedEffect(crisisReq) {
        if (crisisReq) {
            nav.navigate("crisis")
            onCrisisConsumed()
        }
    }
}

@Composable
private fun SplashScreen(app: WatahaApp, onReady: (Boolean) -> Unit) {
    val ready by app.repo.ready.collectAsState()
    val me by app.repo.me.collectAsState()
    var shown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(WatahaColors.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KotwicaLogo(size = 96.dp)
        Spacer(Modifier.height(16.dp))
        Text("POLSKA WATAHA", style = MaterialTheme.typography.headlineLarge, color = WatahaColors.Ink)
        Spacer(Modifier.height(4.dp))
        Text("Biel i czerwień. Razem przetrwamy.", style = MaterialTheme.typography.bodyMedium, color = WatahaColors.Grey)
        Spacer(Modifier.height(10.dp))
        FlagStripe(Modifier.fillMaxWidth().height(8.dp))
        Spacer(Modifier.height(18.dp))
        Text("V0.2 • Czarny Wilk • offline-first • WILK AI", fontSize = 11.sp, color = WatahaColors.Grey)
    }

    LaunchedEffect(ready) {
        if (ready && !shown) {
            shown = true
            kotlinx.coroutines.delay(450)
            onReady(me != null)
        }
    }
}
