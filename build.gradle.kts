import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.addCompilerArgs
import xyz.srnyx.gradlegalaxy.utility.setupJava


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "1.1.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

setupJava("xyz.srnyx", "1.0.0", "A Discord bot for the festivities of Halloween!")
repository(Repository.MAVEN_CENTRAL, Repository.JITPACK)
addCompilerArgs("-parameters")
application.mainClass.set("xyz.srnyx.candychance.CandyChance")

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.16")
    implementation("xyz.srnyx", "lazy-library", "4cb4ceb95f")
}

// Fix some tasks
tasks {
    distZip {
        dependsOn("shadowJar")
    }
    distTar {
        dependsOn("shadowJar")
    }
    startScripts {
        dependsOn("shadowJar")
    }
    startShadowScripts {
        dependsOn("jar")
    }
}
