package com.trailmedic.utils

import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    @Test
    fun formatAsTimerString_correctFormatting() {
        assertEquals("00:00", 0L.formatAsTimerString())
        assertEquals("01:05", 65L.formatAsTimerString())
        assertEquals("10:30", 630L.formatAsTimerString())
    }

    @Test
    fun formatAsDurationSummary_correctFormatting() {
        assertEquals("45 sec", 45L.formatAsDurationSummary())
        assertEquals("2 min 10 sec", 130L.formatAsDurationSummary())
        assertEquals("5 min ", 300L.formatAsDurationSummary())
    }

    @Test
    fun exportAsFormattedText_containsExpectedSections() {
        val session = Session(
            id = "test-session-1",
            emergencyType = "Snake / Insect Bite",
            messages = listOf(
                Message(content = "Snake bit lower calf", isUser = true),
                Message(content = "Keep patient still. Immobilize limb.", isUser = false)
            ),
            firstAidSummary = "1. Immobilize leg\n2. Call for rescue",
            outcomeNote = "Ranger contacted, patient stabilized",
            timestamp = 1700000000000L,
            durationSeconds = 120L
        )

        val exported = session.exportAsFormattedText()
        assertTrue(exported.contains("TRAILMEDIC SESSION REPORT"))
        assertTrue(exported.contains("Emergency Type: Snake / Insect Bite"))
        assertTrue(exported.contains("CONVERSATION TRANSCRIPT:"))
        assertTrue(exported.contains("FIRST AID GIVEN / PROTOCOL:"))
        assertTrue(exported.contains("Ranger contacted, patient stabilized"))
    }
}
