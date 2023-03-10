plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
    java
    `java-library`
    signing
}

defaultTasks("clean", "build")

group = "com.github.mvysny.vaadin-simple-security"
version = "0.3-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Java has no nullable types
    api("org.jetbrains:annotations:23.1.0")

    // vaadin
    compileOnly("com.vaadin:vaadin-core:${properties["vaadin_version"]}")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    // tests
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}")
    testImplementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:${properties["karibu_testing_version"]}")
    // remember this is a Java project :) Kotlin only for tests
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Javadoc> {
    isFailOnError = false
}

publishing {
    repositories {
        maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.properties["ossrhUsername"] as String? ?: "Unknown user"
                password = project.properties["ossrhPassword"] as String? ?: "Unknown user"
            }
        }
    }
    publications {
        create("mavenJava", MavenPublication::class.java).apply {
            groupId = project.group.toString()
            this.artifactId = "vaadin-simple-security"
            version = project.version.toString()
            pom {
                description.set("Very simple security framework for Vaadin")
                name.set("Vaadin-Simple-Security")
                url.set("https://github.com/mvysny/vaadin-simple-security")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("mavi")
                        name.set("Martin Vysny")
                        email.set("martin@vysny.me")
                    }
                }
                scm {
                    url.set("https://github.com/mvysny/vaadin-simple-security")
                }
            }
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exceptions of failed tests in Travis-CI console.
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
