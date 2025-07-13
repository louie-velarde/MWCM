plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.velc.mwcm"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.velc.mwcar"
        minSdk = 19
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.0"

        base.archivesName = "MWCM-$versionName"
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

if (project.file("signing.gradle").exists()) {
    apply(from = "signing.gradle")
}

dependencies {
    compileOnly(libs.xposed)
    implementation(libs.gson)
}