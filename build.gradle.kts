plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "io.seedwing.enforcer"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(
            /* Plugin Dependencies */
            "com.intellij.java",
            "org.jetbrains.idea.maven"
    ))
}

dependencies {
    implementation("com.github.ballerina-platform:lsp4intellij:0.95.1")
    //implementation(files("vendor/lsp4intellij-0.0.1-SNAPSHOT.jar"))

    // The following are only needed when we "vendor" the lsp4intellij JAR
    //implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.15.0")
    //implementation("com.vladsch.flexmark:flexmark:0.34.60")
    //implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
