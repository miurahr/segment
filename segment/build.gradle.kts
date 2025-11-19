import com.github.spotbugs.snom.Confidence

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.spotbugs)
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
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

val xjc: Configuration by configurations.creating

dependencies {
    implementation(libs.commons.logging)
    implementation(libs.jaxb4.api)
    runtimeOnly(libs.jaxb4.runtime)

    "xjc"(libs.jaxb4.api)
    "xjc"(libs.jaxb4.runtime)
    "xjc"(libs.jaxb4.xjc)

    // Test
    testImplementation("junit:junit:4.13.1")
}

val generatedJaxbDir = layout.buildDirectory.dir("generated/xjc/main/java")

// Generate JAXB classes from srx20.xsd into package net.loomchild.segment.srx.io.bind
val generateJaxb by tasks.register("generateJaxb", JavaExec::class) {
    group = "code generation"
    description = "Generates JAXB classes from SRX schema using XJC"
    val outDir = generatedJaxbDir.get().asFile
    outputs.dir(outDir)
    classpath = configurations.getByName("xjc")
    mainClass.set("com.sun.tools.xjc.XJCFacade")
    args = listOf(
        "-d", outDir.absolutePath,
        "-p", "net.loomchild.segment.srx.io.bind",
        file("src/main/resources/net/loomchild/segment/res/xml/srx20.xsd").absolutePath
    )
}

sourceSets {
    main {
        java {
            srcDir(listOf(generateJaxb, "src/main/java"))
        }
    }
}

tasks.compileJava {
    dependsOn(generateJaxb)
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    setFailOnError(false)
    options {
        encoding = "UTF-8"
        jFlags("-Duser.language=en")
    }
    // Exclude generated JAXB package from Javadoc like in Maven config
    exclude("net/loomchild/segment/srx/io/bind/**")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("segment")
                description.set(project.description)
                url.set("https://github.com/miurahr/segment")

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
                    developer {
                        name.set("Hiroshi Miura")
                        email.set("miurahr@linux.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://git@github.com/miurahr/segment.git")
                    developerConnection.set("scm:git:ssh://git@github.com/miurahr/segment.git")
                    url.set("https://github.com/miurahr/segment")
                }
            }
        }
    }
}

tasks.withType(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "net.loomchild.segment")
    }
}

val signKey = listOf("signingKey", "signing.keyId", "signing.gnupg.keyName").find {project.hasProperty(it)}
tasks.withType<Sign> {
    onlyIf { signKey != null && !project.version.toString().endsWith("-SNAPSHOT") }
}
signing {
    when (signKey) {
        "signingKey" -> {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        "signing.keyId" -> {
            val keyId: String? by project
            val password: String? by project
            val secretKeyRingFile: String? by project // e.g. gpg --export-secret-keys > secring.gpg
            useInMemoryPgpKeys(keyId, password, secretKeyRingFile)
        }
        "signing.gnupg.keyName" -> {
            useGpgCmd()
        }
    }
    sign(publishing.publications["mavenJava"])
}

spotbugs {
    reportLevel = Confidence.valueOf("HIGH")
    tasks.spotbugsMain {
        reports.create("html") {
            required.set(true)
        }
    }
    tasks.spotbugsTest {
        enabled = false
    }
}
