package io.github.xpler2.base

import androidx.annotation.CallSuper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.impl.XposedM
import io.github.xpler2.xplerModule

abstract class BaseXposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private lateinit var mModule: XposedM

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        xplerModule = XposedM(startupParam)
            .also { mModule = it }
    }

    @CallSuper
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        mModule.mIsFirstPackage = lpparam.isFirstApplication
        mModule.mClassloader = lpparam.classLoader
        mModule.mPackageName = lpparam.packageName
        mModule.mProcessName = lpparam.processName
    }

    protected val module: XplerModuleInterface
        get() = mModule

    fun log(message: String) = mModule.log(message)

    fun log(message: String, throwable: Throwable) = mModule.log(message, throwable)
}