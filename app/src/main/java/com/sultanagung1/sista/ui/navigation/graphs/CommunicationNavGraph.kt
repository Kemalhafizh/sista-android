package com.sultanagung1.sista.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.sultanagung1.sista.ui.navigation.guardedComposable
import androidx.navigation.navArgument
import com.sultanagung1.sista.ui.announcements.AnnouncementDetailScreen
import com.sultanagung1.sista.ui.announcements.AnnouncementFeedScreen
import com.sultanagung1.sista.ui.announcements.AnnouncementDetailViewModel
import com.sultanagung1.sista.ui.announcements.AnnouncementFeedViewModel
import com.sultanagung1.sista.ui.chat.AdaptiveChatScreen
import com.sultanagung1.sista.ui.chat.ChatScreen
import com.sultanagung1.sista.ui.chat.ChatViewModel
import com.sultanagung1.sista.ui.chat.ConversationListScreen
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.notifications.NotificationCenterScreen
import com.sultanagung1.sista.ui.notifications.NotificationSettingsScreen
import com.sultanagung1.sista.ui.notifications.NotificationViewModel

/**
 * Sub-Navigation Graph untuk Modul Komunikasi, Chat Konsultasi & Notifikasi (FASE 53.2 & FASE 55.1).
 */
fun NavGraphBuilder.communicationNavGraph(
    navController: NavHostController
) {
    guardedComposable(Screen.ConversationList.route) {
        val viewModel: ChatViewModel = hiltViewModel()
        AdaptiveChatScreen(
            viewModel = viewModel,
            onNavigateToChatRoom = { convId ->
                navController.navigate(Screen.Chat.createRoute(convId))
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(
        route = Screen.Chat.route,
        arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
    ) { backStackEntry ->
        val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
        val viewModel: ChatViewModel = hiltViewModel()
        ChatScreen(
            conversationId = convId,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.AnnouncementFeed.route) {
        val viewModel: AnnouncementFeedViewModel = hiltViewModel()
        AnnouncementFeedScreen(
            viewModel = viewModel,
            onNavigateToDetail = { id -> navController.navigate(Screen.AnnouncementDetail.createRoute(id)) },
            onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null,
        )
    }

    guardedComposable(
        route = Screen.AnnouncementDetail.route,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
        val viewModel: AnnouncementDetailViewModel = hiltViewModel()
        AnnouncementDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    guardedComposable(Screen.NotificationCenter.route) {
        val viewModel: NotificationViewModel = hiltViewModel()
        NotificationCenterScreen(
            viewModel = viewModel,
            onNavigateDeepLink = { route -> navController.navigate(route) },
            onNavigateBack = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null,
            onNavigateToSettings = { navController.navigate(Screen.NotificationSettings.route) },
        )
    }

    guardedComposable(Screen.NotificationSettings.route) {
        NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }
}
