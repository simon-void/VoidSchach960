import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "voidchess.VoidchessAppKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":module-common"))
    implementation(project(":module-ui"))
    implementation(project(":module-united"))
}


allprojects {
    group = "de.gmx.simonvoid"
    version = "3.5"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "12"
            }
        }

        withType<Test> {
            useTestNG()
        }
    }
}

tasks {
    register("buildInstaller") {
        dependsOn("build")

        doLast {
            if (JavaVersion.current() < JavaVersion.VERSION_14) {
                throw GradleException("Require Java 14+ to run 'jpackage' (currently ${JavaVersion.current()})")
            }
            val projectVersion = project.version.toString()
            JPackage.buildInstaller(
                name = "VoidChess",
                description = "a chess program",
                appVersion = projectVersion,
                inputDir = "build/libs",
                destinationDir = "build/installer",
                mainJar = "voidchess-$projectVersion-all.jar",
                addModules = listOf("java.desktop"),
                winIcoIconPath = "about/shortcut-icon.ico2",
                winShortcut = true,
                winMenu = true,
                linuxPngIconPath = "about/shortcut-icon2.png",
                linuxShortcut = true,
                linuxMenuGroup = "Games",
                macIcnsIconPath = "about/shortcut-icon2.icns"
            )
        }
    }
}
