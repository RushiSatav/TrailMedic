package com.trailmedic.data.llm

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData

object ConversationManager {

    fun buildPrompt(
        messages: List<Message>,
        phase: ConversationPhase,
        clinicalData: SymptomEmergencyData? = null
    ): String {
        val validMessages = messages.filter { it.content.isNotBlank() }
        val firstUserIdx = validMessages.indexOfFirst { it.isUser }

        val sb = StringBuilder()

        if (firstUserIdx == -1) {
            sb.append("<start_of_turn>user\n")
            sb.append("You are MediTrail, an offline wilderness emergency first aid assistant. Greet the hiker in 1-2 friendly, simple sentences and ask what emergency or injury happened.\n")
            sb.append("<end_of_turn>\n<start_of_turn>model\n")
            return sb.toString()
        }

        val rawDialogue = validMessages.subList(firstUserIdx, validMessages.size)
        val dialogueWindow = if (rawDialogue.size > 6) rawDialogue.takeLast(6) else rawDialogue

        // Structured Clinical Knowledge (RAG) Grounding
        val clinicalKnowledge = clinicalData?.let { data ->
            val topSteps = data.firstAidSteps.take(6).mapIndexed { idx, step -> "${idx + 1}. $step" }.joinToString("\n")
            val topWarnings = data.warningSigns.take(3).joinToString("; ")
            val triageQ = data.questions.firstOrNull() ?: "What symptoms do you observe?"
            """
[VERIFIED FIRST AID PROTOCOL: ${data.name.uppercase()}]
IMMEDIATE FIRST AID ACTIONS:
$topSteps

CRITICAL WARNING SIGNS:
$topWarnings

TRIAGE QUESTION:
$triageQ

EVACUATION:
${data.evacuationNote}
            """.trimIndent()
        } ?: ""

        var lastRole: String? = null

        for (i in dialogueWindow.indices) {
            val msg = dialogueWindow[i]
            val role = if (msg.isUser) "user" else "model"

            // Prevent duplicate consecutive turns
            if (role == lastRole) {
                val lastEndIdx = sb.lastIndexOf("<end_of_turn>\n")
                if (lastEndIdx != -1) {
                    sb.delete(lastEndIdx, sb.length)
                    sb.append("\n").append(msg.content.trim()).append("\n<end_of_turn>\n")
                }
                continue
            }

            sb.append("<start_of_turn>").append(role).append("\n")
            if (i == 0 && msg.isUser) {
                val cleanUserMsg = msg.content.trim()
                val isGreeting = isGreetingText(cleanUserMsg)

                if (isGreeting) {
                    sb.append("You are MediTrail, an offline emergency wilderness first aid assistant.\n")
                    sb.append("Hiker: $cleanUserMsg\n\n")
                    sb.append("Respond as MediTrail in 1-2 friendly, simple sentences: greet the hiker and ask what emergency happened.")
                } else if (phase == ConversationPhase.INTERVIEWING) {
                    sb.append("You are MediTrail, an offline emergency first aid helper.\n")
                    if (clinicalKnowledge.isNotBlank()) {
                        sb.append(clinicalKnowledge).append("\n\n")
                    }
                    sb.append("Hiker Report: $cleanUserMsg\n\n")
                    sb.append("Task: Output immediate first aid action points as a numbered list (1., 2., 3., etc.), followed by a triage question. Use simple, everyday, easy-to-understand words that any normal person can immediately follow. Never use dense medical jargon. Always format instructions in separate points with line breaks.")
                } else {
                    sb.append("You are MediTrail, an offline wilderness first aid assistant.\n")
                    if (clinicalKnowledge.isNotBlank()) {
                        sb.append(clinicalKnowledge).append("\n\n")
                    }
                    sb.append("Emergency situation: $cleanUserMsg\n\n")
                    sb.append("Provide the complete action plan in simple, easy-to-understand words formatted point-by-point: 1. Action steps (numbered), 2. Warning signs (bullet points), 3. Evacuation advice.")
                }
            } else if (i == dialogueWindow.lastIndex && msg.isUser) {
                val cleanUserMsg = msg.content.trim()
                val isGreeting = isGreetingText(cleanUserMsg)

                if (isGreeting) {
                    sb.append("$cleanUserMsg\n(Respond directly as MediTrail in simple, friendly words: greet me and ask what emergency happened.)")
                } else if (phase == ConversationPhase.INTERVIEWING) {
                    if (clinicalKnowledge.isNotBlank()) {
                        sb.append(clinicalKnowledge).append("\n")
                    }
                    sb.append("Hiker: $cleanUserMsg\n(Acknowledge my answer in simple everyday words, provide next-step instructions formatted in separate numbered points, and ask the next triage question. Never repeat the previous question and avoid complex medical jargon.)")
                } else {
                    if (clinicalKnowledge.isNotBlank()) {
                        sb.append(clinicalKnowledge).append("\n")
                    }
                    sb.append("Hiker: $cleanUserMsg\n(Provide numbered first aid steps, warning signs in bullet points, and evacuation advice using simple, plain English. Keep all points on separate lines.)")
                }
            } else {
                sb.append(msg.content.trim()).append("\n")
            }
            sb.append("<end_of_turn>\n")
            lastRole = role
        }

        if (lastRole == "model") {
            val directive = if (phase == ConversationPhase.INTERVIEWING) {
                "Provide the first aid action steps in simple numbered points and ask the next triage question."
            } else {
                "Provide the complete step-by-step first aid instructions and evacuation advice now in simple, plain English points."
            }
            sb.append("<start_of_turn>user\n").append(directive).append("\n<end_of_turn>\n")
        }

        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }

    private fun isGreetingText(text: String): Boolean {
        val clean = text.trim().lowercase()
        return clean in listOf("hi", "hii", "hiii", "hello", "hey", "heyy", "greetings", "good morning", "good evening")
    }
}
