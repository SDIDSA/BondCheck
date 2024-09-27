import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.sdidsa.bondcheck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sdidsa.bondcheck"
        minSdk = 29
        targetSdk = 35
        versionCode = 11
        versionName = "0.11"

        val properties = project.rootProject.file("gradle.properties").inputStream().use { stream ->
            Properties().apply { load(stream) }
        }

        properties.forEach { key, value ->
            if (key.toString().contains("API_")) {
                buildConfigField("String", key.toString(), "\"$value\"")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("cert") {
            keyAlias = project.property("keyAlias") as String
            keyPassword = project.property("keyPassword") as String
            storeFile = file(project.property("storeFile") as String)
            storePassword = project.property("storePassword") as String
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("cert")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("cert")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    implementation(libs.socket.io.client)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid.android)
    implementation(libs.recyclerview)
}