import java.util.Properties
import java.io.FileInputStream

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${BuildConfig.Info.KotlinVersion}")
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${BuildConfig.Info.KotlinVersion}")
    }
}

group = BuildConfig.Info.group
version = BuildConfig.Info.version

plugins {
    id("org.jetbrains.dokka") version BuildConfig.Info.DokkaVersion
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>(){
    outputDirectory.set(rootProject.file("docs/api"))
}

val githubProperties = Properties()
kotlin.runCatching { githubProperties.load(FileInputStream(rootProject.file("github.properties"))) }

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
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