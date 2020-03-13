import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    id("net.researchgate.release") version "2.8.0"
    id("com.jfrog.bintray") version "1.8.4" apply false
    id("com.github.kt3k.coveralls") version "2.8.4" apply false
}

// some parts of the Kotlin DSL don't work inside a `subprojects` block yet, so we do them the old way
// (without typesafe accessors)

subprojects {
    // used in per-subproject dependencies
    @Suppress("UNUSED_VARIABLE") val deps by extra {
        mapOf(
                "jupiter" to "5.5.2",
                "jackson" to "2.10.0",
                "guava"  to "28.1-jre",
                "roaringbitmap" to "0.8.13",
                "commons-collections" to "4.2"
        )
    }

    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.kt3k.coveralls")

    repositories {
        jcenter()
    }

    tasks.withType<JavaCompile> {
        options.isDeprecation = true
        options.isWarnings = true
        if (JavaVersion.current().isJava9Compatible) {
          options.compilerArgs = listOf("--release", "11")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        group = "io.github.richardstartin"
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            // used by coveralls
            xml.isEnabled = true
        }
    }
}

subprojects.filter { listOf("multi-matcher-core").contains(it.name) }.forEach { project ->
    project.run {
        apply(plugin = "maven-publish")
        apply(plugin = "com.jfrog.bintray")

        tasks {
            register<Jar>("sourceJar") {
                from(project.the<SourceSetContainer>()["main"].allJava)
                archiveClassifier.set("sources")
            }

            register<Jar>("docJar") {
                from(project.tasks["javadoc"])
                archiveClassifier.set("javadoc")
            }
        }

        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("bintray") {
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])
                    artifact(tasks["sourceJar"])
                    artifact(tasks["docJar"])

                    // requirements for maven central
                    // https://central.sonatype.org/pages/requirements.html
                    pom {
                        name.set("${project.group}:${project.name}")
                        description.set("Fast classification/tagging of objects.")
                        url.set("https://github.com/richardstartin/multi-matcher")
                        issueManagement {
                            system.set("GitHub Issue Tracking")
                            url.set("https://github.com/richardstartin/multi-matcher/issues")
                        }
                        licenses {
                            license {
                                name.set("Apache 2")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("richardstartin")
                                name.set("Richard Startin")
                                email.set("richard@openkappa.co.uk")
                                url.set("https://richardstartin.github.io/multi-matcher")
                                roles.addAll("architect", "developer", "maintainer")
                                timezone.set("0")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/richardstartin/multi-matcher.git")
                            developerConnection.set("scm:git:https://github.com/richardstartin/multi-matcher.git")
                            url.set("https://github.com/richardstartin/multi-matcher")
                        }
                    }
                }
            }
        }

        configure<BintrayExtension> {
            user = rootProject.findProperty("bintrayUser")?.toString()
            key = rootProject.findProperty("bintrayApiKey")?.toString()
            setPublications("bintray")

            with(pkg) {
                repo = "maven"
                setLicenses("Apache-2.0")
                vcsUrl = "https://github.com/richardstartin/multi-matcher"
                // use "bintray package per artifact" to match the auto-gen'd pkg structure inherited from
                // Maven Central's artifacts
                name = "uk.co.openkappa:${project.name}"
                userOrg = "multi-matcher"

                with(version) {
                    name = project.version.toString()
                    released = java.util.Date().toString()
                    vcsTag = "multi-matcher-${project.version}"
                }
            }
        }
    }
}

tasks {
    create("build") {
        // dummy build task to appease release plugin
    }
}

release {
    tagTemplate = "multi-matcher-\$version"
}

tasks.afterReleaseBuild {
    dependsOn(tasks.named("bintrayUpload"))
}
