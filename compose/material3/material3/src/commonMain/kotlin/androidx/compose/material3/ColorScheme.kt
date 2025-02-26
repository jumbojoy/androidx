/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material3

import androidx.compose.material3.tokens.ColorDark
import androidx.compose.material3.tokens.ColorLight
import androidx.compose.material3.tokens.ColorSchemeKey
import androidx.compose.material3.tokens.Elevation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import kotlin.math.ln

/**
 * A color scheme holds all the named color parameters for a [MaterialTheme].
 *
 * Color schemes are designed to be harmonious, ensure accessible text, and distinguish UI
 * elements and surfaces from one another. There are two built-in baseline schemes,
 * [lightColorScheme] and a [darkColorScheme], that can be used as-is or customized.
 *
 * @property primary The primary color is the color displayed most frequently across your app’s
 * screens and components.
 * @property onPrimary Color used for text and icons displayed on top of the primary color.
 * @property primaryContainer The preferred tonal color of containers.
 * @property onPrimaryContainer The color (and state variants) that should be used for content on
 * top of [primaryContainer].
 * @property inversePrimary Color to be used as a "primary" color in places where the inverse color
 * scheme is needed, such as the button on a SnackBar.
 * @property secondary The secondary color provides more ways to accent and distinguish your
 * product. Secondary colors are best for:
 * - Floating action buttons
 * - Selection controls, like checkboxes and radio buttons
 * - Highlighting selected text
 * - Links and headlines
 * @property onSecondary Color used for text and icons displayed on top of the secondary color.
 * @property secondaryContainer A tonal color to be used in containers.
 * @property onSecondaryContainer The color (and state variants) that should be used for content on
 * top of [secondaryContainer].
 * @property tertiary The tertiary color that can be used to balance primary and secondary
 * colors, or bring heightened attention to an element such as an input field.
 * @property onTertiary Color used for text and icons displayed on top of the tertiary color.
 * @property tertiaryContainer A tonal color to be used in containers.
 * @property onTertiaryContainer The color (and state variants) that should be used for content on
 * top of [tertiaryContainer].
 * @property background The background color that appears behind scrollable content.
 * @property onBackground Color used for text and icons displayed on top of the background color.
 * @property surface The surface color that affect surfaces of components, such as cards, sheets,
 * and menus.
 * @property surface1 Variation of [surface] with an overlay color or colors blended on top of it,
 * giving it a slightly more tonal color.
 * @property surface2 Variation of [surface] with an overlay color or colors blended on top of it,
 * giving it a slightly more tonal color.
 * @property surface3 Variation of [surface] with an overlay color or colors blended on top of it,
 * giving it a slightly more tonal color.
 * @property surface4 Variation of [surface] with an overlay color or colors blended on top of it,
 * giving it a slightly more tonal color.
 * @property surface5 Variation of [surface] with an overlay color or colors blended on top of it,
 * giving it a slightly more tonal color.
 * @property onSurface Color used for text and icons displayed on top of the surface color.
 * @property surfaceVariant Another option for a color with similar uses of [surface].
 * @property onSurfaceVariant The color (and state variants) that can be used for content on top of
 * [surface].
 * @property inverseSurface A color that contrasts sharply with [surface]. Useful for surfaces that
 * sit on top of other surfaces with [surface] color.
 * @property inverseOnSurface A color that contrasts well with [inverseSurface]. Useful for content
 * that sits on top of containers that are [inverseSurface].
 * @property disabled A disabled color.
 * @property onDisabled Color used for text and icons displayed on top of the disabled color.
 * @property error The error color is used to indicate errors in components, such as invalid text in
 * a text field.
 * @property onError Color used for text and icons displayed on top of the error color.
 * @property errorContainer The preferred tonal color of error containers.
 * @property onErrorContainer The color (and state variants) that should be used for content on
 * top of [errorContainer].
 * @property outline Subtle color used for boundaries. Outline color role adds contrast for
 * accessibility purposes.
 */
@Stable
class ColorScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    inversePrimary: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    onTertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    surface1: Color,
    surface2: Color,
    surface3: Color,
    surface4: Color,
    surface5: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    inverseSurface: Color,
    inverseOnSurface: Color,
    disabled: Color,
    onDisabled: Color,
    error: Color,
    onError: Color,
    errorContainer: Color,
    onErrorContainer: Color,
    outline: Color,
) {
    var primary by mutableStateOf(primary, structuralEqualityPolicy())
        internal set
    var onPrimary by mutableStateOf(onPrimary, structuralEqualityPolicy())
        internal set
    var primaryContainer by mutableStateOf(primaryContainer, structuralEqualityPolicy())
        internal set
    var onPrimaryContainer by mutableStateOf(onPrimaryContainer, structuralEqualityPolicy())
        internal set
    var inversePrimary by mutableStateOf(inversePrimary, structuralEqualityPolicy())
        internal set
    var secondary by mutableStateOf(secondary, structuralEqualityPolicy())
        internal set
    var onSecondary by mutableStateOf(onSecondary, structuralEqualityPolicy())
        internal set
    var secondaryContainer by mutableStateOf(secondaryContainer, structuralEqualityPolicy())
        internal set
    var onSecondaryContainer by mutableStateOf(onSecondaryContainer, structuralEqualityPolicy())
        internal set
    var tertiary by mutableStateOf(tertiary, structuralEqualityPolicy())
        internal set
    var onTertiary by mutableStateOf(onTertiary, structuralEqualityPolicy())
        internal set
    var tertiaryContainer by mutableStateOf(tertiaryContainer, structuralEqualityPolicy())
        internal set
    var onTertiaryContainer by mutableStateOf(onTertiaryContainer, structuralEqualityPolicy())
        internal set
    var background by mutableStateOf(background, structuralEqualityPolicy())
        internal set
    var onBackground by mutableStateOf(onBackground, structuralEqualityPolicy())
        internal set
    var surface by mutableStateOf(surface, structuralEqualityPolicy())
        internal set
    var surface1 by mutableStateOf(surface1, structuralEqualityPolicy())
        internal set
    var surface2 by mutableStateOf(surface2, structuralEqualityPolicy())
        internal set
    var surface3 by mutableStateOf(surface3, structuralEqualityPolicy())
        internal set
    var surface4 by mutableStateOf(surface4, structuralEqualityPolicy())
        internal set
    var surface5 by mutableStateOf(surface5, structuralEqualityPolicy())
        internal set
    var onSurface by mutableStateOf(onSurface, structuralEqualityPolicy())
        internal set
    var surfaceVariant by mutableStateOf(surfaceVariant, structuralEqualityPolicy())
        internal set
    var onSurfaceVariant by mutableStateOf(onSurfaceVariant, structuralEqualityPolicy())
        internal set
    var inverseSurface by mutableStateOf(inverseSurface, structuralEqualityPolicy())
        internal set
    var inverseOnSurface by mutableStateOf(inverseOnSurface, structuralEqualityPolicy())
        internal set
    var disabled by mutableStateOf(disabled, structuralEqualityPolicy())
        internal set
    var onDisabled by mutableStateOf(onDisabled, structuralEqualityPolicy())
        internal set
    var error by mutableStateOf(error, structuralEqualityPolicy())
        internal set
    var onError by mutableStateOf(onError, structuralEqualityPolicy())
        internal set
    var errorContainer by mutableStateOf(errorContainer, structuralEqualityPolicy())
        internal set
    var onErrorContainer by mutableStateOf(onErrorContainer, structuralEqualityPolicy())
        internal set
    var outline by mutableStateOf(outline, structuralEqualityPolicy())
        internal set

    /** Returns a copy of this ColorScheme, optionally overriding some of the values. */
    fun copy(
        primary: Color = this.primary,
        onPrimary: Color = this.onPrimary,
        primaryContainer: Color = this.primaryContainer,
        onPrimaryContainer: Color = this.onPrimaryContainer,
        inversePrimary: Color = this.inversePrimary,
        secondary: Color = this.secondary,
        onSecondary: Color = this.onSecondary,
        secondaryContainer: Color = this.secondaryContainer,
        onSecondaryContainer: Color = this.onSecondaryContainer,
        tertiary: Color = this.tertiary,
        onTertiary: Color = this.onTertiary,
        tertiaryContainer: Color = this.tertiaryContainer,
        onTertiaryContainer: Color = this.onTertiaryContainer,
        background: Color = this.background,
        onBackground: Color = this.onBackground,
        surface: Color = this.surface,
        surface1: Color = this.surface1,
        surface2: Color = this.surface2,
        surface3: Color = this.surface3,
        surface4: Color = this.surface4,
        surface5: Color = this.surface5,
        onSurface: Color = this.onSurface,
        surfaceVariant: Color = this.surfaceVariant,
        onSurfaceVariant: Color = this.onSurfaceVariant,
        inverseSurface: Color = this.inverseSurface,
        inverseOnSurface: Color = this.inverseOnSurface,
        disabled: Color = this.disabled,
        onDisabled: Color = this.onDisabled,
        error: Color = this.error,
        onError: Color = this.onError,
        errorContainer: Color = this.errorContainer,
        onErrorContainer: Color = this.onErrorContainer,
        outline: Color = this.outline,
    ): ColorScheme =
        ColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            surface1 = surface1,
            surface2 = surface2,
            surface3 = surface3,
            surface4 = surface4,
            surface5 = surface5,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            disabled = disabled,
            onDisabled = onDisabled,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
        )

    override fun toString(): String {
        return "ColorScheme(" +
            "primary=$primary" +
            "onPrimary=$onPrimary" +
            "primaryContainer=$primaryContainer" +
            "onPrimaryContainer=$onPrimaryContainer" +
            "inversePrimary=$inversePrimary" +
            "secondary=$secondary" +
            "onSecondary=$onSecondary" +
            "secondaryContainer=$secondaryContainer" +
            "onSecondaryContainer=$onSecondaryContainer" +
            "tertiary=$tertiary" +
            "onTertiary=$onTertiary" +
            "tertiaryContainer=$tertiaryContainer" +
            "onTertiaryContainer=$onTertiaryContainer" +
            "background=$background" +
            "onBackground=$onBackground" +
            "surface=$surface" +
            "surface1=$surface1" +
            "surface2=$surface2" +
            "surface3=$surface3" +
            "surface4=$surface4" +
            "surface5=$surface5" +
            "onSurface=$onSurface" +
            "surfaceVariant=$surfaceVariant" +
            "onSurfaceVariant=$onSurfaceVariant" +
            "inverseSurface=$inverseSurface" +
            "inverseOnSurface=$inverseOnSurface" +
            "disabled=$disabled" +
            "onDisabled=$onDisabled" +
            "error=$error" +
            "onError=$onError" +
            "errorContainer=$errorContainer" +
            "onErrorContainer=$onErrorContainer" +
            "outline=$outline" +
            ")"
    }
}

/**
 * Returns a light Material color scheme.
 */
fun lightColorScheme(
    primary: Color = ColorLight.Primary,
    onPrimary: Color = ColorLight.OnPrimary,
    primaryContainer: Color = ColorLight.PrimaryContainer,
    onPrimaryContainer: Color = ColorLight.OnPrimaryContainer,
    inversePrimary: Color = ColorLight.InversePrimary,
    secondary: Color = ColorLight.Secondary,
    onSecondary: Color = ColorLight.OnSecondary,
    secondaryContainer: Color = ColorLight.SecondaryContainer,
    onSecondaryContainer: Color = ColorLight.OnSecondaryContainer,
    tertiary: Color = ColorLight.Tertiary,
    onTertiary: Color = ColorLight.OnTertiary,
    tertiaryContainer: Color = ColorLight.TertiaryContainer,
    onTertiaryContainer: Color = ColorLight.OnTertiaryContainer,
    background: Color = ColorLight.Background,
    onBackground: Color = ColorLight.OnBackground,
    surface: Color = ColorLight.Surface,
    surface1: Color = colorAtElevation(
        elevation = Elevation.Level1,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary40
    ),
    surface2: Color = colorAtElevation(
        elevation = Elevation.Level2,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary40
    ),
    surface3: Color = colorAtElevation(
        elevation = Elevation.Level3,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary40
    ),
    surface4: Color = colorAtElevation(
        elevation = Elevation.Level4,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary40
    ),
    surface5: Color = colorAtElevation(
        elevation = Elevation.Level5,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary40
    ),
    onSurface: Color = ColorLight.OnSurface,
    surfaceVariant: Color = ColorLight.SurfaceVariant,
    onSurfaceVariant: Color = ColorLight.OnSurfaceVariant,
    inverseSurface: Color = ColorLight.InverseSurface,
    inverseOnSurface: Color = ColorLight.InverseOnSurface,
    disabled: Color = ColorLight.Disabled,
    onDisabled: Color = ColorLight.OnDisabled,
    error: Color = ColorLight.Error,
    onError: Color = ColorLight.OnError,
    errorContainer: Color = ColorLight.ErrorContainer,
    onErrorContainer: Color = ColorLight.OnErrorContainer,
    outline: Color = ColorLight.Outline,
): ColorScheme =
    ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        surface1 = surface1,
        surface2 = surface2,
        surface3 = surface3,
        surface4 = surface4,
        surface5 = surface5,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        disabled = disabled,
        onDisabled = onDisabled,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
    )

/**
 * Returns a dark Material color scheme.
 */
fun darkColorScheme(
    primary: Color = ColorDark.Primary,
    onPrimary: Color = ColorDark.OnPrimary,
    primaryContainer: Color = ColorDark.PrimaryContainer,
    onPrimaryContainer: Color = ColorDark.OnPrimaryContainer,
    inversePrimary: Color = ColorDark.InversePrimary,
    secondary: Color = ColorDark.Secondary,
    onSecondary: Color = ColorDark.OnSecondary,
    secondaryContainer: Color = ColorDark.SecondaryContainer,
    onSecondaryContainer: Color = ColorDark.OnSecondaryContainer,
    tertiary: Color = ColorDark.Tertiary,
    onTertiary: Color = ColorDark.OnTertiary,
    tertiaryContainer: Color = ColorDark.TertiaryContainer,
    onTertiaryContainer: Color = ColorDark.OnTertiaryContainer,
    background: Color = ColorDark.Background,
    onBackground: Color = ColorDark.OnBackground,
    surface: Color = ColorDark.Surface,
    surface1: Color = colorAtElevation(
        elevation = Elevation.Level1,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary80
    ),
    surface2: Color = colorAtElevation(
        elevation = Elevation.Level2,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary80
    ),
    surface3: Color = colorAtElevation(
        elevation = Elevation.Level3,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary80
    ),
    surface4: Color = colorAtElevation(
        elevation = Elevation.Level4,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary80
    ),
    surface5: Color = colorAtElevation(
        elevation = Elevation.Level5,
        surface = surface,
        surfaceOverlay = BaselineTonalPalette.primary80
    ),
    onSurface: Color = ColorDark.OnSurface,
    surfaceVariant: Color = ColorDark.SurfaceVariant,
    onSurfaceVariant: Color = ColorDark.OnSurfaceVariant,
    inverseSurface: Color = ColorDark.InverseSurface,
    inverseOnSurface: Color = ColorDark.InverseOnSurface,
    disabled: Color = ColorDark.Disabled,
    onDisabled: Color = ColorDark.OnDisabled,
    error: Color = ColorDark.Error,
    onError: Color = ColorDark.OnError,
    errorContainer: Color = ColorDark.ErrorContainer,
    onErrorContainer: Color = ColorDark.OnErrorContainer,
    outline: Color = ColorDark.Outline,
): ColorScheme =
    ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        surface1 = surface1,
        surface2 = surface2,
        surface3 = surface3,
        surface4 = surface4,
        surface5 = surface5,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        disabled = disabled,
        onDisabled = onDisabled,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
    )

/*
 * Returns the [surface] color with a combination of an overlay layered on top of it.
 *
 * Used to compute the values of surface1 through surface5.
 *
 * The color is computed using the following 2 layers:
 * - Bottom layer: [surface] at 100% opacity.
 * - Overlay: [surfaceOverlay] at varying opacities for each level.
 */
internal fun colorAtElevation(
    elevation: Dp,
    surface: Color,
    surfaceOverlay: Color,
): Color {
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    val surfaceOverlayWithElevation = surfaceOverlay.copy(alpha = alpha)
    return surfaceOverlayWithElevation.compositeOver(surface)
}

/**
 * Updates the internal values of a given [ColorScheme] with values from the [other]
 * [ColorScheme].
 * This allows efficiently updating a subset of [ColorScheme], without recomposing every
 * composable that consumes values from [LocalColorScheme].
 *
 * Because [ColorScheme] is very wide-reaching, and used by many expensive composables in the
 * hierarchy, providing a new value to [LocalColorScheme] causes every composable consuming
 * [LocalColorScheme] to recompose, which is prohibitively expensive in cases such as animating one
 * color in the theme. Instead, [ColorScheme] is internally backed by [mutableStateOf], and this
 * function mutates the internal state of [this] to match values in [other]. This means that any
 * changes will mutate the internal state of [this], and only cause composables that are reading the
 * specific changed value to recompose.
 */
internal fun ColorScheme.updateColorSchemeFrom(other: ColorScheme) {
    primary = other.primary
    onPrimary = other.onPrimary
    primaryContainer = other.primaryContainer
    onPrimaryContainer = other.onPrimaryContainer
    inversePrimary = other.inversePrimary
    secondary = other.secondary
    onSecondary = other.onSecondary
    secondaryContainer = other.secondaryContainer
    onSecondaryContainer = other.onSecondaryContainer
    tertiary = other.tertiary
    onTertiary = other.onTertiary
    tertiaryContainer = other.tertiaryContainer
    onTertiaryContainer = other.onTertiaryContainer
    background = other.background
    onBackground = other.onBackground
    surface = other.surface
    surface1 = other.surface1
    surface2 = other.surface2
    surface3 = other.surface3
    surface4 = other.surface4
    surface5 = other.surface5
    onSurface = other.onSurface
    surfaceVariant = other.surfaceVariant
    onSurfaceVariant = other.onSurfaceVariant
    inverseSurface = other.inverseSurface
    inverseOnSurface = other.inverseOnSurface
    disabled = other.disabled
    onDisabled = other.onDisabled
    error = other.error
    onError = other.onError
    errorContainer = other.errorContainer
    onErrorContainer = other.onErrorContainer
    outline = other.outline
}

/**
 * Helper function for component color tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.colorScheme.fromToken(ExtendedFabBranded.BrandedContainerColor)``
 */
internal fun ColorScheme.fromToken(value: ColorSchemeKey): Color {
    return when (value) {
        ColorSchemeKey.Background -> background
        ColorSchemeKey.Disabled -> disabled
        ColorSchemeKey.Error -> error
        ColorSchemeKey.ErrorContainer -> errorContainer
        ColorSchemeKey.InverseOnSurface -> inverseOnSurface
        ColorSchemeKey.InversePrimary -> inversePrimary
        ColorSchemeKey.InverseSurface -> inverseSurface
        ColorSchemeKey.OnBackground -> onBackground
        ColorSchemeKey.OnDisabled -> onDisabled
        ColorSchemeKey.OnError -> onError
        ColorSchemeKey.OnErrorContainer -> onErrorContainer
        ColorSchemeKey.OnPrimary -> onPrimary
        ColorSchemeKey.OnPrimaryContainer -> onPrimaryContainer
        ColorSchemeKey.OnSecondary -> onSecondary
        ColorSchemeKey.OnSecondaryContainer -> onSecondaryContainer
        ColorSchemeKey.OnSurface -> onSurface
        ColorSchemeKey.OnSurfaceVariant -> onSurfaceVariant
        ColorSchemeKey.OnTertiary -> onTertiary
        ColorSchemeKey.OnTertiaryContainer -> onTertiaryContainer
        ColorSchemeKey.Outline -> outline
        ColorSchemeKey.Primary -> primary
        ColorSchemeKey.PrimaryContainer -> primaryContainer
        ColorSchemeKey.Secondary -> secondary
        ColorSchemeKey.SecondaryContainer -> secondaryContainer
        ColorSchemeKey.Surface -> surface
        ColorSchemeKey.SurfaceVariant -> surfaceVariant
        ColorSchemeKey.Tertiary -> tertiary
        ColorSchemeKey.TertiaryContainer -> tertiaryContainer
        ColorSchemeKey.Surface1 -> surface1
        ColorSchemeKey.Surface2 -> surface2
        ColorSchemeKey.Surface3 -> surface3
        ColorSchemeKey.Surface4 -> surface4
        ColorSchemeKey.Surface5 -> surface5
    }
}

/**
 * CompositionLocal used to pass [ColorScheme] down the tree.
 *
 * Setting the value here is typically done as part of [MaterialTheme], which will automatically
 * handle efficiently updating any changed colors without causing unnecessary recompositions, using
 * [ColorScheme.updateColorSchemeFrom]. To retrieve the current value of this CompositionLocal, use
 * [MaterialTheme.colorScheme].
 */
internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }

/**
 * A low level of alpha used to represent disabled components, such as text in a disabled Button.
 */
internal const val DisabledAlpha = 0.38f
