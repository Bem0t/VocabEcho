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
import com.myApp27.vocabecho.ui.parent.AddCardToDeckScreen
import com.myApp27.vocabecho.ui.parent.AddDeckScreen
import com.myApp27.vocabecho.ui.parent.EditUserCardScreen
import com.myApp27.vocabecho.ui.parent.ManageDeckCardsScreen
import com.myApp27.vocabecho.ui.parent.ManageDecksScreen

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
                },
                onBack = { navController.popBackStack() }
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
                },
                onBack = {
                    // Return to decks screen
                    navController.popBackStack(Routes.DECKS, inclusive = false)
                }
            )
        }

        // 4) Родительские настройки
        composable(Routes.PARENT) {
            ParentSettingsScreen(
                onBack = { navController.popBackStack() },
                onAddDeckClick = { navController.navigate(Routes.ADD_DECK) },
                onManageDecksClick = { navController.navigate(Routes.MANAGE_DECKS) }
            )
        }

        // 5) Добавление колоды
        composable(Routes.ADD_DECK) {
            AddDeckScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 6) Управление колодами
        composable(Routes.MANAGE_DECKS) {
            ManageDecksScreen(
                onBack = { navController.popBackStack() },
                onOpenDeck = { deckId ->
                    navController.navigate(Routes.manageDeckCards(deckId))
                }
            )
        }

        // 7) Карточки колоды
        composable(
            route = Routes.MANAGE_DECK_CARDS,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()

            ManageDeckCardsScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onEditCard = { cardId ->
                    navController.navigate(Routes.editUserCard(deckId, cardId))
                },
                onAddCard = {
                    navController.navigate(Routes.addCardToDeck(deckId))
                }
            )
        }

        // 8) Редактирование карточки
        composable(
            route = Routes.EDIT_USER_CARD,
            arguments = listOf(
                navArgument("deckId") { type = NavType.StringType },
                navArgument("cardId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()
            val cardId = backStackEntry.arguments?.getString("cardId").orEmpty()

            EditUserCardScreen(
                deckId = deckId,
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }

        // 9) Добавление карточки в существующую колоду
        composable(
            route = Routes.ADD_CARD_TO_DECK,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()

            AddCardToDeckScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
