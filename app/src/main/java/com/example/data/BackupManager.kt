package com.example.data

import com.squareup.moshi.JsonClass
import com.example.api.GeminiClient

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val version: Int,
    val exportedAt: String,
    val students: List<BackupStudent>,
    val lessons: List<BackupLesson>,
    val reports: List<BackupReport>
)

@JsonClass(generateAdapter = true)
data class BackupStudent(
    val id: String,
    val name: String,
    val subjects: List<String>,
    val notes: String,
    val customInstructions: String,
    val registrationDate: String
)

@JsonClass(generateAdapter = true)
data class BackupLesson(
    val id: String,
    val studentId: String,
    val lessonNumber: Int,
    val date: String,
    val duration: Int,
    val teacherName: String,
    val subjectsTaught: List<String>,
    val reportArabic: String,
    val reportEnglish: String,
    val achievements: List<String>,
    val weaknesses: List<String>,
    val homeworkAssigned: String,
    val homeworkCompleted: Boolean?,
    val grade: String
)

@JsonClass(generateAdapter = true)
data class BackupReport(
    val id: String,
    val studentId: String,
    val month: String,
    val teacherName: String,
    val generatedAt: String,
    val memorisationProgressScore: String,
    val memorisationProgressComment: String,
    val revisionStrengthScore: String,
    val revisionStrengthComment: String,
    val tajweedFoundationScore: String,
    val tajweedFoundationComment: String,
    val commitmentScore: String,
    val commitmentComment: String,
    val newMemorisation: String?,
    val revision: String?,
    val reading: String?,
    val arabicEvaluation: String?,
    val islamicStudiesEvaluation: String?,
    val strengths: List<String>,
    val recommendations: List<String>,
    val nextMonthPlan: String,
    val isApproved: Boolean
)

object BackupManager {

    fun exportToJson(
        students: List<Student>,
        lessons: List<DailyLesson>,
        reports: List<MonthlyReport>
    ): String {
        val payload = BackupPayload(
            version = 2,
            exportedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
            students = students.map { BackupStudent(it.id, it.name, it.subjects, it.notes, it.customInstructions, it.registrationDate) },
            lessons = lessons.map {
                BackupLesson(
                    it.id, it.studentId, it.lessonNumber, it.date, it.duration, it.teacherName,
                    it.subjectsTaught, it.reportArabic, it.reportEnglish, it.achievements, it.weaknesses,
                    it.homeworkAssigned, it.homeworkCompleted, it.grade
                )
            },
            reports = reports.map {
                BackupReport(
                    it.id, it.studentId, it.month, it.teacherName, it.generatedAt,
                    it.memorisationProgressScore, it.memorisationProgressComment,
                    it.revisionStrengthScore, it.revisionStrengthComment,
                    it.tajweedFoundationScore, it.tajweedFoundationComment,
                    it.commitmentScore, it.commitmentComment,
                    it.newMemorisation, it.revision, it.reading, it.arabicEvaluation, it.islamicStudiesEvaluation,
                    it.strengths, it.recommendations, it.nextMonthPlan, it.isApproved
                )
            }
        )
        val adapter = GeminiClient.moshiParser.adapter(BackupPayload::class.java).indent("  ")
        return adapter.toJson(payload)
    }

    fun importFromJson(jsonStr: String): BackupPayload? {
        val adapter = GeminiClient.moshiParser.adapter(BackupPayload::class.java)
        return adapter.fromJson(jsonStr)
    }
}
