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
import org.apache.tools.ant.filters.ReplaceTokens
import java.text.SimpleDateFormat
import java.util.*


plugins {
    java
    `java-library`
    id("de.jjohannes.extra-java-module-info") version "0.14"
    id("common-build") // Plugin calls common gradle build from buildSrc
    antlr
}

dependencies {
    implementation("com.github.cirdles:commons:bc38781605")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1") // group: 'org.apache.commons', name: 'commons-math3', version: '3.6.1'

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")  //group: 'org.apache.commons', name: 'commons-lang3', version: '3.12.0'
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.20.0")
    // https://mvnrepository.com/artifact/com.zaxxer/SparseBitSet
    implementation("com.zaxxer:SparseBitSet:1.2") //group: 'com.zaxxer', name: 'SparseBitSet', version: '1.2'

    // https://mvnrepository.com/artifact/net.sourceforge.jexcelapi/jxl
    implementation("net.sourceforge.jexcelapi:jxl:2.6.12")

    // modernized update: https://github.com/topobyte/jama
    implementation("com.github.topobyte:jama:master-SNAPSHOT")

    // https://github.com/optimatika/ojAlgo/wiki/Optimisation-Modelling-Advice
    implementation("org.ojalgo:ojalgo:52.0.1")

    // https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.2")

    // https://mvnrepository.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    // https://mvnrepository.com/artifact/org.jblas/jblas
    implementation("org.jblas:jblas:1.2.5")

    // https://mvnrepository.com/artifact/com.thoughtworks.xstream/xstream
    implementation("com.thoughtworks.xstream:xstream:1.4.21")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-rng-core
    implementation("org.apache.commons:commons-rng-core:1.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-rng-simple
    implementation("org.apache.commons:commons-rng-simple:1.6")

    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")


    testImplementation("com.github.cirdles:commons:bc38781605")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener")
    outputDirectory = file("build/generated-src/antlr/main")
}

sourceSets["main"].java.srcDirs("build/generated-src/antlr/main")


tasks.test {
    useJUnitPlatform()
}

val timestamp = {
    SimpleDateFormat("dd MMMM yyyy").format(Date())
}
repositories {
    mavenCentral()
}

extraJavaModuleInfo {
    failOnMissingModuleInfo.set(false)
    automaticModule("commons-bc38781605.jar", "commons.bc38781605")
    // https://github.com/gradle/gradle/issues/12630
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
    automaticModule("org.apache.commons:commons-lang3", "commons.lang3")
    automaticModule("org.apache.commons:commons-rng-core", "commons.rng.core")
    automaticModule("org.apache.commons:commons-rng-simple", "commons.rng.simple")

    automaticModule("net.sourceforge.jexcelapi:jxl", "jxl")//implementation("net.sourceforge.jexcelapi:jxl:2.6.12")


    automaticModule("com.zaxxer:SparseBitSet", "SparseBitSet")
    automaticModule("jama-master-SNAPSHOT.jar", "jama")
    automaticModule("org.ojalgo:ojalgo", "ojalgo")

    automaticModule("org.jblas:jblas", "jblas")

    automaticModule("com.thoughtworks.xstream:xstream", "xstream")//javax.xml.bind:jaxb-api:2.3.1
}


tasks.processResources {
    val tokens = mapOf("pom.version" to version, "timestamp" to timestamp())
    inputs.properties(tokens)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].resources.srcDirs) {
        include("**/*.txt")
        filter<ReplaceTokens>("tokens" to tokens)
    }
}