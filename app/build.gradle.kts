plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
}

android {
    namespace 'com.example.bookapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.bookapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion '1.5.4'
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.0'
    implementation "androidx.compose.ui:ui:1.5.4"
    implementation "androidx.compose.ui:ui-tooling-preview:1.5.4"
    implementation 'androidx.compose.material3:material3:1.1.2'
    
    implementation "androidx.room:room-runtime:2.6.0"
    implementation "androidx.room:room-ktx:2.6.0"
    kapt "androidx.room:room-compiler:2.6.0"
    
    // ✅ Gson برای تبدیل JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
