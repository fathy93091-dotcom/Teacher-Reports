package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val subjects: List<String> = emptyList(),
    val notes: String = "",
    val customInstructions: String = "",
    val registrationDate: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "daily_lessons")
data class DailyLesson(
    @PrimaryKey val id: String = "",
    val studentId: String = "",
    val lessonNumber: Int = 1,
    val date: String = "",
    val duration: Int = 60, // in minutes
    val teacherName: String = "",
    val subjectsTaught: List<String> = emptyList(),
    val reportArabic: String = "",
    val reportEnglish: String = "",
    val achievements: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val homeworkAssigned: String = "",
    val homeworkCompleted: Boolean? = null, // true = Yes, false = No, null = Not Mentioned
    val grade: String = "", // Excellent, Good, Steady, Needs Improvement
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "monthly_reports")
data class MonthlyReport(
    @PrimaryKey val id: String = "",
    val studentId: String = "",
    val month: String = "", // YYYY-MM
    val teacherName: String = "",
    val generatedAt: String = "",
    
    val memorisationProgressScore: String = "",
    val memorisationProgressComment: String = "",
    
    val revisionStrengthScore: String = "",
    val revisionStrengthComment: String = "",
    
    val tajweedFoundationScore: String = "",
    val tajweedFoundationComment: String = "",
    
    val commitmentScore: String = "",
    val commitmentComment: String = "",
    
    val newMemorisation: String? = null,
    val revision: String? = null,
    val reading: String? = null,
    val arabicEvaluation: String? = null,
    val islamicStudiesEvaluation: String? = null,
    
    val strengths: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val nextMonthPlan: String = "",
    val isApproved: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String = "",
    val studentId: String = "",
    val title: String = "",
    val description: String = "",
    val progress: Int = 0, // 0 to 100 percentage
    val isCompleted: Boolean = false,
    val dateCreated: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString("|||")
    }
}
