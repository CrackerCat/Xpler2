package io.github.xpler2.impl

import de.robv.android.xposed.XC_MethodHook
import io.github.xpler2.callback.HookerFunctionImpl
import io.github.xpler2.hookerCallbacks
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import java.lang.reflect.Member

internal class XposedHooker(priority: Int = PRIORITY_DEFAULT) : XC_MethodHook(priority) {
    private val isSkipped = ThreadLocal.withInitial {
        mutableMapOf<Member, Boolean>()
    }

    override fun beforeHookedMethod(param: MethodHookParam) {
        (hookerCallbacks[param.method] as? HookerFunctionImpl)?.beforeParamsInner?.invoke(
            BeforeParams(
                mMember = { param.method },
                mArgs = { param.args },
                mInstance = { param.thisObject },
                mReturnAndSkip = {
                    isSkipped.get()?.put(param.method, true)
                    param.result = it
                },
                mThrowAndSkip = {
                    isSkipped.get()?.put(param.method, true)
                    param.throwable = it
                },
            )
        )
    }

    override fun afterHookedMethod(param: MethodHookParam) {
        (hookerCallbacks[param.method] as? HookerFunctionImpl)?.afterParamsInner?.invoke(
            AfterParams(
                mMember = { param.method },
                mArgs = { param.args },
                mInstance = { param.thisObject },
                mResult = { param.result },
                mThrowable = { param.throwable },
                mIsSkipped = { isSkipped.get()?.get(param.method) == true },
                mSetResult = { param.result = it },
                mSetThrowable = { param.throwable = it }
            )
        )
    }
}