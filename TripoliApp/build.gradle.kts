import Common_build_gradle.Versions.junitVersion

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
    application
    id("de.jjohannes.extra-java-module-info") version "0.14"
    id("common-build") // Plugin calls common gradle build from buildSrc
}
val mainClassName = "org.cirdles.tripoli.gui.TripoliGUI"

application {
    mainClass.set("org.cirdles.tripoli.gui.TripoliGUI")
}

dependencies {
    implementation(project(":TripoliCore"))
    implementation("org.jetbrains:annotations:22.0.0")

    implementation("com.github.cirdles:commons:bc38781605")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    //implementation("org.ojalgo:ojalgo:51.4.0")

    implementation("eu.hansolo.fx:charts:17.1.13")

}

extraJavaModuleInfo {
    failOnMissingModuleInfo.set(false)
    automaticModule("commons-bc38781605.jar", "commons.bc38781605")
    // automaticModule("org.ojalgo:ojalgo", "ojalgo")
}

project(":TripoliApp") {
    description = "Tripoli GUI"
    base.archivesName.set("Tripoli")

}

tasks.create("fatAppJar", Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest.attributes.apply {
        put("Main-Class", mainClassName)
    }

    doFirst { from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) }
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    with(tasks.jar.get())

}