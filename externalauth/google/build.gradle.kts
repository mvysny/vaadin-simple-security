plugins {
    `java-library`
}

dependencies {
    api(project(":vaadin-simple-security"))
    implementation(libs.google.api.client)

    // vaadin
    compileOnly(libs.vaadin.core)
    compileOnly(libs.bundles.jakarta)

    // tests
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kaributesting)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.vaadin.core)
    testImplementation(libs.bundles.jakarta)
    testImplementation(libs.slf4j.simple)
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("externalauth-google")
