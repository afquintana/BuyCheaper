plugins {
    id("com.android.application") version "9.1.1" apply false
<<<<<<< HEAD
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
=======
    id("com.google.dagger.hilt.android") version "2.57" apply false
>>>>>>> origin/codex/fix-kotlin-plugin-conflict-in-build.gradle.kts
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
}
