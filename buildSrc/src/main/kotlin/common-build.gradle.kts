/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    java
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()

    maven { url = uri("https://jitpack.io") }

    maven { url = uri("https://plugins.gradle.org/m2/") }

    flatDir { dirs("libs") }
}

dependencies {
    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:23.0.0") //group: 'org.jetbrains', name: 'annotations', version: '23.0.0'

    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    // https://mvnrepository.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava:guava:31.1-jre")
}

val mavenArtifactId = name
val mavenGroupId = "org.cirdles"
// preserve double quotes in mavenVersion as Tripoli uses regex based on them
val mavenVersion = "0.3.5"//10 Oct 2023

object Versions {
    const val junitVersion = "5.8.2"
    // Creates desired junit version that can be called in all subprojects
}

group = mavenGroupId
version = mavenVersion
val utf8 = "UTF-8"

tasks {
    compileJava {
        options.encoding = utf8
    }

    compileTestJava {
        options.encoding = utf8
    }

    val packageJavadoc by creating(Jar::class) {
        from(javadoc)
        archiveClassifier.set("javadoc")
        dependsOn(javadoc)
        // currently not called unless uncommented in artifacts
    }

    val sourcesJar by creating(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        dependsOn(classes)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        archives(jar)
        archives(sourcesJar)
        //  Uncomment next line to produce javadocs
        //archives(packageJavadoc)
    }
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = utf8
    println("Compiler args: " + options.compilerArgs)
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.withType(JavaCompile::class) {
    options.encoding = utf8
}

tasks.register("CreateFolder") {
    doLast {
        sourceSets["main"].allSource.srcDirs.forEach { srcDir: File ->
            if (!srcDir.isDirectory) {
                println("Create source folder: $srcDir")
                srcDir.mkdirs()
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = mavenArtifactId

            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}