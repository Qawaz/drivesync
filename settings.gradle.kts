pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    
}

rootProject.name = "DriveSync"

include(":demo:android")
include(":demo:desktop")
include(":drivesync")