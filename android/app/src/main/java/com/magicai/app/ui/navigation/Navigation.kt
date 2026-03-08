package com.magicai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.magicai.app.ui.screens.auth.ForgotPasswordScreen
import com.magicai.app.ui.screens.auth.LoginScreen
import com.magicai.app.ui.screens.auth.RegisterScreen
import com.magicai.app.ui.screens.auth.SplashScreen
import com.magicai.app.ui.screens.chat.ChatScreen
import com.magicai.app.ui.screens.documents.DocumentsScreen
import com.magicai.app.ui.screens.home.HomeScreen
import com.magicai.app.ui.screens.image.AIImageScreen
import com.magicai.app.ui.screens.profile.ProfileScreen
import com.magicai.app.ui.screens.subscription.SubscriptionScreen
import com.magicai.app.ui.screens.support.SupportScreen
import com.magicai.app.ui.screens.support.TicketDetailScreen
import com.magicai.app.ui.screens.voiceover.VoiceOverScreen
import com.magicai.app.ui.screens.writer.AIWriterScreen
import com.magicai.app.ui.screens.writer.WriterGenerateScreen
import com.magicai.app.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Chat : Screen("chat/{categorySlug}/{chatId}") {
        fun createRoute(categorySlug: String = "ai-chat-bot", chatId: String = "new") =
            "chat/$categorySlug/$chatId"
    }
    object AIWriter : Screen("ai_writer")
    object WriterGenerate : Screen("writer_generate/{slug}") {
        fun createRoute(slug: String) = "writer_generate/$slug"
    }
    object AIImage : Screen("ai_image")
    object VoiceOver : Screen("voice_over")
    object Documents : Screen("documents")
    object Profile : Screen("profile")
    object Support : Screen("support")
    object TicketDetail : Screen("ticket/{ticketId}") {
        fun createRoute(ticketId: Int) = "ticket/$ticketId"
    }
    object Subscription : Screen("subscription")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    afterSplashDestination: String = startDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(afterSplashDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Main Screens
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { slug ->
                    navController.navigate(Screen.Chat.createRoute(slug, "new"))
                },
                onNavigateToWriter = { navController.navigate(Screen.AIWriter.route) },
                onNavigateToImage = { navController.navigate(Screen.AIImage.route) },
                onNavigateToVoiceOver = { navController.navigate(Screen.VoiceOver.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("categorySlug") { type = NavType.StringType },
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categorySlug = backStackEntry.arguments?.getString("categorySlug") ?: "ai-chat-bot"
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "new"
            ChatScreen(
                categorySlug = categorySlug,
                initialChatId = if (chatId == "new") null else chatId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIWriter.route) {
            AIWriterScreen(
                onNavigateToGenerate = { slug ->
                    navController.navigate(Screen.WriterGenerate.createRoute(slug))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WriterGenerate.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            WriterGenerateScreen(
                slug = slug,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIImage.route) {
            AIImageScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.VoiceOver.route) {
            VoiceOverScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Documents.route) {
            DocumentsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) }
            )
        }

        composable(Screen.Support.route) {
            SupportScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTicket = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                }
            )
        }

        composable(
            route = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 0
            TicketDetailScreen(
                ticketId = ticketId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Subscription.route) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
        }
    }
}
