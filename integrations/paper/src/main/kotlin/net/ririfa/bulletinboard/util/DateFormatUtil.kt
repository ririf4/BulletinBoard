package net.ririfa.bulletinboard.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtil {
    private val formatter = SimpleDateFormat("yyyy:MM:dd_HH:mm:ss")

    fun format(date: Date): String = formatter.format(date)

    fun parse(dateString: String): Date = formatter.parse(dateString)
}