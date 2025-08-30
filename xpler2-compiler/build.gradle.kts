import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.vanniktech.maven.publish")
    id("signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.android.tools.gradle)
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {
        create("xpler2-compiler") {
            id = "io.github.xpler2.compiler"
            implementationClass = "io.github.xpler2.plugin.Xpler2CompilerPlugin"
        }
    }
}

mavenPublishing {
    coordinates("io.github.xpler2", "compiler", "${project.properties["libVersion"]}")

    pom {
        name.set("xpler2-compiler")
        description.set("Xpler2 Compiler Plugin for Xpler2.")
        url.set("https://github.com/Xpler2/xpler2")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("Gang")
                url.set("https://github.com/Xpler2/xpler2")
            }
        }
        scm {
            url.set("https://github.com/Xpler2/xpler2")
            connection.set("scm:git:git://github.com/Xpler2/xpler2.git")
            developerConnection.set("scm:git:ssh://git@github.com/Xpler2/xpler2.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
