plugins {
    id("org.jetbrains.compose") version "1.0.1"
    id("com.android.application")
    kotlin("android")
}

group = BuildConfig.Info.group
version = BuildConfig.Info.version

dependencies {
    implementation("androidx.activity:activity-compose:1.4.0")
    api(compose.ui)
    api(compose.runtime)
    api(compose.material)
    api(compose.foundation)
    implementation(project(":drivesync"))
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.wakaztahir.android"
        minSdk = 21
        targetSdk = 31
        versionCode = BuildConfig.Info.versionCode
        versionName = BuildConfig.Info.version
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    packagingOptions {
        /**
         *  Why was this block added
         *  see https://stackoverflow.com/questions/44342455/more-than-one-file-was-found-with-os-independent-path-meta-inf-license
         */
        with(resources.excludes){
            add("META-INF/DEPENDENCIES")
            add("META-INF/LICENSE")
            add("META-INF/LICENSE.txt")
            add("META-INF/license.txt")
            add("META-INF/NOTICE")
            add("META-INF/NOTICE.txt")
            add("META-INF/notice.txt")
            add("META-INF/ASL2.0")
            add("META-INF/*.kotlin_module")
        }
    }
}