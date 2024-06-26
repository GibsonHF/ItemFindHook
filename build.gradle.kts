plugins {
    id("java")
    `maven-publish`
}

group = "net.botwithus.debug"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        setUrl("https://nexus.botwithus.net/repository/maven-snapshots/")

    }
    maven {
        setUrl("https://jitpack.io")
    }

}

tasks.withType<JavaCompile> {
    sourceCompatibility = "20"
    targetCompatibility = "20"
    options.compilerArgs.add("--enable-preview")
}

val copyJar by tasks.register<Copy>("copyJar") {
    from("build/libs/")
    into("${System.getProperty("user.home")}\\BotWithUs\\scripts\\local\\")
    include("*.jar")
    outputs.upToDateWhen { false }

}

configurations {
    create("includeInJar") {
        this.isTransitive = false
    }
}

tasks.named<Jar>("jar") {
    from({
        configurations["includeInJar"].map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    finalizedBy(copyJar)
}

dependencies {
    implementation("net.botwithus.rs3:botwithus-api:1.0.0-20240404.003355-32")
    implementation("net.botwithus.xapi.public:api:1.0.0-20240211.205614-15")
    "includeInJar"("net.botwithus.xapi.public:api:1.0.0-20240211.205614-15")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}
java {
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
