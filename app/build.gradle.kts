plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)


    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.EGomez.idleecosystem"
    compileSdk = 36

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    defaultConfig {
        applicationId = "com.EGomez.idleecosystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "0.10.0"

        // Nombre visible de la app (las builds de prueba lo reemplazan)
        manifestPlaceholders["appLabel"] = "@string/app_name"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma de release desde variables de entorno (las define GitHub Actions con los
    // secretos del repositorio). Sin esas variables, la compilación local no cambia:
    // Android Studio sigue usando "Generate Signed APK" como siempre.
    val releaseKeystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // Build de pruebas (alpha/beta) que publica el flujo "Preview build".
        // Igual a release, pero con otro applicationId: se instala JUNTO a la app
        // oficial sin reemplazarla, con su propio nombre y sus propios datos.
        create("preview") {
            initWith(getByName("release"))
            applicationIdSuffix = ".preview"
            versionNameSuffix = (project.findProperty("previewSuffix") as String?) ?: "-preview"
            manifestPlaceholders["appLabel"] = (project.findProperty("previewLabel") as String?) ?: "IdleEcosystem Preview"
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    testOptions {
        // Las llamadas a android.util.Log en tests unitarios devuelven valores por defecto
        unitTests.isReturnDefaultValues = true
        // Robolectric (tests de migración de Room) necesita los recursos de Android
        unitTests.isIncludeAndroidResources = true
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("androidx.compose.material:material-icons-extended")

    implementation ("com.google.code.gson:gson:2.10.1")


    // 1. Persistencia (Room Database) - Vital para guardar progreso
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core-ktx:1.6.1")

    // 2. Inyección de Dependencias (Hilt) - Para gestionar Singletons del juego
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // 3. Serialización (Para guardar objetos complejos como JSON si es necesario)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 4. Ciclo de vida y ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.lifecycleRuntimeKtx.get()}")

    implementation("com.google.dagger:hilt-android:2.51.1") // O la versión que tengas
    kapt("com.google.dagger:hilt-android-compiler:2.51.1") // <--- CAMBIA 'ksp' POR 'kapt' AQUÍ

}

// Room exporta el esquema de cada versión de la base de datos a app/schemas.
// Esos JSON se versionan en git y permiten escribir tests de migración exactos.
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
