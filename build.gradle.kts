plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.bcv) apply false
}

dependencies {
    listOf(
        "json-cmp",
    ).forEach { kover(project(":$it")) }
}

val publishableModules = setOf(
    "json-cmp",
)

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    if (name in publishableModules) {
        val jsoncmpGroup = findProperty("jsoncmp.group") as String
        val jsoncmpVersion = findProperty("jsoncmp.version") as String

        apply(plugin = "com.vanniktech.maven.publish")
        apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")

        afterEvaluate {
            tasks.withType<Sign>().configureEach {
                isEnabled = !gradle.startParameter.taskNames.any { it.contains("MavenLocal", ignoreCase = true) }
            }
        }

        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            coordinates(jsoncmpGroup, name, jsoncmpVersion)
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
            signAllPublications()

            pom {
                name.set("JsonCMP")
                description.set("Kotlin Multiplatform Compose JSON viewer and editor component")
                url.set("https://github.com/skymansandy/jsonCMP")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("skymansandy")
                        name.set("skymansandy")
                        email.set("iamsandythedev@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/skymansandy/jsonCMP")
                    connection.set("scm:git:git://github.com/skymansandy/jsonCMP.git")
                    developerConnection.set("scm:git:ssh://github.com/skymansandy/jsonCMP.git")
                }
            }
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        autoCorrect = true
        config.setFrom(rootProject.files("detekt.yml"))
    }

    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    }

    afterEvaluate {
        val kmpSourceDirs = fileTree("src") {
            include("*Main/kotlin/**/*.kt", "*Test/kotlin/**/*.kt")
        }
        if (!kmpSourceDirs.isEmpty) {
            tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
                setSource(files("src"))
                include("**/*.kt")
                exclude("**/build/**")
                config.setFrom(rootProject.files("detekt.yml"))
                buildUponDefaultConfig = true
                autoCorrect = true
                if (file("detekt-baseline.xml").exists()) {
                    baseline.set(file("detekt-baseline.xml"))
                }
            }

            tasks.register<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineAll") {
                setSource(files("src"))
                include("**/*.kt")
                exclude("**/build/**")
                config.setFrom(rootProject.files("detekt.yml"))
                buildUponDefaultConfig = true
                baseline.set(file("detekt-baseline.xml"))
            }
        }
    }
}
