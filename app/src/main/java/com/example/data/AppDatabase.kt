package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): Student?

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsRaw(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)
}

@Dao
interface DailyLessonDao {
    @Query("SELECT * FROM daily_lessons WHERE studentId = :studentId AND isDeleted = 0 ORDER BY date ASC, lessonNumber ASC")
    fun getLessonsForStudent(studentId: String): Flow<List<DailyLesson>>

    @Query("SELECT * FROM daily_lessons WHERE studentId = :studentId AND date LIKE :yearMonth || '%' AND isDeleted = 0 ORDER BY date ASC, lessonNumber ASC")
    suspend fun getLessonsForStudentInMonth(studentId: String, yearMonth: String): List<DailyLesson>

    @Query("SELECT MAX(lessonNumber) FROM daily_lessons WHERE studentId = :studentId AND isDeleted = 0")
    suspend fun getLastLessonNumber(studentId: String): Int?

    @Query("SELECT * FROM daily_lessons")
    suspend fun getAllLessonsRaw(): List<DailyLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: DailyLesson)

    @Delete
    suspend fun deleteLesson(lesson: DailyLesson)

    @Query("DELETE FROM daily_lessons WHERE id = :id")
    suspend fun deleteLessonById(id: String)

    @Query("DELETE FROM daily_lessons WHERE studentId = :studentId")
    suspend fun deleteLessonsForStudent(studentId: String)
}

@Dao
interface MonthlyReportDao {
    @Query("SELECT * FROM monthly_reports WHERE studentId = :studentId AND isDeleted = 0 ORDER BY month DESC")
    fun getReportsForStudent(studentId: String): Flow<List<MonthlyReport>>

    @Query("SELECT * FROM monthly_reports WHERE studentId = :studentId AND month = :month AND isDeleted = 0 LIMIT 1")
    suspend fun getReportForStudentInMonth(studentId: String, month: String): MonthlyReport?

    @Query("SELECT * FROM monthly_reports WHERE month = :month AND isDeleted = 0")
    suspend fun getReportsForMonth(month: String): List<MonthlyReport>

    @Query("SELECT * FROM monthly_reports")
    suspend fun getAllReportsRaw(): List<MonthlyReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: MonthlyReport)

    @Delete
    suspend fun deleteReport(report: MonthlyReport)

    @Query("DELETE FROM monthly_reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("DELETE FROM monthly_reports WHERE studentId = :studentId")
    suspend fun deleteReportsForStudent(studentId: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE studentId = :studentId AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getGoalsForStudent(studentId: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals")
    suspend fun getAllGoalsRaw(): List<Goal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM goals WHERE studentId = :studentId")
    suspend fun deleteGoalsForStudent(studentId: String)
}

@Database(
    entities = [Student::class, DailyLesson::class, MonthlyReport::class, Goal::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun dailyLessonDao(): DailyLessonDao
    abstract fun monthlyReportDao(): MonthlyReportDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "islamic_teacher_reports_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
