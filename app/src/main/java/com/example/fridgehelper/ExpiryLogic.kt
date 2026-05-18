
//TESTY JEDNOSTKOWE
//x

package com.example.fridgehelper

private const val MS_PER_DAY = 24L * 60 * 60 * 1000

fun daysLeft(expiryDate: Long, nowMs: Long): Int =
    ((expiryDate - nowMs) / MS_PER_DAY).toInt()

enum class ExpiryStatus { EXPIRED, WARNING, OK }

fun expiryStatus(expiryDate: Long, nowMs: Long): ExpiryStatus {
    val days = daysLeft(expiryDate, nowMs)
    return when {
        days < 0  -> ExpiryStatus.EXPIRED
        days <= 2 -> ExpiryStatus.WARNING
        else      -> ExpiryStatus.OK
    }
}
