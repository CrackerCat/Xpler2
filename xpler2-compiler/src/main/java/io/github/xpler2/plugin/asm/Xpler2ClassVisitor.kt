package io.github.xpler2.plugin.asm

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class Xpler2ClassVisitor(
    api: Int,
    classVisitor: ClassVisitor,
    private val initial: XplerInitializeBean,
    private val outputDir: String,
) : ClassVisitor(api, classVisitor) {
    private lateinit var mOwnerName: String
    private var mSuperName: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        mOwnerName = name
        mSuperName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        // if the annotation is not compatible with the old API, the `XposedHooker` annotation will be removed.
        val annotation = Type.getType(descriptor)
        if (!initial.lsposedCompatAnnotation
            && annotation.className == "io.github.libxposed.api.annotations.XposedHooker"
        ) {
            return null
        }

        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor? {
        // skip methods that are not `public static`.
        if ((access and Opcodes.ACC_PUBLIC) == 0 || (access and Opcodes.ACC_STATIC) == 0) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        // skip methods that are not single parameter.
        val argumentTypes = Type.getArgumentTypes(descriptor)
        if (argumentTypes.size != 1) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        // skipping single parameter type is not `XplerModuleInterface` or `XposedInterface$BeforeHookCallback` or `XposedInterface$AfterHookCallback` method
        val singleParam = argumentTypes.single()
        if (singleParam.className != "io.github.xpler2.XplerModuleInterface"
            && singleParam.className != "io.github.libxposed.api.XposedInterface\$BeforeHookCallback"
            && singleParam.className != "io.github.libxposed.api.XposedInterface\$AfterHookCallback"
        ) {
            return super.visitMethod(access, name, descriptor, signature, exceptions) // 只处理 XplerModuleInterface 参数
        }

        return Xpler2MethodVisitor(
            api = api,
            methodVisitor = super.visitMethod(
                access,
                name,
                descriptor,
                signature,
                exceptions
            ),
            ownerName = mOwnerName,
            methodMame = name,
            descriptor = descriptor,
            initial = initial,
            outputDir = outputDir,
        )
    }
}