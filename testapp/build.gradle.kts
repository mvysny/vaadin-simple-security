plugins {
    alias(libs.plugins.vaadin)
}

dependencies {
    implementation(project(":vaadin-simple-security"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}") {
        afterEvaluate {
            if (vaadin.productionMode.get()) {
                exclude(module = "vaadin-dev")
            }
        }
    }

    // Vaadin-Boot
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:12.2")

    // logging
    // currently we are logging through the SLF4J API to SLF4J-Simple. See src/main/resources/simplelogger.properties file for the logger configuration
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Fast Vaadin unit-testing with Karibu-Testing: https://github.com/mvysny/karibu-testing
    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v24:2.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
