package com.example.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.MonthlyReport
import com.example.data.Student

object ReportExportHelper {

    fun printHtml(context: Context, htmlContent: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }

    fun generateCombinedHtmlReport(
        reports: List<MonthlyReport>,
        students: List<Student>,
        month: String,
        language: String
    ): String {
        val isArabic = language == "ar"
        val title = if (isArabic) "التقرير الشهري المجمع - لشهر $month" else "Combined Monthly Reports - $month"
        
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("""
            <!DOCTYPE html>
            <html lang="${if (isArabic) "ar" else "en"}" dir="${if (isArabic) "rtl" else "ltr"}">
            <head>
                <meta charset="utf-8">
                <title>$title</title>
                <style>
                    body {
                        font-family: 'Amiri', 'Georgia', 'Arial', serif;
                        background-color: #fcfcfc;
                        color: #333;
                        margin: 0;
                        padding: 20px;
                        direction: ${if (isArabic) "rtl" else "ltr"};
                        line-height: 1.6;
                    }
                    .report-page {
                        background: #ffffff;
                        border: 2px solid #137333;
                        border-radius: 12px;
                        padding: 30px;
                        margin-bottom: 40px;
                        box-shadow: 0 4px 10px rgba(0,0,0,0.05);
                        page-break-after: always;
                    }
                    .header {
                        text-align: center;
                        border-bottom: 3px double #137333;
                        padding-bottom: 15px;
                        margin-bottom: 25px;
                    }
                    .header h1 {
                        color: #137333;
                        margin: 0 0 10px 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .header h2 {
                        color: #b8860b;
                        margin: 0;
                        font-size: 20px;
                    }
                    .meta-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 15px;
                        background-color: #f4fbf6;
                        padding: 15px;
                        border-radius: 8px;
                        margin-bottom: 25px;
                        border-right: ${if (isArabic) "5px solid #137333" else "none"};
                        border-left: ${if (isArabic) "none" else "5px solid #137333"};
                    }
                    .meta-item {
                        font-size: 16px;
                    }
                    .meta-label {
                        font-weight: bold;
                        color: #137333;
                    }
                    .section-title {
                        color: #137333;
                        border-bottom: 2px solid #f0f0f0;
                        padding-bottom: 5px;
                        margin-top: 25px;
                        margin-bottom: 15px;
                        font-size: 18px;
                        font-weight: bold;
                    }
                    .indicators-table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 25px;
                    }
                    .indicators-table th {
                        background-color: #137333;
                        color: white;
                        text-align: ${if (isArabic) "right" else "left"};
                        padding: 12px 10px;
                        font-size: 15px;
                    }
                    .indicators-table td {
                        padding: 12px 10px;
                        border-bottom: 1px solid #eeeeee;
                        font-size: 15px;
                    }
                    .rating-badge {
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 20px;
                        font-weight: bold;
                        font-size: 13px;
                        text-align: center;
                    }
                    .rating-Excellent { background-color: #d4edda; color: #155724; }
                    .rating-Good { background-color: #fff3cd; color: #856404; }
                    .rating-Steady { background-color: #e2e3e5; color: #383d41; }
                    .rating-NeedsImprovement { background-color: #f8d7da; color: #721c24; }
                    
                    .bullet-list {
                        margin: 10px 0;
                        padding-${if (isArabic) "right" else "left"}: 20px;
                    }
                    .bullet-list li {
                        margin-bottom: 8px;
                        font-size: 15px;
                    }
                    .plan-box {
                        background-color: #fdfaf2;
                        border-right: ${if (isArabic) "5px solid #b8860b" else "none"};
                        border-left: ${if (isArabic) "none" else "5px solid #b8860b"};
                        padding: 15px;
                        border-radius: 8px;
                        font-size: 15px;
                    }
                    @media print {
                        body { background: none; padding: 0; }
                        .report-page {
                            box-shadow: none;
                            border: 2px solid #137333;
                            page-break-after: always;
                            margin-bottom: 0;
                        }
                    }
                </style>
            </head>
            <body>
        """.trimIndent())

        if (reports.isEmpty()) {
            val emptyMsg = if (isArabic) {
                "لا توجد تقارير شهرية معتمدة لهذا الشهر في النظام."
            } else {
                "No approved monthly reports found for this month in the database."
            }
            htmlBuilder.append("<div style='text-align:center; padding: 50px; font-size:20px; color:#666;'>$emptyMsg</div>")
        } else {
            reports.forEach { report ->
                val student = students.find { it.id == report.studentId }
                val studentName = student?.name ?: (if (isArabic) "طالب غير معروف" else "Unknown Student")
                
                htmlBuilder.append("""
                    <div class="report-page">
                        <div class="header">
                            <h1>${if (isArabic) "تقرير الأداء والتقييم الشهري" else "Monthly Performance & Evaluation Report"}</h1>
                            <h2>${if (isArabic) "مدرسة العلوم الإسلامية والقرآن الكريم" else "School of Islamic & Quranic Studies"}</h2>
                        </div>
                        
                        <div class="meta-grid">
                            <div class="meta-item">
                                <span class="meta-label">${if (isArabic) "اسم الطالب:" else "Student Name:"}</span> $studentName
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${if (isArabic) "الشهر:" else "Month:"}</span> ${report.month}
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${if (isArabic) "المعلم:" else "Teacher:"}</span> ${report.teacherName}
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">${if (isArabic) "تاريخ الإصدار:" else "Issue Date:"}</span> ${report.generatedAt}
                            </div>
                        </div>
                        
                        <div class="section-title">${if (isArabic) "مؤشرات الأداء العامة للشهر" else "General Monthly Performance Indicators"}</div>
                        <table class="indicators-table">
                            <thead>
                                <tr>
                                    <th>${if (isArabic) "المعيار والتقييم" else "Criterion & Evaluation"}</th>
                                    <th style="width: 150px; text-align: center;">${if (isArabic) "الدرجة" else "Rating"}</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>
                                        <strong>${if (isArabic) "تقدم الحفظ والتسميع" else "Memorisation & Recitation Progress"}</strong><br>
                                        <span style="font-size:13px; color:#555;">${report.memorisationProgressComment}</span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="rating-badge rating-${report.memorisationProgressScore.replace(" ", "")}">
                                            ${getTranslatedScore(report.memorisationProgressScore, isArabic)}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <strong>${if (isArabic) "قوة الحفظ والمراجعة القديمة" else "Revision Strength"}</strong><br>
                                        <span style="font-size:13px; color:#555;">${report.revisionStrengthComment}</span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="rating-badge rating-${report.revisionStrengthScore.replace(" ", "")}">
                                            ${getTranslatedScore(report.revisionStrengthScore, isArabic)}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <strong>${if (isArabic) "مستوى التجويد والتأسيس" else "Tajweed & Foundation Level"}</strong><br>
                                        <span style="font-size:13px; color:#555;">${report.tajweedFoundationComment}</span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="rating-badge rating-${report.tajweedFoundationScore.replace(" ", "")}">
                                            ${getTranslatedScore(report.tajweedFoundationScore, isArabic)}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <strong>${if (isArabic) "الالتزام والتركيز والواجبات" else "Commitment, Focus & Homework"}</strong><br>
                                        <span style="font-size:13px; color:#555;">${report.commitmentComment}</span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="rating-badge rating-${report.commitmentScore.replace(" ", "")}">
                                            ${getTranslatedScore(report.commitmentScore, isArabic)}
                                        </span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                """.trimIndent())

                // Subject details evaluation
                var hasSubjectDetails = false
                val subjectsSection = StringBuilder()
                subjectsSection.append("<div class=\"section-title\">${if (isArabic) "تفاصيل تقييم المواد الدراسية" else "Subject Evaluation Details"}</div>")
                
                report.newMemorisation?.let {
                    if (it.isNotBlank()) {
                        hasSubjectDetails = true
                        subjectsSection.append("<p><strong>${if (isArabic) "• القرآن الكريم - الحفظ الجديد:" else "• Quran - New Memorisation:"}</strong> $it</p>")
                    }
                }
                report.revision?.let {
                    if (it.isNotBlank()) {
                        hasSubjectDetails = true
                        subjectsSection.append("<p><strong>${if (isArabic) "• القرآن الكريم - المراجعة:" else "• Quran - Revision:"}</strong> $it</p>")
                    }
                }
                report.reading?.let {
                    if (it.isNotBlank()) {
                        hasSubjectDetails = true
                        subjectsSection.append("<p><strong>${if (isArabic) "• القرآن الكريم - التلاوة والتجويد:" else "• Quran - Reading & Tajweed Fluency:"}</strong> $it</p>")
                    }
                }
                report.arabicEvaluation?.let {
                    if (it.isNotBlank()) {
                        hasSubjectDetails = true
                        subjectsSection.append("<p><strong>${if (isArabic) "• تقييم مادة اللغة العربية:" else "• Arabic Language Evaluation:"}</strong> $it</p>")
                    }
                }
                report.islamicStudiesEvaluation?.let {
                    if (it.isNotBlank()) {
                        hasSubjectDetails = true
                        subjectsSection.append("<p><strong>${if (isArabic) "• تقييم مادة الدراسات الإسلامية:" else "• Islamic Studies Evaluation:"}</strong> $it</p>")
                    }
                }

                if (hasSubjectDetails) {
                    htmlBuilder.append(subjectsSection.toString())
                }

                // Strengths
                if (report.strengths.isNotEmpty()) {
                    htmlBuilder.append("<div class=\"section-title\">${if (isArabic) "نقاط القوة والتميز" else "Key Strengths"}</div>")
                    htmlBuilder.append("<ul class=\"bullet-list\">")
                    report.strengths.forEach { s ->
                        if (s.isNotBlank()) {
                            htmlBuilder.append("<li>$s</li>")
                        }
                    }
                    htmlBuilder.append("</ul>")
                }

                // Recommendations
                if (report.recommendations.isNotEmpty()) {
                    htmlBuilder.append("<div class=\"section-title\">${if (isArabic) "توجيهات وتوصيات للمنزل" else "Actionable Home Recommendations"}</div>")
                    htmlBuilder.append("<ul class=\"bullet-list\">")
                    report.recommendations.forEach { r ->
                        if (r.isNotBlank()) {
                            htmlBuilder.append("<li>$r</li>")
                        }
                    }
                    htmlBuilder.append("</ul>")
                }

                // Plan for next month
                if (report.nextMonthPlan.isNotBlank()) {
                    htmlBuilder.append("<div class=\"section-title\">${if (isArabic) "خطة الدراسة المقترحة للشهر القادم" else "Proposed Plan for Next Month"}</div>")
                    htmlBuilder.append("<div class=\"plan-box\">${report.nextMonthPlan}</div>")
                }

                htmlBuilder.append("</div>") // End report-page
            }
        }

        htmlBuilder.append("</body></html>")
        return htmlBuilder.toString()
    }

    private fun getTranslatedScore(score: String, isArabic: Boolean): String {
        if (!isArabic) return score
        return when (score.replace(" ", "")) {
            "Excellent" -> "ممتاز"
            "Good" -> "جيد جداً"
            "Steady" -> "مستقر/مقبول"
            "NeedsImprovement" -> "يحتاج تحسين"
            else -> score
        }
    }
}
