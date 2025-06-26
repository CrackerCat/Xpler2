package io.github.xpler2.hooker

import io.github.xpler2.callback.HookerFunction
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.UnhookParams
import io.github.xpler2.xplerModule
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Hook method
 * ```
 * class MyHooker : HookerCallback() {
 *   override fun onBefore(callback: BeforeParams) {
 *     log("onBefore:$this")
 *   }
 *   override fun onAfter(callback: AfterParams) {
 *     log("onBefore:$this")
 *   }
 *   override fun onUnhook(callback: UnhookParams) {
 *     log("onBefore:$this")
 *   }
 * }
 *
 * Application::class.java.getDeclaredMethod("attach", Context::class.java).hooker(MyHooker())
 * ```
 * @param callback callback
 */
fun Method.hooker(callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Method.hooker(priority: Int, callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook method
 * ```
 * Application::class.java.getDeclaredMethod("attach", Context::class.java).hooker {
 *    onBefore {
 *      log("onBefore:$this")
 *    }
 *    onAfter {
 *      log("onAfter:$this")
 *    }
 *    onUnhook {
 *      log("onUnhook:$this")
 *    }
 * }
 * ```
 * @param callback callback
 */
fun Method.hooker(callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Method.hooker(priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook method
 * ```
 * class MyHooker : HookerCallback() {
 *   override fun onBefore(callback: BeforeParams) {
 *     log("onBefore:$this")
 *   }
 *   override fun onAfter(callback: AfterParams) {
 *     log("onBefore:$this")
 *   }
 *   override fun onUnhook(callback: UnhookParams) {
 *     log("onBefore:$this")
 *   }
 * }
 *
 * Application::class.java.getConstructor().hooker(MyHooker())
 *```
 * @param callback callback
 */
fun Constructor<*>.hooker(callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Constructor<*>.hooker(priority: Int, callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook constructor
 * ```
 * Application::class.java.getConstructor().hooker{
 *    onBefore {
 *      log("onBefore:$this")
 *    }
 *    onAfter {
 *      log("onAfter:$this")
 *    }
 *    onUnhook {
 *      log("onUnhook:$this")
 *    }
 * }
 * ```
 * @param callback callback
 */
fun Constructor<*>.hooker(callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Constructor<*>.hooker(priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook all methods
 * ```
 * Application::class.java.hookerMethodAll {
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
 * @param callback callback
 */
fun Class<*>.hookerMethodAll(callback: HookerFunction.() -> Unit): List<UnhookParams?> {
    return declaredMethods
        .map { m ->
            if (Modifier.isAbstract(m.modifiers)) return@map null
            xplerModule.hooker(m, callback)
        }
}

/**
 * Hook all constructors
 * ```
 * Application::class.java.hookerConstructorAll {
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
 * @param callback callback
 */
fun Class<*>.hookerConstructorAll(callback: HookerFunction.() -> Unit): List<UnhookParams?> {
    return declaredConstructors
        .map { c ->
            xplerModule.hooker(c, callback)
        }
}