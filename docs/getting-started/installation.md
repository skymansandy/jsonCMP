# Installation

## Repository Setup

JsonCMP is published to GitHub Packages. Add the repository to your `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/skymansandy/jsonCMP")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_USERNAME")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

> **Note:** GitHub Packages requires authentication. Add `gpr.user` and `gpr.key` to your
> `~/.gradle/gradle.properties` or set `GITHUB_USERNAME` and `GITHUB_TOKEN` environment variables.
> The token needs the `read:packages` scope.

## Gradle (Kotlin DSL)

Add the dependency to your KMP module:

=== "Common"

    ```kotlin
    // build.gradle.kts
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.skymansandy:json-cmp:1.0.0-alpha2")
            }
        }
    }
    ```

=== "Android only"

    ```kotlin
    // build.gradle.kts
    dependencies {
        implementation("dev.skymansandy:json-cmp:1.0.0-alpha2")
    }
    ```

## Requirements

- Kotlin 2.3.10+
- Compose Multiplatform 1.10.2+
- Android minSdk 24+
