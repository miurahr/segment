plugins {
    application
    java
}

application {
    mainClass = "net.loomchild.segment.ui.console.Segment"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":segment"))
    implementation(libs.commons.cli)
    implementation(libs.commons.logging)
    implementation(libs.junit)
}

description = "segment-ui"

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
