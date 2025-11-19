plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.commons.math3)
    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

group = "tokyo.northside"
version = "2.0.5-SNAPSHOT"
description = "Library used to split text into segments."

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

configurations {
    create("xjc")
}

dependencies {
    api("commons-logging:commons-logging:1.2")

    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.3")

    // XJC tool for schema code generation (Gradle replacement for maven-jaxb2-plugin)
    "xjc"("com.sun.xml.bind:jaxb-xjc:2.3.3")

    // Test
    testImplementation("junit:junit:4.13.1")
}

val generatedJaxbDir = layout.buildDirectory.dir("generated/sources/xjc/java")

// Generate JAXB classes from srx20.xsd into package net.loomchild.segment.srx.io.bind
val generateJaxb by tasks.registering(JavaExec::class) {
    group = "code generation"
    description = "Generates JAXB classes from SRX schema using XJC"
    val outDir = generatedJaxbDir.get().asFile
    outputs.dir(outDir)
    classpath = configurations.getByName("xjc")
    mainClass.set("com.sun.tools.xjc.Driver")
    args = listOf(
        "-d", outDir.absolutePath,
        "-p", "net.loomchild.segment.srx.io.bind",
        // Match Maven's include: net/loomchild/segment/res/xml/srx20.xsd
        file("src/main/resources/net/loomchild/segment/res/xml/srx20.xsd").absolutePath
    )
}

sourceSets {
    main {
        java.srcDir(generatedJaxbDir)
    }
}

// Ensure code is generated before compilation
tasks.compileJava {
    dependsOn(generateJaxb)
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    // Exclude generated JAXB package from Javadoc like in Maven config
    exclude("net/loomchild/segment/srx/io/bind/**")
}

// Create a tests jar similar to Maven maven-jar-plugin test-jar goal
val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(testJar)

            pom {
                name.set("segment")
                description.set(project.description)
                url.set("https://github.com/loomchild/segment")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }

                developers {
                    developer {
                        name.set("Jarek Lipski")
                        email.set("pub@loomchild.net")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://git@github.com/loomchild/segment.git")
                    developerConnection.set("scm:git:ssh://git@github.com/loomchild/segment.git")
                    url.set("https://github.com/loomchild/segment")
                }
            }
        }
    }

    repositories {
        // Mirror Maven distributionManagement
        maven {
            name = "OSSRH"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://oss.sonatype.org/content/repositories/snapshots"
                else
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                // Expected to be provided via gradle.properties or environment variables
                // ossrhUsername / ossrhPassword are common variable names
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
