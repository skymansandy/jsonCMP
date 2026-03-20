package dev.skymansandy.jsoncmpsample.model

import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors

enum class ThemeOption(val label: String, val colors: JsonCmpColors) {
    Dark("Dark", JsonCmpColors.Dark),
    Light("Light", JsonCmpColors.Light),
    Monokai("Monokai", JsonCmpColors.Monokai),
    Dracula("Dracula", JsonCmpColors.Dracula),
    SolarizedDark("Solarized", JsonCmpColors.SolarizedDark),
}
