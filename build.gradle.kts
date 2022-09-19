plugins {
    kotlin("multiplatform") version "1.7.10"
    id("com.ullink.nuget") version "2.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64()
        hostOs == "Linux" -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"

                if (isMingwX64) {
                    linkTask.dependsOn("nugetRestore")
                    val execPath = System.getenv("PATH") + System.getProperty("path.separator") + "$projectDir/packages/librdkafka.redist.1.9.2/runtimes/win-x64/native"
                    runTask?.setEnvironment("PATH" to execPath)
                }
            }

        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.icemachined:kafka-client:0.0.22")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        nativeTarget.let {
            getByName("${it.name}Main").dependsOn(nativeMain)
        }
    }
}

tasks.withType<com.ullink.NuGetRestore>().configureEach {
    packagesDirectory = rootProject.file("packages")
}
