pluginManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/comnewrelic-3506")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/comnewrelic-3506")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "NewRelicTest"
include(":app")
