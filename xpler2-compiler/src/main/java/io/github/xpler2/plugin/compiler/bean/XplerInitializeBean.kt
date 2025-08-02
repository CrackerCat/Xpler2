package io.github.xpler2.plugin.compiler.bean

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class XplerInitializeBean(
    val name: String,
    val description: String,
    val scope: List<String>,
    @EncodeDefault val xposed: Boolean = true,
    @EncodeDefault val xposedVersion: Int = 82,
    @EncodeDefault val lsposed: Boolean = true,
    @EncodeDefault val lsposedTargetVersion: Int = 100,
    @EncodeDefault val lsposedMinVersion: Int = 100,
    @EncodeDefault val lsposedStaticScope: Boolean = true,
    @EncodeDefault val lsposedCompatAnnotation: Boolean = true,
    @EncodeDefault val xposedInit: String = "$name\$X${Random.nextInt(0, 999)}",
    @EncodeDefault val lsposedInit: String = "$name\$L${Random.nextInt(0, 999)}",
)