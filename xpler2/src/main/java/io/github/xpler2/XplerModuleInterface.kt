package io.github.xpler2

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.os.ParcelFileDescriptor
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.callback.HookerFunction
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

// module
lateinit var xplerModule: XplerModuleInterface

// store hook
val hookerCallbacks = mutableMapOf<Member, HookerFunction>()

// log
fun Context.logger(message: String) = logger(message, null)

fun Context.logger(message: String, throwable: Throwable?) {
    XplerLogger.logger(message, throwable)
}

interface XplerModuleInterface {
    /**
     * Hook method
     * ```
     * hooker(Application::class.java.getDeclaredMethod("attach", Context::class.java)) {
     *   onBefore {
     *     log("onBefore:$this")
     *   }
     *   onAfter {
     *     log("onAfter:$this")
     *   }
     *   onUnhook {
     *     log("onUnhook:$this")
     *   }
     * }
     * ```
     * @param method method
     * @param callback callback
     */
    fun hooker(
        method: Method,
        callback: HookerFunction.() -> Unit,
    ): UnhookParams?

    fun hooker(
        method: Method,
        priority: Int,
        callback: HookerFunction.() -> Unit,
    ): UnhookParams?

    /**
     * Hook method
     * ```
     * class MyHooker : HookerCallback() {
     *   override fun onBefore(callback: BeforeParams.() -> Unit) {
     *     log("onBefore:$this")
     *   }
     *   override fun onAfter(callback: AfterParams.() -> Unit) {
     *     log("onAfter:$this")
     *   }
     *   override fun onUnhook(callback: UnhookParams.() -> Unit) {
     *     log("onUnhook:$this")
     *   }
     * }
     *
     * hooker(Application::class.java.getConstructor(), MyHooker())
     * ```
     */
    fun hooker(
        method: Method,
        callback: HookerCallback,
    ): UnhookParams?

    fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback,
    ): UnhookParams?

    /**
     * Hook constructor
     * ```
     * hooker(Application::class.java.getConstructor()) {
     *   onBefore {
     *     log("onBefore:$this")
     *   }
     *   onAfter {
     *     log("onAfter:$this")
     *   }
     *   onUnhook {
     *     log("onUnhook:$this")
     *   }
     * }
     * ```
     * @param method constructor
     * @param callback callback
     */
    fun hooker(
        method: Constructor<*>,
        callback: HookerFunction.() -> Unit,
    ): UnhookParams?

    fun hooker(
        method: Constructor<*>,
        priority: Int,
        callback: HookerFunction.() -> Unit,
    ): UnhookParams?

    /**
     * Hook method
     * ```
     * class MyHooker : HookerCallback() {
     *   override fun onBefore(callback: BeforeParams.() -> Unit) {
     *     log("onBefore:$this")
     *   }
     *   override fun onAfter(callback: AfterParams.() -> Unit) {
     *     log("onAfter:$this")
     *   }
     *   override fun onUnhook(callback: UnhookParams.() -> Unit) {
     *     log("onUnhook:$this")
     *   }
     * }
     *
     * hooker(Application::class.java.getConstructor(), MyHooker())
     * ```
     */
    fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback,
    ): UnhookParams?

    fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback,
    ): UnhookParams?

    /// wrapper
    val api: Int

    val frameworkName: String

    val frameworkVersion: String

    val frameworkVersionCode: Long

    val isFirstPackage: Boolean

    val classLoader: ClassLoader

    val packageName: String

    val processName: String

    val modulePackageName: String?

    val modulePath: String?

    fun modulePackageInfo(context: Context): PackageInfo?

    fun deoptimize(method: Method): Boolean

    fun <T> deoptimize(constructor: Constructor<T>): Boolean

    fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any?

    fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?)

    fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any?

    fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?)

    fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T

    fun <T, U> newInstanceSpecial(constructor: Constructor<T>, subClass: Class<U>, vararg args: Any): U

    fun getRemotePreferences(group: String): SharedPreferences

    fun listRemoteFiles(): Array<String>

    fun openRemoteFile(name: String): ParcelFileDescriptor

    fun log(message: String, throwable: Throwable?)

    fun log(message: String)

    fun stackTraceString(): String
}