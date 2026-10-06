package com.example.learndsandalgorithm

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.learndsandalgorithm.core.AppContainer
import com.example.learndsandalgorithm.presentation.navigation.AppNavigation
import com.example.learndsandalgorithm.presentation.home.HomeViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val container = remember { AppContainer() }
        val homeViewModel = remember {
            HomeViewModel(
                contentRepository = container.contentRepository,
                progressRepository = container.progressRepository
            )
        }
        
        AppNavigation(
            homeViewModel = homeViewModel,
            contentRepository = container.contentRepository,
            progressRepository = container.progressRepository
        )
    }
}
