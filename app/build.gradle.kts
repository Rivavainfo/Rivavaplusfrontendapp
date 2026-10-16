
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rivavafi.universal"
    compileSdk = 35

    // Load local.properties for local development
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use {
            localProperties.load(it)
        }
    }

    // GitHub Actions sets RIVAVA_CI_SIGNING=true.
    // In CI, release signing is mandatory.
    val requireCiSigning =
        System.getenv("RIVAVA_CI_SIGNING")
            ?.equals("true", ignoreCase = true) == true

    fun signingValue(name: String): String? {
        return System.getenv(name)
            ?.takeIf { it.isNotBlank() }
            ?: localProperties.getProperty(name)
                ?.takeIf { it.isNotBlank() }
    }

    val releaseStoreFilePath =
        signingValue("RELEASE_STORE_FILE")
            ?: "release-key.jks"

    val releaseKeystoreFile = file(releaseStoreFilePath)
    val releaseStorePassword =
        signingValue("RELEASE_STORE_PASSWORD")
    val releaseKeyAlias =
        signingValue("RELEASE_KEY_ALIAS")
    val releaseKeyPassword =
        signingValue("RELEASE_KEY_PASSWORD")

    val signingReady =
        releaseKeystoreFile.isFile &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

    // Prevent unsigned release bundles in GitHub Actions.
    if (requireCiSigning && !signingReady) {
        throw GradleException(
            "RIVAVA RELEASE SIGNING FAILED: " +
            "A valid keystore file and all four signing " +
            "configuration values are required. " +
            "Check RELEASE_KEYSTORE_BASE64, " +
            "RELEASE_STORE_PASSWORD, " +
            "RELEASE_KEY_ALIAS and " +
            "RELEASE_KEY_PASSWORD in GitHub Actions. " +
            "The workflow must decode the keystore to " +
            "app/release-key.jks before building."
        )
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreFile

            if (signingReady) {
                storePassword = releaseStorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!

                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true

                println(
                    "RIVAVA SIGNING: Release signing configured."
                )
            } else {
                println(
                    "RIVAVA SIGNING: Credentials unavailable. " +
                    "Release outputs will be unsigned " +
                    "outside mandatory CI signing mode."
                )
            }
        }
    }

    defaultConfig {
        applicationId = "com.rivavafi.universal"
        minSdk = 24
        targetSdk = 35
        versionCode = 34
        versionName = "34"

        // API key injection
        buildConfigField(
            "String",
            "ALPHA_VANTAGE_API_KEY",
            "\"${System.getenv("ALPHA_VANTAGE_API_KEY") ?: localProperties.getProperty("alphavantage.apikey") ?: ""}\""
        )

        buildConfigField(
            "String",
            "FINNHUB_API_KEY",
            "\"${System.getenv("FINNHUB_API_KEY") ?: localProperties.getProperty("finnhub.apikey") ?: ""}\""
        )

        buildConfigField(
            "String",
            "RESEND_API_KEY",
            "\"${System.getenv("RESEND_API_KEY") ?: localProperties.getProperty("resend.apikey") ?: ""}\""
        )

        buildConfigField(
            "String",
            "MASSIVE_API_KEY",
            "\"${System.getenv("MASSIVE_API_KEY") ?: localProperties.getProperty("massive.apikey") ?: ""}\""
        )

        buildConfigField(
            "String",
            "BACKEND_URL",
            "\"${System.getenv("BACKEND_URL") ?: localProperties.getProperty("backend.url") ?: "https://backend-453t.onrender.com/"}\""
        )

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    splits {
        abi {
            isEnable = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // Distribution flavors
    flavorDimensions.add("distribution")

    productFlavors {
        create("play") {
            dimension = "distribution"
        }

        create("fullSms") {
            dimension = "distribution"
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false

            // Keep original release optimization settings
            isMinifyEnabled = true
            isShrinkResources = true

            // Apply the same signing configuration
            // to both playRelease and fullSmsRelease.
            if (signingReady) {
                signingConfig =
                    signingConfigs.getByName("release")

                println(
                    "RIVAVA RELEASE: Signing enabled " +
                    "for all release distribution flavors."
                )
            } else if (requireCiSigning) {
                throw GradleException(
                    "Unsigned Rivava CI release is prohibited."
                )
            }

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
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
        buildConfig = true
    }

    lint {
        abortOnError = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }

        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // Firebase and Google Auth
    implementation(
        platform("com.google.firebase:firebase-bom:32.8.0")
    )
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.android.gms:play-services-auth:21.6.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Jetpack Compose
    val composeBom =
        platform("androidx.compose:compose-bom:2024.02.02")

    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil:2.6.0")

    // WorkManager
    val workVersion = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")

    // Razorpay Payment Gateway
    implementation("com.razorpay:checkout:1.6.33")

    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Instrumentation Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
