plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val releaseSigningEnvironment = listOf(
    "ANDROID_KEYSTORE_PATH",
    "ANDROID_KEYSTORE_PASSWORD",
    "ANDROID_KEY_ALIAS",
    "ANDROID_KEY_PASSWORD"
)
val releaseSigningValues = releaseSigningEnvironment.associateWith { providers.environmentVariable(it).orNull }

android {
    namespace = "com.tradedharma.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.tradedharma.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    signingConfigs {
        create("release") {
            storeFile = releaseSigningValues["ANDROID_KEYSTORE_PATH"]?.let { rootProject.file(it) }
            storePassword = releaseSigningValues["ANDROID_KEYSTORE_PASSWORD"]
            keyAlias = releaseSigningValues["ANDROID_KEY_ALIAS"]
            keyPassword = releaseSigningValues["ANDROID_KEY_PASSWORD"]
            storeType = "JKS"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

gradle.taskGraph.whenReady {
    val releaseArtifactRequested = allTasks.any { task ->
        task.project == project && (
            task.name == "assembleRelease" ||
                task.name.startsWith("packageRelease") ||
                task.name.startsWith("bundleRelease")
            )
    }
    if (releaseArtifactRequested) {
        val missing = releaseSigningEnvironment.filter { releaseSigningValues[it].isNullOrBlank() }
        if (missing.isNotEmpty()) {
            throw GradleException("Release signing requires environment variables: ${missing.joinToString()}")
        }
        val keystorePath = requireNotNull(releaseSigningValues["ANDROID_KEYSTORE_PATH"])
        if (!rootProject.file(keystorePath).isFile) {
            throw GradleException("ANDROID_KEYSTORE_PATH must point to an existing keystore file.")
        }
    }
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

val composeBom = dependencies.platform("androidx.compose:compose-bom:2026.08.00")
val roomVersion = "2.8.4"

dependencies {
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.10.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
