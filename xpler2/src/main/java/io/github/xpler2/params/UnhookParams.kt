package io.github.xpler2.params

import java.lang.reflect.Member

data class UnhookParams(
    private val mOrigin: () -> Member,
    private val mUnhook: () -> Unit,
) {
    val member get() = mOrigin()

    fun unhook() = mUnhook()
}