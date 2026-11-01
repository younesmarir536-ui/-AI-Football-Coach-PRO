plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.aifootballcoach.pro"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.aifootballcoach.pro"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2-radar-capture"
    }
}
