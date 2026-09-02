package com.trailmedic.domain.model

enum class EmergencyCategory(val id: String, val label: String, val keywords: String, val iconName: String) {
    FRACTURE("fracture", "Fracture / Fall", "fall bone fracture snap leg arm broken dislocation", "broken_image"),
    BREATHING("breathing", "Breathing Problem", "breathing chest breath inhale asthma wheezing hape altitude", "air"),
    BLEEDING("bleeding", "Bleeding / Wound", "blood cut wound bleeding hemorrhage laceration", "water_drop"),
    HYPOTHERMIA("hypothermia", "Hypothermia / Cold", "cold shiver hypothermia freeze frostbite winter snow", "ac_unit"),
    BITE("bite", "Snake / Insect Bite", "snake bite insect sting venom spider scorpion sting", "pest_control"),
    CARDIAC("cardiac", "Cardiac / Heart", "heart chest pain cardiac attack cpr pressure", "favorite"),
    HEAD("head", "Head Injury", "head concussion dizzy unconscious blackout skull", "psychology"),
    GENERAL("general", "Other Emergency", "help emergency medical general heat illness", "medical_services");

    companion object {
        fun fromId(id: String?): EmergencyCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}

data class SymptomEmergencyData(
    val id: String,
    val name: String,
    val triggerKeywords: List<String>,
    val questions: List<String>,
    val firstAidSteps: List<String>,
    val warningSigns: List<String>,
    val evacuationNote: String
)

data class SymptomTreeRoot(
    val version: Int,
    val emergencies: List<SymptomEmergencyData>
)

data class FirstAidIntentData(
    val tag: String,
    val name: String,
    val patterns: List<String> = emptyList(),
    val firstAidSteps: List<String> = emptyList(),
    val warningSigns: List<String> = emptyList(),
    val triageQuestion: String = "",
    val evacuationNote: String = "",
    val responses: List<String> = emptyList()
) {
    fun toSymptomEmergencyData(): SymptomEmergencyData {
        return SymptomEmergencyData(
            id = tag.lowercase().replace(" ", "_"),
            name = name.ifBlank { tag },
            triggerKeywords = patterns + tag,
            questions = if (triageQuestion.isNotBlank()) listOf(triageQuestion) else listOf("How severe is the condition?", "Is the patient conscious and breathing?"),
            firstAidSteps = firstAidSteps.ifEmpty { responses },
            warningSigns = warningSigns,
            evacuationNote = evacuationNote.ifBlank { "If symptoms worsen or do not improve, seek medical assistance." }
        )
    }
}

data class ClinicalExtractionResult(
    val conditionTag: String,
    val conditionName: String,
    val confidence: Float,
    val firstAidSteps: List<String>,
    val warningSigns: List<String>,
    val triageQuestion: String,
    val evacuationNote: String,
    val directResponse: String,
    val matchedPattern: String? = null
)

