plugins{
    java
    `maven-publish`
}

val sourceCompatibility = "17"
val targetCompatibility = "17"

repositories {
    mavenCentral()

    maven { url = uri("https://jitpack.io") }

    maven { url = uri("https://plugins.gradle.org/m2/") }

    flatDir { dirs("libs") }
}

val mavenArtifactId = name
val mavenGroupId = "org.cirdles"
val mavenVersion = "0.0.2"

dependencies {
    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:23.0.0") //group: 'org.jetbrains', name: 'annotations', version: '23.0.0'

}



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




publishing {
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
            version = mavenVersion
            group = mavenGroupId

      }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}


//publishing {
//    publications {
//        (MavenPublication) {
//            groupId = mavenGroupId
//            artifactId = mavenArtifactId
//            version = mavenVersion
//
//            from components.java
//
//                    pom {
//                        licenses {
//                            license {
//                                name = 'The Apache License, Version 2.0'
//                                url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
//                            }
//                        }
//                    }
//        }
//    }
//}

//tasks.create( "Creates the source folders if they do not exist.") doLast {
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//    sourceSets*.allSource*.srcDirs*.each { File srcDir ->
//        if (!srcDir.isDirectory()) {
//            println "Creating source folder: ${srcDir}"
//            srcDir.mkdirs()
//        }
//    }
//}