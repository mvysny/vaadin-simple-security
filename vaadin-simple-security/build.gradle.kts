plugins {
    `java-library`
}

dependencies {
    // Java has no nullable types
    api(libs.jetbrains.annotations)

    // vaadin
    compileOnly(libs.vaadin.core)
    compileOnly(libs.bundles.jakarta)

    // tests
    testImplementation(libs.dynatest)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.vaadin.core)
    testImplementation(libs.bundles.jakarta)
    testImplementation(libs.kaributesting)
    // remember this is a Java project :) Kotlin only for tests
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation(libs.slf4j.simple)
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-simple-security")
