plugins {
    `java-library`
}

dependencies {
    // Java has no nullable types
    api("org.jetbrains:annotations:24.0.1")

    // vaadin
    compileOnly("com.vaadin:vaadin-core:${properties["vaadin_version"]}")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // tests
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}")
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    testImplementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v24:${properties["karibu_testing_version"]}")
    // remember this is a Java project :) Kotlin only for tests
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-simple-security")
