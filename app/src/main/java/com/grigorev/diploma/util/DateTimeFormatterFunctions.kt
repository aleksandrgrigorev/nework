package com.grigorev.diploma.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private val calendar = Calendar.getInstance()
private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun formatDateTime(dateTime: String): String? {
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
        ?.let { formatter.format(it) }
}

fun formatDate(date: String): String? {
    return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
        ?.let { dateFormatter.format(it) }
}

fun pickDate(editText: EditText?, context: Context?) {
    val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = monthOfYear
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

        editText?.setText(SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time))
    }

    DatePickerDialog(context!!,
        datePicker,
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    ).show()
}

fun pickTime(editText: EditText, context: Context) {
    val timePicker = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute

        editText.setText(SimpleDateFormat("HH:mm", Locale.ROOT).format(calendar.time))
    }

    TimePickerDialog(
        context,
        timePicker,
        calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE],
        true
    ).show()
}