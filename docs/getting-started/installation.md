# Installation

## Repository Setup

JsonCMP is published to Maven Central. Add `mavenCentral()` to your repositories in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

## Gradle (Kotlin DSL)

Add the dependency to your KMP module:

=== "Common"

    ```kotlin
    // build.gradle.kts
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.skymansandy:json-cmp:<version>")
            }
        }
    }
    ```

=== "Android only"

    ```kotlin
    // build.gradle.kts
    dependencies {
        implementation("dev.skymansandy:json-cmp:<version>")
    }
    ```

## Requirements

- Kotlin 2.3.10+
- Compose Multiplatform 1.10.2+
- Android minSdk 24+
