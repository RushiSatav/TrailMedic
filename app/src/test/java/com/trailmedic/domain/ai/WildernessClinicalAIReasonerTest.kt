package com.trailmedic.domain.ai

import com.trailmedic.domain.model.ClinicalExtractionResult
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.FirstAidIntentData
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.repository.SymptomTreeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WildernessClinicalAIReasonerTest {

    private lateinit var aiReasoner: WildernessClinicalAIReasoner
    private lateinit var extractor: ClinicalKnowledgeExtractor

    private val fakeRepo = object : SymptomTreeRepository {
        private val mockData = SymptomEmergencyData(
            id = "bleeding",
            name = "Bleeding / Severe Wound",
            triggerKeywords = listOf("blood", "bleeding", "wound", "cut"),
            questions = listOf(
                "Is the blood spurting under high pressure or flowing steadily?",
                "Is there an object embedded inside the wound?",
                "Is the patient showing signs of shock?"
            ),
            firstAidSteps = listOf(
                "Apply immediate direct, firm pressure over the wound using sterile gauze.",
                "Maintain firm continuous pressure for at least 10 full minutes.",
                "If arterial bleeding occurs: apply a tourniquet 2-3 inches above wound.",
                "Wrap with elastic pressure bandage."
            ),
            warningSigns = listOf(
                "Blood continues heavy spurting",
                "Rapid drop in consciousness",
                "Signs of systemic shock"
            ),
            evacuationNote = "Activate Satellite SOS. Urgent evacuation required."
        )

        override fun getAllEmergencies(): List<SymptomEmergencyData> = listOf(mockData)
        override fun getFirstAidIntents(): List<FirstAidIntentData> = emptyList()
        override fun getEmergencyById(id: String): SymptomEmergencyData? = mockData
        override fun extractKnowledge(query: String): ClinicalExtractionResult? = extractor.extractKnowledgeForPrompt(query)
        override fun findMatchingEmergency(query: String): SymptomEmergencyData? = mockData
        override fun getCategoryFallback(category: EmergencyCategory): SymptomEmergencyData = mockData
    }

    @Before
    fun setUp() {
        extractor = ClinicalKnowledgeExtractor()
        aiReasoner = WildernessClinicalAIReasoner(fakeRepo, extractor)
    }

    @Test
    fun detectEmergencyCategory_withSnakeKeywords_returnsBite() {
        val messages = listOf(
            Message(content = "My friend got bitten by a viper snake on the trail", isUser = true)
        )
        val detected = aiReasoner.detectEmergencyCategory(messages, EmergencyCategory.GENERAL)
        assertEquals(EmergencyCategory.BITE, detected)
    }

    @Test
    fun detectEmergencyCategory_withBleedingKeywords_returnsBleeding() {
        val messages = listOf(
            Message(content = "Deep gash on leg, blood is spurting from artery", isUser = true)
        )
        val detected = aiReasoner.detectEmergencyCategory(messages, EmergencyCategory.GENERAL)
        assertEquals(EmergencyCategory.BLEEDING, detected)
    }

    @Test
    fun detectEmergencyCategory_withFractureKeywords_returnsFracture() {
        val messages = listOf(
            Message(content = "He fell down the ridge and heard a snap, bone is broken", isUser = true)
        )
        val detected = aiReasoner.detectEmergencyCategory(messages, EmergencyCategory.GENERAL)
        assertEquals(EmergencyCategory.FRACTURE, detected)
    }

    @Test
    fun detectEmergencyCategory_withBreathingKeywords_returnsBreathing() {
        val messages = listOf(
            Message(content = "High altitude mountain pass, severe shortness of breath and wheezing", isUser = true)
        )
        val detected = aiReasoner.detectEmergencyCategory(messages, EmergencyCategory.GENERAL)
        assertEquals(EmergencyCategory.BREATHING, detected)
    }

    @Test
    fun detectEmergencyCategory_withHypothermiaKeywords_returnsHypothermia() {
        val messages = listOf(
            Message(content = "Soaked in ice cold river, shivering uncontrollably and numb", isUser = true)
        )
        val detected = aiReasoner.detectEmergencyCategory(messages, EmergencyCategory.GENERAL)
        assertEquals(EmergencyCategory.HYPOTHERMIA, detected)
    }

    @Test
    fun bleedingFlow_turn1UserSaysYes_givesTourniquetAdviceAndAsksNextQuestionWithoutRepetition() {
        // Turn 0: User reports fall and bleeding
        val msg1 = Message(content = "my friend fell down from the trail and he is bleeding", isUser = true)
        val resp1 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BLEEDING,
            messages = listOf(msg1),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 0
        )
        assertTrue(resp1.contains("IMMEDIATE FIRST AID ACTIONS:"))
        assertTrue(resp1.contains("Press down firmly") || resp1.contains("direct pressure"))
        assertTrue(resp1.contains("blood") && (resp1.contains("spurting") || resp1.contains("shooting")))

        // Turn 1: User replies "yes" (blood is spurting)
        val botMsg1 = Message(content = resp1, isUser = false)
        val userMsg2 = Message(content = "yes", isUser = true)
        val resp2 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BLEEDING,
            messages = listOf(msg1, botMsg1, userMsg2),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 1
        )

        // Must NOT be identical to resp1
        assertNotEquals(resp1, resp2)
        // Must give tourniquet protocol in simple points
        assertTrue(resp2.contains("TOURNIQUET") || resp2.contains("BLEEDING"))
        assertTrue(resp2.contains("tourniquet") || resp2.contains("Tourniquet"))
        // Must ask the NEXT question (wound location & embedded object)
        assertTrue(resp2.contains("Where on the body is the injury") || resp2.contains("Where on the body is the wound"))
        assertTrue(resp2.contains("stuck inside") || resp2.contains("embedded inside"))

        // Turn 2: User replies "on his leg, a rock is stuck inside"
        val botMsg2 = Message(content = resp2, isUser = false)
        val userMsg3 = Message(content = "on his leg, a rock is stuck inside", isUser = true)
        val resp3 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BLEEDING,
            messages = listOf(msg1, botMsg1, userMsg2, botMsg2, userMsg3),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 2
        )

        assertNotEquals(resp2, resp3)
        assertTrue(resp3.contains("OBJECT") || resp3.contains("STUCK"))
        assertTrue(resp3.contains("DO NOT pull out") || resp3.contains("Do NOT attempt to remove"))
        assertTrue(resp3.contains("shock") || resp3.contains("dizzy"))
    }

    @Test
    fun bleedingFlow_turn1UserSaysNo_givesSteadyBleedingAdviceAndAsksNextQuestionWithoutRepetition() {
        val msg1 = Message(content = "my friend fell down from the trail and he is bleeding", isUser = true)
        val resp1 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BLEEDING,
            messages = listOf(msg1),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 0
        )

        val botMsg1 = Message(content = resp1, isUser = false)
        val userMsg2 = Message(content = "no, just flowing", isUser = true)
        val resp2 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BLEEDING,
            messages = listOf(msg1, botMsg1, userMsg2),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 1
        )

        assertNotEquals(resp1, resp2)
        assertTrue(resp2.contains("STEADY BLEEDING"))
        assertTrue(resp2.contains("bandage") || resp2.contains("cloth"))
        assertTrue(resp2.contains("stuck inside") || resp2.contains("embedded inside"))
    }

    @Test
    fun fractureFlow_multiTurnProgression_doesNotRepeatQuestions() {
        val msg1 = Message(content = "fell down and heard a snap, bone broken", isUser = true)
        val resp1 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.FRACTURE,
            messages = listOf(msg1),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 0
        )
        assertTrue(resp1.contains("bone poking out") || resp1.contains("open break") || resp1.contains("open fracture"))

        val botMsg1 = Message(content = resp1, isUser = false)
        val userMsg2 = Message(content = "skin is closed but badly bent", isUser = true)
        val resp2 = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.FRACTURE,
            messages = listOf(msg1, botMsg1, userMsg2),
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 1
        )

        assertNotEquals(resp1, resp2)
        assertTrue(resp2.contains("CLOSED") || resp2.contains("BROKEN BONE"))
        assertTrue(resp2.contains("fingers or toes") || resp2.contains("warm"))
    }

    @Test
    fun generateDynamicResponse_withGreeting_returnsWelcomePrompt() {
        val messages = listOf(
            Message(content = "Hello", isUser = true)
        )
        val response = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BITE,
            messages = messages,
            phase = ConversationPhase.INTERVIEWING,
            questionIndex = 0
        )
        assertTrue(response.contains("MediTrail") || response.contains("TrailMedic"))
    }

    @Test
    fun generateDynamicResponse_withDiagnosingPhase_returnsFormattedAssessment() {
        val messages = listOf(
            Message(content = "Snake bit leg, very pale and dizzy with shock", isUser = true)
        )
        val response = aiReasoner.generateDynamicResponse(
            category = EmergencyCategory.BITE,
            messages = messages,
            phase = ConversationPhase.DIAGNOSING,
            questionIndex = 3
        )
        assertTrue(response.contains("ACTION PROTOCOL:"))
        assertTrue(response.contains("CRITICAL WARNING SIGNS"))
        assertTrue(response.contains("EVACUATION & SATELLITE SOS"))
    }
}
