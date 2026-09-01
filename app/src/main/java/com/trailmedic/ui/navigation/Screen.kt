package com.trailmedic.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object ModelDownload : Screen("model_download")
    data object Home : Screen("home")
    data object Chat : Screen("chat/{categoryId}") {
        fun createRoute(categoryId: String) = "chat/$categoryId"
    }
    data object Result : Screen("result/{sessionId}") {
        fun createRoute(sessionId: String) = "result/$sessionId"
    }
    data object History : Screen("history")
    data object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }
    data object Settings : Screen("settings")
}
