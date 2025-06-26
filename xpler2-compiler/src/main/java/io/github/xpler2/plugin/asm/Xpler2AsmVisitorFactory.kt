package io.github.xpler2.plugin.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File


abstract class Xpler2AsmVisitorFactory : AsmClassVisitorFactory<Xpler2AsmVisitorFactory.Params> {

    interface Params : InstrumentationParameters {
        @get:Input
        var outputDir: String

        @get:Input
        var initializeCacheDir: String
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val params = parameters.get()
        val initializeCache = XplerInitializeCache.cache(params.initializeCacheDir)
        return Xpler2ClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = nextClassVisitor,
            initial = initializeCache.initializeBean,
            outputDir = params.outputDir,
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val params = parameters.get()
        val initializeCache = XplerInitializeCache.cache(params.initializeCacheDir)
        val sourceName = initializeCache.sourcePath
            .replace(".java", "")
            .replace(".kt", "Kt")
            .replace(File.separator, ".")

        return (classData.className.indexOf("io.github.libxposed") != -1
                || classData.className.indexOf("io.github.xpler2") != -1
                || sourceName.indexOf(classData.className) != -1)
    }
}