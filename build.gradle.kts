import org.jetbrains.dokka.Platform
import java.net.URL

// ------------------------------------------------------ core

plugins {
    kotlin("js") version Versions.kotlin
    id("org.jetbrains.dokka") version Versions.dokka
    `maven-publish`
}

group = "dev.fritz2"
version = "0.3.0"

// ------------------------------------------------------ repositories

val repositories = arrayOf(
    "https://oss.sonatype.org/content/repositories/snapshots/",
    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
)

repositories {
    mavenLocal()
    mavenCentral()
    repositories.forEach { maven(it) }
}

// ------------------------------------------------------ dependencies

dependencies {
    implementation("dev.fritz2:core:${Versions.fritz2}")
    testImplementation(kotlin("test-js"))
}

kotlin {
    js {
        explicitApi()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
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
