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

    @get:Throws(UnsupportedOperationException::class)
    override val scope: List<String>
        get() = throw UnsupportedOperationException("current xposed api does not support `getScope`")

    @Throws(UnsupportedOperationException::class)
    override fun removeScope(packageName: String): String? {
        throw UnsupportedOperationException("current xposed api does not support `removeScope`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun requestScope(packageName: String) {
        throw UnsupportedOperationException("current xposed api does not support `requestScope`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun getRemotePreferences(group: String) {
        throw UnsupportedOperationException("current xposed api does not support `getRemotePreferences`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun deleteRemotePreferences(group: String) {
        throw UnsupportedOperationException("current xposed api does not support `deleteRemotePreferences`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun listRemoteFiles(): Array<String> {
        throw UnsupportedOperationException("current xposed api does not support `listRemoteFiles`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun openRemoteFile(name: String) {
        throw UnsupportedOperationException("current xposed api does not support `openRemoteFile`")
    }

    @Throws(UnsupportedOperationException::class)
    override fun deleteRemoteFile(name: String) {
        throw UnsupportedOperationException("current xposed api does not support `deleteRemoteFile`")
    }
}