plugins {
    application
    id("common-build")
}
val mainClassName = "org.cirdles.tripoli.gui.TripoliGUI"

application {
    mainClass.set("org.cirdles.tripoli.gui.TripoliGUI")
}






dependencies {
    implementation(project(":TripoliCore"))
    implementation("org.jetbrains:annotations:22.0.0")
    val junitVersion = "5.8.2"


    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}


project(":TripoliApp") {
    description = "Tripoli GUI"

}

tasks.create("fatAppJar", Jar::class){
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest.attributes.apply {
        put("Main-Class", mainClassName)
    }

    from( configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } )
    with(tasks.jar.get())

}