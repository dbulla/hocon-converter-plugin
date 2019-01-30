group = "com.nurflugel"
version = "1.0-SNAPSHOT"


buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", "1.3.11")) }
}

//plugins {
//	id("org.jetbrains.intellij") version "0.2.18"
//	kotlin("jvm") version "1.2.30"
//kotlin("jvm") version "1.3.11"
//}


plugins {
    idea
    kotlin("jvm") version "1.3.11"
    id("org.jetbrains.intellij") version "0.4.2"
//    id("org.jetbrains.grammarkit") version "2018.2.2"
//    id("de.undercouch.download") version "3.4.3"
//    id("net.saliman.properties") version "1.4.6"
}


intellij {
    updateSinceUntilBuild = false
    instrumentCode = true
    version = "2017.3"
}