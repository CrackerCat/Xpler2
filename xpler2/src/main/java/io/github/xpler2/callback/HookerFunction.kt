package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams

interface HookerFunction {
    val module: XplerModuleInterface

    val unhook: UnhookParams?

    fun onBefore(params: BeforeParams.() -> Unit)

    fun onAfter(params: AfterParams.() -> Unit)

}