plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.LifeSync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.LifeSync"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gson)
    testImplementation(libs.junit)
    implementation(libs.michalsvec.single.row.calendar)
    implementation(libs.whiteelephant.monthandyearpicker) {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-core-utils")
    }
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}