package com.example.fridgehelper

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpiryLogicTest {

    private val DAY = 24L * 60 * 60 * 1000
    private val NOW = 1_000_000_000_000L // stała "teraz" — testy deterministyczne

    // --- expiryStatus ---

    @Test
    fun `produkt przeterminowany 2 dni temu jest EXPIRED`() {
        val status = expiryStatus(expiryDate = NOW - 2 * DAY, nowMs = NOW)
        assertEquals(ExpiryStatus.EXPIRED, status)
    }

    @Test
    fun `produkt przeterminowany 1 dzien temu jest EXPIRED`() {
        val status = expiryStatus(expiryDate = NOW - 1 * DAY, nowMs = NOW)
        assertEquals(ExpiryStatus.EXPIRED, status)
    }

    @Test
    fun `produkt wygasajacy dzisiaj (daysLeft=0) jest WARNING`() {
        // daysLeft == 0: data ważności == teraz — zgodnie z logiką obecnego kodu
        val status = expiryStatus(expiryDate = NOW, nowMs = NOW)
        assertEquals(ExpiryStatus.WARNING, status)
    }

    @Test
    fun `produkt wygasajacy za 1 dzien jest WARNING`() {
        val status = expiryStatus(expiryDate = NOW + 1 * DAY, nowMs = NOW)
        assertEquals(ExpiryStatus.WARNING, status)
    }

    @Test
    fun `produkt wygasajacy za 2 dni jest WARNING`() {
        val status = expiryStatus(expiryDate = NOW + 2 * DAY, nowMs = NOW)
        assertEquals(ExpiryStatus.WARNING, status)
    }

    @Test
    fun `produkt wygasajacy za 5 dni jest OK`() {
        val status = expiryStatus(expiryDate = NOW + 5 * DAY, nowMs = NOW)
        assertEquals(ExpiryStatus.OK, status)
    }

    // --- daysLeft ---

    @Test
    fun `daysLeft zwraca poprawna liczbe dni dla roznych dat`() {
        assertEquals(-2, daysLeft(expiryDate = NOW - 2 * DAY, nowMs = NOW))
        assertEquals(-1, daysLeft(expiryDate = NOW - 1 * DAY, nowMs = NOW))
        assertEquals(0,  daysLeft(expiryDate = NOW,           nowMs = NOW))
        assertEquals(1,  daysLeft(expiryDate = NOW + 1 * DAY, nowMs = NOW))
        assertEquals(5,  daysLeft(expiryDate = NOW + 5 * DAY, nowMs = NOW))
    }
}
