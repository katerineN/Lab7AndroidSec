package com.example.lab7androidsec

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab7androidsec.data.AppContainer
import com.example.lab7androidsec.data.AppDataContainer
import com.example.lab7androidsec.home.HomeDestination
import com.example.lab7androidsec.home.HomeScreen

@Composable

fun Lab7App(navController: NavHostController = rememberNavController()) {
    Lab7NavHost(navController = navController)
}

class Lab7Application : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface NavigationDestination {
    val route: String
    val titleResourceId: Int
}

@Composable
fun Lab7NavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen()
        }
    }
}