# ProGuard rules for TrailMedic

# Keep Room entities and DAOs
-keep class com.trailmedic.data.local.entity.** { *; }
-keep class com.trailmedic.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep MediaPipe GenAI LLM
-keep class com.google.mediapipe.tasks.genai.** { *; }

# Keep Gson models
-keepclassmembers class com.trailmedic.domain.model.** { *; }

# Keep Dagger / Hilt
-keep class * extends dagger.hilt.internal.UnsafeCasts
-keepclassmembers class * {
    @javax.inject.Inject *;
    @dagger.Provides *;
}
