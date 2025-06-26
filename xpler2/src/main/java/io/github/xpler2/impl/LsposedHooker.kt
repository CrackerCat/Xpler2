package io.github.xpler2.impl

import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import io.github.xpler2.callback.HookerFunctionImpl
import io.github.xpler2.hookerCallbacks
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams

// Default hooker
@XposedHooker
internal object LsposedHooker : XposedInterface.Hooker {

    @JvmStatic
    @BeforeInvocation
    fun before(callback: XposedInterface.BeforeHookCallback) {
        (hookerCallbacks[callback.member] as? HookerFunctionImpl)?.beforeParamsInner?.invoke(
            BeforeParams(
                mMember = { callback.member },
                mArgs = { callback.args },
                mInstance = { callback.thisObject },
                mReturnAndSkip = { callback.returnAndSkip(it) },
                mThrowAndSkip = { callback.throwAndSkip(it) },
            )
        )
    }

    @JvmStatic
    @AfterInvocation
    fun after(callback: XposedInterface.AfterHookCallback) {
        (hookerCallbacks[callback.member] as? HookerFunctionImpl)?.afterParamsInner?.invoke(
            AfterParams(
                mMember = { callback.member },
                mArgs = { callback.args },
                mInstance = { callback.thisObject },
                mResult = { callback.result },
                mThrowable = { callback.throwable },
                mIsSkipped = { callback.isSkipped },
                mSetResult = { callback.result = it },
                mSetThrowable = { callback.throwable = it }
            )
        )
    }
}