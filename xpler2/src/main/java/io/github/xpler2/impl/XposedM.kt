package io.github.xpler2.impl

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
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

internal class XposedM(
    private val mStartupParam: IXposedHookZygoteInit.StartupParam,
) : XplerModuleInterface {
    var mIsFirstPackage: Boolean = false
    lateinit var mClassloader: ClassLoader
    lateinit var mPackageName: String
    lateinit var mProcessName: String
    private val mUnhooks by lazy { mutableListOf<UnhookParams>() }

    private fun buildXposedHookerCallbackImpl(
        method: Member,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerFunction.() -> Unit,
    ): UnhookParams? {
        val impl = HookerFunctionImpl()
            .also { hookerCallbacks[method] = it }
            .apply(callback)

        val unhookOriginal = XposedBridge.hookMethod(method, XposedHooker(priority))

        return UnhookParams(
            mOrigin = { unhookOriginal.hookedMethod },
            mUnhook = { unhookOriginal.unhook() },
        ).also {
            impl.unhookParamsInner = it
            mUnhooks.add(it)
        }
    }

    override fun hooker(
        method: Method,
        callback: HookerFunction.() -> Unit
    ) = buildXposedHookerCallbackImpl(method, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerFunction.() -> Unit
    ) = buildXposedHookerCallbackImpl(method, priority, callback)

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
    ) = buildXposedHookerCallbackImpl(method, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Constructor<*>,
        priority: Int,
        callback: HookerFunction.() -> Unit
    ) = buildXposedHookerCallbackImpl(method, priority, callback)

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
        get() = XposedBridge.getXposedVersion()

    override val frameworkName: String
        get() {
            val declaredField = XposedBridge::class.java.getDeclaredField("TAG")
            declaredField.isAccessible = true
            return "${declaredField.get(null) ?: "Unknown"}"
        }

    override val frameworkVersion: String
        get() = "Unknown"

    override val frameworkVersionCode: Long
        get() = -1

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
        get() = mStartupParam.modulePath

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

    @Throws(UnsupportedOperationException::class)
    override fun deoptimize(method: Method): Boolean {
        throw UnsupportedOperationException("current xposed api does not support `deoptimize`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun <T> deoptimize(constructor: Constructor<T>): Boolean {
        throw UnsupportedOperationException("current xposed api does not support `deoptimize`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(method, instance, args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?) {
        XposedBridge.invokeOriginalMethod(constructor, instance, args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    override fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any? {
        throw UnsupportedOperationException("current xposed api does not support `invokeSpecial`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    override fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?) {
        throw UnsupportedOperationException("current xposed api does not support `invokeSpecial`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
        UnsupportedOperationException::class,
    )
    override fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T {
        throw UnsupportedOperationException("current xposed api does not support `newInstanceOrigin`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
        UnsupportedOperationException::class,
    )
    override fun <T, U> newInstanceSpecial(
        constructor: Constructor<T>,
        subClass: Class<U>,
        vararg args: Any
    ): U {
        throw UnsupportedOperationException("current xposed api does not support `newInstanceSpecial`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun getRemotePreferences(group: String): SharedPreferences {
        throw UnsupportedOperationException("current xposed api does not support `getRemotePreferences`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun listRemoteFiles(): Array<String> {
        throw UnsupportedOperationException("current xposed api does not support `listRemoteFiles`")
    }

    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    override fun openRemoteFile(name: String): ParcelFileDescriptor {
        throw UnsupportedOperationException("current xposed api does not support `openRemoteFile`")
    }

    override fun log(message: String, throwable: Throwable?) {
        if (throwable != null)
            XposedBridge.log(Exception(message, throwable))
        else
            XposedBridge.log(message)
    }

    override fun log(message: String) = log(message, null)

    override fun stackTraceString(): String {
        return Log.getStackTraceString(RuntimeException("stackTraceString"))
    }
}