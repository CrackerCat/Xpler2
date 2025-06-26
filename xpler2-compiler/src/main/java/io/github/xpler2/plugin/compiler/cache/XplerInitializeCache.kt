package io.github.xpler2.plugin.compiler.cache

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class XplerInitializeCache(
    val sourcePath: String,
    val initializeBean: XplerInitializeBean,
) {
    companion object {
        const val NAME = "xpler_initialize_cache.json"

        fun cache(outputDir: String): XplerInitializeCache {
            val cacheFile = File(outputDir, "cache").resolve(NAME)
            return Json.decodeFromString(cacheFile.readText())
        }
    }

    fun into(outputDir: String) {
        val cacheDir = File(outputDir, "cache").apply { mkdirs() }
        cacheDir.resolve(NAME).writeText(Json.encodeToString(this))
    }
}