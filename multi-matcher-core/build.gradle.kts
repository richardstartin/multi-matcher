val deps: Map<String, String> by extra

dependencies {
    implementation("org.roaringbitmap:RoaringBitmap:${deps["roaringbitmap"]}")
    implementation("org.apache.commons:commons-collections4:${deps["commons-collections"]}")
    implementation("it.unimi.dsi:fastutil:${deps["fastutil"]}")
    testImplementation("com.google.guava:guava:${deps["guava"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${deps["jupiter"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${deps["jupiter"]}")
    testCompile("org.junit.jupiter:junit-jupiter-params:${deps["jupiter"]}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${deps["jackson"]}")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:${deps["jackson"]}")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${deps["jackson"]}")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${deps["jackson"]}")


}


tasks.test {
    useJUnitPlatform()
    failFast = true
    maxParallelForks = 8
}