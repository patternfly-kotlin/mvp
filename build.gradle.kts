import org.jetbrains.dokka.Platform
import java.net.URL

plugins {
    kotlin("js") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    `maven-publish`
}

group = "dev.fritz2"
version = "0.3.0"

object Meta {
    const val desc = "MVP implementation based on fritz2"
    const val license = "Apache-2.0"
    const val githubRepo = "hpehl/fritz2-mvp"
}

object Versions {
    const val fritz2 = "0.9"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    implementation("dev.fritz2:core:${Versions.fritz2}")
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(BOTH) {
        explicitApi()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        sourceSets {
            named("main") {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                }
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

tasks {
    dokkaHtml.configure {
        dokkaSourceSets {
            named("main") {
                noJdkLink.set(false)
                noStdlibLink.set(false)
                includeNonPublic.set(false)
                skipEmptyPackages.set(true)
                platform.set(Platform.js)
                includes.from("src/main/resources/module.md")
                samples.from("src/main/resources/")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/${Meta.githubRepo}/blob/master/src/main/kotlin/"))
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    this.url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
                }
                externalDocumentationLink {
                    this.url.set(URL("https://api.fritz2.dev/core/core/"))
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("hpehl")
                        name.set("Harald Pehl")
                        organization.set("Red Hat")
                        organizationUrl.set("https://developers.redhat.com/")
                    }
                }
                scm {
                    url.set("https://github.com/${Meta.githubRepo}.git")
                    connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${Meta.githubRepo}.git")
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${Meta.githubRepo}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
