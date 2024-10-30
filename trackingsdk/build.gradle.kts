plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")

}

android {
    namespace = "com.app.trackingsdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(platform(libs.firebase.bom.v3210))

    implementation(libs.firebase.config.ktx)
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation (libs.adjust.android)
    implementation (libs.installreferrer)
    implementation (libs.adjust.android.webbridge)

    implementation (libs.installreferrer)
    implementation (libs.play.services.ads.identifier)

    // OneSignal
    implementation ("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
    implementation ("com.revenuecat.purchases:purchases:8.8.1")
    implementation ("com.revenuecat.purchases:purchases-ui:8.8.1")
    implementation ("com.android.billingclient:billing:5.0.0")
//    implementation(libs.play.services.measurement.api) {
//        exclude(group = "com.google.android.gms", module = "play-services-measurement")
//    }


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.abbas-alayan23"
                artifactId = "tracking-sdk"
                version = "2.3"
            }
        }
    }
}
