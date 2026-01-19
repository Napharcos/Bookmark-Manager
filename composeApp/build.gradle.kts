plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            kotlin.srcDir("src/jsMain/kotlin")
            resources.srcDir("src/jsMain/resources")

            dependencies {
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(libs.serialization.json)
                implementation(libs.indexeddb)
            }
        }
    }
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

aboutLibraries {
    export {
        outputFile = file("src/jsMain/resources/aboutlibraries.json")
    }
}