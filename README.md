# Osana_Hasan_1812243-Hasan_Nadeem_1812193
GUMSHUDA

Osama Hasan 1812243
Hasan Nadeem 1812193

Steps to deploy the project along with the way of installation

1. Android version 4.0.2
2. Gradle version 6.1.1
3. Connect you android project to firebase
4. Add these changes to your realtime database rules
{
  "rules": {
    ".read": "auth != null",  // 2022-3-11
    ".write": "auth != null",  // 2022-3-11
  }
}

5. MinSdkVersion 21
6. TargetSdkVersion 29
7. compileSdkVersion 29
8. buildToolsVersion "30.0.3"

9. Add all the dependencies
dependencies {
    implementation 'androidx.appcompat:appcompat:1.2.0'
    
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    
    implementation 'androidx.cardview:cardview:1.0.0'
    
    implementation 'com.google.android.material:material:1.2.1'
    
    implementation "androidx.constraintlayout:constraintlayout:2.1.3"
    
    implementation 'androidx.core:core-ktx:1.3.2'
    
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.4.10"
    
    implementation 'com.cuberto:liquid-swipe:1.0.0'
    
    implementation 'com.airbnb.android:lottie:3.5.0'
    
    implementation 'com.github.mohammadatif:CircularImageView:1.0.1'
    
    implementation 'com.google.android.gms:play-services-auth:20.2.0'
    
    implementation 'com.squareup.picasso:picasso:2.71828'
    
    implementation 'com.onesignal:OneSignal:3.11.1'
    
    implementation 'com.facebook.android:facebook-android-sdk:13.2.0'
    
    implementation 'com.google.android.gms:play-services-maps:18.0.2'
    
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'

    implementation 'com.google.firebase:firebase-database:20.0.5'
    
    implementation 'com.google.firebase:firebase-core:21.0.0'
    
    
    implementation 'com.google.firebase:firebase-auth:21.0.5'
    
    implementation platform('com.google.firebase:firebase-bom:29.0.4')
    
    implementation 'com.google.firebase:firebase-analytics'
    
    implementation 'com.google.firebase:firebase-storage:20.0.1'
    
    implementation 'com.google.firebase:firebase-messaging:23.0.5'

    implementation 'com.android.volley:volley:1.2.1'
    implementation 'com.google.code.gson:gson:2.9.0'
    
    testImplementation 'junit:junit:4+'
    
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
    
    implementation 'com.hbb20:ccp:2.3.1'

}

10. Sync your project and run
