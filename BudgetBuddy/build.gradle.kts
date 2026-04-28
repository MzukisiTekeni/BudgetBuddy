// Top-level build.gradle
// AGP 8.7.3 works with Android Studio Ladybug (2024.2.1) and Meerkat (2024.3.1)
// Kotlin 2.0.21 is the stable version that ships with these Studio releases
// KSP replaces kapt — it is faster and fully supported on Kotlin 2.x

plugins {
    id(  "com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
