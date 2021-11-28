plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.8.2"
}

group = "cn.sincky.mirai"
version = "2.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation("com.rometools:rome:1.16.0")
    implementation("org.xerial:sqlite-jdbc:3.36.0.2")
    compileOnly("net.mamoe.yamlkt:yamlkt:+")
}

