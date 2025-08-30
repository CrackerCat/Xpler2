package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import io.github.xpler2.xplerModule

open class HookerCallback {
    internal var unhookParamsInner: (() -> UnhookParams?)? = null

    val module: XplerModuleInterface
        get() = xplerModule

    val unhook: UnhookParams?
        get() = unhookParamsInner?.invoke()

    open fun onBefore(params: BeforeParams) {
    }

    open fun onAfter(params: AfterParams) {
    }
}