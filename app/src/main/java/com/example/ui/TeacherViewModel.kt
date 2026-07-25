package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.DailyAnalysisResult
import com.example.api.GeminiService
import com.example.api.MonthlyAnalysisResult
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    data class StudentProfile(val studentId: String) : Screen()
    data class DailyLessonEntry(val studentId: String?) : Screen()
    data class MonthlyReportGen(val studentId: String?) : Screen()
    object Settings : Screen()
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class TeacherViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = TeacherRepository(db)
    private val geminiService = GeminiService()
    private val prefs = application.getSharedPreferences("teacher_reports_prefs", Context.MODE_PRIVATE)

    // --- Screen State ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    companion object {
        const val DEFAULT_GEMINI_API_KEY = "AQ.Ab8RN6KdLT7U42yCqCxZSs0ZN17_atSXsRgnS0Bt_9Ra08Z7VA"
        const val DEFAULT_FIREBASE_API_KEY = "AIzaSyAjIBUFG-WKNzy7mDAFQO3Sh-chxZ8XzUs"
        const val DEFAULT_FIREBASE_PROJECT_ID = "teacher-reports-d168b"
        const val DEFAULT_FIREBASE_APP_ID = "1:318105019832:web:b5db2e2d587fe1391c1b4d"
    }

    // --- Settings State ---
    private val _teacherName = MutableStateFlow(prefs.getString("teacher_name", "") ?: "")
    val teacherName: StateFlow<String> = _teacherName.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString("gemini_api_key", "")?.takeIf { it.isNotBlank() } ?: DEFAULT_GEMINI_API_KEY
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _aiModel = MutableStateFlow(prefs.getString("ai_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val aiModel: StateFlow<String> = _aiModel.asStateFlow()

    private val _globalInstructions = MutableStateFlow(
        prefs.getString("global_instructions", "Always write the monthly reports in a warm, encouraging, parent-friendly tone. Highlight achievements first, be honest about weaknesses without discouraging the student, and present actionable next steps.") ?: ""
    )
    val globalInstructions: StateFlow<String> = _globalInstructions.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("app_language", "ar") ?: "ar")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    // --- Cloud / Offline Auth States ---
    private val _isCloudMode = MutableStateFlow(false)
    val isCloudMode: StateFlow<Boolean> = _isCloudMode.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _firebaseApiKey = MutableStateFlow(
        prefs.getString("firebase_api_key", "")?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_API_KEY
    )
    val firebaseApiKey: StateFlow<String> = _firebaseApiKey.asStateFlow()

    private val _firebaseProjectId = MutableStateFlow(
        prefs.getString("firebase_project_id", "")?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_PROJECT_ID
    )
    val firebaseProjectId: StateFlow<String> = _firebaseProjectId.asStateFlow()

    private val _firebaseAppId = MutableStateFlow(
        prefs.getString("firebase_app_id", "")?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_APP_ID
    )
    val firebaseAppId: StateFlow<String> = _firebaseAppId.asStateFlow()

    // --- DB State Flows ---
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<String?>(null)
    val selectedStudentId: StateFlow<String?> = _selectedStudentId.asStateFlow()

    private val _lessonsForSelectedStudent = MutableStateFlow<List<DailyLesson>>(emptyList())
    val lessonsForSelectedStudent: StateFlow<List<DailyLesson>> = _lessonsForSelectedStudent.asStateFlow()

    private val _reportsForSelectedStudent = MutableStateFlow<List<MonthlyReport>>(emptyList())
    val reportsForSelectedStudent: StateFlow<List<MonthlyReport>> = _reportsForSelectedStudent.asStateFlow()

    private val _goalsForSelectedStudent = MutableStateFlow<List<Goal>>(emptyList())
    val goalsForSelectedStudent: StateFlow<List<Goal>> = _goalsForSelectedStudent.asStateFlow()

    // --- Firebase Observers & Jobs ---
    private var isFirebaseInitialized = false
    private var studentsListenerRegistration: ListenerRegistration? = null
    private var lessonsListenerRegistration: ListenerRegistration? = null
    private var reportsListenerRegistration: ListenerRegistration? = null
    private var goalsListenerRegistration: ListenerRegistration? = null
    private var roomCollectJob: kotlinx.coroutines.Job? = null

    // --- Sync State Flows ---
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _showSyncConflictDialog = MutableStateFlow(false)
    val showSyncConflictDialog: StateFlow<Boolean> = _showSyncConflictDialog.asStateFlow()

    init {
        val savedName = prefs.getString("teacher_name", "") ?: ""
        if (savedName.isNotBlank()) {
            _teacherName.value = savedName
        } else {
            _teacherName.value = "Teacher"
        }

        val auth = getAuth()
        if (auth != null && auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser!!)
            _isCloudMode.value = true
            _currentScreen.value = Screen.Dashboard
        } else {
            _authState.value = AuthState.Unauthenticated
            _isCloudMode.value = false
            _currentScreen.value = Screen.Login
        }

        observeData()
        registerNetworkCallback()
    }

    private fun initFirebaseSafely(): Boolean {
        if (isFirebaseInitialized) return true
        return try {
            val customApiKey = prefs.getString("firebase_api_key", "") ?: ""
            val customProjectId = prefs.getString("firebase_project_id", "") ?: ""
            val customAppId = prefs.getString("firebase_app_id", "") ?: ""

            val apiKeyToUse = if (customApiKey.isNotBlank()) customApiKey else DEFAULT_FIREBASE_API_KEY
            val projectIdToUse = if (customProjectId.isNotBlank()) customProjectId else DEFAULT_FIREBASE_PROJECT_ID
            val appIdToUse = if (customAppId.isNotBlank()) customAppId else DEFAULT_FIREBASE_APP_ID

            if (FirebaseApp.getApps(getApplication()).isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                app.delete()
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKeyToUse)
                .setApplicationId(appIdToUse)
                .setProjectId(projectIdToUse)
                .build()
            FirebaseApp.initializeApp(getApplication(), options)
            isFirebaseInitialized = true
            true
        } catch (e: Exception) {
            Log.e("TeacherViewModel", "Firebase initialization failed", e)
            false
        }
    }

    fun getAuth(): FirebaseAuth? {
        return if (initFirebaseSafely()) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null
    }

    fun getFirestore(): FirebaseFirestore? {
        return if (initFirebaseSafely()) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null
    }

    fun signIn(email: String, password: String, rememberMe: Boolean, onResult: (Boolean, String) -> Unit) {
        val auth = getAuth()
        if (auth == null) {
            onResult(false, "Firebase not available")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val displayName = user.displayName ?: email.substringBefore("@")
                    prefs.edit().apply {
                        putBoolean("remember_me", rememberMe)
                        putString("teacher_name", displayName)
                        putBoolean("firebase_cloud_enabled", true)
                        apply()
                    }
                    _teacherName.value = displayName
                    _isCloudMode.value = true
                    _authState.value = AuthState.Success(user)
                    observeData()
                    navigateTo(Screen.Dashboard)
                    onResult(true, "Success")
                } else {
                    _authState.value = AuthState.Error("User empty")
                    onResult(false, "Error: User empty")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed")
                onResult(false, e.localizedMessage ?: "Login failed")
            }
    }

    fun signUp(email: String, password: String, name: String, rememberMe: Boolean, onResult: (Boolean, String) -> Unit) {
        val auth = getAuth()
        if (auth == null) {
            onResult(false, "Firebase not available")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates)
                    prefs.edit().apply {
                        putBoolean("remember_me", rememberMe)
                        putString("teacher_name", name)
                        putBoolean("firebase_cloud_enabled", true)
                        apply()
                    }
                    _teacherName.value = name
                    _isCloudMode.value = true
                    _authState.value = AuthState.Success(user)
                    observeData()
                    navigateTo(Screen.Dashboard)
                    onResult(true, "Success")
                } else {
                    _authState.value = AuthState.Error("User empty")
                    onResult(false, "Error: User empty")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Signup failed")
                onResult(false, e.localizedMessage ?: "Signup failed")
            }
    }

    fun logout() {
        getAuth()?.signOut()
        _isCloudMode.value = false
        prefs.edit().putBoolean("firebase_cloud_enabled", false).apply()
        prefs.edit().remove("has_chosen_sync_option").remove("saved_sync_option").apply()
        _authState.value = AuthState.Unauthenticated
        observeData()
        navigateTo(Screen.Login)
    }

    fun observeData() {
        roomCollectJob?.cancel()
        studentsListenerRegistration?.remove()
        lessonsListenerRegistration?.remove()
        reportsListenerRegistration?.remove()
        goalsListenerRegistration?.remove()

        // ALWAYS collect from room repository as our single source of truth!
        roomCollectJob = viewModelScope.launch {
            launch {
                repository.getAllStudents().collect { list ->
                    _students.value = list
                }
            }
            launch {
                _selectedStudentId.collectLatest { id ->
                    if (id != null) {
                        val lJob = launch {
                            repository.getLessonsForStudent(id).collect { list ->
                                _lessonsForSelectedStudent.value = list
                            }
                        }
                        val rJob = launch {
                            repository.getReportsForStudent(id).collect { list ->
                                _reportsForSelectedStudent.value = list
                            }
                        }
                        val gJob = launch {
                            repository.getGoalsForStudent(id).collect { list ->
                                _goalsForSelectedStudent.value = list
                            }
                        }
                        _selectedStudentId.collect { currentId ->
                            if (currentId != id) {
                                lJob.cancel()
                                rJob.cancel()
                                gJob.cancel()
                            }
                        }
                    } else {
                        _lessonsForSelectedStudent.value = emptyList()
                        _reportsForSelectedStudent.value = emptyList()
                        _goalsForSelectedStudent.value = emptyList()
                    }
                }
            }
        }

        // If Cloud Sync is enabled, run an automatic synchronization in the background on data observation init!
        if (_isCloudMode.value && getAuth()?.currentUser != null) {
            triggerBackgroundSync()
        }
    }

    // --- Network and Smart Sync Internals ---
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        try {
            val connectivityManager = getApplication<Application>()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (_isCloudMode.value && getAuth()?.currentUser != null) {
                        triggerBackgroundSync()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("TeacherViewModel", "Failed to register network callback", e)
        }
    }

    fun triggerBackgroundSync() {
        if (!_isCloudMode.value) return
        val uid = getAuth()?.currentUser?.uid ?: return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (!isNetworkAvailable()) {
                _syncStatus.value = SyncStatus.WaitingForInternet
                return@launch
            }
            
            _syncStatus.value = SyncStatus.Syncing
            try {
                val firestore = getFirestore() ?: throw Exception("Firestore not initialized")
                
                // 1. Fetch remote data first
                val remoteStudents = firestore.collection("teachers").document(uid).collection("students").get().await().toObjects(Student::class.java)
                val remoteLessons = firestore.collection("teachers").document(uid).collection("lessons").get().await().toObjects(DailyLesson::class.java)
                val remoteReports = firestore.collection("teachers").document(uid).collection("reports").get().await().toObjects(MonthlyReport::class.java)
                val remoteGoals = firestore.collection("teachers").document(uid).collection("goals").get().await().toObjects(Goal::class.java)
                
                // 2. Fetch local data (including deleted ones to properly sync soft delete status)
                val localStudents = repository.getAllStudentsRaw()
                val localLessons = repository.getAllLessonsRaw()
                val localReports = repository.getAllReportsRaw()
                val localGoals = repository.getAllGoalsRaw()
                
                // 3. Check for conflict if it's the first sync and both contain data
                val hasLocalData = localStudents.isNotEmpty() || localLessons.isNotEmpty() || localReports.isNotEmpty() || localGoals.isNotEmpty()
                val hasRemoteData = remoteStudents.isNotEmpty() || remoteLessons.isNotEmpty() || remoteReports.isNotEmpty() || remoteGoals.isNotEmpty()
                
                val hasChosenSync = prefs.getBoolean("has_chosen_sync_option", false)
                
                if (hasLocalData && hasRemoteData && !hasChosenSync) {
                    _showSyncConflictDialog.value = true
                    _syncStatus.value = SyncStatus.Idle
                    return@launch
                }
                
                val savedOptionStr = prefs.getString("saved_sync_option", "MERGE") ?: "MERGE"
                val option = SyncOption.valueOf(savedOptionStr)
                
                executeSyncWithOptions(
                    option,
                    uid,
                    localStudents, remoteStudents,
                    localLessons, remoteLessons,
                    localReports, remoteReports,
                    localGoals, remoteGoals
                )
                
                _syncStatus.value = SyncStatus.Success
            } catch (e: Exception) {
                Log.e("TeacherViewModel", "Sync failed", e)
                _syncStatus.value = SyncStatus.Failed(e.localizedMessage ?: "Sync failed")
            }
        }
    }

    private suspend fun executeSyncWithOptions(
        option: SyncOption,
        uid: String,
        localStudents: List<Student>, remoteStudents: List<Student>,
        localLessons: List<DailyLesson>, remoteLessons: List<DailyLesson>,
        localReports: List<MonthlyReport>, remoteReports: List<MonthlyReport>,
        localGoals: List<Goal>, remoteGoals: List<Goal>
    ) {
        val firestore = getFirestore() ?: return
        
        when (option) {
            SyncOption.MERGE -> {
                // Students sync
                val mergedStudents = mergeEntities(localStudents, remoteStudents, { it.id }, { it.lastUpdated })
                for (student in mergedStudents) {
                    repository.insertStudent(student)
                    firestore.collection("teachers").document(uid).collection("students").document(student.id).set(student).await()
                }
                
                // Lessons sync
                val mergedLessons = mergeEntities(localLessons, remoteLessons, { it.id }, { it.lastUpdated })
                for (lesson in mergedLessons) {
                    repository.insertLesson(lesson)
                    firestore.collection("teachers").document(uid).collection("lessons").document(lesson.id).set(lesson).await()
                }
                
                // Reports sync
                val mergedReports = mergeEntities(localReports, remoteReports, { it.id }, { it.lastUpdated })
                for (report in mergedReports) {
                    repository.insertReport(report)
                    firestore.collection("teachers").document(uid).collection("reports").document(report.id).set(report).await()
                }
                
                // Goals sync
                val mergedGoals = mergeEntities(localGoals, remoteGoals, { it.id }, { it.lastUpdated })
                for (goal in mergedGoals) {
                    repository.insertGoal(goal)
                    firestore.collection("teachers").document(uid).collection("goals").document(goal.id).set(goal).await()
                }
            }
            
            SyncOption.LOCAL_ONLY -> {
                for (student in localStudents) {
                    firestore.collection("teachers").document(uid).collection("students").document(student.id).set(student).await()
                }
                for (lesson in localLessons) {
                    firestore.collection("teachers").document(uid).collection("lessons").document(lesson.id).set(lesson).await()
                }
                for (report in localReports) {
                    firestore.collection("teachers").document(uid).collection("reports").document(report.id).set(report).await()
                }
                for (goal in localGoals) {
                    firestore.collection("teachers").document(uid).collection("goals").document(goal.id).set(goal).await()
                }
            }
            
            SyncOption.CLOUD_ONLY -> {
                for (student in remoteStudents) {
                    repository.insertStudent(student)
                }
                for (lesson in remoteLessons) {
                    repository.insertLesson(lesson)
                }
                for (report in remoteReports) {
                    repository.insertReport(report)
                }
                for (goal in remoteGoals) {
                    repository.insertGoal(goal)
                }
            }
        }
    }

    private fun <T> mergeEntities(
        local: List<T>,
        remote: List<T>,
        idSelector: (T) -> String,
        timestampSelector: (T) -> Long
    ): List<T> {
        val localMap = local.associateBy(idSelector)
        val remoteMap = remote.associateBy(idSelector)
        val allIds = localMap.keys + remoteMap.keys
        val mergedList = mutableListOf<T>()
        
        for (id in allIds) {
            val loc = localMap[id]
            val rem = remoteMap[id]
            if (loc != null && rem != null) {
                if (timestampSelector(loc) >= timestampSelector(rem)) {
                    mergedList.add(loc)
                } else {
                    mergedList.add(rem)
                }
            } else if (loc != null) {
                mergedList.add(loc)
            } else if (rem != null) {
                mergedList.add(rem)
            }
        }
        return mergedList
    }

    fun resolveSyncConflict(option: SyncOption) {
        prefs.edit().apply {
            putBoolean("has_chosen_sync_option", true)
            putString("saved_sync_option", option.name)
            apply()
        }
        _showSyncConflictDialog.value = false
        triggerBackgroundSync()
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result, null)
                } else {
                    cont.resumeWithException(task.exception ?: Exception("Unknown error"))
                }
            }
        }

    // --- Gemini Operational State ---
    private val _isDailyAnalyzing = MutableStateFlow(false)
    val isDailyAnalyzing: StateFlow<Boolean> = _isDailyAnalyzing.asStateFlow()

    private val _dailyAnalysisResult = MutableStateFlow<DailyAnalysisResult?>(null)
    val dailyAnalysisResult: StateFlow<DailyAnalysisResult?> = _dailyAnalysisResult.asStateFlow()

    private val _isMonthlyAnalyzing = MutableStateFlow(false)
    val isMonthlyAnalyzing: StateFlow<Boolean> = _isMonthlyAnalyzing.asStateFlow()

    private val _monthlyAnalysisResult = MutableStateFlow<MonthlyAnalysisResult?>(null)
    val monthlyAnalysisResult: StateFlow<MonthlyAnalysisResult?> = _monthlyAnalysisResult.asStateFlow()

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()

    fun clearError() {
        _operationError.value = null
    }

    fun clearSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }

    // --- Navigation Helpers ---
    fun navigateTo(screen: Screen) {
        if (screen is Screen.StudentProfile) {
            _selectedStudentId.value = screen.studentId
        }
        _currentScreen.value = screen
    }

    // --- Settings Savers ---
    fun saveSettings(
        name: String,
        key: String,
        model: String,
        globalInst: String
    ) {
        prefs.edit().apply {
            putString("teacher_name", name)
            putString("gemini_api_key", key)
            putString("ai_model", model)
            putString("global_instructions", globalInst)
            apply()
        }
        _teacherName.value = name
        _apiKey.value = key
        _aiModel.value = model
        _globalInstructions.value = globalInst
    }

    fun saveFirebaseSettings(
        apiKey: String,
        projectId: String,
        appId: String,
        enabled: Boolean
    ) {
        prefs.edit().apply {
            putString("firebase_api_key", apiKey)
            putString("firebase_project_id", projectId)
            putString("firebase_app_id", appId)
            putBoolean("firebase_cloud_enabled", enabled)
            apply()
        }
        _firebaseApiKey.value = apiKey
        _firebaseProjectId.value = projectId
        _firebaseAppId.value = appId
        _isCloudMode.value = enabled

        // Reinitialize Firebase safely
        isFirebaseInitialized = false
        initFirebaseSafely()

        // Sync auth state
        if (enabled) {
            val auth = getAuth()
            if (auth != null && auth.currentUser != null) {
                _authState.value = AuthState.Success(auth.currentUser!!)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }

        observeData()
    }

    fun toggleLanguage() {
        val newLang = if (_language.value == "ar") "en" else "ar"
        prefs.edit().putString("app_language", newLang).apply()
        _language.value = newLang
    }

    fun toggleDarkMode() {
        val newMode = !_darkMode.value
        prefs.edit().putBoolean("dark_mode", newMode).apply()
        _darkMode.value = newMode
    }

    // --- Student Actions ---
    fun addOrUpdateStudent(
        id: String?,
        name: String,
        subjects: List<String>,
        notes: String,
        customInstructions: String
    ) {
        viewModelScope.launch {
            val studentId = id ?: UUID.randomUUID().toString()
            val regDate = if (id != null) {
                repository.getStudentById(id)?.registrationDate ?: getTodayDateString()
            } else {
                getTodayDateString()
            }
            val student = Student(
                id = studentId,
                name = name,
                subjects = subjects,
                notes = notes,
                customInstructions = customInstructions,
                registrationDate = regDate,
                lastUpdated = System.currentTimeMillis(),
                isDeleted = false
            )
            repository.insertStudent(student)
            triggerBackgroundSync()
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            if (student != null) {
                val now = System.currentTimeMillis()
                // Soft delete student
                repository.insertStudent(student.copy(isDeleted = true, lastUpdated = now))
                
                // Soft delete all lessons
                val lessons = repository.getLessonsForStudent(studentId).first()
                for (l in lessons) {
                    repository.insertLesson(l.copy(isDeleted = true, lastUpdated = now))
                }
                
                // Soft delete all reports
                val reports = repository.getReportsForStudent(studentId).first()
                for (r in reports) {
                    repository.insertReport(r.copy(isDeleted = true, lastUpdated = now))
                }
                
                // Soft delete all goals
                val goals = repository.getGoalsForStudent(studentId).first()
                for (g in goals) {
                    repository.insertGoal(g.copy(isDeleted = true, lastUpdated = now))
                }
            }
            
            if (_selectedStudentId.value == studentId) {
                _selectedStudentId.value = null
            }
            triggerBackgroundSync()
            navigateTo(Screen.Dashboard)
        }
    }

    // --- Daily Lesson Actions ---
    fun clearDailyAnalysis() {
        _dailyAnalysisResult.value = null
    }

    fun analyzeDailyLesson(
        studentId: String,
        lessonNumber: Int,
        lessonDate: String,
        durationMinutes: Int,
        teacherName: String,
        activeSubjects: List<String>,
        arabicReportText: String
    ) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId) ?: return@launch

            _isDailyAnalyzing.value = true
            _operationError.value = null
            try {
                val activeKey = _apiKey.value.ifBlank { com.example.BuildConfig.GEMINI_API_KEY.ifBlank { DEFAULT_GEMINI_API_KEY } }
                if (activeKey.isBlank() || activeKey.startsWith("MY_")) {
                    throw Exception("Missing Gemini API key. Please enter it in Settings.")
                }

                val result = geminiService.analyzeDailyReport(
                    apiKey = activeKey,
                    model = _aiModel.value,
                    student = student,
                    lessonNumber = lessonNumber,
                    lessonDate = lessonDate,
                    durationMinutes = durationMinutes,
                    teacherName = teacherName,
                    activeSubjects = activeSubjects,
                    arabicReportText = arabicReportText,
                    globalInstructions = _globalInstructions.value,
                    studentInstructions = student.customInstructions
                )
                _dailyAnalysisResult.value = result
            } catch (e: Exception) {
                _operationError.value = e.localizedMessage ?: e.message ?: "Analysis failed."
            } finally {
                _isDailyAnalyzing.value = false
            }
        }
    }

    fun saveDailyLesson(
        studentId: String,
        lessonNumber: Int,
        lessonDate: String,
        duration: Int,
        teacherName: String,
        subjectsTaught: List<String>,
        reportArabic: String,
        reportEnglish: String,
        achievements: List<String>,
        weaknesses: List<String>,
        homeworkAssigned: String,
        homeworkCompleted: Boolean?,
        grade: String
    ) {
        viewModelScope.launch {
            val lessonId = UUID.randomUUID().toString()
            val lesson = DailyLesson(
                id = lessonId,
                studentId = studentId,
                lessonNumber = lessonNumber,
                date = lessonDate,
                duration = duration,
                teacherName = teacherName,
                subjectsTaught = subjectsTaught,
                reportArabic = reportArabic,
                reportEnglish = reportEnglish,
                achievements = achievements,
                weaknesses = weaknesses,
                homeworkAssigned = homeworkAssigned,
                homeworkCompleted = homeworkCompleted,
                grade = grade,
                lastUpdated = System.currentTimeMillis(),
                isDeleted = false
            )
            repository.insertLesson(lesson)
            clearDailyAnalysis()
            triggerBackgroundSync()
            navigateTo(Screen.StudentProfile(studentId))
        }
    }

    fun deleteLesson(lessonId: String, studentId: String) {
        viewModelScope.launch {
            val lessons = repository.getLessonsForStudent(studentId).first()
            val lesson = lessons.find { it.id == lessonId }
            if (lesson != null) {
                repository.insertLesson(lesson.copy(isDeleted = true, lastUpdated = System.currentTimeMillis()))
            }
            triggerBackgroundSync()
        }
    }

    suspend fun getNextLessonNumber(studentId: String): Int {
        return repository.getLastLessonNumber(studentId) + 1
    }

    suspend fun getNextLessonNumberInMonth(studentId: String, yearMonth: String): Int {
        return repository.getLessonsForStudentInMonth(studentId, yearMonth).size + 1
    }

    // --- Monthly Report Actions ---
    fun clearMonthlyAnalysis() {
        _monthlyAnalysisResult.value = null
    }

    fun generateMonthlyDraft(studentId: String, month: String) {
        viewModelScope.launch {
            val student = repository.getStudentById(studentId) ?: return@launch
            
            val lessons = repository.getLessonsForStudentInMonth(studentId, month)
            if (lessons.isEmpty()) {
                _operationError.value = "No lessons recorded for this student in $month"
                return@launch
            }
            _isMonthlyAnalyzing.value = true
            _operationError.value = null
            try {
                val activeKey = _apiKey.value.ifBlank { com.example.BuildConfig.GEMINI_API_KEY.ifBlank { DEFAULT_GEMINI_API_KEY } }
                if (activeKey.isBlank() || activeKey.startsWith("MY_")) {
                    throw Exception("Missing Gemini API key. Please enter it in Settings.")
                }

                val draft = geminiService.generateMonthlyReportDraft(
                    apiKey = activeKey,
                    model = _aiModel.value,
                    student = student,
                    month = month,
                    dailyLessons = lessons,
                    globalInstructions = _globalInstructions.value,
                    studentInstructions = student.customInstructions
                )
                _monthlyAnalysisResult.value = draft
            } catch (e: Exception) {
                _operationError.value = e.localizedMessage ?: e.message ?: "Evaluation failed."
            } finally {
                _isMonthlyAnalyzing.value = false
            }
        }
    }

    fun saveMonthlyReport(
        studentId: String,
        month: String,
        teacherName: String,
        memorisationProgressScore: String,
        memorisationProgressComment: String,
        revisionStrengthScore: String,
        revisionStrengthComment: String,
        tajweedFoundationScore: String,
        tajweedFoundationComment: String,
        commitmentScore: String,
        commitmentComment: String,
        newMemorisation: String?,
        revision: String?,
        reading: String?,
        arabicEvaluation: String?,
        islamicStudiesEvaluation: String?,
        strengths: List<String>,
        recommendations: List<String>,
        nextMonthPlan: String
    ) {
        viewModelScope.launch {
            val reportId = UUID.randomUUID().toString()
            val report = MonthlyReport(
                id = reportId,
                studentId = studentId,
                month = month,
                teacherName = teacherName,
                generatedAt = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                memorisationProgressScore = memorisationProgressScore,
                memorisationProgressComment = memorisationProgressComment,
                revisionStrengthScore = revisionStrengthScore,
                revisionStrengthComment = revisionStrengthComment,
                tajweedFoundationScore = tajweedFoundationScore,
                tajweedFoundationComment = tajweedFoundationComment,
                commitmentScore = commitmentScore,
                commitmentComment = commitmentComment,
                newMemorisation = newMemorisation,
                revision = revision,
                reading = reading,
                arabicEvaluation = arabicEvaluation,
                islamicStudiesEvaluation = islamicStudiesEvaluation,
                strengths = strengths,
                recommendations = recommendations,
                nextMonthPlan = nextMonthPlan,
                isApproved = true,
                lastUpdated = System.currentTimeMillis(),
                isDeleted = false
            )
            repository.insertReport(report)
            clearMonthlyAnalysis()
            triggerBackgroundSync()
            navigateTo(Screen.StudentProfile(studentId))
        }
    }

    fun deleteReport(reportId: String, studentId: String) {
        viewModelScope.launch {
            val reports = repository.getReportsForStudent(studentId).first()
            val report = reports.find { it.id == reportId }
            if (report != null) {
                repository.insertReport(report.copy(isDeleted = true, lastUpdated = System.currentTimeMillis()))
            }
            triggerBackgroundSync()
        }
    }

    suspend fun getReportsForMonth(month: String): List<MonthlyReport> {
        return repository.getReportsForMonth(month)
    }

    // --- Backup & Import Logic ---
    fun exportBackup(onExportComplete: (String) -> Unit) {
        viewModelScope.launch {
            val studentsList = repository.getAllStudents().first()
            val lessonsList = mutableListOf<DailyLesson>()
            val reportsList = mutableListOf<MonthlyReport>()
            
            for (s in studentsList) {
                lessonsList.addAll(repository.getLessonsForStudent(s.id).first())
                reportsList.addAll(repository.getReportsForStudent(s.id).first())
            }
            
            val json = BackupManager.exportToJson(studentsList, lessonsList, reportsList)
            onExportComplete(json)
        }
    }

    fun importBackup(jsonString: String, onImportComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val payload = BackupManager.importFromJson(jsonString)
                if (payload == null) {
                    onImportComplete(false, "Invalid backup format.")
                    return@launch
                }
                
                db.runInTransaction {
                    viewModelScope.launch {
                        payload.students.forEach { s ->
                            repository.insertStudent(
                                Student(
                                    s.id, s.name, s.subjects, s.notes, s.customInstructions, s.registrationDate,
                                    lastUpdated = System.currentTimeMillis(), isDeleted = false
                                )
                            )
                        }
                        payload.lessons.forEach { l ->
                            repository.insertLesson(
                                DailyLesson(
                                    l.id, l.studentId, l.lessonNumber, l.date, l.duration, l.teacherName,
                                    l.subjectsTaught, l.reportArabic, l.reportEnglish, l.achievements, l.weaknesses,
                                    l.homeworkAssigned, l.homeworkCompleted, l.grade,
                                    lastUpdated = System.currentTimeMillis(), isDeleted = false
                                )
                            )
                        }
                        payload.reports.forEach { r ->
                            repository.insertReport(
                                MonthlyReport(
                                    r.id, r.studentId, r.month, r.teacherName, r.generatedAt,
                                    r.memorisationProgressScore, r.memorisationProgressComment,
                                    r.revisionStrengthScore, r.revisionStrengthComment,
                                    r.tajweedFoundationScore, r.tajweedFoundationComment,
                                    r.commitmentScore, r.commitmentComment,
                                    r.newMemorisation, r.revision, r.reading, r.arabicEvaluation, r.islamicStudiesEvaluation,
                                    r.strengths, r.recommendations, r.nextMonthPlan, r.isApproved,
                                    lastUpdated = System.currentTimeMillis(), isDeleted = false
                                )
                            )
                        }
                    }
                }
                onImportComplete(true, "Backup imported successfully!")
            } catch (e: Exception) {
                onImportComplete(false, "Import failed: ${e.localizedMessage}")
            }
        }
    }

    // --- Goal Actions ---
    fun addGoal(studentId: String, title: String, description: String) {
        val id = UUID.randomUUID().toString()
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val goal = Goal(
            id = id,
            studentId = studentId,
            title = title,
            description = description,
            progress = 0,
            isCompleted = false,
            dateCreated = dateStr,
            lastUpdated = System.currentTimeMillis(),
            isDeleted = false
        )
        viewModelScope.launch {
            repository.insertGoal(goal)
            triggerBackgroundSync()
        }
    }

    fun updateGoalProgress(goal: Goal, progress: Int, isCompleted: Boolean) {
        val updatedGoal = goal.copy(
            progress = progress,
            isCompleted = isCompleted,
            lastUpdated = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertGoal(updatedGoal)
            triggerBackgroundSync()
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            val goals = repository.getAllGoalsRaw()
            val goal = goals.find { it.id == goalId }
            if (goal != null) {
                repository.insertGoal(goal.copy(isDeleted = true, lastUpdated = System.currentTimeMillis()))
            }
            triggerBackgroundSync()
        }
    }

    private fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
