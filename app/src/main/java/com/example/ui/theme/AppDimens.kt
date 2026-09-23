package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowFormFactor {
    COMPACT,
    MEDIUM,
    EXPANDED
}

data class AppDimens(
    val formFactor: WindowFormFactor,
    val paddingHorizontal: Dp,
    val paddingVertical: Dp,
    val cardSpacing: Dp,
    val cardCornerRadius: Dp,
    val headerHeight: Dp,
    val maxContentWidth: Dp,
    val gridColumns: Int
) {
    companion object {
        fun get(formFactor: WindowFormFactor): AppDimens {
            return when (formFactor) {
                WindowFormFactor.COMPACT -> AppDimens(
                    formFactor = WindowFormFactor.COMPACT,
                    paddingHorizontal = 16.dp,
                    paddingVertical = 12.dp,
                    cardSpacing = 14.dp,
                    cardCornerRadius = 14.dp,
                    headerHeight = 64.dp,
                    maxContentWidth = Dp.Infinity,
                    gridColumns = 1
                )
                WindowFormFactor.MEDIUM -> AppDimens(
                    formFactor = WindowFormFactor.MEDIUM,
                    paddingHorizontal = 24.dp,
                    paddingVertical = 16.dp,
                    cardSpacing = 18.dp,
                    cardCornerRadius = 16.dp,
                    headerHeight = 72.dp,
                    maxContentWidth = 840.dp,
                    gridColumns = 2
                )
                WindowFormFactor.EXPANDED -> AppDimens(
                    formFactor = WindowFormFactor.EXPANDED,
                    paddingHorizontal = 32.dp,
                    paddingVertical = 24.dp,
                    cardSpacing = 22.dp,
                    cardCornerRadius = 20.dp,
                    headerHeight = 80.dp,
                    maxContentWidth = 1200.dp,
                    gridColumns = 3
                )
            }
        }
    }
}

val LocalAppDimens = compositionLocalOf { AppDimens.get(WindowFormFactor.COMPACT) }
