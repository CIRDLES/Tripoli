import org.apache.tools.ant.filters.ReplaceTokens
import java.util.Date
import java.text.SimpleDateFormat



plugins {
    java
    `java-library`
    id("de.jjohannes.extra-java-module-info") version "0.11"
    id("common-build")
}


dependencies {
    implementation("com.github.cirdles:commons:bc38781605")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1") // group: 'org.apache.commons', name: 'commons-math3', version: '3.6.1'
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")  //group: 'org.apache.commons', name: 'commons-lang3', version: '3.12.0'

    // https://mvnrepository.com/artifact/com.zaxxer/SparseBitSet
    implementation("com.zaxxer:SparseBitSet:1.2") //group: 'com.zaxxer', name: 'SparseBitSet', version: '1.2'
    // https://mvnrepository.com/artifact/org.apache.poi/poi
    implementation("org.apache.poi:poi:5.2.2") //group: 'org.apache.poi', name: 'poi', version: '5.2.2'

    // https://mvnrepository.com/artifact/gov.nist.math/jama
    //implementation group: 'gov.nist.math', name: 'jama', version: '1.0.3'
    // modernized update: https://github.com/topobyte/jama
    implementation("com.github.topobyte:jama:master-SNAPSHOT")

    implementation("com.thoughtworks.xstream:xstream:1.4.19") //group: 'com.thoughtworks.xstream', name: 'xstream', version: '1.4.19'

    testImplementation("com.github.cirdles:commons:bc38781605")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

val timestamp = {
    SimpleDateFormat("dd MMMM yyyy").format(Date())
}

extraJavaModuleInfo {
    failOnMissingModuleInfo.set(false)
    automaticModule("commons-bc38781605.jar", "commons.bc38781605")
    automaticModule("org.apache.commons-math3-3.6.1.jar", "org.apache.commons-math3")
    automaticModule("org.apache.commons-lang3-3.12.0.jar", "org.apache.commons-lang3")
    automaticModule("com.zaxxer.SparseBitSet-1.2.jar", "com.zaxxer.SparseBitSet")
    automaticModule("jama-master-SNAPSHOT.jar", "jama")
}

tasks.processResources {
    val tokens = mapOf("pom.version" to version, "timestamp" to timestamp())
    inputs.properties(tokens)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].resources.srcDirs) {

        include("**/*.txt")
        //expand("pom.version" to version, "timestamp" to timestamp())
        filter<ReplaceTokens>("tokens" to tokens)


    }
}

