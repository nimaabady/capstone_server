pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.24"
    }
}
rootProject.name = "server"

include(":auth")
include(":security")
include(":messaging")
include(":friends")
include(":gateway")