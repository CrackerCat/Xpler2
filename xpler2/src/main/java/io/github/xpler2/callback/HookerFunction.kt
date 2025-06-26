package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams

interface HookerFunction {
    val module: XplerModuleInterface

    fun onBefore(params: BeforeParams.() -> Unit)

    fun onAfter(params: AfterParams.() -> Unit)

    fun onUnhook(params: UnhookParams.() -> Unit)
}