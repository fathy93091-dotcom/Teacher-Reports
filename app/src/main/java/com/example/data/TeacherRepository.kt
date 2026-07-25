package com.example.data

import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val db: AppDatabase) {
    private val studentDao = db.studentDao()
    private val dailyLessonDao = db.dailyLessonDao()
    private val monthlyReportDao = db.monthlyReportDao()
    private val goalDao = db.goalDao()

    // Student Operations
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()
    
    suspend fun getStudentById(id: String): Student? = studentDao.getStudentById(id)
    
    suspend fun getAllStudentsRaw(): List<Student> = studentDao.getAllStudentsRaw()
    
    suspend fun insertStudent(student: Student) = studentDao.insertStudent(student)
    
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    
    suspend fun deleteStudent(student: Student) {
        val now = System.currentTimeMillis()
        studentDao.insertStudent(student.copy(isDeleted = true, lastUpdated = now))
    }

    suspend fun deleteStudentById(id: String) {
        val now = System.currentTimeMillis()
        val student = studentDao.getStudentById(id)
        if (student != null) {
            studentDao.insertStudent(student.copy(isDeleted = true, lastUpdated = now))
        }
    }

    // Daily Lesson Operations
    fun getLessonsForStudent(studentId: String): Flow<List<DailyLesson>> = dailyLessonDao.getLessonsForStudent(studentId)
    
    suspend fun getLessonsForStudentInMonth(studentId: String, yearMonth: String): List<DailyLesson> =
        dailyLessonDao.getLessonsForStudentInMonth(studentId, yearMonth)
        
    suspend fun getLastLessonNumber(studentId: String): Int =
        dailyLessonDao.getLastLessonNumber(studentId) ?: 0

    suspend fun getAllLessonsRaw(): List<DailyLesson> = dailyLessonDao.getAllLessonsRaw()
        
    suspend fun insertLesson(lesson: DailyLesson) = dailyLessonDao.insertLesson(lesson)
    
    suspend fun deleteLesson(lesson: DailyLesson) {
        val now = System.currentTimeMillis()
        dailyLessonDao.insertLesson(lesson.copy(isDeleted = true, lastUpdated = now))
    }
    
    suspend fun deleteLessonById(id: String) {
        val now = System.currentTimeMillis()
        val lessons = dailyLessonDao.getAllLessonsRaw()
        val lesson = lessons.find { it.id == id }
        if (lesson != null) {
            dailyLessonDao.insertLesson(lesson.copy(isDeleted = true, lastUpdated = now))
        }
    }

    // Monthly Report Operations
    fun getReportsForStudent(studentId: String): Flow<List<MonthlyReport>> = monthlyReportDao.getReportsForStudent(studentId)
    
    suspend fun getReportForStudentInMonth(studentId: String, month: String): MonthlyReport? =
        monthlyReportDao.getReportForStudentInMonth(studentId, month)

    suspend fun getReportsForMonth(month: String): List<MonthlyReport> =
        monthlyReportDao.getReportsForMonth(month)

    suspend fun getAllReportsRaw(): List<MonthlyReport> = monthlyReportDao.getAllReportsRaw()
        
    suspend fun insertReport(report: MonthlyReport) = monthlyReportDao.insertReport(report)
    
    suspend fun deleteReport(report: MonthlyReport) {
        val now = System.currentTimeMillis()
        monthlyReportDao.insertReport(report.copy(isDeleted = true, lastUpdated = now))
    }
    
    suspend fun deleteReportById(id: String) {
        val now = System.currentTimeMillis()
        val reports = monthlyReportDao.getAllReportsRaw()
        val report = reports.find { it.id == id }
        if (report != null) {
            monthlyReportDao.insertReport(report.copy(isDeleted = true, lastUpdated = now))
        }
    }

    // Goal Operations
    fun getGoalsForStudent(studentId: String): Flow<List<Goal>> = goalDao.getGoalsForStudent(studentId)

    suspend fun getAllGoalsRaw(): List<Goal> = goalDao.getAllGoalsRaw()

    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)

    suspend fun deleteGoalById(id: String) {
        val now = System.currentTimeMillis()
        val goals = goalDao.getAllGoalsRaw()
        val goal = goals.find { it.id == id }
        if (goal != null) {
            goalDao.insertGoal(goal.copy(isDeleted = true, lastUpdated = now))
        }
    }
}
