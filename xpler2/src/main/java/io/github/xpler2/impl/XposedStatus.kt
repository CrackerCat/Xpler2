package io.github.xpler2.impl

import io.github.xpler2.XplerModuleStatus

object XposedStatus : XplerModuleStatus {
    override val isActivate: Boolean
        get() = apiVersion != -1

    override val apiVersion: Int
        get() = -1

    override val frameworkName: String
        get() = "Unknown"

    override val frameworkVersion: String
        get() = "Unknown"

    override val frameworkVersionCode: Long
        get() = -1L

    override val scope: List<String>
        get() = emptyList()

    @Throws(UnsupportedOperationException::class)
    override fun removeScope(packageName: String): String? {
        throw UnsupportedOperationException("Xposed does not support scope management.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun requestScope(packageName: String) {
        throw UnsupportedOperationException("Xposed does not support scope management.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun getRemotePreferences(group: String) {
        throw UnsupportedOperationException("Xposed does not support remote preferences.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun deleteRemotePreferences(group: String) {
        throw UnsupportedOperationException("Xposed does not support remote preferences.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun listRemoteFiles(): Array<String> {
        throw UnsupportedOperationException("Xposed does not support remote file management.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun openRemoteFile(name: String) {
        throw UnsupportedOperationException("Xposed does not support remote file management.")
    }

    @Throws(UnsupportedOperationException::class)
    override fun deleteRemoteFile(name: String) {
        throw UnsupportedOperationException("Xposed does not support remote file management.")
    }
}