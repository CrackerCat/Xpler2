package io.github.xpler2

interface XplerModuleStatus {
    companion object {
        @JvmStatic
        fun getInstance(): XplerModuleStatus? {
            return null // ASM bytecode injection will be implemented by the xpler2-compiler plugin.
        }
    }

    /**
     * Whether the module is activated.
     * This is true if the module is activated in the Xposed framework.
     */
    val isActivate: Boolean

    /**
     * The API version of the framework.
     */
    val apiVersion: Int

    /**
     * The name of the framework.
     */
    val frameworkName: String

    /**
     * The version of the framework.
     */
    val frameworkVersion: String

    /**
     * The version code of the framework.
     */
    val frameworkVersionCode: Long

    /**
     * The scope of the module.
     */
    val scope: List<String>

    /**
     * Remove a package from the scope.
     *
     * @param packageName The name of the package to remove.
     * @return The removed package name, or null if not found.
     */
    fun removeScope(packageName: String): String?

    /**
     * Request a scope for a package.
     *
     * @param packageName The name of the package to request scope for.
     */
    fun requestScope(packageName: String)

    /**
     * Get remote preferences from Xposed framework. If the group does not exist, it will be created.
     */
    fun getRemotePreferences(group: String)

    /**
     * Delete a group of remote preferences.
     */
    fun deleteRemotePreferences(group: String)

    /**
     * List all files in the module's shared data directory.
     */
    fun listRemoteFiles(): Array<String>

    /**
     * Open a file in the module's shared data directory. The file will be created if not exists.
     */
    fun openRemoteFile(name: String)

    /**
     * Delete a file in the module's shared data directory.
     */
    fun deleteRemoteFile(name: String)
}