package dev.skymansandy.jsoncmpsample.data

val sampleJson = """
{
    "name": "JsonCMP",
    "version": "1.0.0",
    "description": "Kotlin Multiplatform Compose JSON viewer and editor",
    "platforms": ["Android", "iOS", "JVM Desktop"],
    "features": {
        "viewer": true,
        "editor": true,
        "search": true,
        "folding": true,
        "sorting": true
    },
    "themes": [
        {"name": "Dark", "type": "dark"},
        {"name": "Light", "type": "light"},
        {"name": "Monokai", "type": "dark"},
        {"name": "Dracula", "type": "dark"},
        {"name": "Solarized Dark", "type": "dark"}
    ],
    "author": {
        "name": "skymansandy",
        "github": "https://github.com/skymansandy"
    },
    "license": "Apache-2.0",
    "stars": null
}
""".trimIndent()
