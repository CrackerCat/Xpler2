package io.github.xpler2.plugin.compiler.config

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import org.gradle.api.file.Directory
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Build Xposed project configuration
 */
object XposedConfig {
    fun init(outputDirectory: Directory, config: XplerInitializeBean) {
        generateXposedConfig(outputDirectory, config)
    }

    private fun generateXposedConfig(outputDirectory: Directory, config: XplerInitializeBean) {
        val configAar = outputDirectory.asFile.resolve("core/xposed-config.aar")
            .also { it.parentFile.mkdirs() }

        if (!config.xposed) {
            configAar.delete() // if it's not an Xposed project, delete the profile
            return
        }

        val zos = ZipOutputStream(configAar.outputStream())

        // write out AndroidManifest.xml
        val (androidManifestName, androidManifestValue) = generateAndroidManifestXml(config)
        zos.putNextEntry(ZipEntry(androidManifestName))
        zos.write(androidManifestValue.toByteArray())
        zos.closeEntry()

        // write out assets/xposed_init
        val (assetsXposedInitName, assetsXposedInitValue) = generateAssetsXposedInit(config)
        zos.putNextEntry(ZipEntry(assetsXposedInitName))
        zos.write(assetsXposedInitValue.toByteArray())
        zos.closeEntry()

        // write out res/values/values.xml
        val (resValuesName, resValuesValue) = generateResValues(config)
        zos.putNextEntry(ZipEntry(resValuesName))
        zos.write(resValuesValue.toByteArray())
        zos.closeEntry()

        // write out R.txt
        val (rName, rValue) = generateR(config)
        zos.putNextEntry(ZipEntry(rName))
        zos.write(rValue.toByteArray())
        zos.closeEntry()

        zos.close()
    }

    private fun generateAndroidManifestXml(config: XplerInitializeBean): Pair<String, String> {
        val name = "AndroidManifest.xml"
        val value = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"io.github.xpler2.xposed\" >\n" +
                "\n" +
                "    <application>\n" +
                "        <meta-data\n" +
                "            android:name=\"xposedmodule\"\n" +
                "            android:value=\"true\" />\n" +
                "        <meta-data\n" +
                "            android:name=\"xposedscope\"\n" +
                "            android:resource=\"@array/xposed_scope\" />\n" +
                "        <meta-data\n" +
                "            android:name=\"xposeddescription\"\n" +
                "            android:value=\"@string/xposed_description\" />\n" +
                "        <meta-data\n" +
                "            android:name=\"xposedminversion\"\n" +
                "            android:value=\"${config.xposedVersion}\" />\n" +
                "    </application>\n" +
                "\n" +
                "</manifest>"

        return name to value
    }

    private fun generateAssetsXposedInit(config: XplerInitializeBean): Pair<String, String> {
        val name = "assets/xposed_init"
        val value = config.innerXposedInit

        return name to value
    }

    private fun generateResValues(config: XplerInitializeBean): Pair<String, String> {
        val name = "res/values/values.xml"
        val value = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <string-array name=\"xposed_scope\">\n" +
                "${config.scope.joinToString("\n") { "        <item>$it</item>" }}\n" +
                "    </string-array>\n" +
                "    <string name=\"xposed_description\">${config.description}</string>\n" +
                "</resources>"

        return name to value
    }

    private fun generateR(config: XplerInitializeBean): Pair<String, String> {
        val name = "R.txt"
        val value = "int array xposed_scope 0x0\n" +
                "int string xposed_description 0x0\n"

        return name to value
    }
}