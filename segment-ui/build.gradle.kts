plugins {
    application
    java
}

application {
    mainClass = "net.loomchild.segment.ui.console.Segment"
    applicationName = "segment"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":segment"))
    implementation(libs.commons.cli)
    implementation(libs.commons.logging)
}

description = "The segment CLI command."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
