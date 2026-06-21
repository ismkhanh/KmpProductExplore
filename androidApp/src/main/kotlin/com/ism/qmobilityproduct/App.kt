package com.ism.qmobilityproduct

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ism.qmobilityproduct.screens.DetailScreen
import com.ism.qmobilityproduct.screens.ListScreen
import kotlinx.serialization.Serializable

@Serializable
object ListDestination

@Serializable
data class DetailDestination(val productId: Int)

@Serializable
object FavouritesDestination


@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = ListDestination
            ) {
                composable<ListDestination> {
                    ListScreen(
                        navigateToDetails = { productId ->
                            navController.navigate(DetailDestination(productId))
                        },
                        navigateToFavourites = {
                            navController.navigate(FavouritesDestination)
                        },
                    )
                }
                composable<FavouritesDestination> {
                    Column { }
                }
                composable<DetailDestination> { backStackEntry ->
                    DetailScreen(
                        productId = backStackEntry.toRoute<DetailDestination>().productId,
                        navigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}