package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BourbonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: BourbonViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(viewModel, navController)
                    }
                    composable(Routes.REVIEW_WELCOME) {
                        ReviewWelcomeScreen(viewModel, navController)
                    }
                    composable(Routes.RE_REVIEW_ROUTER) {
                        ReReviewRouterScreen(viewModel, navController)
                    }
                    composable(Routes.BOTTLE_INFO) {
                        BottleInfoScreen(viewModel, navController)
                    }
                    composable(Routes.OWNED) {
                        OwnedScreen(viewModel, navController)
                    }
                    composable(Routes.POUR) {
                        PourScreen(viewModel, navController)
                    }
                    composable(Routes.NOSE) {
                        NotesRatingScreen(
                            title = "Nose",
                            notes = viewModel.noseNotes,
                            onNotesChange = { viewModel.noseNotes = it },
                            score = viewModel.noseScore,
                            onScoreChange = { viewModel.noseScore = it },
                            backRoute = Routes.POUR,
                            forwardRoute = Routes.PALATE,
                            navController = navController,
                            stepTitle = "Physical Sensations"
                        )
                    }
                    composable(Routes.PALATE) {
                        NotesRatingScreen(
                            title = "Palate",
                            notes = viewModel.palateNotes,
                            onNotesChange = { viewModel.palateNotes = it },
                            score = viewModel.palateScore,
                            onScoreChange = { viewModel.palateScore = it },
                            backRoute = Routes.NOSE,
                            forwardRoute = Routes.FINISH,
                            navController = navController,
                            stepTitle = "Physical Sensations"
                        )
                    }
                    composable(Routes.FINISH) {
                        NotesRatingScreen(
                            title = "Finish",
                            notes = viewModel.finishNotes,
                            onNotesChange = { viewModel.finishNotes = it },
                            score = viewModel.finishScore,
                            onScoreChange = { viewModel.finishScore = it },
                            backRoute = Routes.PALATE,
                            forwardRoute = Routes.PROFILE_WHEEL,
                            navController = navController,
                            stepTitle = "Physical Sensations"
                        )
                    }
                    composable(Routes.PROFILE_WHEEL) {
                        ProfileWheelScreen(viewModel, navController)
                    }
                    composable(Routes.OVERALL) {
                        NotesRatingScreen(
                            title = "Overall Evaluation",
                            notes = viewModel.overallNotes,
                            onNotesChange = { viewModel.overallNotes = it },
                            score = viewModel.overallScore,
                            onScoreChange = { viewModel.overallScore = it },
                            backRoute = Routes.PROFILE_WHEEL,
                            forwardRoute = Routes.HOME,
                            navController = navController,
                            stepTitle = "Physical Sensations",
                            isSaveButton = true,
                            onSaveClick = {
                                viewModel.saveReview {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.HOME) { inclusive = false }
                                    }
                                }
                            }
                        )
                    }
                    composable(Routes.COLLECTION_LIST) {
                        CollectionListScreen(viewModel, navController)
                    }
                    composable("${Routes.SPECIFIC_BOTTLE}/{bottleId}") { backStackEntry ->
                        val bottleId = backStackEntry.arguments?.getString("bottleId")?.toLongOrNull() ?: 0L
                        SpecificBottleScreen(bottleId, viewModel, navController)
                    }
                    composable("${Routes.SPECIFIC_SUB_BOTTLE}/{subBottleId}/{bottleId}") { backStackEntry ->
                        val subId = backStackEntry.arguments?.getString("subBottleId")?.toLongOrNull() ?: 0L
                        val bottleId = backStackEntry.arguments?.getString("bottleId")?.toLongOrNull() ?: 0L
                        SpecificSubBottleScreen(subId, bottleId, viewModel, navController)
                    }
                    composable("${Routes.READ_ONLY_REVIEW}/{reviewId}") { backStackEntry ->
                        val revId = backStackEntry.arguments?.getString("reviewId")?.toLongOrNull() ?: 0L
                        ReadOnlyReviewWalkthrough(revId, viewModel, navController)
                    }
                    composable(Routes.STATS) {
                        StatsScreen(viewModel, navController)
                    }
                    composable(Routes.BLIND_SETUP) {
                        BlindSetupScreen(viewModel, navController)
                    }
                    composable(Routes.BLIND_POUR_GUESS) {
                        BlindPourGuessScreen(viewModel, navController)
                    }
                    composable(Routes.BLIND_RANKS) {
                        BlindRanksScreen(viewModel, navController)
                    }
                    composable(Routes.BLIND_REVEAL) {
                        BlindRevealScreen(viewModel, navController)
                    }
                    composable(Routes.COMPLETED_BLINDS) {
                        CompletedBlindsScreen(viewModel, navController)
                    }
                }
            }
        }
    }
}
