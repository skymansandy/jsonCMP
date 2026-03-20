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

        group = jsoncmpGroup
        version = jsoncmpVersion

        apply(plugin = "maven-publish")

        afterEvaluate {
            extensions.findByType<PublishingExtension>()?.apply {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri(
                            System.getenv("MAVEN_REPO_URL")
                                ?: "https://maven.pkg.github.com/skymansandy/jsonCMP"
                        )
                        credentials {
                            username = System.getenv("MAVEN_REPO_USERNAME")
                                ?: findProperty("gpr.user") as? String ?: ""
                            password = System.getenv("MAVEN_REPO_PASSWORD")
                                ?: findProperty("gpr.key") as? String ?: ""
                        }
                    }
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
