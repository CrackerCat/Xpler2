package io.github.xpler2.plugin

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import io.github.xpler2.plugin.asm.Xpler2AsmVisitorFactory
import io.github.xpler2.plugin.compiler.task.Xpler2CompilerTask
import org.gradle.api.Plugin
import org.gradle.api.Project

// `.aar` file structure reference: https://developer.android.com/studio/projects/android-library?hl=zh-cn#aar-contents
class Xpler2CompilerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(AppPlugin::class.java))
            throw RuntimeException("`xpler2-compiler` is only allowed to be applied in the Application module.")

        // compiler
        val compileSourceFiles = target.fileTree("src/main") { configTree ->
            configTree.include("**/*.kt", "**/*.java")
            configTree.exclude("**/build/**", "**/generated/**")
        }
        val compileOutputDirectory = target.layout.buildDirectory.dir("generated/xpler2").get()
        val compilerTask = target.tasks.register(
            "xpler2Compiler",
            Xpler2CompilerTask::class.java,
        ) {
            it.sourceFiles = compileSourceFiles
            it.outputDirectory = compileOutputDirectory
        }
        target.tasks.named("preBuild") { it.dependsOn(compilerTask) }

        target.dependencies.add(
            "implementation",
            target.fileTree(
                mapOf(
                    "dir" to "${compileOutputDirectory.dir("core")}",
                    "include" to listOf("*.aar")
                )
            )
        )

        // asm transform
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            // debug directory
            val debugDir = target.layout
                .buildDirectory
                .dir("intermediates/classes/debug/transformDebugClassesWithAsm/dirs")
                .get()

            // release directory
            val releaseDir = target.layout
                .buildDirectory
                .dir("intermediates/classes/release/transformReleaseClassesWithAsm/dirs")
                .get()

            variant.instrumentation.apply {
                transformClassesWith(
                    Xpler2AsmVisitorFactory::class.java,
                    InstrumentationScope.ALL,
                ) { params ->
                    params.outputDir = if (variant.debuggable) debugDir.asFile.absolutePath else releaseDir.asFile.absolutePath
                    params.initializeCacheDir = compileOutputDirectory.asFile.absolutePath
                }
            }
        }
    }
}