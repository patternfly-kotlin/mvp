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
            register("jsMain") {
                displayName = "JS"
                platform = "js"
            }
        }
    }
}
