package dev.skymansandy.jsoncmp.config

import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors

sealed class JsonTheme(val label: String, val colors: JsonCmpColors) {
    data object Dark : JsonTheme("Dark", JsonCmpColors.Dark)
    data object Light : JsonTheme("Light", JsonCmpColors.Light)
    data object Monokai : JsonTheme("Monokai", JsonCmpColors.Monokai)
    data object Dracula : JsonTheme("Dracula", JsonCmpColors.Dracula)
    data object SolarizedDark : JsonTheme("Solarized", JsonCmpColors.SolarizedDark)
    data class Custom(val customColors: JsonCmpColors) : JsonTheme("Custom", customColors)
}
