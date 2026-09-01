package com.trailmedic.data.llm

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message

object ConversationManager {

    private val SYSTEM_PROMPT = """
        You are TrailMedic, an offline emergency first aid assistant.
        Give clear, direct, and brief guidance.
        Maximum 2 short sentences. Do not repeat.
    """.trimIndent()

    fun buildPrompt(messages: List<Message>, phase: ConversationPhase): String {
        val sb = StringBuilder()

        // Filter out empty messages
        val validMessages = messages.filter { it.content.isNotBlank() }

        // Find the first user message
        val firstUserIdx = validMessages.indexOfFirst { it.isUser }

        if (firstUserIdx == -1) {
            val instruction = if (phase == ConversationPhase.INTERVIEWING) {
                "Ask one direct question to assess the emergency or injury."
            } else {
                "Provide immediate numbered first aid instructions for a wilderness emergency."
            }
            sb.append("<start_of_turn>user\n")
            sb.append(SYSTEM_PROMPT).append("\n\n")
            sb.append(instruction).append("\n")
            sb.append("<end_of_turn>\n")
            sb.append("<start_of_turn>model\n")
            return sb.toString()
        }

        // Keep dialogue window bounded to last 6 messages to keep prefill fast and light
        val rawDialogue = validMessages.subList(firstUserIdx, validMessages.size)
        val dialogueFromFirstUser = if (rawDialogue.size > 6) rawDialogue.takeLast(6) else rawDialogue

        var lastRole: String? = null

        for (i in dialogueFromFirstUser.indices) {
            val msg = dialogueFromFirstUser[i]
            val role = if (msg.isUser) "user" else "model"

            // Prevent consecutive duplicate turns with the same role
            if (role == lastRole) {
                val lastEndIdx = sb.lastIndexOf("<end_of_turn>\n")
                if (lastEndIdx != -1) {
                    sb.delete(lastEndIdx, sb.length)
                    sb.append("\n").append(msg.content.trim()).append("\n<end_of_turn>\n")
                }
                continue
            }

            sb.append("<start_of_turn>").append(role).append("\n")
            if (i == 0) {
                sb.append(SYSTEM_PROMPT).append("\n\n")
                sb.append("User Emergency Report: ").append(msg.content.trim()).append("\n")
            } else {
                sb.append(msg.content.trim()).append("\n")
            }
            sb.append("<end_of_turn>\n")
            lastRole = role
        }

        if (lastRole == "model") {
            val nextInstruction = if (phase == ConversationPhase.INTERVIEWING) {
                "Ask your next single clarifying triage question."
            } else {
                "Provide numbered first aid steps, warning signs, and evacuation advice."
            }
            sb.append("<start_of_turn>user\n").append(nextInstruction).append("\n<end_of_turn>\n")
        }

        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }
}

