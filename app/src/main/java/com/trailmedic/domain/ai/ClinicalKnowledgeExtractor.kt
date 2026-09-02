package com.trailmedic.domain.ai

import com.trailmedic.domain.model.ClinicalExtractionResult
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.FirstAidIntentData
import com.trailmedic.domain.model.SymptomEmergencyData
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class ClinicalKnowledgeExtractor @Inject constructor() {

    private val intents = mutableListOf<FirstAidIntentData>()
    private val emergencies = mutableListOf<SymptomEmergencyData>()

    /**
     * Initializes knowledge bases with loaded dataset elements.
     */
    fun loadDatasets(
        firstAidIntents: List<FirstAidIntentData>,
        symptomEmergencies: List<SymptomEmergencyData>
    ) {
        intents.clear()
        intents.addAll(firstAidIntents)
        emergencies.clear()
        emergencies.addAll(symptomEmergencies)
    }

    /**
     * Extracts the most relevant clinical first-aid protocol for a user query.
     */
    fun extractKnowledgeForPrompt(query: String): ClinicalExtractionResult? {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank() || isPureGreeting(cleanQuery)) {
            return null
        }

        var bestIntentMatch: FirstAidIntentData? = null
        var bestIntentScore = 0f
        var bestMatchedPattern: String? = null

        // 1. Search First Aid Intents Dataset (43+ Conditions)
        for (intent in intents) {
            val score = calculateIntentScore(cleanQuery, intent)
            if (score.first > bestIntentScore) {
                bestIntentScore = score.first
                bestIntentMatch = intent
                bestMatchedPattern = score.second
            }
        }

        // 2. Search Wilderness Emergencies Dataset
        var bestEmergencyMatch: SymptomEmergencyData? = null
        var bestEmergencyScore = 0f

        for (emergency in emergencies) {
            val score = calculateEmergencyScore(cleanQuery, emergency)
            if (score > bestEmergencyScore) {
                bestEmergencyScore = score
                bestEmergencyMatch = emergency
            }
        }

        // Return the highest confidence extract
        if (bestIntentScore >= bestEmergencyScore && bestIntentMatch != null && bestIntentScore >= 0.25f) {
            val steps = bestIntentMatch.firstAidSteps.ifEmpty { bestIntentMatch.responses }
            val responseText = bestIntentMatch.responses.firstOrNull()
                ?: steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")

            return ClinicalExtractionResult(
                conditionTag = bestIntentMatch.tag,
                conditionName = bestIntentMatch.name.ifBlank { bestIntentMatch.tag },
                confidence = bestIntentScore,
                firstAidSteps = steps,
                warningSigns = bestIntentMatch.warningSigns,
                triageQuestion = bestIntentMatch.triageQuestion.ifBlank { "How severe are the symptoms and when did this occur?" },
                evacuationNote = bestIntentMatch.evacuationNote.ifBlank { "Seek professional medical assistance if symptoms worsen or persist." },
                directResponse = responseText,
                matchedPattern = bestMatchedPattern
            )
        } else if (bestEmergencyMatch != null && bestEmergencyScore >= 0.25f) {
            val steps = bestEmergencyMatch.firstAidSteps
            val responseText = steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")

            return ClinicalExtractionResult(
                conditionTag = bestEmergencyMatch.id,
                conditionName = bestEmergencyMatch.name,
                confidence = bestEmergencyScore,
                firstAidSteps = steps,
                warningSigns = bestEmergencyMatch.warningSigns,
                triageQuestion = bestEmergencyMatch.questions.firstOrNull() ?: "What symptoms do you observe?",
                evacuationNote = bestEmergencyMatch.evacuationNote,
                directResponse = responseText,
                matchedPattern = bestEmergencyMatch.name
            )
        }

        return null
    }

    /**
     * Maps an extracted result or query to the closest EmergencyCategory.
     */
    fun mapToCategory(query: String, defaultCategory: EmergencyCategory = EmergencyCategory.GENERAL): EmergencyCategory {
        val lower = query.lowercase()
        val extract = extractKnowledgeForPrompt(query)
        val tag = extract?.conditionTag?.lowercase() ?: ""

        return when {
            tag in listOf("cpr", "cardiac") || containsAny(lower, "heart", "cardiac", "chest pain", "angina", "cardiac arrest", "cpr") -> EmergencyCategory.CARDIAC
            tag in listOf("snake bite", "stings", "insect bites", "bite", "animal bite") || containsAny(lower, "snake", "sanke", "viper", "cobra", "sting", "bee", "wasp", "spider", "scorpion", "hornet", "dog bite") -> EmergencyCategory.BITE
            tag in listOf("cuts", "abrasions", "wound", "normal bleeding", "bleeding") || containsAny(lower, "blood", "bleed", "cut", "wound", "scrape", "gash", "laceration", "spurting", "artery") -> EmergencyCategory.BLEEDING
            tag in listOf("fracture", "broken toe", "sprains", "strains", "pulled muscle") || containsAny(lower, "fracture", "bone", "broken", "snap", "sprain", "twisted ankle", "strain", "fall") -> EmergencyCategory.FRACTURE
            tag in listOf("choking", "breathing", "drowning", "nasal congestion", "cough", "sore throat", "cold") || containsAny(lower, "chok", "breath", "asthma", "wheez", "drown", "cough", "altitude") -> EmergencyCategory.BREATHING
            tag in listOf("frost bite", "hypothermia") || containsAny(lower, "cold", "frostbite", "shiver", "freez", "hypothermia", "snow", "ice") -> EmergencyCategory.HYPOTHERMIA
            tag in listOf("head injury", "headache", "fainting", "vertigo", "seizure", "head") || containsAny(lower, "head", "concussion", "skull", "dizzy", "faint", "seizure", "vertigo", "blackout") -> EmergencyCategory.HEAD
            else -> defaultCategory
        }
    }

    /**
     * Generates a structured Grounding Context string for Local AI Prompting (RAG).
     */
    fun buildClinicalGroundingContext(extract: ClinicalExtractionResult): String {
        val stepsFormatted = extract.firstAidSteps.take(6).mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")
        val warningsFormatted = if (extract.warningSigns.isNotEmpty()) {
            extract.warningSigns.take(3).joinToString("; ")
        } else "Condition worsening, infection signs, or unresponsiveness"

        return """
[VERIFIED CLINICAL FIRST AID PROTOCOL: ${extract.conditionName.uppercase()}]
IMMEDIATE FIRST AID ACTIONS:
$stepsFormatted

CRITICAL WARNING SIGNS:
$warningsFormatted

TRIAGE QUESTION:
${extract.triageQuestion}

EVACUATION / MEDICAL NOTE:
${extract.evacuationNote}
        """.trimIndent()
    }

    private fun calculateIntentScore(cleanQuery: String, intent: FirstAidIntentData): Pair<Float, String?> {
        val tagLower = intent.tag.lowercase()
        val nameLower = intent.name.lowercase()

        // 1. Direct tag match or typo-tolerant match
        if (cleanQuery == tagLower || cleanQuery == nameLower) {
            return 1.0f to intent.tag
        }

        // 2. Pattern exact and fuzzy matching
        var maxPatternScore = 0f
        var bestPattern: String? = null

        for (pattern in intent.patterns) {
            val pLower = pattern.lowercase().replace("?", "").replace("!", "").trim()
            if (cleanQuery == pLower) {
                return 1.0f to pattern
            }

            if (cleanQuery.contains(pLower) || pLower.contains(cleanQuery)) {
                val score = 0.90f
                if (score > maxPatternScore) {
                    maxPatternScore = score
                    bestPattern = pattern
                }
            }

            // Word overlap / Jaccard similarity
            val qTokens = tokenize(cleanQuery)
            val pTokens = tokenize(pLower)
            if (qTokens.isNotEmpty() && pTokens.isNotEmpty()) {
                val intersection = qTokens.intersect(pTokens).size
                val union = qTokens.union(pTokens).size
                val jaccard = intersection.toFloat() / union.toFloat()
                if (jaccard > maxPatternScore) {
                    maxPatternScore = jaccard
                    bestPattern = pattern
                }
            }
        }

        // 3. Keyword / Root token match
        val tagTokens = tokenize(tagLower) + tokenize(nameLower)
        val queryTokens = tokenize(cleanQuery)
        val tagMatches = tagTokens.count { tagTok -> queryTokens.any { qTok -> matchToken(qTok, tagTok) } }
        val tagScore = if (tagTokens.isNotEmpty()) (tagMatches.toFloat() / tagTokens.size.toFloat()) * 0.85f else 0f

        val finalScore = max(maxPatternScore, tagScore)
        return finalScore to bestPattern
    }

    private fun calculateEmergencyScore(cleanQuery: String, emergency: SymptomEmergencyData): Float {
        val qTokens = tokenize(cleanQuery)
        val kwTokens = emergency.triggerKeywords.flatMap { tokenize(it) }.distinct()

        if (cleanQuery.contains(emergency.name.lowercase()) || cleanQuery.contains(emergency.id.lowercase())) {
            return 0.92f
        }

        val matches = kwTokens.count { kw -> qTokens.any { qTok -> matchToken(qTok, kw) } }
        return if (kwTokens.isNotEmpty()) (matches.toFloat() / max(1, qTokens.size).toFloat()).coerceAtMost(0.88f) else 0f
    }

    private fun tokenize(text: String): Set<String> {
        val stopWords = setOf("what", "to", "do", "if", "how", "cure", "treat", "which", "medicine", "apply", "take", "i", "get", "got", "my", "me", "a", "an", "the", "for", "is", "in", "on", "and", "or", "of", "with")
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords }
            .toSet()
    }

    private fun matchToken(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.startsWith(b) || b.startsWith(a)) return true
        // Handle common medical/wilderness typos
        if (isTypoOf(a, b)) return true
        return false
    }

    private fun isTypoOf(s1: String, s2: String): Boolean {
        if (kotlin.math.abs(s1.length - s2.length) > 1) return false
        val pairs = listOf(
            "sanke" to "snake",
            "bleding" to "bleeding",
            "chokng" to "choking",
            "bruis" to "bruise",
            "fractr" to "fracture",
            "splintr" to "splinter",
            "poisn" to "poison",
            "faint" to "fainting",
            "vertgo" to "vertigo"
        )
        return pairs.any { (p1, p2) -> (s1 == p1 && s2 == p2) || (s1 == p2 && s2 == p1) }
    }

    private fun isPureGreeting(text: String): Boolean {
        val trimmed = text.trim().lowercase()
        return trimmed in listOf("hi", "hii", "hiii", "hello", "hey", "heyy", "greetings", "good morning", "good evening", "who are you")
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }
}
