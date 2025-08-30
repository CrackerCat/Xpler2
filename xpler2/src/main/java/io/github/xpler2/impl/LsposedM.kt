package io.github.xpler2.impl

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.callback.HookerFunction
import io.github.xpler2.callback.HookerFunctionImpl
import io.github.xpler2.hookerCallbacks
import io.github.xpler2.params.UnhookParams
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Member
import java.lang.reflect.Method

internal class LsposedM(
    private val mXposedInterface: XposedInterface,
) : XplerModuleInterface {
    var mIsFirstPackage: Boolean = false
    lateinit var mClassloader: ClassLoader
    lateinit var mPackageName: String
    lateinit var mProcessName: String
    private val mUnhooks by lazy { mutableListOf<UnhookParams>() }

    private fun <T : Member> buildLsposedHookerCallbackImpl(
        method: T,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerFunction.() -> Unit,
    ): UnhookParams? {
        val impl = HookerFunctionImpl()
            .also { hookerCallbacks[method] = it }
            .apply(callback)

        val unhookOriginal = when (method) {
            is Method -> mXposedInterface.hook(method, priority, LsposedHooker::class.java)
            is Constructor<*> -> mXposedInterface.hook(method, priority, LsposedHooker::class.java)
            else -> throw IllegalArgumentException("Unsupported member type: ${method.javaClass.name}")
        }

        return UnhookParams(
            mOrigin = { unhookOriginal.origin },
            mUnhook = { unhookOriginal.unhook() },
        ).also {
            impl.unhookParamsInner = it
            mUnhooks.add(it)
        }
    }

    override fun hooker(
        method: Method,
        callback: HookerFunction.() -> Unit
    ) = buildLsposedHookerCallbackImpl(method, XposedInterface.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerFunction.() -> Unit
    ) = buildLsposedHookerCallbackImpl(method, priority, callback)

    override fun hooker(
        method: Method,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(method) {
            callback.unhookParamsInner = { this.unhook }
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
        }
    }

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(method, priority) {
            callback.unhookParamsInner = { this.unhook }
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
        }
    }

    override fun hooker(
        method: Constructor<*>,
        callback: HookerFunction.() -> Unit
    ) = buildLsposedHookerCallbackImpl(method, XposedInterface.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Constructor<*>,
        priority: Int,
        callback: HookerFunction.() -> Unit
    ) = buildLsposedHookerCallbackImpl(method, priority, callback)

    override fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(constructor) {
            callback.unhookParamsInner = { this.unhook }
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
        }
    }

    override fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(constructor, priority) {
            callback.unhookParamsInner = { this.unhook }
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
        }
    }

    override val unhooks: List<UnhookParams>
        get() = mUnhooks

    override val api: Int
        get() {
            val declaredField = XposedInterface::class.java.getDeclaredField("API") ?: return -1
            declaredField.isAccessible = true
            return declaredField.getInt(null)
        }

    override val frameworkName: String
        get() = mXposedInterface.frameworkName

    override val frameworkVersion: String
        get() = mXposedInterface.frameworkVersion

    override val frameworkVersionCode: Long
        get() = mXposedInterface.frameworkVersionCode

    override val isFirstPackage: Boolean
        get() = mIsFirstPackage

    override val classLoader: ClassLoader
        get() = mClassloader

    override val packageName: String
        get() = mPackageName

    override val processName: String
        get() = mProcessName

    override val modulePackageName: String?
        get() = null // ASM bytecode injection will be implemented by the xpler2-compiler plugin.

    override val modulePath: String?
        get() = mXposedInterface.applicationInfo.sourceDir

    override fun modulePackageInfo(context: Context): PackageInfo? {
        val moduleFile = modulePath?.let { File(it) } ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val flags = PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
            context.packageManager.getPackageArchiveInfo(moduleFile.absolutePath, flags)
        } else {
            context.packageManager.getPackageArchiveInfo(
                moduleFile.absolutePath,
                PackageManager.GET_ACTIVITIES
            )
        }
    }

    override fun deoptimize(method: Method): Boolean {
        return mXposedInterface.deoptimize(method)
    }

    override fun <T> deoptimize(constructor: Constructor<T>): Boolean {
        return mXposedInterface.deoptimize(constructor)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return mXposedInterface.invokeOrigin(method, instance, *args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?) {
        instance ?: throw IllegalArgumentException("instance is null")
        mXposedInterface.invokeOrigin(constructor, instance, *args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any? {
        return mXposedInterface.invokeSpecial(method, instance, *args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?) {
        instance ?: throw IllegalArgumentException("instance is null")
        mXposedInterface.invokeSpecial(method, instance, *args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
    )
    override fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T {
        return mXposedInterface.newInstanceOrigin(constructor, *args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
    )
    override fun <T, U> newInstanceSpecial(
        constructor: Constructor<T>,
        subClass: Class<U>,
        vararg args: Any
    ): U {
        return mXposedInterface.newInstanceSpecial(constructor, subClass, *args)
    }

    override fun getRemotePreferences(group: String): SharedPreferences {
        return mXposedInterface.getRemotePreferences(group)
    }

    override fun listRemoteFiles(): Array<String> {
        return mXposedInterface.listRemoteFiles()
    }

    @Throws(FileNotFoundException::class)
    override fun openRemoteFile(name: String): ParcelFileDescriptor {
        return mXposedInterface.openRemoteFile(name)
    }

    override fun log(message: String, throwable: Throwable?) {
        if (throwable != null)
            mXposedInterface.log(message, throwable)
        else
            mXposedInterface.log(message)
    }

    override fun log(message: String) = log(message, null)

    override fun stackTraceString(): String {
        return Log.getStackTraceString(RuntimeException("stackTraceString"))
    }
}