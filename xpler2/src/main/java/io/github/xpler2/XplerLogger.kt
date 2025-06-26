package io.github.xpler2

import java.util.Calendar

object XplerLogger {
    fun times(): String {
        val instance = Calendar.getInstance()
        val year = instance.get(Calendar.YEAR)
        val month = "${instance.get(Calendar.MONTH) + 1}".padStart(2, '0')
        val date = "${instance.get(Calendar.DATE)}".padStart(2, '0')
        val hour = "${instance.get(Calendar.HOUR)}".padStart(2, '0')
        val minute = "${instance.get(Calendar.MINUTE)}".padStart(2, '0')
        val second = "${instance.get(Calendar.SECOND)}".padStart(2, '0')
        val millisecond = "${instance.get(Calendar.MILLISECOND)}".padStart(3, '0')
        return "${year}-${month}-${date}T$hour:$minute:$second.$millisecond"
    }

    // prints the logs to the manager
    fun logger(message: String) {
        logger(message, null)
    }

    // prints the logs to the manager
    fun logger(message: String, throwable: Throwable?) {
        xplerModule.log(message, throwable)
    }
}