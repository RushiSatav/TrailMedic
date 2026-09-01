package com.trailmedic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val emergencyType: String,
    val messagesJson: String, // Gson serialized List<Message>
    val firstAidSummary: String,
    val timestamp: Long,
    val durationSeconds: Long,
    val outcomeNote: String
) {
    fun toDomain(gson: Gson): Session {
        val type = object : TypeToken<List<Message>>() {}.type
        val messages: List<Message> = try {
            gson.fromJson(messagesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return Session(
            id = id,
            emergencyType = emergencyType,
            messages = messages,
            firstAidSummary = firstAidSummary,
            timestamp = timestamp,
            durationSeconds = durationSeconds,
            outcomeNote = outcomeNote
        )
    }

    companion object {
        fun fromDomain(session: Session, gson: Gson): SessionEntity {
            return SessionEntity(
                id = session.id,
                emergencyType = session.emergencyType,
                messagesJson = gson.toJson(session.messages),
                firstAidSummary = session.firstAidSummary,
                timestamp = session.timestamp,
                durationSeconds = session.durationSeconds,
                outcomeNote = session.outcomeNote
            )
        }
    }
}
