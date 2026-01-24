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
        kotlinCompilerExtensionVersion = "1.5.14" // <--- AGREGA O CORRIGE ESTO
    }

    defaultConfig {
        applicationId = "com.EGomez.idleecosystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "0.9.0"

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
    kotlinOptions {
        jvmTarget = "11"
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

    // 2. Inyección de Dependencias (Hilt) - Para gestionar Singletons del juego
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // 3. Serialización (Para guardar objetos complejos como JSON si es necesario)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 4. Ciclo de vida y ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("com.google.dagger:hilt-android:2.51.1") // O la versión que tengas
    kapt("com.google.dagger:hilt-android-compiler:2.51.1") // <--- CAMBIA 'ksp' POR 'kapt' AQUÍ

}