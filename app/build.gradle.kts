plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Kotlin 2: el compilador de Compose es un plugin de Kotlin (ya no composeOptions)
    alias(libs.plugins.kotlin.compose)
    // KSP reemplaza a kapt para Room y Hilt: compila más rápido y no genera stubs de Java
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.EGomez.idleecosystem"
    compileSdk = 36

    buildFeatures {
        compose = true
    }


    defaultConfig {
        applicationId = "com.EGomez.idleecosystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "0.11.0"

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


    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Persistencia (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Inyección de dependencias (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Códigos de partida (exportar / importar)
    implementation(libs.gson)

    // Tests de migración de Room con Robolectric
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
}

// Room exporta el esquema de cada versión de la base de datos a app/schemas.
// Esos JSON se versionan en git y permiten escribir tests de migración exactos.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
