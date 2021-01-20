# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/bengallienne/Android SDK/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn okio.**
-dontwarn com.google.android.gms.**
-dontwarn org.xmlpull.v1.**
-dontwarn com.caverock.androidsvg.**
-dontwarn org.osmdroid.**

# Retrofit
-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
#-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.xml.stream.** { *; }
-keep class retrofit.** { *; }
-keep class com.google.appengine.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn com.squareup.okhttp3.*
-dontwarn rx.**
-dontwarn javax.xml.stream.**
-dontwarn com.google.appengine.**
-dontwarn java.nio.file.**
-dontwarn org.codehaus.**

-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepattributes EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Add any classes the interact with gson
# the following line is for illustration purposes
-keep class com.olam.digital.ofis.HomeActivity
-keep class com.olam.digital.ofis.authentication.AccessTockenTask
-keep class com.olam.digital.ofis.authentication.AuthenticationClient
-keep class com.olam.digital.ofis.authentication.AuthenticationInterface

# Hide warnings about references to newer platforms in the library
-dontwarn android.support.v7.**
# don't process support library
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

#OFIS class
-dontwarn org.xmlpull.v1.XmlPullParser
-dontwarn org.xmlpull.v1.XmlSerializer
-keep class org.xmlpull.v1.* {*;}

#-keep class com.olam.digital.ofis.** { *;}
-keep class com.olam.digital.ofis.authentication.** { *; }
-keep class com.olam.digital.ofis.authentication.model.** { *; }

#
#-keep class com.olam.digital.ofis.biometric.** { *; }

#-keep class com.olam.digital.ofis.childlabour.** { *; }
#-keep class com.olam.digital.ofis.childlabour.adapter.** { *; }
#-keep class com.olam.digital.ofis.childlabour.businesslogic.** { *; }
#-keep class com.olam.digital.ofis.childlabour.dto.** { *; }
#-keep interface com.olam.digital.ofis.childlabour.listener.** { *; }
#
#-keep class com.olam.digital.ofis.gps.** { *; }
#-keep class com.olam.digital.ofis.gps.businesslogic.** { *; }
#-keep class com.olam.digital.ofis.gps.dto.** { *; }
#-keep class com.olam.digital.ofis.gps.utils.** { *; }
#-keep interface com.olam.digital.ofis.gps.utils.** { *; }
#
#
#-keep class com.olam.digital.ofis.logging.** { *; }
#-keep class com.olam.digital.ofis.matoma.** { *; }
#-keep class com.olam.digital.ofis.model.** { *; }
#-keep class com.olam.digital.ofis.recordpolygon.** { *; }
#-keep class com.olam.digital.ofis.tasks.** { *; }

#Dependency Library
-keep class com.scottyab.** { *; }


# GSON requirements
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# design support library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
     public <init>(android.content.Context, android.util.AttributeSet, int);
}
#Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.**
-dontnote retrofit2.Platform
-keepattributes Signature
-keepattributes Exceptions

-keep class retrofit2.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**


#NeuroTech
-keep class com.neurotec.** { *; }
-keep class com.sun.jna.** { *; }

-dontwarn com.sun.jna.**