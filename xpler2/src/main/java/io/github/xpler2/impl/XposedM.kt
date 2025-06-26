package io.github.xpler2.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.res.AssetManager
import android.content.res.Resources
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

    private fun buildXposedHookerCallbackImpl(
        method: Member,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerFunction.() -> Unit,
    ): UnhookParams? {
        val impl = HookerFunctionImpl()
            .also { hookerCallbacks[method] = it }
            .apply(callback)

        val unhookOriginal = XposedBridge.hookMethod(method, XposedHooker(priority))
        var unhookParams: UnhookParams? = null
        impl.unhookParamsInner?.invoke(
            UnhookParams(
                mOrigin = { unhookOriginal.hookedMethod },
                mUnhook = { unhookOriginal.unhook() },
            ).also { unhookParams = it }
        )

        return unhookParams
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
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
            onUnhook { callback.onUnhook(this) }
        }
    }

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(method, priority) {
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
            onUnhook { callback.onUnhook(this) }
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
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
            onUnhook { callback.onUnhook(this) }
        }
    }

    override fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback
    ): UnhookParams? {
        return hooker(constructor, priority) {
            onBefore { callback.onBefore(this) }
            onAfter { callback.onAfter(this) }
            onUnhook { callback.onUnhook(this) }
        }
    }

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

    @Throws(
        IllegalAccessException::class,
        IllegalArgumentException::class,
        InvocationTargetException::class,
    )
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    @SuppressWarnings("deprecation")
    override fun injectResource(resources: Resources?) {
        if (resources == null || modulePath == null)
            throw IllegalArgumentException("context or modulePath is null")

        val method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            .also { it.isAccessible = true }

        val assets = resources.assets ?: return
        method.invoke(assets, modulePath) // 添加插件资源
    }

    @Throws(
        IllegalAccessException::class,
        IllegalArgumentException::class,
        InvocationTargetException::class,
    )
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    @SuppressWarnings("deprecation")
    override fun resourcesWrapper(resources: Resources?): Resources? {
        if (resources == null || modulePath == null)
            throw IllegalArgumentException("resources or modulePath is null")

        val method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            .also { it.isAccessible = true }

        val assets = resources.assets ?: return null
        method.invoke(assets, modulePath) as? Int ?: -1 // 添加插件资源
        return Resources(assets, resources.displayMetrics, resources.configuration)
    }

    @Throws(IllegalAccessException::class)
    override fun deoptimize(method: Method): Boolean {
        throw IllegalAccessException("current xposed api does not support `deoptimize`")
    }

    @Throws(IllegalAccessException::class)
    override fun <T> deoptimize(constructor: Constructor<T>): Boolean {
        throw IllegalAccessException("current xposed api does not support `deoptimize`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(method, instance, args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?) {
        XposedBridge.invokeOriginalMethod(constructor, instance, args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any? {
        throw IllegalAccessException("current xposed api does not support `invokeSpecial`")
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?) {
        throw IllegalAccessException("current xposed api does not support `invokeSpecial`")
    }

    @Throws(
        InvocationTargetException::class,
        java.lang.IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
    )
    override fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T {
        throw IllegalAccessException("current xposed api does not support `newInstanceOrigin`")
    }

    @Throws(
        InvocationTargetException::class,
        java.lang.IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
    )
    override fun <T, U> newInstanceSpecial(
        constructor: Constructor<T>,
        subClass: Class<U>,
        vararg args: Any
    ): U {
        throw IllegalAccessException("current xposed api does not support `newInstanceSpecial`")
    }

    @Throws(IllegalAccessException::class)
    override fun getRemotePreferences(group: String): SharedPreferences {
        throw IllegalAccessException("current xposed api does not support `getRemotePreferences`")
    }

    @Throws(IllegalAccessException::class)
    override fun listRemoteFiles(): Array<String> {
        throw IllegalAccessException("current xposed api does not support `listRemoteFiles`")
    }

    @Throws(FileNotFoundException::class, IllegalAccessException::class)
    override fun openRemoteFile(name: String): ParcelFileDescriptor {
        throw IllegalAccessException("current xposed api does not support `openRemoteFile`")
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