plugins{
    java
    `maven-publish`
}





java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
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

}


val mavenArtifactId = name
val mavenGroupId = "org.cirdles"
val mavenVersion = "0.0.2"

group = mavenGroupId
version = mavenVersion



tasks {

    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }




//    val packageJavadoc by tasks.registering(Jar::class){
//        from(javadoc)
//        archiveClassifier.set("javadoc")
//        dependsOn(javadoc)
//
//    }

    val sourcesJar by creating(Jar::class){
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        dependsOn(classes)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }


    artifacts {
        archives(jar)
        archives(sourcesJar)
        //  Uncomment next line to produce javadocs
        //    archives packageJavadoc
    }

}

tasks.withType(JavaCompile::class) {

    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = "UTF-8"
    println("Compiler args: " + options.compilerArgs)

}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.register("CreateFolder") {
    doLast{
        //duplicatesStrategy = DuplicatesStrategy.INCLUDE
        sourceSets["main"].allSource.srcDirs.forEach{ srcDir: File ->
            if (!srcDir.isDirectory()){
                println("Create source folder: $srcDir")
                srcDir.mkdirs()
            }
        }
    }

}



publishing {
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")

      }
    }

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




