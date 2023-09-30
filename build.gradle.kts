import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.google.protobuf") version "0.9.4"
    id("com.github.ben-manes.versions") version "0.48.0"
}

group = "bagguley.kotlingrpc"
version = "1.0-SNAPSHOT"

val grpcVersion = "1.58.0"
val protobufVersion = "3.24.3"
val kotlinGrpcVersion = "1.4.0"
val coroutinesVersion = "1.7.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${coroutinesVersion}")
    implementation("com.google.protobuf:protobuf-kotlin:${protobufVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-kotlin-stub:${kotlinGrpcVersion}")

    runtimeOnly("io.grpc:grpc-netty:${grpcVersion}")

    testImplementation(kotlin("test", "1.8.10"))
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        kotlin {
            srcDir(project.layout.buildDirectory.dir("generated/source/proto/main/grpckt"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.register<JavaExec>("server") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("bagguley.kotlingrpc.CountServerKt")
}

tasks.register<JavaExec>("client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("bagguley.kotlingrpc.CountClientKt")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${kotlinGrpcVersion}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach{
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}
