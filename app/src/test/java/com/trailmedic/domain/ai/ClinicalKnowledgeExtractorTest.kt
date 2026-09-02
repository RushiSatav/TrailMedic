package com.trailmedic.domain.ai

import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.FirstAidIntentData
import com.trailmedic.domain.model.SymptomEmergencyData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClinicalKnowledgeExtractorTest {

    private lateinit var extractor: ClinicalKnowledgeExtractor

    @Before
    fun setUp() {
        extractor = ClinicalKnowledgeExtractor()
        val intents = listOf(
            FirstAidIntentData(
                tag = "Cuts",
                name = "Cuts & Minor Lacerations",
                patterns = listOf("What to do if Cuts?", "How to cure Cuts?", "Cuts", "cut my finger"),
                firstAidSteps = listOf("Wash the cut properly", "Apply gentle pressure", "Apply Petroleum Jelly", "Cover with sterile bandage"),
                warningSigns = listOf("Cut deeper than 1/4 inch", "Pus oozing"),
                triageQuestion = "Is the cut actively bleeding heavily?",
                evacuationNote = "Seek medical care if bleeding persists over 10 minutes."
            ),
            FirstAidIntentData(
                tag = "stings",
                name = "Bee & Wasp Stings",
                patterns = listOf("How do you treat Sting?", "Stings", "What to do if you get a sting?", "bee sting", "hornet sting"),
                firstAidSteps = listOf("Remove stinger immediately", "Apply ice pack for 15-20 minutes", "Apply hydrocortisone cream"),
                warningSigns = listOf("Difficulty breathing", "Swelling of throat"),
                triageQuestion = "Are they experiencing any throat tightness?",
                evacuationNote = "Use Epipen immediately if anaphylaxis develops."
            ),
            FirstAidIntentData(
                tag = "animal bite",
                name = "Animal Bite",
                patterns = listOf("How do you treat a animal bite?", "How do you treat a dog bite?", "i got bit by a dog"),
                firstAidSteps = listOf("Wash with soap and water for 15 minutes", "Apply pressure", "Apply antibiotic ointment"),
                warningSigns = listOf("Rabies exposure risk", "Deep puncture"),
                triageQuestion = "Was it a stray or wild animal?",
                evacuationNote = "Seek urgent Rabies and Tetanus vaccination."
            ),
            FirstAidIntentData(
                tag = "CPR",
                name = "Cardiopulmonary Resuscitation",
                patterns = listOf("How to give CPR??", "what to do in a CPR?", "chest compressions"),
                firstAidSteps = listOf("Place heel of hand on center of chest", "Push hard and fast at 100-120 bpm", "Compress 2 inches deep"),
                warningSigns = listOf("Unresponsive victim", "No breathing"),
                triageQuestion = "Is the person unresponsive and not breathing?",
                evacuationNote = "Continue CPR until emergency responders arrive."
            )
        )

        val emergencies = listOf(
            SymptomEmergencyData(
                id = "bite",
                name = "Snake / Insect Bite",
                triggerKeywords = listOf("snake", "bite", "viper", "cobra"),
                questions = listOf("Do you see 2 puncture marks?"),
                firstAidSteps = listOf("Keep patient calm and still", "Position limb at or below heart", "Never cut or suck venom"),
                warningSigns = listOf("Rapid swelling", "Difficulty swallowing"),
                evacuationNote = "Activate Satellite SOS."
            )
        )

        extractor.loadDatasets(intents, emergencies)
    }

    @Test
    fun extractKnowledgeForPrompt_withExactCutQuery_returnsCutProtocol() {
        val result = extractor.extractKnowledgeForPrompt("How to cure Cuts?")
        assertNotNull(result)
        assertEquals("Cuts", result?.conditionTag)
        assertTrue(result?.firstAidSteps?.any { it.contains("Wash") } == true)
        assertTrue(result?.triageQuestion?.contains("bleeding") == true)
    }

    @Test
    fun extractKnowledgeForPrompt_withBeeSting_returnsStingProtocol() {
        val result = extractor.extractKnowledgeForPrompt("I got a bee sting on my arm")
        assertNotNull(result)
        assertEquals("stings", result?.conditionTag)
        assertTrue(result?.firstAidSteps?.any { it.contains("stinger") } == true)
    }

    @Test
    fun extractKnowledgeForPrompt_withDogBite_returnsAnimalBiteProtocol() {
        val result = extractor.extractKnowledgeForPrompt("i got bit by a dog on the trail")
        assertNotNull(result)
        assertEquals("animal bite", result?.conditionTag)
        assertTrue(result?.firstAidSteps?.any { it.contains("soap") } == true)
    }

    @Test
    fun extractKnowledgeForPrompt_withCPRQuery_returnsCPRProtocol() {
        val result = extractor.extractKnowledgeForPrompt("How to give CPR??")
        assertNotNull(result)
        assertEquals("CPR", result?.conditionTag)
        assertTrue(result?.firstAidSteps?.any { it.contains("100-120") } == true)
    }

    @Test
    fun extractKnowledgeForPrompt_withTypoSanke_matchesSnakeBite() {
        val result = extractor.extractKnowledgeForPrompt("sanke bite on leg")
        assertNotNull(result)
        assertEquals("bite", result?.conditionTag)
    }

    @Test
    fun buildClinicalGroundingContext_producesFormattedRAGPrompt() {
        val extract = extractor.extractKnowledgeForPrompt("What to do if Cuts?")
        assertNotNull(extract)
        val grounding = extractor.buildClinicalGroundingContext(extract!!)
        assertTrue(grounding.contains("[VERIFIED CLINICAL FIRST AID PROTOCOL: CUTS & MINOR LACERATIONS]"))
        assertTrue(grounding.contains("IMMEDIATE FIRST AID ACTIONS:"))
        assertTrue(grounding.contains("CRITICAL WARNING SIGNS:"))
        assertTrue(grounding.contains("TRIAGE QUESTION:"))
    }

    @Test
    fun mapToCategory_mapsCorrectly() {
        assertEquals(EmergencyCategory.BLEEDING, extractor.mapToCategory("I cut my finger, deep gash"))
        assertEquals(EmergencyCategory.BITE, extractor.mapToCategory("wasp sting on leg"))
        assertEquals(EmergencyCategory.CARDIAC, extractor.mapToCategory("chest compressions cpr"))
    }
}
