plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.velc.mwcm"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "me.velc.mwcar"
        minSdk = 19
        versionCode = 7
        versionName = "1.1.1"

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