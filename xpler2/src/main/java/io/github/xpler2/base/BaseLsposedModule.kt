package io.github.xpler2.base

import androidx.annotation.CallSuper
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.impl.LsposedM
import io.github.xpler2.xplerModule

// BaseXposedModule
abstract class BaseLsposedModule(
    base: XposedInterface,
    param: XposedModuleInterface.ModuleLoadedParam,
) : XposedModule(base, param) {
    private var mModule: LsposedM = LsposedM(base).also {
        it.mProcessName = param.processName
        xplerModule = it
    }

    @CallSuper
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        mModule.mIsFirstPackage = param.isFirstPackage
        mModule.mClassloader = param.classLoader
        mModule.mPackageName = param.packageName
    }

    protected val module: XplerModuleInterface
        get() = mModule
}