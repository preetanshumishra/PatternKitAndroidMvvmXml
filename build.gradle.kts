plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.google.devtools.ksp") version "2.3.5" apply false
    kotlin("jvm") version "2.2.20" apply false
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
