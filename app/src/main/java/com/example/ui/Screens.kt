package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.DailyAnalysisResult
import com.example.api.MonthlyAnalysisResult
import com.example.data.DailyLesson
import com.example.data.MonthlyReport
import com.example.data.Student
import com.example.ui.theme.*
import java.util.*

@Composable
fun AppNavigation(viewModel: TeacherViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val showSyncConflictDialog by viewModel.showSyncConflictDialog.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val error by viewModel.operationError.collectAsStateWithLifecycle()
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showSyncConflictDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = {
                Text(
                    text = if (language == "ar") "مزامنة البيانات السحابية" else "Cloud Data Synchronization",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = if (language == "ar") {
                        "تم الكشف عن وجود بيانات محلية وسحابية في نفس الوقت. يرجى اختيار كيفية التعامل مع هذه البيانات على الفور:"
                    } else {
                        "Local and cloud data detected simultaneously. Please select how you want to handle this data immediately:"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.resolveSyncConflict(SyncOption.MERGE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == "ar") "دمج البيانات (موصى به)" else "Merge Data (Recommended)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { viewModel.resolveSyncConflict(SyncOption.LOCAL_ONLY) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == "ar") "استخدام البيانات المحلية فقط" else "Use Local Data Only",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { viewModel.resolveSyncConflict(SyncOption.CLOUD_ONLY) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == "ar") "تنزيل البيانات السحابية فقط" else "Use Cloud Data Only",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    if (currentScreen is Screen.Login) {
        LoginScreen(viewModel)
    } else {
        Scaffold(
            topBar = { AppTopBar(viewModel) },
            bottomBar = { AppBottomBar(viewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Smart Sync status banner
                AnimatedVisibility(
                    visible = syncStatus !is SyncStatus.Idle,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val (styleInfo, icon) = when (val status = syncStatus) {
                        is SyncStatus.Syncing -> {
                            val msg = if (language == "ar") "جاري المزامنة مع السحابة..." else "Syncing with cloud..."
                            Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, msg) to Icons.Default.Sync
                        }
                        is SyncStatus.Success -> {
                            val msg = if (language == "ar") "تمت المزامنة بنجاح" else "Synced successfully"
                            Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, msg) to Icons.Default.Done
                        }
                        is SyncStatus.WaitingForInternet -> {
                            val msg = if (language == "ar") "في انتظار الاتصال بالإنترنت..." else "Waiting for internet..."
                            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, msg) to Icons.Default.WifiOff
                        }
                        is SyncStatus.Failed -> {
                            val msg = if (language == "ar") "فشلت المزامنة: ${status.error}" else "Sync failed: ${status.error}"
                            Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, msg) to Icons.Default.Warning
                        }
                        else -> Triple(Color.Transparent, Color.Transparent, "") to Icons.Default.Sync
                    }

                    if (syncStatus is SyncStatus.Success) {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(3000)
                            viewModel.clearSyncStatus()
                        }
                    }

                    Surface(
                        color = styleInfo.first,
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = styleInfo.second,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = styleInfo.third,
                                style = MaterialTheme.typography.bodyMedium,
                                color = styleInfo.second,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    when (val screen = currentScreen) {
                        is Screen.Login -> LoginScreen(viewModel)
                        is Screen.Dashboard -> DashboardScreen(viewModel)
                        is Screen.StudentProfile -> StudentProfileScreen(viewModel, screen.studentId)
                        is Screen.DailyLessonEntry -> DailyLessonEntryScreen(viewModel, screen.studentId)
                        is Screen.MonthlyReportGen -> MonthlyReportGenScreen(viewModel, screen.studentId)
                        is Screen.Settings -> SettingsScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: TeacherViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.toggleLanguage() }) {
                Text(
                    text = if (language == "ar") "English" else "العربية",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = Translation.get("appName", language),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isRegisterMode) Translation.get("register", language) else Translation.get("login", language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isRegisterMode = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translation.get("login", language),
                            color = if (!isRegisterMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRegisterMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isRegisterMode = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translation.get("register", language),
                            color = if (isRegisterMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isRegisterMode) {
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text(Translation.get("teacherName", language)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(Translation.get("email", language)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Translation.get("password", language)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rememberMe = !rememberMe }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Translation.get("rememberMe", language),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, Translation.get("invalidEmail", language), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(context, Translation.get("shortPassword", language), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isRegisterMode) {
                        if (teacherName.isBlank()) {
                            Toast.makeText(context, Translation.get("teacherNameRequired", language), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.signUp(email, password, teacherName, rememberMe) { success, msg ->
                            if (success) {
                                Toast.makeText(context, Translation.get("registrationSuccess", language), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "${Translation.get("authError", language)}: $msg", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        viewModel.signIn(email, password, rememberMe) { success, msg ->
                            if (success) {
                                Toast.makeText(context, Translation.get("authSuccess", language), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "${Translation.get("authError", language)}: $msg", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isRegisterMode) Translation.get("register", language) else Translation.get("login", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(viewModel: TeacherViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isCloudMode by viewModel.isCloudMode.collectAsStateWithLifecycle()

    TopAppBar(
        title = {
            Text(
                text = Translation.get("appName", language),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (currentScreen != Screen.Dashboard) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            if (isCloudMode) {
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            // Language switch
            TextButton(onClick = { viewModel.toggleLanguage() }) {
                Text(
                    text = if (language == "ar") "English" else "العربية",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            // Dark Mode switch
            IconButton(onClick = { viewModel.toggleDarkMode() }) {
                Icon(
                    imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun AppBottomBar(viewModel: TeacherViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(Translation.get("dashboard", language), fontSize = 10.sp) },
            selected = currentScreen is Screen.Dashboard,
            onClick = { viewModel.navigateTo(Screen.Dashboard) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Book, contentDescription = "Daily Lesson") },
            label = { Text(Translation.get("lessonEntry", language), fontSize = 10.sp) },
            selected = currentScreen is Screen.DailyLessonEntry,
            onClick = { viewModel.navigateTo(Screen.DailyLessonEntry(null)) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Monthly Reports") },
            label = { Text(Translation.get("monthlyReports", language), fontSize = 10.sp) },
            selected = currentScreen is Screen.MonthlyReportGen,
            onClick = { viewModel.navigateTo(Screen.MonthlyReportGen(null)) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text(Translation.get("settings", language), fontSize = 10.sp) },
            selected = currentScreen is Screen.Settings,
            onClick = { viewModel.navigateTo(Screen.Settings) }
        )
    }
}

// --- DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(viewModel: TeacherViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()

    var showAddStudentDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = Translation.get("totalStudents", language),
                        value = students.size.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Student List Label
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translation.get("students", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showAddStudentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Translation.get("addStudent", language))
                    }
                }
            }

            if (students.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Translation.get("noStudents", language),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(students) { student ->
                    StudentCard(
                        student = student,
                        language = language,
                        onViewProfile = { viewModel.navigateTo(Screen.StudentProfile(student.id)) },
                        onNewLog = { viewModel.navigateTo(Screen.DailyLessonEntry(student.id)) },
                        onNewReport = { viewModel.navigateTo(Screen.MonthlyReportGen(student.id)) }
                    )
                }
            }
        }
    }

    if (showAddStudentDialog) {
        StudentDialog(
            language = language,
            onDismiss = { showAddStudentDialog = false },
            onSave = { name, subjects, notes, customInst ->
                viewModel.addOrUpdateStudent(null, name, subjects, notes, customInst)
                showAddStudentDialog = false
            }
        )
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    language: String,
    onViewProfile: () -> Unit,
    onNewLog: () -> Unit,
    onNewReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.firstOrNull()?.uppercase() ?: "",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        student.subjects.forEach { sub ->
                            val label = when (sub) {
                                "Quran" -> Translation.get("quran", language)
                                "Arabic" -> Translation.get("arabic", language)
                                else -> Translation.get("islamicStudies", language)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (sub) {
                                            "Quran" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            "Arabic" -> Color(0xFF1A73E8).copy(alpha = 0.1f)
                                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = when (sub) {
                                        "Quran" -> MaterialTheme.colorScheme.primary
                                        "Arabic" -> Color(0xFF1A73E8)
                                        else -> MaterialTheme.colorScheme.secondary
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translation.get("viewProfile", language), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNewLog,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Translation.get("lessonEntry", language), fontSize = 12.sp)
                }
            }
        }
    }
}

// --- STUDENT PROFILE SCREEN ---
@Composable
fun StudentProfileScreen(viewModel: TeacherViewModel, studentId: String) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val student = students.find { it.id == studentId }

    val lessons by viewModel.lessonsForSelectedStudent.collectAsStateWithLifecycle()
    val reports by viewModel.reportsForSelectedStudent.collectAsStateWithLifecycle()
    val goals by viewModel.goalsForSelectedStudent.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Timeline, 1 = Archive
    var showEditStudentDialog by remember { mutableStateOf(false) }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalTitle by remember { mutableStateOf("") }
    var goalDesc by remember { mutableStateOf("") }

    if (student == null) {
        Text("Student not found")
        return
    }

    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text(Translation.get("addGoal", language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text(Translation.get("goalTitle", language)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalDesc,
                        onValueChange = { goalDesc = it },
                        label = { Text(Translation.get("goalDesc", language)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            viewModel.addGoal(studentId, goalTitle, goalDesc)
                            goalTitle = ""
                            goalDesc = ""
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text(Translation.get("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text(Translation.get("cancel", language))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bio & Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionContainer(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${Translation.get("registeredDate", language)}: ${student.registrationDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${Translation.get("activeSubjects", language)}: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    student.subjects.forEach { sub ->
                                        val subLabel = when (sub) {
                                            "Quran" -> Translation.get("quran", language)
                                            "Arabic" -> Translation.get("arabic", language)
                                            else -> Translation.get("islamicStudies", language)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = subLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { showEditStudentDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteStudent(student.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEA4335))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (student.notes.isNotBlank()) {
                        ExpandableText(
                            text = "${Translation.get("notes", language)}: ${student.notes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLinesCollapsed = 2,
                            language = language
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.DailyLessonEntry(student.id)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Translation.get("lessonEntry", language), fontSize = 13.sp)
                        }
                        Button(
                            onClick = { viewModel.navigateTo(Screen.MonthlyReportGen(student.id)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = "")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Translation.get("generateDraftBtn", language), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Stats Row
        item {
            val totalHours = lessons.sumOf { it.duration } / 60
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    title = Translation.get("totalLessons", language),
                    value = lessons.size.toString(),
                    icon = Icons.Default.LibraryBooks,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = Translation.get("totalHours", language),
                    value = "$totalHours h",
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Learning Goals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translation.get("goals", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showAddGoalDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = Translation.get("addGoal", language),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (goals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Translation.get("noGoals", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            goals.forEach { goal ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (goal.isCompleted) {
                                            Color(0xFFE6F4EA).copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (goal.isCompleted) Color(0xFF137333).copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = goal.title,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (goal.isCompleted) Color(0xFF137333) else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (goal.description.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = goal.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = goal.isCompleted,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.updateGoalProgress(goal, if (isChecked) 100 else goal.progress, isChecked)
                                                    }
                                                )
                                                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Goal",
                                                        tint = Color(0xFFEA4335),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Progress row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${Translation.get("goalProgress", language)}: ${goal.progress}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Slider(
                                                value = goal.progress.toFloat(),
                                                onValueChange = { value ->
                                                    val prog = value.toInt()
                                                    viewModel.updateGoalProgress(goal, prog, prog == 100)
                                                },
                                                valueRange = 0f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab Row
        item {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(Translation.get("timeline", language), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(Translation.get("monthlyReportsArchive", language), fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeTab == 0) {
            // TIMELINE OF LOGS
            if (lessons.isEmpty()) {
                item {
                    Text(
                        text = Translation.get("noLessons", language),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(lessons.reversed()) { lesson ->
                    DailyLessonTimelineItem(lesson = lesson, language = language, onDelete = {
                        viewModel.deleteLesson(lesson.id, studentId)
                    })
                }
            }
        } else {
            // MONTHLY REPORTS ARCHIVE
            if (reports.isEmpty()) {
                item {
                    Text(
                        text = Translation.get("noReports", language),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reports) { report ->
                    MonthlyReportArchiveCard(report = report, language = language, onDelete = {
                        viewModel.deleteReport(report.id, studentId)
                    })
                }
            }
        }
    }

    if (showEditStudentDialog) {
        StudentDialog(
            student = student,
            language = language,
            onDismiss = { showEditStudentDialog = false },
            onSave = { name, subjects, notes, customInst ->
                viewModel.addOrUpdateStudent(student.id, name, subjects, notes, customInst)
                showEditStudentDialog = false
            }
        )
    }
}

@Composable
fun DailyLessonTimelineItem(
    lesson: DailyLesson,
    language: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${Translation.get("lessonNumber", language)} ${lesson.lessonNumber}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${lesson.date} · ${lesson.duration} ${Translation.get("lessonDuration", language)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    IconButton(onClick = {
                        val fullReport = buildString {
                            append(lesson.reportArabic)
                            if (lesson.reportEnglish.isNotBlank()) {
                                append("\n\nTranslation:\n")
                                append(lesson.reportEnglish)
                            }
                            if (lesson.homeworkAssigned.isNotBlank()) {
                                append("\n\n${Translation.get("homework", language)}: ${lesson.homeworkAssigned}")
                            }
                            append("\n${Translation.get("grade", language)}: ${lesson.grade}")
                        }
                        clipboardManager.setText(AnnotatedString(fullReport))
                        Toast.makeText(context, Translation.get("copied", language), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        val fullReport = buildString {
                            append("📝 *${Translation.get("report", language)} - ${lesson.date}*\n\n")
                            append(lesson.reportArabic)
                            if (lesson.reportEnglish.isNotBlank()) {
                                append("\n\n*Translation:*\n")
                                append(lesson.reportEnglish)
                            }
                            if (lesson.homeworkAssigned.isNotBlank()) {
                                append("\n\n📚 *${Translation.get("homework", language)}:*\n${lesson.homeworkAssigned}")
                            }
                            append("\n\n⭐ *${Translation.get("grade", language)}:* ")
                            append(when (lesson.grade) {
                                "Excellent" -> Translation.get("scoreExcellent", language)
                                "Good" -> Translation.get("scoreGood", language)
                                "Steady" -> Translation.get("scoreSteady", language)
                                else -> Translation.get("scoreNeedsImprovement", language)
                            })
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, fullReport)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, Translation.get("share", language))
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEA4335).copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    ExpandableText(
                        text = lesson.reportArabic,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = amiriFontFamily,
                        textDirection = TextDirection.Rtl,
                        maxLinesCollapsed = 3,
                        language = language,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (lesson.reportEnglish.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExpandableText(
                            text = lesson.reportEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLinesCollapsed = 2,
                            language = language,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (lesson.homeworkAssigned.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableText(
                    text = "${Translation.get("homework", language)}: ${lesson.homeworkAssigned}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLinesCollapsed = 2,
                    language = language
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${Translation.get("grade", language)}: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (lesson.grade) {
                                "Excellent" -> Color(0xFFE6F4EA)
                                "Good" -> Color(0xFFE8F0FE)
                                "Steady" -> Color(0xFFFEF7E0)
                                else -> Color(0xFFFCE8E6)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (lesson.grade) {
                            "Excellent" -> Translation.get("scoreExcellent", language)
                            "Good" -> Translation.get("scoreGood", language)
                            "Steady" -> Translation.get("scoreSteady", language)
                            else -> Translation.get("scoreNeedsImprovement", language)
                        },
                        fontSize = 11.sp,
                        color = when (lesson.grade) {
                            "Excellent" -> Color(0xFF137333)
                            "Good" -> Color(0xFF1A73E8)
                            "Steady" -> Color(0xFFB06000)
                            else -> Color(0xFFC5221F)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyReportArchiveCard(
    report: MonthlyReport,
    language: String,
    onDelete: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Report - ${report.month}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Issued on: ${report.generatedAt} by ${report.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEA4335))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showReportDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = "")
                Spacer(modifier = Modifier.width(6.dp))
                Text(Translation.get("viewReport", language))
            }
        }
    }

    if (showReportDialog) {
        ReportPreviewDialog(
            report = report,
            language = language,
            onDismiss = { showReportDialog = false }
        )
    }
}

// --- DAILY LESSON ENTRY FORM ---
@Composable
fun DailyLessonEntryScreen(viewModel: TeacherViewModel, initialStudentId: String?) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()

    var studentId by remember { mutableStateOf(initialStudentId ?: "") }
    var lessonNumber by remember { mutableStateOf(1) }
    var lessonDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var duration by remember { mutableStateOf(60) }
    var reportArabic by remember { mutableStateOf("") }
    var selectedSubjects = remember { mutableStateListOf<String>() }

    val selectedStudent = students.find { it.id == studentId }

    // Reset selected subjects only when student selection changes
    LaunchedEffect(studentId) {
        if (studentId.isNotBlank()) {
            selectedSubjects.clear()
            selectedStudent?.subjects?.let { selectedSubjects.addAll(it) }
        }
    }

    // Recalculate lesson number when student or date changes
    LaunchedEffect(studentId, lessonDate) {
        if (studentId.isNotBlank()) {
            val yearMonth = if (lessonDate.length >= 7) lessonDate.substring(0, 7) else ""
            if (yearMonth.isNotEmpty()) {
                lessonNumber = viewModel.getNextLessonNumberInMonth(studentId, yearMonth)
            } else {
                lessonNumber = viewModel.getNextLessonNumber(studentId)
            }
        }
    }

    val isAnalyzing by viewModel.isDailyAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.dailyAnalysisResult.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Voice Dictation recognizer trigger contract
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                reportArabic = if (reportArabic.isBlank()) spokenText else "$reportArabic $spokenText"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Translation.get("lessonEntry", language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Student selector
                Text(Translation.get("studentName", language), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = selectedStudent?.name ?: Translation.get("chooseStudent", language),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    studentId = s.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Date, Lesson Number, and Duration
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Translation.get("lessonNumber", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lessonNumber.toString(),
                            onValueChange = { lessonNumber = it.toIntOrNull() ?: 1 },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(Translation.get("lessonDate", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lessonDate,
                            onValueChange = { lessonDate = it },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Translation.get("lessonDuration", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = duration.toString(),
                            onValueChange = { duration = it.toIntOrNull() ?: 60 },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Subjects taught checkboxes
                selectedStudent?.let { s ->
                    Text(Translation.get("activeSubjects", language), fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        s.subjects.forEach { sub ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedSubjects.contains(sub),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedSubjects.add(sub) else selectedSubjects.remove(sub)
                                    }
                                )
                                Text(
                                    text = when (sub) {
                                        "Quran" -> Translation.get("quran", language)
                                        "Arabic" -> Translation.get("arabic", language)
                                        else -> Translation.get("islamicStudies", language)
                                    }
                                )
                            }
                        }
                    }
                }

                // Arabic detailed input field with built-in voice mic dictation button
                Text(Translation.get("writeReportArabic", language), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reportArabic,
                        onValueChange = { reportArabic = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontFamily = amiriFontFamily,
                            textDirection = TextDirection.Rtl
                        )
                    )

                    // Floating Voice Mic Dictation Button inside textbox
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, Translation.get("writeReportArabic", language))
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice Recognition not supported on this device.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Dictation",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gemini call button
                Button(
                    onClick = {
                        if (studentId.isBlank() || reportArabic.isBlank()) {
                            Toast.makeText(context, "Please select student and type report", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.analyzeDailyLesson(
                            studentId = studentId,
                            lessonNumber = lessonNumber,
                            lessonDate = lessonDate,
                            durationMinutes = duration,
                            teacherName = teacherName.ifBlank { "Teacher" },
                            activeSubjects = selectedSubjects.toList(),
                            arabicReportText = reportArabic
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translation.get("translating", language))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translation.get("translateBtn", language))
                    }
                }
            }
        }

        // Live AI Analysis Preview Panel
        analysisResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ AI Extracted Report",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current
                        TextButton(
                            onClick = {
                                val fullReport = buildString {
                                    append(result.reportEnglish)
                                    if (result.achievements.isNotEmpty()) {
                                        append("\n\n${Translation.get("achievements", language)}:\n")
                                        result.achievements.forEach { append("• $it\n") }
                                    }
                                    if (result.weaknesses.isNotEmpty()) {
                                        append("\n${Translation.get("weaknesses", language)}:\n")
                                        result.weaknesses.forEach { append("• $it\n") }
                                    }
                                    append("\n${Translation.get("homework", language)}: ${result.homeworkAssigned}")
                                    append("\n${Translation.get("grade", language)}: ${result.grade}")
                                }
                                clipboardManager.setText(AnnotatedString(fullReport))
                                Toast.makeText(context, Translation.get("copied", language), Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Translation.get("copy", language), style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExpandableText(
                            text = result.reportEnglish,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLinesCollapsed = 3,
                            language = language
                        )

                        if (result.achievements.isNotEmpty()) {
                            Text(Translation.get("achievements", language), fontWeight = FontWeight.Bold)
                            result.achievements.forEach { ach ->
                                ExpandableText(
                                    text = "• $ach",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLinesCollapsed = 2,
                                    language = language
                                )
                            }
                        }

                        if (result.weaknesses.isNotEmpty()) {
                            Text(Translation.get("weaknesses", language), fontWeight = FontWeight.Bold)
                            result.weaknesses.forEach { weak ->
                                ExpandableText(
                                    text = "• $weak",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLinesCollapsed = 2,
                                    language = language
                                )
                            }
                        }

                        ExpandableText(
                            text = "${Translation.get("homework", language)}: ${result.homeworkAssigned}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLinesCollapsed = 2,
                            language = language
                        )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${Translation.get("grade", language)}: ", fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE6F4EA))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(result.grade, color = Color(0xFF137333), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.saveDailyLesson(
                                studentId = studentId,
                                lessonNumber = lessonNumber,
                                lessonDate = lessonDate,
                                duration = duration,
                                teacherName = teacherName.ifBlank { "Teacher" },
                                subjectsTaught = selectedSubjects.toList(),
                                reportArabic = reportArabic,
                                reportEnglish = result.reportEnglish,
                                achievements = result.achievements,
                                weaknesses = result.weaknesses,
                                homeworkAssigned = result.homeworkAssigned,
                                homeworkCompleted = result.homeworkCompleted,
                                grade = result.grade
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translation.get("saveLesson", language))
                    }
                }
            }
        }
    }
}

// --- MONTHLY REPORT COMPILER SCREEN ---
@Composable
fun MonthlyReportGenScreen(viewModel: TeacherViewModel, initialStudentId: String?) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()

    var studentId by remember { mutableStateOf(initialStudentId ?: "") }
    var monthString by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())) }
    var exportMonthString by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())) }

    val selectedStudent = students.find { it.id == studentId }

    val isAnalyzing by viewModel.isMonthlyAnalyzing.collectAsStateWithLifecycle()
    val draftResult by viewModel.monthlyAnalysisResult.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Indicators values
    var memScore by remember { mutableStateOf("Excellent") }
    var memComment by remember { mutableStateOf("") }
    var revScore by remember { mutableStateOf("Excellent") }
    var revComment by remember { mutableStateOf("") }
    var tajScore by remember { mutableStateOf("Excellent") }
    var tajComment by remember { mutableStateOf("") }
    var comScore by remember { mutableStateOf("Excellent") }
    var comComment by remember { mutableStateOf("") }

    // Subjects paragraphs
    var newMemorisationText by remember { mutableStateOf("") }
    var revisionText by remember { mutableStateOf("") }
    var readingText by remember { mutableStateOf("") }
    var arabicText by remember { mutableStateOf("") }
    var islamicText by remember { mutableStateOf("") }

    var strengthsText by remember { mutableStateOf("") }
    var recommendationsText by remember { mutableStateOf("") }
    var nextPlanText by remember { mutableStateOf("") }

    // Synchronize edit form when draft compiles
    LaunchedEffect(draftResult) {
        draftResult?.let { draft ->
            memScore = draft.indicators.memorisationProgress.score
            memComment = draft.indicators.memorisationProgress.comment
            revScore = draft.indicators.revisionStrength.score
            revComment = draft.indicators.revisionStrength.comment
            tajScore = draft.indicators.tajweedFoundation.score
            tajComment = draft.indicators.tajweedFoundation.comment
            comScore = draft.indicators.commitment.score
            comComment = draft.indicators.commitment.comment

            newMemorisationText = draft.sections.newMemorisation ?: ""
            revisionText = draft.sections.revision ?: ""
            readingText = draft.sections.reading ?: ""
            arabicText = draft.sections.arabicEvaluation ?: ""
            islamicText = draft.sections.islamicStudiesEvaluation ?: ""

            strengthsText = draft.strengths.joinToString("\n")
            recommendationsText = draft.recommendations.joinToString("\n")
            nextPlanText = draft.nextMonthPlan
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Translation.get("monthlyReports", language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(Translation.get("studentName", language), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(selectedStudent?.name ?: Translation.get("chooseStudent", language))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    studentId = s.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text(Translation.get("selectMonth", language), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = monthString,
                    onValueChange = { monthString = it },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (studentId.isBlank()) {
                            Toast.makeText(context, "Please select student.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.generateMonthlyDraft(studentId, monthString)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translation.get("generatingDraft", language))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translation.get("generateDraftBtn", language))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = Translation.get("combinedReportsTitle", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Translation.get("combinedReportsDesc", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = exportMonthString,
                    onValueChange = { exportMonthString = it },
                    label = { Text(Translation.get("selectMonth", language)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                val coroutineScope = rememberCoroutineScope()
                
                val saveDocLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/msword")
                ) { uri ->
                    if (uri != null) {
                        coroutineScope.launch {
                            val reports = viewModel.getReportsForMonth(exportMonthString)
                            if (reports.isEmpty()) {
                                Toast.makeText(context, String.format(Translation.get("noReportsForMonth", language), exportMonthString), Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            val html = ReportExportHelper.generateCombinedHtmlReport(reports, students, exportMonthString, language)
                            try {
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    outputStream.write(html.toByteArray(Charsets.UTF_8))
                                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val reports = viewModel.getReportsForMonth(exportMonthString)
                                if (reports.isEmpty()) {
                                    Toast.makeText(context, String.format(Translation.get("noReportsForMonth", language), exportMonthString), Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                val html = ReportExportHelper.generateCombinedHtmlReport(reports, students, exportMonthString, language)
                                val jobName = "Reports_$exportMonthString"
                                ReportExportHelper.printHtml(context, html, jobName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translation.get("printPdfBtn", language), fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val reports = viewModel.getReportsForMonth(exportMonthString)
                                if (reports.isEmpty()) {
                                    Toast.makeText(context, String.format(Translation.get("noReportsForMonth", language), exportMonthString), Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                val filename = "Monthly_Reports_$exportMonthString.doc"
                                saveDocLauncher.launch(filename)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B73E8))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Translation.get("downloadWordBtn", language), fontSize = 11.sp)
                    }
                }
            }
        }

        // Compiled Edit Fields Form
        draftResult?.let {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(Translation.get("indicators", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // 4 Indicators
                IndicatorEditCard(Translation.get("memorisationProgress", language), memScore, memComment, { memScore = it }, { memComment = it })
                IndicatorEditCard(Translation.get("revisionStrength", language), revScore, revComment, { revScore = it }, { revComment = it })
                IndicatorEditCard(Translation.get("tajweedFoundation", language), tajScore, tajComment, { tajScore = it }, { tajComment = it })
                IndicatorEditCard(Translation.get("commitment", language), comScore, comComment, { comScore = it }, { comComment = it })

                Text(Translation.get("subjectDetails", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (selectedStudent?.subjects?.contains("Quran") == true) {
                    OutlinedTextField(
                        value = newMemorisationText,
                        onValueChange = { newMemorisationText = it },
                        label = { Text("Quran - New Memorisation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = revisionText,
                        onValueChange = { revisionText = it },
                        label = { Text("Quran - Revision") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = readingText,
                        onValueChange = { readingText = it },
                        label = { Text("Quran - Reading Fluency") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedStudent?.subjects?.contains("Arabic") == true) {
                    OutlinedTextField(
                        value = arabicText,
                        onValueChange = { arabicText = it },
                        label = { Text("Arabic Language Evaluation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedStudent?.subjects?.contains("Islamic Studies") == true) {
                    OutlinedTextField(
                        value = islamicText,
                        onValueChange = { islamicText = it },
                        label = { Text("Islamic Studies Evaluation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = strengthsText,
                    onValueChange = { strengthsText = it },
                    label = { Text("Key Strengths (One line per item)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recommendationsText,
                    onValueChange = { recommendationsText = it },
                    label = { Text("Recommendations (One line per item)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextPlanText,
                    onValueChange = { nextPlanText = it },
                    label = { Text("Proposed Next Month Plan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.saveMonthlyReport(
                            studentId = studentId,
                            month = monthString,
                            teacherName = teacherName.ifBlank { "Teacher" },
                            memorisationProgressScore = memScore,
                            memorisationProgressComment = memComment,
                            revisionStrengthScore = revScore,
                            revisionStrengthComment = revComment,
                            tajweedFoundationScore = tajScore,
                            tajweedFoundationComment = tajComment,
                            commitmentScore = comScore,
                            commitmentComment = comComment,
                            newMemorisation = if (newMemorisationText.isBlank()) null else newMemorisationText,
                            revision = if (revisionText.isBlank()) null else revisionText,
                            reading = if (readingText.isBlank()) null else readingText,
                            arabicEvaluation = if (arabicText.isBlank()) null else arabicText,
                            islamicStudiesEvaluation = if (islamicText.isBlank()) null else islamicText,
                            strengths = strengthsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                            recommendations = recommendationsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                            nextMonthPlan = nextPlanText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Translation.get("saveReportBtn", language))
                }
            }
        }
    }
}

@Composable
fun IndicatorEditCard(
    title: String,
    score: String,
    comment: String,
    onScoreChange: (String) -> Unit,
    onCommentChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Excellent", "Good", "Steady", "Needs Improvement").forEach { s ->
                    val isSelected = score == s
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onScoreChange(s) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = s.substringBefore(" "),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter Commentary") }
            )
        }
    }
}

// --- SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: TeacherViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val aiModel by viewModel.aiModel.collectAsStateWithLifecycle()
    val globalInstructions by viewModel.globalInstructions.collectAsStateWithLifecycle()

    val firebaseCloudEnabledFlow by viewModel.isCloudMode.collectAsStateWithLifecycle()

    var nameInput by remember { mutableStateOf(teacherName) }
    var modelSelection by remember { mutableStateOf(aiModel) }
    var instructionsInput by remember { mutableStateOf(globalInstructions) }

    var firebaseCloudEnabledToggle by remember { mutableStateOf(firebaseCloudEnabledFlow) }

    var authEmailInput by remember { mutableStateOf("") }
    var authPasswordInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Launchers for Backup Export/Import file operations
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportBackup { json ->
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val json = stream.bufferedReader().use { r -> r.readText() }
                viewModel.importBackup(json) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val isCloudMode by viewModel.isCloudMode.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Translation.get("generalSettings", language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(Translation.get("teacherName", language)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(Translation.get("aiModel", language), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("gemini-3.5-flash", "gemini-3.1-pro-preview").forEach { m ->
                        val isSelected = modelSelection == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { modelSelection = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m.substringAfter("gemini-"),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = instructionsInput,
                    onValueChange = { instructionsInput = it },
                    label = { Text(Translation.get("globalAiInstructions", language)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.saveSettings(nameInput, apiKey, modelSelection, instructionsInput)
                        Toast.makeText(context, Translation.get("settingsSaved", language), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Translation.get("save", language))
                }
            }
        }

        // Firebase Cloud Sync Configuration Section
        val isArabic = language == "ar"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) "بوابة المزامنة السحابية (Firebase)" else "Cloud Sync Portal (Firebase)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Switch(
                        checked = firebaseCloudEnabledToggle,
                        onCheckedChange = { 
                            firebaseCloudEnabledToggle = it 
                            viewModel.saveFirebaseSettings(
                                TeacherViewModel.DEFAULT_FIREBASE_API_KEY,
                                TeacherViewModel.DEFAULT_FIREBASE_PROJECT_ID,
                                TeacherViewModel.DEFAULT_FIREBASE_APP_ID,
                                it
                            )
                        }
                    )
                }

                Text(
                    text = if (isArabic) "تتيح لك المزامنة السحابية حفظ جميع بياناتك ومزامنتها مباشرة عبر الإنترنت للوصول السريع ومشاركتها." else "Cloud sync allows you to back up and synchronize all your data online for fast access and sharing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (firebaseCloudEnabledToggle) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isArabic) "بوابة المصادقة والحساب" else "Authentication & Account Portal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    when (val authState = authState) {
                        is AuthState.Success -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isArabic) "الحساب المتصل سحابياً:" else "Cloud Connected Account:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = authState.user.email ?: "",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.logout() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(Translation.get("logout", language), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        else -> {
                            if (authState is AuthState.Loading) {
                                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                }
                            } else {
                                if (authState is AuthState.Error) {
                                    Text(
                                        text = authState.message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = authEmailInput,
                                    onValueChange = { authEmailInput = it },
                                    label = { Text(Translation.get("email", language)) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = authPasswordInput,
                                    onValueChange = { authPasswordInput = it },
                                    label = { Text(Translation.get("password", language)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (authEmailInput.isBlank() || authPasswordInput.isBlank()) {
                                                Toast.makeText(context, if (isArabic) "يرجى كتابة البريد وكلمة المرور" else "Please type email and password", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.signIn(authEmailInput, authPasswordInput, true) { success, msg ->
                                                Toast.makeText(context, if (success) Translation.get("authSuccess", language) else msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(Translation.get("login", language), fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (authEmailInput.isBlank() || authPasswordInput.isBlank()) {
                                                Toast.makeText(context, if (isArabic) "يرجى كتابة البريد وكلمة المرور" else "Please type email and password", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.signUp(authEmailInput, authPasswordInput, nameInput.ifBlank { "Teacher" }, true) { success, msg ->
                                                Toast.makeText(context, if (success) Translation.get("registrationSuccess", language) else msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isArabic) "إنشاء حساب" else "Sign Up", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Backup and Restore Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = Translation.get("backupRestore", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Translation.get("exportImportDesc", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { exportFileLauncher.launch("Islamic_Teacher_Backup.json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Translation.get("exportBackup", language), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { importFileLauncher.launch("application/json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = "")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Translation.get("importBackup", language), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- STUDENT CREATION DIALOG ---
@Composable
fun StudentDialog(
    student: Student? = null,
    language: String,
    onDismiss: () -> Unit,
    onSave: (String, List<String>, String, String) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }
    var customInst by remember { mutableStateOf(student?.customInstructions ?: "") }
    val selectedSubjects = remember {
        mutableStateListOf<String>().apply {
            if (student != null) {
                addAll(student.subjects)
            } else {
                add("Quran")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (student != null) Translation.get("editStudent", language) else Translation.get("addStudent", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translation.get("studentName", language)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(Translation.get("activeSubjects", language), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Quran", "Arabic", "Islamic Studies").forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedSubjects.contains(s),
                                onCheckedChange = { checked ->
                                    if (checked) selectedSubjects.add(s) else selectedSubjects.remove(s)
                                }
                            )
                            Text(
                                text = when (s) {
                                    "Quran" -> Translation.get("quran", language)
                                    "Arabic" -> Translation.get("arabic", language)
                                    else -> Translation.get("islamicStudies", language)
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customInst,
                    onValueChange = { customInst = it },
                    label = { Text(Translation.get("customStudentInstructions", language)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Translation.get("notes", language)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onSave(name, selectedSubjects.toList(), notes, customInst)
                }
            }) {
                Text(Translation.get("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

// --- REPORT PREVIEW DIALOG ---
@Composable
fun ReportPreviewDialog(
    report: MonthlyReport,
    language: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report Evaluation - ${report.month}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current
                        IconButton(onClick = {
                            val fullReport = buildString {
                                append("Monthly Report - ${report.month}\n")
                                append("Teacher: ${report.teacherName}\n")
                                append("Generated At: ${report.generatedAt}\n\n")
                                append("--- ${Translation.get("indicators", language)} ---\n")
                                append("${Translation.get("memorisationProgress", language)}: ${report.memorisationProgressScore} - ${report.memorisationProgressComment}\n")
                                append("${Translation.get("revisionStrength", language)}: ${report.revisionStrengthScore} - ${report.revisionStrengthComment}\n")
                                append("${Translation.get("tajweedFoundation", language)}: ${report.tajweedFoundationScore} - ${report.tajweedFoundationComment}\n")
                                append("${Translation.get("commitment", language)}: ${report.commitmentScore} - ${report.commitmentComment}\n\n")
                                append("--- ${Translation.get("subjectDetails", language)} ---\n")
                                report.newMemorisation?.let { append("• New Memorisation: $it\n") }
                                report.revision?.let { append("• Revision: $it\n") }
                                report.reading?.let { append("• Reading & Tajweed: $it\n") }
                                report.arabicEvaluation?.let { append("• Arabic Evaluation: $it\n") }
                                report.islamicStudiesEvaluation?.let { append("• Islamic Studies Evaluation: $it\n") }
                                if (report.strengths.isNotEmpty()) {
                                    append("\n--- ${Translation.get("strengths", language)} ---\n")
                                    report.strengths.forEach { append("• $it\n") }
                                }
                                if (report.recommendations.isNotEmpty()) {
                                    append("\n--- ${Translation.get("recommendations", language)} ---\n")
                                    report.recommendations.forEach { append("• $it\n") }
                                }
                                append("\n--- ${Translation.get("nextMonthPlan", language)} ---\n")
                                append(report.nextMonthPlan)
                            }
                            clipboardManager.setText(AnnotatedString(fullReport))
                            Toast.makeText(context, Translation.get("copied", language), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        IconButton(onClick = {
                            val fullReport = buildString {
                                append("📊 *${Translation.get("monthlyReports", language)} - ${report.month}*\n")
                                append("👤 *Teacher:* ${report.teacherName}\n")
                                append("📅 *Date:* ${report.generatedAt}\n\n")
                                append("⭐ *${Translation.get("indicators", language)}* ⭐\n")
                                append("• *${Translation.get("memorisationProgress", language)}:* ${report.memorisationProgressScore}\n_${report.memorisationProgressComment}_\n")
                                append("• *${Translation.get("revisionStrength", language)}:* ${report.revisionStrengthScore}\n_${report.revisionStrengthComment}_\n")
                                append("• *${Translation.get("tajweedFoundation", language)}:* ${report.tajweedFoundationScore}\n_${report.tajweedFoundationComment}_\n")
                                append("• *${Translation.get("commitment", language)}:* ${report.commitmentScore}\n_${report.commitmentComment}_\n\n")
                                append("📚 *${Translation.get("subjectDetails", language)}*\n")
                                report.newMemorisation?.let { append("• *New Memorisation:* $it\n") }
                                report.revision?.let { append("• *Revision:* $it\n") }
                                report.reading?.let { append("• *Reading & Tajweed:* $it\n") }
                                report.arabicEvaluation?.let { append("• *Arabic Evaluation:* $it\n") }
                                report.islamicStudiesEvaluation?.let { append("• *Islamic Studies Evaluation:* $it\n") }
                                if (report.strengths.isNotEmpty()) {
                                    append("\n💪 *${Translation.get("strengths", language)}*\n")
                                    report.strengths.forEach { append("• $it\n") }
                                }
                                if (report.recommendations.isNotEmpty()) {
                                    append("\n💡 *${Translation.get("recommendations", language)}*\n")
                                    report.recommendations.forEach { append("• $it\n") }
                                }
                                append("\n🎯 *${Translation.get("nextMonthPlan", language)}*\n")
                                append(report.nextMonthPlan)
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, fullReport)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, Translation.get("share", language))
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Bio Meta Data
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Teacher: ${report.teacherName}")
                                Text("Generated At: ${report.generatedAt}")
                            }
                        }

                        // Indicators
                        Text(Translation.get("indicators", language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IndicatorPrintRow(Translation.get("memorisationProgress", language), report.memorisationProgressScore, report.memorisationProgressComment, language)
                        IndicatorPrintRow(Translation.get("revisionStrength", language), report.revisionStrengthScore, report.revisionStrengthComment, language)
                        IndicatorPrintRow(Translation.get("tajweedFoundation", language), report.tajweedFoundationScore, report.tajweedFoundationComment, language)
                        IndicatorPrintRow(Translation.get("commitment", language), report.commitmentScore, report.commitmentComment, language)

                        // Subject specific Evaluations
                        Text(Translation.get("subjectDetails", language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        report.newMemorisation?.let { ExpandableText("• New Memorisation: $it", maxLinesCollapsed = 2, language = language) }
                        report.revision?.let { ExpandableText("• Revision: $it", maxLinesCollapsed = 2, language = language) }
                        report.reading?.let { ExpandableText("• Reading & Tajweed: $it", maxLinesCollapsed = 2, language = language) }
                        report.arabicEvaluation?.let { ExpandableText("• Arabic Evaluation: $it", maxLinesCollapsed = 2, language = language) }
                        report.islamicStudiesEvaluation?.let { ExpandableText("• Islamic Studies Evaluation: $it", maxLinesCollapsed = 2, language = language) }

                        // Strengths
                        if (report.strengths.isNotEmpty()) {
                            Text(Translation.get("strengths", language), fontWeight = FontWeight.Bold)
                            report.strengths.forEach { s -> ExpandableText("• $s", maxLinesCollapsed = 2, language = language) }
                        }

                        // Recommendations
                        if (report.recommendations.isNotEmpty()) {
                            Text(Translation.get("recommendations", language), fontWeight = FontWeight.Bold)
                            report.recommendations.forEach { r -> ExpandableText("• $r", maxLinesCollapsed = 2, language = language) }
                        }

                        // Plan
                        Text(Translation.get("nextMonthPlan", language), fontWeight = FontWeight.Bold)
                        ExpandableText(report.nextMonthPlan, maxLinesCollapsed = 3, language = language)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Translation.get("cancel", language))
                }
            }
        }
    }
}

@Composable
fun IndicatorPrintRow(title: String, score: String, comment: String, language: String = "ar") {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (comment.isNotBlank()) {
                ExpandableText(
                    text = comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLinesCollapsed = 2,
                    language = language
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE6F4EA))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(score, color = Color(0xFF137333), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    fontWeight: FontWeight? = null,
    maxLinesCollapsed: Int = 3,
    language: String = "ar",
    textDirection: TextDirection? = null
) {
    if (text.isBlank()) return

    var isExpanded by remember(text) { mutableStateOf(false) }
    var isTextOverflown by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize()) {
        val styledTextStyle = textDirection?.let { style.copy(textDirection = it) } ?: style

        SelectionContainer {
            Text(
                text = text,
                style = styledTextStyle,
                color = color,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                maxLines = if (isExpanded) Int.MAX_VALUE else maxLinesCollapsed,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                        isTextOverflown = true
                    }
                }
            )
        }

        if (isTextOverflown || isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isExpanded) Translation.get("showLess", language) else Translation.get("showMore", language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
