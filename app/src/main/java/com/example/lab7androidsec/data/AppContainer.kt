package com.example.lab7androidsec.data

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lab7androidsec.Lab7Application
import com.example.lab7androidsec.home.HomeViewModel

interface AppContainer {
    val healthRepository: HealthRepository
}

class AppDataContainer(private val context: Context) : AppContainer{

    override val healthRepository: HealthRepository by lazy{
        HealthDataRepository(context)
    }
}

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                lab7Application().container.healthRepository
            )
        }
    }
}

fun CreationExtras.lab7Application(): Lab7Application =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Lab7Application)