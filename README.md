<img src="https://github.com/CIRDLES/cirdles.github.com/blob/master/assets/icons/TripoliJune2022.png" alt="Tripoli Logo" width="100">

[![Build Status](https://app.travis-ci.com/CIRDLES/Tripoli.svg?branch=main)](https://app.travis-ci.com/CIRDLES/Tripoli)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ba56cb4ee23948cc8a75a8b85f107fc5)](https://www.codacy.com/gh/CIRDLES/Tripoli/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=CIRDLES/Tripoli&amp;utm_campaign=Badge_Grade)

Tripoli    
==========
We are developing Tripoli here - consider contributing to our project!
-----

User Guide
==========

Starting June 2024, **Tripoli User Guide** can be found [here](https://noahmclean.github.io/TripoliDocs/).

---

**NOTE: 9.March.2023.**  Tripoli is providing a large number of plots and may cause performance errors during rendering.  
    The fix is described [here](https://bell-sw.com/announcements/2022/04/26/insufficient-video-memory-causing-nullpointerexceptions-in-javafx-apps/).  Thus, when launching the '.jar' file as described below, add " -Dprism.maxvram=2G " between "java" and "-jar."


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
