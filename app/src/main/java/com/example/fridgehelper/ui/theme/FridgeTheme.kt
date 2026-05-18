package com.example.fridgehelper.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green900 = Color(0xFF1A3D2B)
val Green700 = Color(0xFF2D6A4F)
val Green500 = Color(0xFF52B788)
val Green100 = Color(0xFFEBF5E8)
val Green050 = Color(0xFFF7FAF4)

val Amber700 = Color(0xFFD4A017)
val Amber100 = Color(0xFFFEF9EB)
val Amber900 = Color(0xFF8A6A00)

val Coral700 = Color(0xFFE74C3C)
val Coral100 = Color(0xFFFDECEA)
val Coral900 = Color(0xFFC0392B)

val GreenBorder   = Color(0xFFC8DEC3)
val TextPrimary   = Color(0xFF1A3D2B)
val TextSecondary = Color(0xFF6B8C73)
val TextTertiary  = Color(0xFFB2D9A6)

val TopBarTitle  = Color(0xFFD4EDDA)
val NavInactive  = Color(0xFF7BAE8C)

val CardBorderWarn    = Color(0xFFF2D97C)
val CardBorderExpired = Color(0xFFF4A8A0)
val StatusOkText      = Color(0xFF2A7A3F)

val ViewfinderBg = Color(0xFF0D2418)

val BadgeHaveText = Color(0xFF1E8449)


private val LightColors = lightColorScheme(
    primary            = Green700,
    onPrimary          = Color.White,
    primaryContainer   = Green100,
    onPrimaryContainer = Green900,

    secondary            = Amber700,
    onSecondary          = Color.White,
    secondaryContainer   = Amber100,
    onSecondaryContainer = Amber900,

    error            = Coral700,
    onError          = Color.White,
    errorContainer   = Coral100,
    onErrorContainer = Coral900,

    background      = Green050,
    onBackground    = TextPrimary,
    surface         = Color.White,
    onSurface       = TextPrimary,
    surfaceVariant  = Green100,
    onSurfaceVariant = TextSecondary,

    outline        = GreenBorder,
    outlineVariant = GreenBorder,
)


private val DarkColors = darkColorScheme(
    primary            = Green500,
    onPrimary          = Green900,
    primaryContainer   = Green900,
    onPrimaryContainer = Green100,

    secondary            = Amber700,
    onSecondary          = Color.Black,
    secondaryContainer   = Color(0xFF3D2E00),
    onSecondaryContainer = Amber100,

    error            = Coral700,
    onError          = Color.Black,
    errorContainer   = Color(0xFF4A1010),
    onErrorContainer = Coral100,

    background      = Color(0xFF0F1F16),
    onBackground    = Green100,
    surface         = Color(0xFF1A2E20),
    onSurface       = Green100,
    surfaceVariant  = Color(0xFF1E3828),
    onSurfaceVariant = TextTertiary,

    outline        = Color(0xFF3A5C44),
    outlineVariant = Color(0xFF2A4535),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun fridgeTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor          = Green900,
    titleContentColor       = TopBarTitle,
    navigationIconContentColor = TopBarTitle,
    actionIconContentColor  = TopBarTitle
)

@Composable
fun FridgeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}