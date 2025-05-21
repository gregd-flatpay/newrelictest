import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("newrelic")
}

android {
    namespace = "com.example.newrelictest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.newrelictest"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val newRelicToken =
            getLocalProperties().getProperty("NEW_RELIC_TOKEN", "TOKEN_NOT_CONFIGURED")
        buildConfigField(
            "String",
            "NEW_RELIC_TOKEN",
            "\"$newRelicToken\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}


newrelic {
    // exclude debug build types and flavors
    logInstrumentationEnabled.set(false)
    defaultInteractionsEnabled.set(false)
    uploadMapsForVariant("release", "Release")
    excludePackageInstrumentation("com.android")
    excludePackageInstrumentation("com.github")
    excludePackageInstrumentation("com.google")
    excludePackageInstrumentation("android.support.*")
    excludePackageInstrumentation("io.jsonwebtoken.*")
    excludePackageInstrumentation("javax.*")
    excludePackageInstrumentation("jakarta.inject.*")
    excludePackageInstrumentation("coil.*")
    excludePackageInstrumentation("okhttp3.*")
    excludePackageInstrumentation("okio.*")
    excludePackageInstrumentation("kotlin.*")
    excludePackageInstrumentation("kotlinx.*")
    excludePackageInstrumentation("androidx.*")
    excludePackageInstrumentation("dagger.*")
    excludePackageInstrumentation("org.*")
}

fun getLocalProperties(): Properties {
    val props = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { props.load(it) }
    }
    return props
}


dependencies {
    implementation(libs.newrelic.agent)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}