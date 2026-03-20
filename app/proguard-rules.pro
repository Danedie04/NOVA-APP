# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the default proguard-android-optimize.txt file.

# Keep VPN service classes
-keep class com.novavpn.app.vpn.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes
-keep class com.novavpn.app.data.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
