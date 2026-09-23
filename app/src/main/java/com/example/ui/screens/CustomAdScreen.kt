package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomAdScreen(
    onAdFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    StartupRewardedAdScreen(
        onAdFinished = onAdFinished,
        modifier = modifier
    )
}

