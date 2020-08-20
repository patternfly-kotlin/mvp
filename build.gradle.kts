plugins {
    id("org.jetbrains.kotlin.js") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    `maven-publish`
}

group = "dev.fritz2"
version = "0.8-SNAPSHOT"

repositories {
    mavenLocal()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("dev.fritz2:core:0.8-SNAPSHOT")
}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }

            runTask {
                cssSupport.enabled = true
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }

    sourceSets {
        val test by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
