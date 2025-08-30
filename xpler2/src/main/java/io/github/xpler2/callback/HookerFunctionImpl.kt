package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import io.github.xpler2.xplerModule

class HookerFunctionImpl() : HookerFunction {
    internal var beforeParamsInner: (BeforeParams.() -> Unit)? = null
        private set

    internal var afterParamsInner: (AfterParams.() -> Unit)? = null
        private set

    internal var unhookParamsInner: UnhookParams? = null

    override val module: XplerModuleInterface
        get() = xplerModule

    override val unhook: UnhookParams?
        get() = unhookParamsInner

    override fun onBefore(params: BeforeParams.() -> Unit) {
        beforeParamsInner = params
    }

    override fun onAfter(params: AfterParams.() -> Unit) {
        afterParamsInner = params
    }
}