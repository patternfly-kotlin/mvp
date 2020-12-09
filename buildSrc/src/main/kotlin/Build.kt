import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.publish.maven.MavenPom

object Constants {
    const val group = "dev.fritz2"
    const val version = "0.1.0"
}

object PluginVersions {
    const val js = "1.4.20"
    const val dokka = "1.4.10"
}

object Versions {
    const val fritz2 = "0.8"
    const val kotest = "4.3.1"
}

fun DependencyHandler.fritz2() {
    add("implementation", "dev.fritz2:core:${Versions.fritz2}")
}

fun DependencyHandler.kotest() {
    add("testImplementation", "io.kotest:kotest-framework-api:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-assertions-core:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-property:${Versions.kotest}")
    add("testImplementation", "io.kotest:kotest-framework-engine:${Versions.kotest}")
}

fun MavenPom.defaultPom() {
    name.set("mvp")
    description.set("MVP implementation based on fritz2")
    url.set("https://github.com/hpehl/fritz2-mvp")
    licenses {
        license {
            name.set("Apache-2.0")
            url.set("https://opensource.org/licenses/Apache-2.0")
        }
    }
    developers {
        developer {
            id.set("hpehl")
            name.set("Harald Pehl")
            organization.set("Red Hat")
            organizationUrl.set("http://www.redhat.com")
        }
    }
    scm {
        url.set("https://github.com/hpehl/fritz2-mvp.git")
        connection.set("scm:git:git://github.com/hpehl/fritz2-mvp.git")
        developerConnection.set("scm:git:git://github.com/hpehl/fritz2-mvp.git")
    }
}
