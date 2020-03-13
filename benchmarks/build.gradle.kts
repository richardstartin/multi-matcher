import java.net.URI

plugins {
    id("me.champeau.gradle.jmh") version "0.4.8"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

val deps: Map<String, String> by extra

repositories {
    mavenCentral()
}

dependencies {


    listOf(
            project(":multi-matcher-core"),
            "org.openjdk.jol:jol-core:0.10"
    ).forEach {
        jmh(it)
        testRuntime(it)
    }
}

jmh {
    jmhVersion = "1.23"
    // tests depend on jmh, not the other way around
    isIncludeTests = false
    warmupIterations = 5
    iterations = 5
    fork = 1
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}


// jmhJar task provided by jmh gradle plugin is currently broken
// https://github.com/melix/jmh-gradle-plugin/issues/97
// so instead, we configure the shadowJar task to have JMH bits in it
tasks.shadowJar {
    archiveBaseName.set("benchmarks")
    archiveVersion.set("")
    archiveClassifier.set("")

    manifest {
        attributes(Pair("Main-Class", "org.openjdk.jmh.Main"))
        attributes(Pair("Multi-Release", "true"))
    }

    // include dependencies
    configurations.add(project.configurations.jmh.get())
    // include benchmark classes
    from(project.sourceSets.jmh.get().output)
    // include generated java source, BenchmarkList and other JMH resources
    from(tasks.jmhRunBytecodeGenerator.get().outputs)
    // include compiled generated classes
    from(tasks.jmhCompileGeneratedClasses.get().outputs)

    dependsOn(tasks.jmhCompileGeneratedClasses)
}
