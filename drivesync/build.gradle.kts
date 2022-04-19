import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

group = BuildConfig.Info.group
version = BuildConfig.Info.version

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31

        consumerProguardFiles("proguard-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    android {
        publishLibraryVariants("release")
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("com.wakaztahir:kmp-storage:1.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-ktx:1.4.0")
                implementation("com.google.android.gms:play-services-auth:20.1.0")
                implementation("com.google.http-client:google-http-client-gson:1.40.1")
                implementation("com.google.apis:google-api-services-drive:v3-rev20211107-1.32.1") {
                    exclude(group = "org.apache.httpcomponents")
                    exclude(group = "com.google.guava")
                }
                implementation("com.google.api-client:google-api-client-android:1.32.1") {
                    exclude(group = "org.apache.httpcomponents")
                    exclude(group = "com.google.guava")
                }
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation("com.google.auth:google-auth-library-oauth2-http:1.3.0")
                implementation("com.google.http-client:google-http-client-gson:1.40.1")
                implementation("com.google.apis:google-api-services-drive:v3-rev20211107-1.32.1") {
                    exclude(group = "org.apache.httpcomponents")
                    exclude(group = "com.google.guava")
                }
                implementation("com.google.api-client:google-api-client:1.32.1")
            }
        }
        val desktopTest by getting
    }
}


val githubProperties = Properties()
kotlin.runCatching { githubProperties.load(FileInputStream(rootProject.file("github.properties"))) }

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "GithubPackages"
                url = uri("https://maven.pkg.github.com/codeckle/drivesync")

                runCatching {
                    credentials {
                        username = (githubProperties["gpr.usr"] ?: System.getenv("GPR_USER")).toString()
                        password = (githubProperties["gpr.key"] ?: System.getenv("GPR_API_KEY")).toString()
                    }
                }.onFailure { it.printStackTrace() }
            }
        }
    }
}