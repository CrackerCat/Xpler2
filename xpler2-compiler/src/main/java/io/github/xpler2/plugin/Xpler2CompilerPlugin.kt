package io.github.xpler2.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.github.xpler2.plugin.asm.Xpler2AsmVisitorFactory
import io.github.xpler2.plugin.compiler.task.Xpler2CompilerTask
import org.gradle.api.Plugin
import org.gradle.api.Project

// `.aar` file structure reference: https://developer.android.com/studio/projects/android-library?hl=zh-cn#aar-contents
class Xpler2CompilerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(AppPlugin::class.java))
            throw RuntimeException("`xpler2-compiler` is only allowed to be applied in the Application module.")

        // compiler output directory
        val compilerOutputDirectory = target.layout
            .buildDirectory
            .dir("generated/xpler2")
            .get()

        // compiler core directory
        val coreDirectory = compilerOutputDirectory
            .dir("core")
            .also { it.asFile.mkdirs() }

        // compiler cache directory
        val cacheDirectory = compilerOutputDirectory
            .dir("cache")
            .also { it.asFile.mkdirs() }

        // debug directory
        val intermediatesDirectory = target.layout
            .buildDirectory
            .dir("intermediates/classes/{variant}/transform{variant}ClassesWithAsm/dirs")
            .get()

        // compiler task
        val sourceFiles = target.fileTree("src/main") { configTree ->
            configTree.include("**/*.kt", "**/*.java")
            configTree.exclude("**/build/**", "**/generated/**")
        }
        val compilerTask = target.tasks.register(
            "xpler2Compiler",
            Xpler2CompilerTask::class.java,
        ) { task ->
            task.sourceFiles = sourceFiles
            task.coreDirectory = coreDirectory
            task.cacheDirectory = cacheDirectory
            task.intermediatesPath = intermediatesDirectory.asFile.absolutePath
        }
        target.tasks.named("preBuild") { it.dependsOn(compilerTask) }

        // dependency
        target.dependencies.add(
            "implementation",
            target.fileTree(
                mapOf(
                    "dir" to "$coreDirectory",
                    "include" to listOf("*.aar")
                )
            )
        )

        // asm transform
        val appModuleExtension = target.extensions.getByType(BaseAppModuleExtension::class.java)
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val applicationId = appModuleExtension.defaultConfig.applicationId

            variant.instrumentation.apply {
                transformClassesWith(
                    Xpler2AsmVisitorFactory::class.java,
                    InstrumentationScope.ALL,
                ) { params ->
                    params.cacheDirectory = cacheDirectory
                    params.applicationId = applicationId
                    params.debuggable = variant.debuggable
                }
            }
        }
    }
}