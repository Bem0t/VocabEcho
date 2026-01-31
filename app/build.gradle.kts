plugins {
    alias(libs.plugins.android.application)
    //Добавлен мной ------------------
    //alias(libs.plugins.kotlin.android)
    //------------------ ------------------
    alias(libs.plugins.kotlin.compose)

    //-------------------- Добавлено мной ----------------------------
    alias(libs.plugins.kotlin.serialization)
    //id("com.google.devtools.ksp")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myApp27.vocabecho"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.myApp27.vocabecho"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //-------------------- Добавлено мной ----------------------------
    implementation(libs.kotlinx.serialization.json)
    //ksp("androidx.room:room-compiler:2.8.4")
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    ksp(libs.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}