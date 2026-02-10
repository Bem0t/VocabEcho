package com.myApp27.vocabecho.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myApp27.vocabecho.ui.BrowseDeckDetailScreen
import com.myApp27.vocabecho.ui.BrowseDecksScreen
import com.myApp27.vocabecho.ui.DecksScreen
import com.myApp27.vocabecho.ui.FeedbackScreen
import com.myApp27.vocabecho.ui.LearnScreen
import com.myApp27.vocabecho.ui.ParentSettingsScreen
import com.myApp27.vocabecho.ui.parent.AddCardToDeckScreen
import com.myApp27.vocabecho.ui.parent.AddDeckScreen
import com.myApp27.vocabecho.ui.parent.EditUserCardScreen
import com.myApp27.vocabecho.ui.parent.ManageDeckCardsScreen
import com.myApp27.vocabecho.ui.parent.ManageDecksScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val animDurationMs = 240
    val enterForward: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(animDurationMs)
        ) + fadeIn(animationSpec = tween(animDurationMs))
    }
    val exitForward: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(animDurationMs)
        ) + fadeOut(animationSpec = tween(animDurationMs))
    }
    val enterBack: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(animDurationMs)
        ) + fadeIn(animationSpec = tween(animDurationMs))
    }
    val exitBack: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(animDurationMs)
        ) + fadeOut(animationSpec = tween(animDurationMs))
    }

    NavHost(
        navController = navController,
        startDestination = Routes.DECKS
    ) {

        // 1) Главный экран: выбор колоды
        composable(
            route = Routes.DECKS,
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) {
            DecksScreen(
                onDeckClick = { deckId ->
                    navController.navigate(Routes.learn(deckId))
                },
                onParentsClick = {
                    navController.navigate(Routes.PARENT)
                },
                onBrowseClick = {
                    navController.navigate(Routes.BROWSE_DECKS)
                }
            )
        }

        // 2) Экран обучения: learn/{deckId}
        composable(
            route = Routes.LEARN,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType }),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
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
            ),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
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
        composable(
            route = Routes.PARENT,
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) {
            ParentSettingsScreen(
                onBack = { navController.popBackStack() },
                onAddDeckClick = { navController.navigate(Routes.ADD_DECK) },
                onManageDecksClick = { navController.navigate(Routes.MANAGE_DECKS) }
            )
        }

        // 5) Добавление колоды
        composable(
            route = Routes.ADD_DECK,
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) {
            AddDeckScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 6) Управление колодами
        composable(
            route = Routes.MANAGE_DECKS,
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) {
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
            arguments = listOf(navArgument("deckId") { type = NavType.StringType }),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
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
            ),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
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
            arguments = listOf(navArgument("deckId") { type = NavType.StringType }),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()

            AddCardToDeckScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }

        // 10) Обучение — список колод для просмотра
        composable(
            route = Routes.BROWSE_DECKS,
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) {
            BrowseDecksScreen(
                onDeckClick = { deckId ->
                    navController.navigate(Routes.browseDeckDetail(deckId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 11) Обучение — слово-перевод для конкретной колоды
        composable(
            route = Routes.BROWSE_DECK_DETAIL,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType }),
            enterTransition = enterForward,
            exitTransition = exitForward,
            popEnterTransition = enterBack,
            popExitTransition = exitBack
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId").orEmpty()

            BrowseDeckDetailScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
