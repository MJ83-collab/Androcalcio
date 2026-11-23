plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "it.androcalcio"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.androcalcio"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "0.6.0"
    }

    // Chiave fissa nel repository: ogni build si installa sopra la precedente senza disinstallare.
    // (Prima ogni build su GitHub usava una chiave di debug nuova e l'aggiornamento falliva.)
    signingConfigs {
        create("fissa") {
            storeFile = file("androcalcio.keystore")
            storePassword = "androcalcio"
            keyAlias = "androcalcio"
            keyPassword = "androcalcio"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fissa")
        }
        release {
            // R8: codice e risorse inutilizzati tolti, APK più piccolo e avvio più rapido
            isMinifyEnabled = true
            isShrinkResources = true
            // Firmato con la chiave fissa: installabile subito (nessun Play Store in gioco)
            signingConfig = signingConfigs.getByName("fissa")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }

    // I test stampano le statistiche di taratura nel log della build
    testOptions {
        unitTests.all { it.testLogging { showStandardStreams = true } }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
