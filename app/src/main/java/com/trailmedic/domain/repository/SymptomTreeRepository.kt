package com.trailmedic.domain.repository

import com.trailmedic.domain.model.ClinicalExtractionResult
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.FirstAidIntentData
import com.trailmedic.domain.model.SymptomEmergencyData

interface SymptomTreeRepository {
    fun getAllEmergencies(): List<SymptomEmergencyData>
    fun getEmergencyById(id: String): SymptomEmergencyData?
    fun findMatchingEmergency(query: String): SymptomEmergencyData?
    fun getCategoryFallback(category: EmergencyCategory): SymptomEmergencyData
    fun extractKnowledge(query: String): ClinicalExtractionResult?
    fun getFirstAidIntents(): List<FirstAidIntentData>
}

