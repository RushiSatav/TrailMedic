package com.trailmedic.domain.ai

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.repository.SymptomTreeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WildernessClinicalAIReasoner @Inject constructor(
    private val symptomTreeRepository: SymptomTreeRepository
) {

    /**
     * Dynamically detects the medical emergency topic from user conversation,
     * including typos (e.g. 'sanke', 'bleding', 'fractr') and multi-turn keywords.
     */
    fun detectEmergencyCategory(messages: List<Message>, defaultCategory: EmergencyCategory): EmergencyCategory {
        val userTexts = messages.filter { it.isUser }.joinToString(" ") { it.content.lowercase() }
        if (userTexts.isBlank()) return defaultCategory

        // 1. Snake / Venom / Bites
        if (containsAny(userTexts, "snake", "sanke", "snak", "viper", "cobra", "rattler", "krait", "bite", "bitten", "bit", "fang", "venom", "spider", "scorpion", "insect", "wasp", "bee", "sting", "stung")) {
            return EmergencyCategory.BITE
        }

        // 2. Severe Bleeding / Cuts / Wounds
        if (containsAny(userTexts, "blood", "bleed", "bleeding", "bleding", "cut", "wound", "stab", "gash", "laceration", "artery", "spurting", "hemorrhage", "sliced")) {
            return EmergencyCategory.BLEEDING
        }

        // 3. Fractures / Broken Bones / Falls
        if (containsAny(userTexts, "bone", "broken", "broke", "break", "snap", "snapped", "fracture", "fractr", "fall", "fell", "dislocation", "dislocated", "twist", "twisted", "sprain", "swollen ankle")) {
            return EmergencyCategory.FRACTURE
        }

        // 4. Breathing / Altitude / HAPE / HACE
        if (containsAny(userTexts, "breath", "breathing", "gasping", "wheezing", "asthma", "altitude", "mountain", "climbing", "hape", "hace", "choking", "choke", "suffocating", "airway")) {
            return EmergencyCategory.BREATHING
        }

        // 5. Hypothermia / Frostbite / Freezing
        if (containsAny(userTexts, "cold", "freeze", "freezing", "frostbite", "shiver", "shivering", "hypothermia", "frozen", "ice", "snow", "soaked", "drenched", "numb fingers")) {
            return EmergencyCategory.HYPOTHERMIA
        }

        // 6. Cardiac / Chest Pain / Heart Attack
        if (containsAny(userTexts, "heart", "cardiac", "chest pain", "pressure", "angina", "attack", "cpr", "pulse", "palpitations", "collapsed")) {
            return EmergencyCategory.CARDIAC
        }

        // 7. Head Injury / Concussion
        if (containsAny(userTexts, "head", "concussion", "skull", "dizzy", "dizziness", "blackout", "unconscious", "passed out", "fainted", "memory loss")) {
            return EmergencyCategory.HEAD
        }

        return defaultCategory
    }

    /**
     * Generates a contextually intelligent clinical response based on user inputs.
     */
    fun generateDynamicResponse(
        category: EmergencyCategory,
        messages: List<Message>,
        phase: ConversationPhase,
        questionIndex: Int
    ): String {
        val activeCategory = detectEmergencyCategory(messages, category)
        val data = symptomTreeRepository.getCategoryFallback(activeCategory)
        val userInputs = messages.filter { it.isUser }.map { it.content.lowercase() }
        val latestInput = userInputs.lastOrNull() ?: ""

        return when (phase) {
            ConversationPhase.INTERVIEWING -> generateInterviewQuestion(activeCategory, data, userInputs, latestInput, questionIndex)
            ConversationPhase.DIAGNOSING -> generateDiagnosisAndTreatment(activeCategory, data, userInputs)
        }
    }

    private fun generateInterviewQuestion(
        category: EmergencyCategory,
        data: SymptomEmergencyData,
        userInputs: List<String>,
        latestInput: String,
        questionIndex: Int
    ): String {
        // 0. Natural greetings / general inquiries
        if (isGreeting(latestInput)) {
            return "Hello! I'm TrailMedic, your offline wilderness emergency assistant. Please tell me what happened — are you or your companion injured, bitten, bleeding, or feeling unwell?"
        }

        // Dynamic context-aware responses tailored to specific clinical scenarios
        when (category) {
            EmergencyCategory.BITE -> {
                if (containsAny(latestInput, "saw", "look", "spot", "near") && !containsAny(latestInput, "bit", "bitten", "wound")) {
                    return "Did the snake bite your friend? If bitten, look at the area: do you see 2 distinct puncture marks, bleeding, or rapid swelling?"
                }
                if (containsAny(latestInput, "yes", "bit", "bitten", "leg", "arm", "hand", "foot", "ankle")) {
                    return "Understood. Keep him completely STILL — do NOT let him walk. Position the bitten limb below heart level. Is he feeling dizzy, nauseous, or having trouble breathing?"
                }
                if (containsAny(latestInput, "no", "did not", "didn't", "safe")) {
                    return "Relieved to hear no bite occurred. Move slowly away without sudden movements. Check him for any falls or panic-induced injuries."
                }
            }
            EmergencyCategory.BLEEDING -> {
                if (containsAny(latestInput, "artery", "spurting", "heavy", "lot of blood", "gushing")) {
                    return "CRITICAL: Apply direct, heavy pressure immediately with a clean cloth. Is the bleeding located on an arm or leg where a tourniquet can be placed above the wound?"
                }
                if (containsAny(latestInput, "stopped", "controlled", "slow")) {
                    return "Good work controlling the flow. Is there any object embedded inside the wound (rock, wood, metal), or is the wound clear?"
                }
            }
            EmergencyCategory.FRACTURE -> {
                if (containsAny(latestInput, "bone", "open", "sticking out", "skin")) {
                    return "Do NOT push the bone back in. Are you able to feel a pulse or warm blood circulation in the fingers or toes below the break?"
                }
                if (containsAny(latestInput, "ankle", "wrist", "twisted", "sprain")) {
                    return "Is the joint visibly bent out of its natural shape, or is there only swelling and tenderness when bearing weight?"
                }
            }
            EmergencyCategory.BREATHING -> {
                if (containsAny(latestInput, "altitude", "mountain", "high", "pass", "peak")) {
                    return "Are they coughing up wet/pink fluid, or walking with severe unsteadiness like they are intoxicated (signs of HAPE/HACE)?"
                }
            }
            EmergencyCategory.HYPOTHERMIA -> {
                if (containsAny(latestInput, "stopped shivering", "slurring", "confused", "stumble")) {
                    return "CRITICAL: Stopped shivering indicates Severe Hypothermia. Are their clothes currently wet, and do you have a sleeping bag or emergency shelter available right now?"
                }
            }
            else -> {}
        }

        // Fallback to indexed clinical question from tree if no specialized branch matches
        val qIdx = questionIndex.coerceIn(0, data.questions.lastIndex)
        return data.questions[qIdx]
    }

    private fun generateDiagnosisAndTreatment(
        category: EmergencyCategory,
        data: SymptomEmergencyData,
        userInputs: List<String>
    ): String {
        val stepsFormatted = data.firstAidSteps.mapIndexed { idx, step -> "${idx + 1}. $step" }.joinToString("\n")
        val warningsFormatted = data.warningSigns.joinToString("\n") { "• $it" }

        val summaryTitle = when (category) {
            EmergencyCategory.BITE -> "EMERGENCY ASSESSMENT: Suspected Snakebite / Envenomation Protocol"
            EmergencyCategory.BLEEDING -> "EMERGENCY ASSESSMENT: Severe Hemorrhage & Wound Management"
            EmergencyCategory.FRACTURE -> "EMERGENCY ASSESSMENT: Musculoskeletal Fracture / Immobilization"
            EmergencyCategory.BREATHING -> "EMERGENCY ASSESSMENT: Acute Respiratory Distress / Altitude Triage"
            EmergencyCategory.HYPOTHERMIA -> "EMERGENCY ASSESSMENT: Hypothermia & Cold Injury Protocol"
            EmergencyCategory.CARDIAC -> "EMERGENCY ASSESSMENT: Acute Cardiac Emergency Protocol"
            EmergencyCategory.HEAD -> "EMERGENCY ASSESSMENT: Traumatic Head Injury & Concussion Protocol"
            EmergencyCategory.GENERAL -> "EMERGENCY ASSESSMENT: Wilderness First Aid Protocol (${data.name})"
        }

        return """
$summaryTitle

ACTION PROTOCOL:
$stepsFormatted

CRITICAL WARNING SIGNS (Situation Worsening):
$warningsFormatted

EVACUATION & SATELLITE SOS:
${data.evacuationNote}
        """.trimIndent()
    }

    private fun isGreeting(text: String): Boolean {
        val trimmed = text.trim().lowercase()
        return trimmed in listOf("hi", "hii", "hiii", "hello", "hey", "heyy", "greetings", "good morning", "good evening", "who are you", "what can you do", "help")
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }
}
