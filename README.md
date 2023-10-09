<img src="https://github.com/CIRDLES/cirdles.github.com/blob/master/assets/icons/TripoliJune2022.png" alt="Tripoli Logo" width="100">

Tripoli    
==========
We are developing Tripoli here - consider contributing to our project!
-----

[![Build Status](https://app.travis-ci.com/CIRDLES/Tripoli.svg?branch=main)](https://app.travis-ci.com/CIRDLES/Tripoli)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ba56cb4ee23948cc8a75a8b85f107fc5)](https://www.codacy.com/gh/CIRDLES/Tripoli/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=CIRDLES/Tripoli&amp;utm_campaign=Badge_Grade)

Tripoli is developed using Java 17 and JavaFX 17 and designed to run in the specialized open source Java Virtual
Machines (JDK/JRE) that include JavaFX **- denoted by "full" or "JDK-fx" -** and can be found:

[Liberica JDK/JRE 17 full](https://bell-sw.com/pages/downloads/#/java-17-lts%20/%20current) (select **full** from drop-down list)

[Azul JDK-fx 17](https://www.azul.com/downloads/?package=jdk-fx#download-openjdk)

Tripoli will not run correctly using previous versions (< 17) of the Java Virtual Machine.

Installation Instructions
------------

1)  Download the most recent Tripoli 'jar' file from [here](https://github.com/CIRDLES/Tripoli/releases).

2)  Download the JDK/JRE 17 for your operating system as a compressed archive and expand it anywhere you choose. If you
   want to make this version the default on your operating system, there are many online tutorials to follow. The Java
   executable is in the "bin" folder and is named "java"  for Mac and Linux, and "java.exe" for Windows. To run the
   Tripoli "jar" file, open a terminal window and paste in the path to the java executable, followed by a space
   character and the flag "-jar" followed by a space character and the path to the Tripoli "jar" file.
   
**NOTE: 9.March.2023.**  Tripoli is providing a large number of plots and may cause performance errors during rendering.  
    The fix is described [here](https://bell-sw.com/announcements/2022/04/26/insufficient-video-memory-causing-nullpointerexceptions-in-javafx-apps/).  Thus, when launching the '.jar' file as described below, add " -Dprism.maxvram=2G " between "java" and "-jar."

Windows example using Zulu from Azul assuming terminal is running in folder containing Tripoli "jar":

```text
C:\MYJAVA\zulu-17.jdk\Contents\Home\bin\java.exe -jar Tripoli-1.0.0.jar
```

Mac/Linux example using Zulu from Azul assuming terminal is running in folder containing Tripoli "jar":

```text
/Users/yourName/Documents/MYJAVA/zulu-17.jdk/Contents/Home/bin/java -jar Tripoli-1.0.0.jar
```

If you need to have a copy of Tripoli that runs on a specific OS and a Java JDK or JRE that does not include JavaFX, we
can provide one or provide instructions for you to build one from the source code.

### Note to Developers

Tripoli can be compiled from the source code by using [Gradle 7.4.2](https://gradle.org/releases/) using the same JDK/JRE 17.

```text
gradle clean build 
```

Tripoli "jar" file can then be built:

```text
gradle fatappjar
```

The resulting 'jar' file will be written to "Tripoli/tripoliApp/build/libs/"
