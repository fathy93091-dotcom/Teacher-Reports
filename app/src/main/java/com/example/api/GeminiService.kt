package com.example.api

import android.util.Log
import com.example.data.DailyLesson
import com.example.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    suspend fun analyzeDailyReport(
        apiKey: String,
        model: String,
        student: Student,
        lessonNumber: Int,
        lessonDate: String,
        durationMinutes: Int,
        teacherName: String,
        activeSubjects: List<String>,
        arabicReportText: String,
        globalInstructions: String = "",
        studentInstructions: String = ""
    ): DailyAnalysisResult = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are an experienced Islamic educator, Quran teacher, and Arabic language expert assistant.
            Your job is to read daily lesson logs written in Arabic, understand the educational context, translate the details to warm, professional, parent-friendly English, and extract structured metrics.

            Core Principles:
            1. Accuracy & Honesty: Never invent information. Never exaggerate achievements or weaknesses.
            2. English Tone: Write in clear, warm, parent-friendly, professional English. Preserve Islamic terminology (e.g. Surah names, Tajweed rules like Ghunnah, Qalqalah, Ikhfa, Qa'idah Nooraniah, Arabic grammar terms) but format them in a readable English form.
            3. Shorthand & Abbreviations Decoding:
               - Identify Quranic chapters shorthand: e.g. "حفظ الملك ١-٥" means "Memorised Surah Al-Mulk, verses 1 to 5"; "النبأ" means "Surah An-Naba"; "عم" means "Juzz Amma".
               - Identify Tajweed shorthand: e.g. "المد" means "Tajweed elongation rule (Madd)"; "الغنة" means "nasalization rule (Ghunnah)"; "قلقلة" means "vibration rule (Qalqalah)".
               - Identify foundational books: e.g. "القاعدة" or "نورانية" means "Qa'idah Nooraniah foundation reading book".
            4. Custom Writing Preferences: You must customize the wording, focus, and layout based on the teacher's specifications below:
               - Teacher's General Writing Preferences: "${globalInstructions.ifBlank { "No specific preference." }}"
               - Instructions for this Student (${student.name}): "${studentInstructions.ifBlank { "No specific instructions." }}"

            You must respond with a JSON object matching this schema. Do not wrap the JSON in Markdown block ticks (no ```json or similar):
            {
              "subjectsTaught": ["Quran", "Arabic", "Islamic Studies"], // only include subjects that are actually discussed in the report text
              "reportEnglish": "A detailed, paragraph-form English translation and description of the lesson. Incorporate student name, lesson date, lesson number, lesson duration, and teacher's name exactly. Translate specific events into clear English recitation and revision bullet points. Make it warm, clear, and direct for parents to read, incorporating the teacher's style preferences.",
              "achievements": ["achievement 1", "achievement 2"],
              "weaknesses": ["weakness 1", "weakness 2"],
              "homeworkAssigned": "Description of homework assigned",
              "homeworkCompleted": true, // true if explicitly marked complete/done, false if marked not done, null if not mentioned
              "grade": "Excellent" // select exactly one: "Excellent", "Good", "Steady", or "Needs Improvement" based on lesson
            }
        """.trimIndent()

        val userPrompt = """
            Student Name: ${student.name}
            Lesson Number: $lessonNumber
            Lesson Date: $lessonDate
            Duration: $durationMinutes minutes
            Teacher Name: $teacherName
            Active Enrolled Subjects: ${activeSubjects.joinToString(", ")}
            Daily Lesson Report in Arabic:
            \"\"\"
            $arabicReportText
            \"\"\"
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = GeminiClient.service.generateContent(model, apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini API.")
            
            // Clean markdown blocks if Gemini included them
            val cleanJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = GeminiClient.moshiParser.adapter(DailyAnalysisResult::class.java)
            adapter.fromJson(cleanJson) ?: throw Exception("JSON Parsing returned null")
        } catch (e: Exception) {
            Log.e("GeminiService", "analyzeDailyReport failed", e)
            throw e
        }
    }

    suspend fun generateMonthlyReportDraft(
        apiKey: String,
        model: String,
        student: Student,
        month: String,
        dailyLessons: List<DailyLesson>,
        globalInstructions: String = "",
        studentInstructions: String = ""
    ): MonthlyAnalysisResult = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are an experienced Islamic educator, Quran teacher, and Arabic language expert assistant.
            Your job is to compile a professional, evidence-based monthly evaluation report for a student based *only* on their daily lesson logs from this month.

            Core Principles:
            1. Evidence-Based: Every monthly conclusion must be supported by observations in the daily reports. Do not invent achievements, weaknesses, or recommendations.
            2. Honest & Encouraging: Never hide educational weaknesses, but maintain a positive, warm, encouraging tone suitable for parents.
            3. Preserving Terminology: Use correct and accurate Islamic/Arabic terms (e.g. Tajweed rules, Quranic terms) in English. Decipher abbreviations correctly.
            4. Custom Writing Preferences: Apply these specific style directions to structure, focus, or phrasing:
               - Teacher's General Writing Preferences: "${globalInstructions.ifBlank { "No specific preference." }}"
               - Instructions for this Student (${student.name}): "${studentInstructions.ifBlank { "No specific instructions." }}"
            5. If no lessons are provided, or if there is insufficient evidence for a subject or indicator, state "No lessons recorded" or "Insufficient evidence to evaluate" instead of guessing.

            Evaluate 4 Performance Indicators. Each indicator must have a score (exactly: "Excellent", "Good", "Steady", "Needs Improvement") and a brief supporting comment:
            - Memorisation Progress (focus on how much was memorized and retention)
            - Revision Strength (how well does the student retain older memorised parts)
            - Tajweed & Foundation (pronunciation, rules, reading fluency)
            - Commitment (attendance, focus, homework completion rate)

            Provide qualitative evaluations for active subjects:
            - New Memorisation (for Quran students): what was memorised, pace, accuracy, or null if not active
            - Revision (for Quran students): what was revised, retention quality, or null if not active
            - Reading (for Quran students): reading fluency, foundation rules, or null if not active
            - Arabic Language Evaluation (for Arabic students): grammar, vocabulary, reading/writing progress, or null if not active
            - Islamic Studies Evaluation (for Islamic Studies students): lesson understanding, Islamic manners, religious knowledge, or null if not active

            Provide:
            - Strengths: list of actual strengths observed this month
            - Recommendations: practical, actionable suggestions for home study
            - Next Month's Study Plan: targets for next month based on progress

            You must respond with a JSON object matching this schema. Do not wrap the JSON in Markdown block ticks (no ```json or similar):
            {
              "indicators": {
                "memorisationProgress": { "score": "Excellent/Good/Steady/Needs Improvement", "comment": "comment..." },
                "revisionStrength": { "score": "Excellent/Good/Steady/Needs Improvement", "comment": "comment..." },
                "tajweedFoundation": { "score": "Excellent/Good/Steady/Needs Improvement", "comment": "comment..." },
                "commitment": { "score": "Excellent/Good/Steady/Needs Improvement", "comment": "comment..." }
              },
              "sections": {
                "newMemorisation": "detailed English paragraph, or null if not Quran active",
                "revision": "detailed English paragraph, or null if not Quran active",
                "reading": "detailed English paragraph, or null if not Quran active",
                "arabicEvaluation": "detailed English paragraph, or null if not Arabic active",
                "islamicStudiesEvaluation": "detailed English paragraph, or null if not Islamic Studies active"
              },
              "strengths": ["strength 1", "strength 2"],
              "recommendations": ["recommendation 1", "recommendation 2"],
              "nextMonthPlan": "A warm, cohesive study plan paragraph outlining goals for the upcoming month."
            }
        """.trimIndent()

        val formattedLessons = dailyLessons.mapIndexed { idx, lesson ->
            """
                ---
                Lesson #${idx + 1}
                Date: ${lesson.date}
                Duration: ${lesson.duration} mins
                Subjects Taught: ${lesson.subjectsTaught.joinToString(", ")}
                Report (Arabic): ${lesson.reportArabic}
                Report (English): ${lesson.reportEnglish}
                Achievements: ${lesson.achievements.joinToString(", ")}
                Weaknesses: ${lesson.weaknesses.joinToString(", ")}
                Homework: ${lesson.homeworkAssigned}
                Homework Completed: ${when(lesson.homeworkCompleted) { true -> "Yes" false -> "No" else -> "Not mentioned" }}
            """.trimIndent()
        }.joinToString("\n")

        val userPrompt = """
            Student Name: ${student.name}
            Evaluation Month: $month
            Student Active Enrolled Subjects: ${student.subjects.joinToString(", ")}

            Here are the daily lesson records for this month:
            =========================================
            $formattedLessons
            =========================================

            Analyze the above reports, search for patterns, and generate the structured monthly evaluation draft in JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = GeminiClient.service.generateContent(model, apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini API.")
            
            val cleanJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = GeminiClient.moshiParser.adapter(MonthlyAnalysisResult::class.java)
            adapter.fromJson(cleanJson) ?: throw Exception("JSON Parsing returned null")
        } catch (e: Exception) {
            Log.e("GeminiService", "generateMonthlyReportDraft failed", e)
            throw e
        }
    }
}
