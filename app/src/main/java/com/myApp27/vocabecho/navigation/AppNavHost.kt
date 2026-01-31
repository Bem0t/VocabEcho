package com.myApp27.vocabecho.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myApp27.vocabecho.ui.DecksScreen
import com.myApp27.vocabecho.ui.FeedbackScreen
import com.myApp27.vocabecho.ui.LearnScreen
import com.myApp27.vocabecho.ui.ParentSettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DECKS
    ) {

        // 1) Главный экран: выбор колоды
        composable(Routes.DECKS) {
            DecksScreen(
                onDeckClick = { deckId ->
                    navController.navigate(Routes.learn(deckId))
                },
                onParentsClick = {
                    navController.navigate(Routes.PARENT)
                }
            )
        }

        // 2) Экран обучения: learn/{deckId}
        composable(
            route = Routes.LEARN,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()

            LearnScreen(
                deckId = deckId,
                onChecked = { cardId, answer ->
                    navController.navigate(Routes.feedback(deckId, cardId, answer))
                }
            )
        }

        // 3) Экран обратной связи: feedback/{deckId}/{cardId}/{answer}
        composable(
            route = Routes.FEEDBACK,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("cardId") { type = NavType.StringType },
                navArgument("answer") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()
            val cardId = backStackEntry.arguments?.getString("cardId").orEmpty()
            val answer = backStackEntry.arguments?.getString("answer").orEmpty()

            FeedbackScreen(
                deckId = deckId,
                cardId = cardId,
                userAnswer = answer,
                onNext = {
                    // Возвращаемся на обучение этой же колоды, не копим feedback в back stack
                    navController.navigate(Routes.learn(deckId)) {
                        popUpTo(Routes.learn(deckId)) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 4) Родительские настройки
        composable(Routes.PARENT) {
            ParentSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
