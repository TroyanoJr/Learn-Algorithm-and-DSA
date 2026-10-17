import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

val requestedTasks = gradle.startParameter.taskNames
val isBundleReleaseRequested = requestedTasks.any { task: String ->
    task == "bundleRelease" || task == ":androidApp:bundleRelease" || task.endsWith(":bundleRelease")
}

android {
    namespace = "com.example.learndsandalgorithm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.troyadev.learndsandalgorithm"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    val keyPropertiesFile = rootProject.file("key.properties")
    val keyProperties = Properties()
    val hasKeyProperties = keyPropertiesFile.exists()

    if (isBundleReleaseRequested && !hasKeyProperties) {
        throw GradleException(
            "Release App Bundle build failed: 'key.properties' is missing. " +
            "Valid release signing credentials must be configured in 'key.properties' before generating the upload bundle."
        )
    }

    if (hasKeyProperties) {
        keyPropertiesFile.inputStream().use { keyProperties.load(it) }
    }

    signingConfigs {
        if (hasKeyProperties) {
            create("release") {
                val storeFilePath = keyProperties.getProperty("storeFile")
                if (!storeFilePath.isNullOrBlank()) {
                    storeFile = rootProject.file(storeFilePath)
                }
                storePassword = keyProperties.getProperty("storePassword")
                keyAlias = keyProperties.getProperty("keyAlias")
                keyPassword = keyProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (hasKeyProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}