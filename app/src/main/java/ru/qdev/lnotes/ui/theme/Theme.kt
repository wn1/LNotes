/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reply.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Material 3 color schemes
private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = Color(0xFFFFFFFF),
    primary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xff00a3a3),

    secondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF000000),

    tertiary = Color(0xff00a3a3),
    tertiaryContainer = Color(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
    primary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xff00a3a3),

    secondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFFFFFF),

    tertiary = Color(0xff00a3a3),
    tertiaryContainer = Color(0xFFFFFFFF)
)

//@ExperimentalMaterial3Api
//private val AppRippleConfiguration =
//    RippleConfiguration(
//        color = Color.Red,
//        rippleAlpha = RippleAlpha(
//            draggedAlpha = 0.4f,
//            focusedAlpha = 0.3f,
//            hoveredAlpha = 0.4f,
//            pressedAlpha = 0.4f
//        )
//    )

@ExperimentalMaterial3Api
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
//        CompositionLocalProvider(LocalRippleConfiguration provides AppRippleConfiguration) {
            content()
//        }
    }
}

@Composable
fun ColorScheme.isDark() = this.background.luminance() <= 0.5

