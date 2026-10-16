# Keep attributes required for reflection, annotations, inner classes, and generics (Gson / Retrofit / Firestore / Hilt)
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault

# Keep Application class and entry points
-keep class com.rivavafi.universal.RivavaApp { *; }
-keep class com.rivavafi.universal.HomeActivity { *; }

# Keep BroadcastReceivers
-keep class com.rivavafi.universal.sms.SmsReceiver { *; }
-keep public class * extends android.content.BroadcastReceiver

# Keep Hilt / Dagger generated classes and Workers
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# Keep Compose specific classes
-keep class androidx.compose.** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}

# Keep Room generated classes and DAO interfaces
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.rivavafi.universal.data.local.** { *; }

# Keep Data Transfer Objects (DTOs), Gson serializable models, Firebase Firestore models
-keep class com.rivavafi.universal.data.model.** { *; }
-keepclassmembers class com.rivavafi.universal.data.model.** { *; }

-keep class com.rivavafi.universal.data.remote.** { *; }
-keepclassmembers class com.rivavafi.universal.data.remote.** { *; }

-keep class com.rivavafi.universal.data.network.** { *; }
-keepclassmembers class com.rivavafi.universal.data.network.** { *; }

-keep class com.rivavafi.universal.sms.ParsedSmsTransaction { *; }

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Retrofit API interfaces and parameters
-keep interface com.rivavafi.universal.data.network.** { *; }
-keep interface com.rivavafi.universal.domain.api.** { *; }

# Proguard / Razorpay rules
-dontwarn proguard.annotation.**
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**
