import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
}

// Load API key from local.properties
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            implementation(libs.datastore.preferences)
            implementation(libs.okio)

            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)

            implementation(libs.navigation.compose)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation("androidx.datastore:datastore-preferences:1.1.1")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
            implementation(libs.okio)
        }
    }
}

android {
    namespace = "com.learncore.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.learncore.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
        )
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("LearnCoreDatabase") {
            packageName.set("com.learncore.data.local")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
            migrationOutputFileFormat.set(".sqm")
            verifyMigrations.set(false)
            version = 3
        }
    }
}

dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
}

kover {
    reports {
        filters {
            excludes {
                packages(
                    // Infrastructure - tidak bisa ditest tanpa platform
                    "com.learncore.core.di",
                    "com.learncore.core.util",
                    "com.learncore.core.notification",
                    "com.learncore.core.network",

                    // Android entry point
                    "com.learncore.android",

                    // SQLDelight generated & remote
                    "com.learncore.data.remote.api",
                    "com.learncore.data.remote.dto",
                    "com.learncore.data.repository",

                    // DataStore factory (platform-specific)
                    // UserPreferences tetap masuk

                    // Compose navigation & theme (tidak ada logic)
                    "com.learncore.presentation.navigation",
                    "com.learncore.presentation.theme",

                    // Generated resources
                    "learncore.composeapp.generated.resources"
                )
                classes(
                    // Android/iOS entry points
                    "com.learncore.MainKt",
                    "com.learncore.AppKt",
                    "com.learncore.App",

                    // SQLDelight generated classes di data.local
                    "com.learncore.data.local.LearnCore*",
                    "com.learncore.data.local.Get*",
                    "com.learncore.data.local.Count*",
                    "com.learncore.data.local.Insert*",
                    "com.learncore.data.local.Update*",
                    "com.learncore.data.local.Delete*",
                    "com.learncore.data.local.TaskEntity",
                    "com.learncore.data.local.TaskEntity$*",
                    "com.learncore.data.local.PomodoroSession*",
                    "com.learncore.data.local.LearnCoreDatabase*",
                    "com.learncore.data.local.datastore.DataStoreFactory*",

                    // Compose Screen files (top-level @Composable = *Kt class)
                    "com.learncore.presentation.screens.ai.AIAssistantScreenKt*",
                    "com.learncore.presentation.screens.tasks.AddEditTaskScreenKt*",
                    "com.learncore.presentation.screens.tasks.TaskListScreenKt*",
                    "com.learncore.presentation.screens.tasks.TaskDetailScreenKt*",
                    "com.learncore.presentation.screens.tasks.PlatformDateTimePicker*",
                    "com.learncore.presentation.screens.profile.ProfileScreenKt*",
                    "com.learncore.presentation.screens.profile.HelpSupportScreenKt*",
                    "com.learncore.presentation.screens.profile.AccountEditScreenKt*",
                    "com.learncore.presentation.screens.profile.ProfilePhotoSection*",
                    "com.learncore.presentation.screens.dashboard.DashboardScreenKt*",
                    "com.learncore.presentation.screens.pomodoro.PomodoroScreenKt*",
                    "com.learncore.presentation.components.SharedComponentsKt*",

                    // Compose lambda singletons (auto-generated)
                    "com.learncore.presentation.screens.*.*ComposableSingletons*",
                    "*ComposableSingletons*",

                    // Koin / DI
                    "*.*_Factory*",
                    "*.*Module*",
                    "*.BuildConfig"
                )
            }
        }
        verify {
            rule {
                minBound(70)
            }
        }
    }
}
