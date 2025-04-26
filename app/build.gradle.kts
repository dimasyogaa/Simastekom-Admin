plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-parcelize")
}

android {
    namespace = "com.yogadimas.simastekom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yogadimas.simastekom"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "DEBUG", "true")

        buildConfigField("String", "BASE_URL", "\"http://192.168.69.106:8000\"")
        buildConfigField("String", "API", "\"/api/\"")

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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            pickFirsts.add("META-INF/LICENSE-notice.md")
            pickFirsts.add("META-INF/LICENSE.md")
        }
    }

}

dependencies {

    implementation(libs.koin.android)


    implementation(libs.androidx.paging.runtime.ktx)


    implementation(libs.androidx.swiperefreshlayout)


    implementation(libs.bcrypt)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.ktx)

    implementation(libs.coil)

    // tidak perlu implementasi safe args disini
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // UI test
    implementation(libs.androidx.espresso.idling.resource)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)


    // JUnit 4 untuk pengujian unit dan UI
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)

    // JUnit 5 untuk pengujian unit
    testImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)

}

// agar gradle mengenali junit5
tasks.withType<Test> {
    useJUnitPlatform()
}


