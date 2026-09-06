plugins {
    alias(libs.plugins.android.application)
}

// A google-services.json projektspecifikus és nincs verziókövetve (lásd README).
// Ha hiányzik, a Firebase plugin nélkül is lefordul a projekt – így a repo
// klónozás után azonnal buildelhető, a Firebase funkciók viszont nem működnek.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.lifecycle(
        "app/google-services.json hiányzik – a build Firebase konfiguráció nélkül készül. " +
            "A bejelentkezés/regisztráció csak saját google-services.json fájllal működik."
    )
}

android {
    namespace = "com.example.wormgame"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wormgame"
        minSdk = 28
        targetSdk = 35
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.core)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // A Firebase könyvtárak verzióját a BOM tartja szinkronban.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
