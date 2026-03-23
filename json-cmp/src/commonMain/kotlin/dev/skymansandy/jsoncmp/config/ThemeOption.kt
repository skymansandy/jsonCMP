package dev.skymansandy.jsoncmp.config

import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors

sealed class ThemeOption(val label: String, val colors: JsonCmpColors) {
    data object Dark : ThemeOption("Dark", JsonCmpColors.Dark)
    data object Light : ThemeOption("Light", JsonCmpColors.Light)
    data object Monokai : ThemeOption("Monokai", JsonCmpColors.Monokai)
    data object Dracula : ThemeOption("Dracula", JsonCmpColors.Dracula)
    data object SolarizedDark : ThemeOption("Solarized", JsonCmpColors.SolarizedDark)
    data class Custom(val customColors: JsonCmpColors) : ThemeOption("Custom", customColors)
}
