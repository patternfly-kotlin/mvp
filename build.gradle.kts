plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("maven-publish")
}

group = "dev.fritz2"
version = "0.8-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    jcenter()
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8-SNAPSHOT")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks {
    dokkaHtml {
        dokkaSourceSets {
            register("commonMain") {
                platform = "js"
            }
            register("jsMain") {
                platform = "js"
            }
        }
    }
}
