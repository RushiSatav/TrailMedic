package com.trailmedic.data.llm

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationManagerTest {

    @Test
    fun buildPrompt_emptyMessages_generatesInitialPrompt() {
        val prompt = ConversationManager.buildPrompt(emptyList(), ConversationPhase.INTERVIEWING)
        assertTrue(prompt.contains("<start_of_turn>user"))
        assertTrue(prompt.contains("MediTrail") || prompt.contains("TrailMedic"))
        assertTrue(prompt.contains("<start_of_turn>model"))
    }

    @Test
    fun buildPrompt_withConversationHistory_formatsTurnsCorrectly() {
        val messages = listOf(
            Message(content = "I cut my hand on a sharp rock", isUser = true),
            Message(content = "Is there heavy spurting blood?", isUser = false),
            Message(content = "No, just steady dark bleeding", isUser = true)
        )
        val prompt = ConversationManager.buildPrompt(messages, ConversationPhase.INTERVIEWING)
        assertTrue(prompt.contains("I cut my hand on a sharp rock"))
        assertTrue(prompt.contains("<start_of_turn>model"))
        assertTrue(prompt.contains("Is there heavy spurting blood?"))
        assertTrue(prompt.contains("No, just steady dark bleeding"))
    }

    @Test
    fun buildPrompt_withClinicalData_embedsVerifiedKnowledge() {
        val messages = listOf(
            Message(content = "Saw a venomous viper snake", isUser = true)
        )
        val mockClinicalData = com.trailmedic.domain.model.SymptomEmergencyData(
            id = "bite",
            name = "Snake / Insect Bite",
            triggerKeywords = listOf("snake"),
            questions = listOf("Did the snake bite you?"),
            firstAidSteps = listOf("Immobilize limb below heart level"),
            warningSigns = listOf("Rapid swelling"),
            evacuationNote = "Activate Satellite SOS"
        )
        val prompt = ConversationManager.buildPrompt(messages, ConversationPhase.INTERVIEWING, mockClinicalData)
        assertTrue(prompt.contains("VERIFIED FIRST AID PROTOCOL"))
        assertTrue(prompt.contains("Immobilize limb below heart level"))
        assertTrue(prompt.contains("Saw a venomous viper snake"))
    }
}
