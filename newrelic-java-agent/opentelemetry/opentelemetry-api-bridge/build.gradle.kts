buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.gradleup.shadow:shadow-gradle-plugin:9.2.1")
    }
}

plugins {
    id("java")
    id("com.gradleup.shadow") version("9.2.1")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // OpenTelemetry dependencies for dimensional metrics
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.54.1"))
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-exporter-logging")

    // New Relic Java agent API dependency
    implementation("com.newrelic.agent.java:newrelic-api:9.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.example.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}
