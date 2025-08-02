package io.github.xpler2.plugin.compiler.config

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import org.gradle.api.file.Directory
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Build the LSposed project configuration
 */
object LsposedConfig {
    fun init(coreDirectory: Directory, config: XplerInitializeBean) {
        generateLsposedConfig(coreDirectory, config)
    }

    private fun generateLsposedConfig(coreDirectory: Directory, config: XplerInitializeBean) {
        val configAar = coreDirectory.asFile.resolve("lsposed-config.aar")
            .also { it.parentFile.mkdirs() }

        if (!config.lsposed) {
            configAar.delete() // if it is not an LSposed project, then delete the configuration file
            return
        }

        val zos = ZipOutputStream(configAar.outputStream())

        // write out AndroidManifest.xml
        val (androidManifestName, androidManifestValue) = generateAndroidManifestXml(config)
        zos.putNextEntry(ZipEntry(androidManifestName))
        zos.write(androidManifestValue.toByteArray())
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

        val classesJar = ByteArrayOutputStream()
        val classesZos = ZipOutputStream(classesJar)

        // write out META-INF/xposed/java_init.list
        val (metaInfJavaInitName, metaInfJavaInitValue) = generateMetaInfJavaInit(config)
        classesZos.putNextEntry(ZipEntry(metaInfJavaInitName))
        classesZos.write(metaInfJavaInitValue.toByteArray())
        classesZos.closeEntry()

        // write out META-INF/xposed/module.prop
        val (metaInfModulePropName, metaInfModulePropValue) = generateMetaInfModuleProp(config)
        classesZos.putNextEntry(ZipEntry(metaInfModulePropName))
        classesZos.write(metaInfModulePropValue.toByteArray())
        classesZos.closeEntry()

        // write out META-INF/xposed/scope.list
        val (metaInfScopeListName, metaInfScopeListValue) = generateMetaInfScopeList(config)
        classesZos.putNextEntry(ZipEntry(metaInfScopeListName))
        classesZos.write(metaInfScopeListValue.toByteArray())
        classesZos.closeEntry()

        classesZos.close()

        // write out classes.jar
        zos.putNextEntry(ZipEntry("classes.jar"))
        zos.write(classesJar.toByteArray())
        zos.closeEntry()

        zos.close()
    }

    private fun generateAndroidManifestXml(config: XplerInitializeBean): Pair<String, String> {
        val name = "AndroidManifest.xml"
        val value = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"io.github.xpler2.lsposed\" >\n" +
                "\n" +
                "    <application android:description=\"@string/lsposed_description\" >\n" +
                "    </application>\n" +
                "\n" +
                "</manifest>"

        return name to value
    }

    private fun generateMetaInfJavaInit(config: XplerInitializeBean): Pair<String, String> {
        val name = "META-INF/xposed/java_init.list"
        val value = config.lsposedInit

        return name to value
    }

    private fun generateMetaInfModuleProp(config: XplerInitializeBean): Pair<String, String> {
        val name = "META-INF/xposed/module.prop"
        val value = "minApiVersion=${config.lsposedMinVersion}\n" +
                "targetApiVersion=${config.lsposedTargetVersion}\n" +
                "staticScope=${config.lsposedStaticScope}"

        return name to value
    }

    private fun generateMetaInfScopeList(config: XplerInitializeBean): Pair<String, String> {
        val name = "META-INF/xposed/scope.list"
        val value = config.scope.joinToString("\n")

        return name to value
    }

    private fun generateResValues(config: XplerInitializeBean): Pair<String, String> {
        val name = "res/values/values.xml"
        val value = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <string name=\"lsposed_description\">${config.description}</string>\n" +
                "</resources>"

        return name to value
    }

    private fun generateR(config: XplerInitializeBean): Pair<String, String> {
        val name = "R.txt"
        val value = "int string lsposed_description 0x0\n"

        return name to value
    }
}