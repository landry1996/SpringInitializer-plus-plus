plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.springforge"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

intellij {
    version.set("2024.1")
    type.set("IC")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
        changeNotes.set("""
            <ul>
                <li>Generate Spring Boot projects with hexagonal, DDD, and layered architectures</li>
                <li>AI-powered recommendations and code generation</li>
                <li>MySQL, PostgreSQL, MongoDB database support</li>
                <li>Docker Compose auto-generation</li>
            </ul>
        """.trimIndent())
    }

    publishPlugin {
        token.set(System.getenv("INTELLIJ_PUBLISH_TOKEN"))
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}
