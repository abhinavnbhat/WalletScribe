plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.notificationlogger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.notificationlogger"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Google Sheet ID (between /d/ and /edit in the URL)
        buildConfigField("String", "SHEET_ID", "\"1c0uFS5i1FbljYZjRgwjI58Elw8bWgOfWxH30NJFimmU\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig  = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.md"
            excludes += "META-INF/*.txt"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // JSON factory for Google APIs (Gson) – needs to match your 2.x client libs
    implementation("com.google.api-client:google-api-client-gson:2.0.0")
    // Google Gson itself (lenient parsing support)
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Google Sheets & Google API client (latest compatible versions) ---
    implementation("com.google.apis:google-api-services-sheets:v4-rev20250603-2.0.0") {
        exclude(group = "com.google.guava")      // avoid duplicate classes
    }
    implementation("com.google.api-client:google-api-client-android:2.5.0") {
        exclude(group = "com.google.guava")
    }

    // Hashing
    implementation("com.google.guava:guava:31.1-android")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
