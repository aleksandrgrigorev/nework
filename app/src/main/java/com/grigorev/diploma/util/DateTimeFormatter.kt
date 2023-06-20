package com.grigorev.diploma.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeFormatter {

    private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")

    fun formatDateTime(dateTime: String): String? {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
            ?.let { formatter.format(it) }
    }
}