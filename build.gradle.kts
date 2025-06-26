import java.util.Properties


// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidDynamicFeature) apply false
    alias(libs.plugins.androidLibrary) apply false
}


private fun getEnvOrProperty(key: String): String {
    return System.getenv(key) ?: File(rootDir, "local.properties").takeIf { it.exists() }?.inputStream()?.use {
        Properties().apply { load(it) }.getProperty(key)
    }.orEmpty()
}

private val baseUrlApi = getEnvOrProperty("BASE_URL_API")

extensions.extraProperties["BASE_URL_API"] = baseUrlApi