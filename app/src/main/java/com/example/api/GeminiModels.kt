package com.example.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Daily Lesson Analysis Models parsed from Gemini JSON output ---
@JsonClass(generateAdapter = true)
data class DailyAnalysisResult(
    val subjectsTaught: List<String>,
    val reportEnglish: String,
    val achievements: List<String>,
    val weaknesses: List<String>,
    val homeworkAssigned: String,
    val homeworkCompleted: Boolean?,
    val grade: String // Excellent, Good, Steady, Needs Improvement
)

// --- Monthly Evaluation Compilation Models parsed from Gemini JSON output ---
@JsonClass(generateAdapter = true)
data class MonthlyAnalysisResult(
    val indicators: MonthlyIndicators,
    val sections: MonthlySections,
    val strengths: List<String>,
    val recommendations: List<String>,
    val nextMonthPlan: String
)

@JsonClass(generateAdapter = true)
data class MonthlyIndicators(
    val memorisationProgress: IndicatorDetail,
    val revisionStrength: IndicatorDetail,
    val tajweedFoundation: IndicatorDetail,
    val commitment: IndicatorDetail
)

@JsonClass(generateAdapter = true)
data class IndicatorDetail(
    val score: String, // Excellent, Good, Steady, Needs Improvement
    val comment: String
)

@JsonClass(generateAdapter = true)
data class MonthlySections(
    val newMemorisation: String?,
    val revision: String?,
    val reading: String?,
    val arabicEvaluation: String?,
    val islamicStudiesEvaluation: String?
)
