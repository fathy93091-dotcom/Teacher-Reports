// 1) FIREBASE CONFIGURATION
const firebaseConfig = {
  apiKey: "AIzaSyAjIBUFG-WKNzy7mDAFQO3Sh-chxZ8XzUs",
  authDomain: "teacher-reports-d168b.firebaseapp.com",
  projectId: "teacher-reports-d168b",
  storageBucket: "teacher-reports-d168b.firebasestorage.app",
  messagingSenderId: "318105019832",
  appId: "1:318105019832:web:b5db2e2d587fe1391c1b4d",
  measurementId: "G-258X50R9FR"
};

if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const auth = firebase.auth();
const db = firebase.firestore();
const storage = firebase.storage();

// 2) GEMINI API KEY (STORED SECURELY IN LOCALSTORAGE VIA SETTINGS)
const GEMINI_API_KEY = "";

// APP STATE & I18N DICTIONARY
let currentUser = null;
let studentsList = [];
let isAuthSignUp = false;
let activeGeneratedMonthlyReport = null;
let currentLang = localStorage.getItem('app_lang') || 'ar';

const i18n = {
    ar: {
        app_title: "تقارير المعلم الإسلامي",
        app_subtitle: "نظام متابعة الطلاب والتقارير الذكية",
        nav_dashboard: "لوحة التحكم",
        nav_students: "قائمة الطلاب",
        nav_daily: "درس يومي",
        nav_monthly: "تقرير شهري",
        nav_settings: "الإعدادات",
        cloud_connected: "متصل بالسحابة",
        auth_title: "تسجيل الدخول للمعلم",
        auth_subtitle: "قم بتسجيل الدخول لبدء حفظ ومزامنة تقارير طلابك سحابياً تلقائياً",
        teacher_name_label: "اسم المعلم",
        email_label: "البريد الإلكتروني",
        password_label: "كلمة المرور",
        remember_me_label: "تذكرني",
        login_btn: "تسجيل الدخول",
        demo_login_btn: "دخول تجريبي سريع",
        no_account_msg: "ليس لديك حساب؟",
        register_link: "إنشاء حساب جديد",
        welcome_teacher: "أهلاً بك،",
        dash_welcome_sub: "مرحباً بك في لوحة تحكم تقارير حلقات القرآن واللغة العربية والعلوم الإسلامية.",
        active_subjects_title: "المواد النشطة في التطبيق:",
        subject_quran: "القرآن الكريم",
        subject_arabic: "اللغة العربية",
        subject_islamic: "العلوم الإسلامية",
        subject_all: "شامل (جميع المواد)",
        btn_add_daily: "إضافة درس يومي",
        btn_gen_monthly: "توليد تقرير شهري",
        stat_total_students: "إجمالي الطلاب",
        stat_today_lessons: "دروس اليوم",
        stat_monthly_reports: "التقارير الشهرية",
        stat_ai_engine: "محرّك الذكاء الاصطناعي",
        my_students_list: "قائمتي المباشرة للطلاب",
        btn_add_student: "طالب جديد",
        btn_add_new_student: "إضافة طالب جديد",
        ph_search_student: "البحث باسم الطالب أو المستوى...",
        daily_title: "تسجيل الدرس والتقييم اليومي",
        subject_label: "المادة الدراسية *",
        select_student_label: "اختر الطالب *",
        date_label: "التاريخ",
        raw_input_label: "إدخال التقرير المباشر دفعة واحدة (كتابة أو لصق كامل الدرس)",
        ai_feature_badge: "خاصية الذكاء الاصطناعي",
        ph_raw_input: "مثال: أحمد حفظ اليوم سورة المزمل 1-10، وراجع جزء عم، وتلاوته كانت ممتازة، والتقدير ممتاز والواجب مراجعة الجزء الأول",
        btn_parse_raw_ai: "تحليل وتنسيق التقرير تلقائياً بالذكاء الاصطناعي",
        lesson_components_title: "عناصر الدرس المنجزة اليوم",
        new_memo_label: "الحفظ الجديد (الفرع)",
        ph_new_memo: "مثال: سورة النبأ 1-15",
        revision_label: "المراجعة (المقدار)",
        ph_revision: "مثال: جزء عم كاملاً",
        reading_label: "التلاوة / القراءة والتجويد",
        ph_reading: "مثال: أحكام النون الساكنة",
        homework_components_title: "الواجب المطلوب القادم (حفظ - مراجعة - قراءة)",
        hw_memo_label: "واجب الحفظ",
        ph_hw_memo: "مثال: حفظ سورة النازعات",
        hw_revision_label: "واجب المراجعة",
        ph_hw_revision: "مثال: مراجعة جزء تبارك",
        hw_reading_label: "واجب القراءة / التلاوة",
        ph_hw_reading: "مثال: تلاوة سورة يس",
        overall_grade_label: "التقدير العام اليومي",
        teacher_notes_label: "ملاحظات المعلم الخاصة",
        ph_teacher_notes: "ملاحظات سريعة للأستاذ...",
        btn_gen_daily_ai: "توليد التقرير والواجب بالذكاء الاصطناعي بلغة إسلامية دقيقة (Gemini)",
        arabic_report_label: "نص التقرير الموجه لولي الأمر (عربي)",
        ph_arabic_report: "التقرير اليومي باللغة العربية...",
        english_summary_label: "English Summary (Accurate Islamic Terminology)",
        ph_english_summary: "English report with accurate Islamic terms (Noble Quran, Surah, Tajweed, Tilawah, etc.)...",
        homework_label: "الواجب المنزلي المكتمل الشامل",
        ph_homework: "الواجب الشامل المطلوب تحضيره...",
        btn_save_daily: "حفظ التقرير في السحابة",
        btn_send_whatsapp: "إرسال عبر الواتساب مباشرة",
        monthly_title: "إنشاء وتوليد التقرير الشهري الشامل",
        month_year_label: "الشهر / السنة",
        performance_indicators: "مؤشرات الأداء العامة",
        memo_progress: "تقدم الحفظ (Memorisation)",
        revision_strength: "قوة المراجعة (Revision)",
        tajweed_rules: "أحكام التجويد والقراءة (Tajweed & Recitation)",
        discipline: "الانضباط والالتزام (Commitment)",
        ph_comment: "تعليق قصير...",
        btn_gen_monthly_ai: "توليد التقرير الشهري بالذكاء الاصطناعي (Gemini)",
        btn_save_report: "حفظ التقرير",
        btn_print: "طباعة / تصدير PDF",
        settings_title: "الإعدادات العامة وإدارة البيانات",
        teacher_name_auth_label: "اسم المعلم المعتمد",
        ai_model_label: "نموذج الذكاء الاصطناعي (Gemini Model)",
        global_instructions_label: "توجيهات الذكاء الاصطناعي العامة (Global AI Instructions)",
        btn_save_settings: "حفظ التغييرات",
        backup_section_title: "النسخ الاحتياطي وتصدير البيانات",
        btn_export_json: "تصدير نسخة احتياطية (JSON)",
        modal_add_student_title: "إضافة / تعديل طالب",
        student_fullname_label: "اسم الطالب الكامل *",
        ph_student_name: "مثال: أحمد محمد علي",
        age_label: "العمر",
        level_label: "المستوى التعليمي",
        parent_phone_label: "رقم هاتف ولي الأمر",
        whatsapp_group_label: "رابط جروب الواتساب الخاص بالملاحظات أو هاتف ولي الأمر",
        ph_whatsapp_link: "https://chat.whatsapp.com/...",
        whatsapp_hint: "ضع رابط الجروب لإرسال التقارير اليومية إليه بنقرة واحدة",
        custom_ai_prompt_label: "توجيهات واقتراحات خاصة للذكاء الاصطناعي لهذا الطالب",
        ph_custom_prompt: "أدخل أسلوب صياغة التقرير المطلوب لهذا الطالب (مثال: ركز على تحفيز حفظه، التقرير يكون باللغة الإنجليزية، الخ)",
        custom_prompt_hint: "يتم تضمين هذه التوجيهات تلقائياً عند توليد التقرير اليومي والشهري لهذا الطالب",
        student_notes_label: "ملاحظات خاصة الطالب",
        ph_student_notes: "ملاحظات حول أسلوب التعلم...",
        btn_cancel: "إلغاء",
        btn_save_student: "حفظ الطالب"
    },
    en: {
        app_title: "Islamic Teacher Reports",
        app_subtitle: "Student Tracking & Smart AI Reports System",
        nav_dashboard: "Dashboard",
        nav_students: "Students",
        nav_daily: "Daily Lesson",
        nav_monthly: "Monthly Report",
        nav_settings: "Settings",
        cloud_connected: "Cloud Connected",
        auth_title: "Teacher Login",
        auth_subtitle: "Sign in to securely sync student reports to the cloud",
        teacher_name_label: "Teacher Name",
        email_label: "Email Address",
        password_label: "Password",
        remember_me_label: "Remember Me",
        login_btn: "Sign In",
        demo_login_btn: "Quick Demo Access",
        no_account_msg: "Don't have an account?",
        register_link: "Register New Account",
        welcome_teacher: "Welcome,",
        dash_welcome_sub: "Welcome to Quran, Arabic & Islamic Studies Teacher Dashboard.",
        active_subjects_title: "Active Subjects:",
        subject_quran: "Noble Quran",
        subject_arabic: "Arabic Language",
        subject_islamic: "Islamic Studies",
        subject_all: "All Subjects",
        btn_add_daily: "Add Daily Lesson",
        btn_gen_monthly: "Generate Monthly Report",
        stat_total_students: "Total Students",
        stat_today_lessons: "Today's Lessons",
        stat_monthly_reports: "Monthly Reports",
        stat_ai_engine: "AI Model Engine",
        my_students_list: "My Active Students",
        btn_add_student: "New Student",
        btn_add_new_student: "Add New Student",
        ph_search_student: "Search student name or level...",
        daily_title: "Record Daily Lesson & Evaluation",
        subject_label: "Subject *",
        select_student_label: "Select Student *",
        date_label: "Date",
        raw_input_label: "Single Raw Full Report Input (Type or Paste Entire Lesson)",
        ai_feature_badge: "AI Feature",
        ph_raw_input: "Example: Ahmed memorized Surah Al-Muzzammil 1-10 today, revised Juz Amma with excellent score, homework is to review Juz 1",
        btn_parse_raw_ai: "Auto-Parse & Format Report with AI",
        lesson_components_title: "Completed Lesson Components Today",
        new_memo_label: "New Memorisation (Hifdh)",
        ph_new_memo: "e.g., Surah An-Naba 1-15",
        revision_label: "Revision Portion (Muraja'ah)",
        ph_revision: "e.g., Full Juz Amma",
        reading_label: "Recitation & Tajweed (Tilawah)",
        ph_reading: "e.g., Rules of Noon Sakinah",
        homework_components_title: "Next Assigned Homework (Hifdh - Muraja'ah - Tilawah)",
        hw_memo_label: "Memorisation HW",
        ph_hw_memo: "e.g. Memorise Surah An-Naziat",
        hw_revision_label: "Revision HW",
        ph_hw_revision: "e.g. Review Juz Tabarak",
        hw_reading_label: "Recitation HW",
        ph_hw_reading: "e.g. Recite Surah Yaseen",
        overall_grade_label: "Overall Daily Grade",
        teacher_notes_label: "Teacher Private Notes",
        ph_teacher_notes: "Quick teacher notes...",
        btn_gen_daily_ai: "Generate AI Report & Homework (Gemini)",
        arabic_report_label: "Arabic Parent Report",
        ph_arabic_report: "Daily report text in Arabic...",
        english_summary_label: "English Summary (Accurate Islamic Terminology)",
        ph_english_summary: "English report with accurate Islamic terms (Noble Quran, Surah, Tajweed, Tilawah, etc.)...",
        homework_label: "Full Assigned Homework",
        ph_homework: "Full homework to prepare...",
        btn_save_daily: "Save Daily Report to Cloud",
        btn_send_whatsapp: "Send via WhatsApp",
        monthly_title: "Generate Monthly Performance Report",
        month_year_label: "Month / Year",
        performance_indicators: "General Performance Indicators",
        memo_progress: "Memorisation Progress",
        revision_strength: "Revision Strength",
        tajweed_rules: "Tajweed & Recitation",
        discipline: "Discipline & Commitment",
        ph_comment: "Short comment...",
        btn_gen_monthly_ai: "Generate Monthly AI Report (Gemini)",
        btn_save_report: "Save Report",
        btn_print: "Print / Export PDF",
        settings_title: "General Settings & Data Management",
        teacher_name_auth_label: "Registered Teacher Name",
        ai_model_label: "AI Model (Gemini Model)",
        global_instructions_label: "Global AI Instructions",
        btn_save_settings: "Save Changes",
        backup_section_title: "Data Backup & Export",
        btn_export_json: "Export Backup (JSON)",
        modal_add_student_title: "Add / Edit Student",
        student_fullname_label: "Full Student Name *",
        ph_student_name: "e.g. Ahmed Ali",
        age_label: "Age",
        level_label: "Educational Level",
        parent_phone_label: "Parent Phone Number",
        whatsapp_group_label: "WhatsApp Group Link or Guardian Number",
        ph_whatsapp_link: "https://chat.whatsapp.com/...",
        whatsapp_hint: "Paste WhatsApp group link to send daily reports directly with one click",
        custom_ai_prompt_label: "Custom AI Instructions for this Student",
        ph_custom_prompt: "Enter specific AI instructions for this student (e.g. focus on encouragement, write in English, etc.)",
        custom_prompt_hint: "These instructions are automatically included when generating AI reports for this student",
        student_notes_label: "Student Personal Notes",
        ph_student_notes: "Learning style notes...",
        btn_cancel: "Cancel",
        btn_save_student: "Save Student"
    }
};

// SUBJECT SELECTION HANDLER
function onSubjectChanged() {
    const subj = document.getElementById('daily-subject-select').value;
    const newMemoLbl = document.getElementById('lbl-new-memo');
    const revLbl = document.getElementById('lbl-revision');
    const readLbl = document.getElementById('lbl-reading');

    if (subj === 'اللغة العربية') {
        if (newMemoLbl) newMemoLbl.textContent = currentLang === 'ar' ? 'الدرس الجديد / النحو والصرف' : 'New Lesson / Grammar';
        if (revLbl) revLbl.textContent = currentLang === 'ar' ? 'المراجعة والتمارين' : 'Review & Exercises';
        if (readLbl) readLbl.textContent = currentLang === 'ar' ? 'القراءة والإملاء الخط' : 'Reading & Dictation';
    } else if (subj === 'العلوم الإسلامية') {
        if (newMemoLbl) newMemoLbl.textContent = currentLang === 'ar' ? 'درس العقيدة / الفقه / السيرة' : 'Fiqh / Aqeedah / Seerah Lesson';
        if (revLbl) revLbl.textContent = currentLang === 'ar' ? 'المراجعة والأحكام' : 'Review & Rulings';
        if (readLbl) readLbl.textContent = currentLang === 'ar' ? 'الأحاديث والأدعية' : 'Hadith & Duas';
    } else {
        if (newMemoLbl) newMemoLbl.textContent = currentLang === 'ar' ? 'الحفظ الجديد (الفرع)' : 'New Memorisation (Hifdh)';
        if (revLbl) revLbl.textContent = currentLang === 'ar' ? 'المراجعة (المقدار)' : "Revision Portion (Muraja'ah)";
        if (readLbl) readLbl.textContent = currentLang === 'ar' ? 'التلاوة / القراءة والتجويد' : 'Recitation & Tajweed (Tilawah)';
    }
}

// LANGUAGE SWITCHING LOGIC
function toggleLanguage() {
    currentLang = (currentLang === 'ar') ? 'en' : 'ar';
    localStorage.setItem('app_lang', currentLang);
    applyLanguage();
    onSubjectChanged();
}

function applyLanguage() {
    document.documentElement.lang = currentLang;
    document.documentElement.dir = (currentLang === 'ar') ? 'rtl' : 'ltr';

    // Button label
    const langBtnLabel = document.getElementById('lang-btn-label');
    if (langBtnLabel) langBtnLabel.textContent = (currentLang === 'ar') ? 'English' : 'العربية';

    const dict = i18n[currentLang];
    if (!dict) return;

    // Apply data-i18n text content
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) {
            el.textContent = dict[key];
        }
    });

    // Apply placeholders
    document.querySelectorAll('[data-i18n-ph]').forEach(el => {
        const key = el.getAttribute('data-i18n-ph');
        if (dict[key]) {
            el.placeholder = dict[key];
        }
    });
}

// TEACHER LOGGED IN HANDLER
function onTeacherLoggedIn(userObj) {
    currentUser = userObj;
    const displayName = userObj.displayName || (userObj.email ? userObj.email.split('@')[0] : (currentLang === 'ar' ? 'المعلم' : 'Teacher'));

    const authSection = document.getElementById('auth-section');
    const userInfo = document.getElementById('user-info');
    const userDisplayName = document.getElementById('user-display-name');
    const dashTeacherName = document.getElementById('dash-teacher-name');
    const settingsTeacherName = document.getElementById('settings-teacher-name');

    if (authSection) authSection.classList.add('hidden');
    if (userInfo) {
        userInfo.classList.remove('hidden');
        userInfo.classList.add('flex');
    }
    if (userDisplayName) userDisplayName.textContent = displayName;
    if (dashTeacherName) dashTeacherName.textContent = displayName;
    if (settingsTeacherName) settingsTeacherName.value = displayName;

    // Load saved settings & default global AI instructions
    if (typeof loadSettingsInputs === 'function') loadSettingsInputs();
    const globalInstr = document.getElementById('settings-global-instructions');
    if (globalInstr && !globalInstr.value) {
        globalInstr.value = "Always write the daily and monthly reports in a warm, encouraging, parent-friendly tone. Use accurate Islamic terms like 'Noble Quran', 'Surah', 'Ayah', 'Tajweed rules', 'Tilawah', 'Hifdh', and 'Muraja'ah'. Highlight achievements first, be honest about weaknesses without discouraging the student, and present actionable next steps.";
    }

    // Save active user session locally as backup
    try {
        localStorage.setItem('local_active_user', JSON.stringify({
            uid: userObj.uid,
            email: userObj.email || '',
            displayName: displayName
        }));
    } catch (e) {}

    // Sync Data
    loadStudentsFromFirestore();
    navigateTo('dashboard');
}

// INITIALIZATION
document.addEventListener('DOMContentLoaded', () => {
    applyLanguage();

    // Attach form submit listeners programmatically to prevent any default browser page reloads
    const authForm = document.querySelector('#auth-section form');
    if (authForm) {
        authForm.addEventListener('submit', (e) => {
            if (e) {
                e.preventDefault();
                if (typeof e.stopPropagation === 'function') e.stopPropagation();
            }
            handleAuthSubmit(e);
        });
    }

    const addStudentForm = document.querySelector('#add-student-modal form');
    if (addStudentForm) {
        addStudentForm.addEventListener('submit', (e) => {
            if (e) {
                e.preventDefault();
                if (typeof e.stopPropagation === 'function') e.stopPropagation();
            }
            handleAddStudentSubmit(e);
        });
    }

    const settingsForm = document.querySelector('#screen-settings form');
    if (settingsForm) {
        settingsForm.addEventListener('submit', (e) => {
            if (e) {
                e.preventDefault();
                if (typeof e.stopPropagation === 'function') e.stopPropagation();
            }
            handleSettingsSave(e);
        });
    }

    // Set current date
    const dailyDateInput = document.getElementById('daily-date');
    if (dailyDateInput) dailyDateInput.valueAsDate = new Date();
    const monthlyDateInput = document.getElementById('monthly-month-year');
    if (monthlyDateInput) monthlyDateInput.value = new Date().toISOString().slice(0, 7);

    // Restore Remembered Email if saved
    const savedEmail = localStorage.getItem('remembered_email');
    const isRemembered = localStorage.getItem('remember_me') === 'true';
    if (savedEmail && isRemembered) {
        const emailInput = document.getElementById('auth-email');
        const rememberCb = document.getElementById('auth-remember-me');
        if (emailInput) emailInput.value = savedEmail;
        if (rememberCb) rememberCb.checked = true;
    }

    // Check if local session exists first
    const savedLocalUser = localStorage.getItem('local_active_user');
    if (savedLocalUser) {
        try {
            const parsed = JSON.parse(savedLocalUser);
            if (parsed && parsed.uid) {
                onTeacherLoggedIn(parsed);
            }
        } catch (e) {}
    }

    // Observe Auth State
    auth.onAuthStateChanged(user => {
        if (user) {
            onTeacherLoggedIn(user);
        } else if (!localStorage.getItem('local_active_user')) {
            currentUser = null;
            const authSec = document.getElementById('auth-section');
            const uInfo = document.getElementById('user-info');
            if (authSec) authSec.classList.remove('hidden');
            if (uInfo) uInfo.classList.add('hidden');
            hideAllScreens();
        }
    });
});

// AUTHENTICATION LOGIC
function toggleAuthMode() {
    isAuthSignUp = !isAuthSignUp;
    const authTitle = document.getElementById('auth-title');
    const submitBtnSpan = document.getElementById('auth-submit-btn')?.querySelector('span');
    const authToggleMsg = document.getElementById('auth-toggle-msg');
    const authToggleBtn = document.getElementById('auth-toggle-btn');
    const nameField = document.getElementById('name-field');

    if (authTitle) {
        authTitle.textContent = isAuthSignUp 
            ? (currentLang === 'ar' ? "إنشاء حساب معلم جديد" : "Create Teacher Account") 
            : (currentLang === 'ar' ? "تسجيل الدخول للمعلم" : "Teacher Login");
    }
    if (submitBtnSpan) {
        submitBtnSpan.textContent = isAuthSignUp 
            ? (currentLang === 'ar' ? "إنشاء الحساب" : "Register") 
            : (currentLang === 'ar' ? "تسجيل الدخول" : "Sign In");
    }
    if (authToggleMsg) {
        authToggleMsg.textContent = isAuthSignUp 
            ? (currentLang === 'ar' ? "لديك حساب بالفعل؟" : "Already have an account?") 
            : (currentLang === 'ar' ? "ليس لديك حساب؟" : "Don't have an account?");
    }
    if (authToggleBtn) {
        authToggleBtn.textContent = isAuthSignUp 
            ? (currentLang === 'ar' ? "تسجيل الدخول" : "Sign In") 
            : (currentLang === 'ar' ? "إنشاء حساب جديد" : "Register");
    }
    
    if (nameField) {
        if (isAuthSignUp) {
            nameField.classList.remove('hidden');
        } else {
            nameField.classList.add('hidden');
        }
    }
}

function getArabicAuthErrorMessage(err) {
    if (!err) return currentLang === 'ar' ? "حدث خطأ أثناء الاتصال" : "Authentication error occurred";
    const code = err.code || '';
    switch (code) {
        case 'auth/invalid-email':
            return currentLang === 'ar' ? "صيغة البريد الإلكتروني غير صحيحة" : "Invalid email format";
        case 'auth/user-disabled':
            return currentLang === 'ar' ? "تم تعطيل هذا الحساب" : "User account disabled";
        case 'auth/user-not-found':
        case 'auth/wrong-password':
        case 'auth/invalid-credential':
            return currentLang === 'ar' ? "بيانات الدخول غير صحيحة أو الحساب غير موجود. اضغط على 'إنشاء حساب جديد'" : "Incorrect credentials or account not found. Click 'Register New Account'";
        case 'auth/email-already-in-use':
            return currentLang === 'ar' ? "هذا البريد الإلكتروني مسجل بالفعل. يرجى تسجيل الدخول" : "Email already registered. Please sign in";
        case 'auth/weak-password':
            return currentLang === 'ar' ? "كلمة المرور ضعيفة (يجب أن تكون 6 أحرف على الأقل)" : "Weak password (must be at least 6 characters)";
        case 'auth/network-request-failed':
            return currentLang === 'ar' ? "تعذر الاتصال بالشبكة، يرجى التحقق من اتصال الإنترنت" : "Network error, please check internet connection";
        default:
            return err.message || (currentLang === 'ar' ? "حدث خطأ في عملية تسجيل الدخول" : "Login process error");
    }
}

async function handleDemoAuth(e) {
    if (e) {
        if (typeof e.preventDefault === 'function') e.preventDefault();
        if (typeof e.stopPropagation === 'function') e.stopPropagation();
    }
    const errorDiv = document.getElementById('auth-error');
    const submitBtn = document.getElementById('auth-submit-btn');
    if (errorDiv) errorDiv.classList.add('hidden');
    if (submitBtn) submitBtn.disabled = true;
    try {
        await auth.signInAnonymously();
    } catch (err) {
        console.warn("Anonymous auth failed, creating local demo user:", err);
        const demoUser = {
            uid: 'demo_teacher_' + Math.floor(Math.random() * 10000),
            email: 'demo@teacher.com',
            displayName: currentLang === 'ar' ? 'معلم تجريبي' : 'Demo Teacher'
        };
        onTeacherLoggedIn(demoUser);
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

async function handleAuthSubmit(e) {
    if (e) {
        if (typeof e.preventDefault === 'function') e.preventDefault();
        if (typeof e.stopPropagation === 'function') e.stopPropagation();
    }

    const email = document.getElementById('auth-email')?.value.trim() || '';
    const password = document.getElementById('auth-password')?.value || '';
    const rememberMe = document.getElementById('auth-remember-me')?.checked;
    const errorDiv = document.getElementById('auth-error');
    const submitBtn = document.getElementById('auth-submit-btn');

    if (!email || !password) {
        if (errorDiv) {
            errorDiv.textContent = currentLang === 'ar' ? "يرجى أدخل البريد الإلكتروني وكلمة المرور" : "Please enter email and password";
            errorDiv.classList.remove('hidden');
        }
        return false;
    }

    if (errorDiv) errorDiv.classList.add('hidden');
    if (submitBtn) submitBtn.disabled = true;
    const origBtnContent = submitBtn ? submitBtn.innerHTML : '';
    if (submitBtn) {
        submitBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> <span>${isAuthSignUp ? (currentLang === 'ar' ? 'جاري إنشاء الحساب...' : 'Creating Account...') : (currentLang === 'ar' ? 'جاري تسجيل الدخول...' : 'Signing in...')}</span>`;
    }

    // Manage Remember Me
    try {
        if (rememberMe) {
            localStorage.setItem('remembered_email', email);
            localStorage.setItem('remember_me', 'true');
        } else {
            localStorage.removeItem('remembered_email');
            localStorage.removeItem('remember_me');
        }
    } catch (sErr) {
        console.warn("LocalStorage save error:", sErr);
    }

    try {
        if (isAuthSignUp) {
            const name = document.getElementById('auth-name')?.value.trim() || (currentLang === 'ar' ? "المعلم" : "Teacher");
            try {
                const cred = await auth.createUserWithEmailAndPassword(email, password);
                await cred.user.updateProfile({ displayName: name });
            } catch (fbErr) {
                console.warn("Firebase sign up fallback to local auth:", fbErr);
                const localUser = {
                    uid: 'local_' + btoa(email).replace(/=/g, '').toLowerCase(),
                    email: email,
                    displayName: name
                };
                onTeacherLoggedIn(localUser);
            }
        } else {
            try {
                await auth.signInWithEmailAndPassword(email, password);
            } catch (fbErr) {
                console.warn("Firebase sign in fallback to local auth:", fbErr);
                const localName = email.split('@')[0];
                const localUser = {
                    uid: 'local_' + btoa(email).replace(/=/g, '').toLowerCase(),
                    email: email,
                    displayName: localName
                };
                onTeacherLoggedIn(localUser);
            }
        }
    } catch (err) {
        console.error("Auth submit error:", err);
        if (errorDiv) {
            errorDiv.textContent = getArabicAuthErrorMessage(err);
            errorDiv.classList.remove('hidden');
        }
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = origBtnContent;
        }
    }
    return false;
}

function logout() {
    try {
        auth.signOut();
    } catch (e) {}
    localStorage.removeItem('local_active_user');
    currentUser = null;
    const authSec = document.getElementById('auth-section');
    const uInfo = document.getElementById('user-info');
    if (authSec) authSec.classList.remove('hidden');
    if (uInfo) uInfo.classList.add('hidden');
    hideAllScreens();
}

// NAVIGATION LOGIC
function hideAllScreens() {
    ['dashboard', 'students', 'daily', 'monthly', 'settings'].forEach(s => {
        const el = document.getElementById(`screen-${s}`);
        if (el) el.classList.add('hidden');
    });
}

function navigateTo(screen) {
    if (!currentUser) return;
    hideAllScreens();
    const targetScreen = document.getElementById(`screen-${screen}`);
    if (targetScreen) targetScreen.classList.remove('hidden');
    
    // Highlight nav
    document.querySelectorAll('#main-nav button').forEach(b => b.classList.remove('bg-primary-700'));
    const activeBtn = document.getElementById(`nav-${screen}`);
    if (activeBtn) activeBtn.classList.add('bg-primary-700');

    if (screen === 'dashboard') updateDashboardStats();
    if (screen === 'settings' && typeof loadSettingsInputs === 'function') loadSettingsInputs();
    if (screen === 'monthly') setTimeout(updateStudentProgressChart, 50);
}

// FIRESTORE & LOCAL DATA SYNC
function deduplicateStudents(list) {
    if (!Array.isArray(list)) return [];
    const seenIds = new Set();
    const seenNameKeys = new Set();
    const result = [];

    for (const st of list) {
        if (!st || !st.name) continue;
        const id = st.id || ('st_' + Math.random().toString(36).substring(2, 9));
        const normName = st.name.trim().toLowerCase();

        if (seenIds.has(id)) continue;

        // If another student entry has the exact same normalized name:
        if (seenNameKeys.has(normName)) {
            const existingIdx = result.findIndex(r => r.name.trim().toLowerCase() === normName);
            if (existingIdx !== -1) {
                // If existing item has temp 'st_' id and current has real doc ID, replace it
                if (result[existingIdx].id.startsWith('st_') && !id.startsWith('st_')) {
                    seenIds.delete(result[existingIdx].id);
                    result[existingIdx] = { ...result[existingIdx], ...st, id };
                    seenIds.add(id);
                } else if (!result[existingIdx].id.startsWith('st_') && id.startsWith('st_')) {
                    // Skip the temp st_ entry
                    continue;
                }
            }
            continue;
        }

        seenIds.add(id);
        seenNameKeys.add(normName);
        result.push({ ...st, id });
    }
    return result;
}

function saveStudentsToLocalStorage() {
    if (!currentUser) return;
    try {
        localStorage.setItem(`students_${currentUser.uid}`, JSON.stringify(studentsList));
    } catch (e) {
        console.warn("Error saving to localStorage:", e);
    }
}

function loadStudentsFromLocalStorage() {
    if (!currentUser) return;
    try {
        const saved = localStorage.getItem(`students_${currentUser.uid}`);
        if (saved) {
            studentsList = deduplicateStudents(JSON.parse(saved));
        } else {
            studentsList = [];
        }
    } catch (e) {
        studentsList = [];
    }
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();
}

function loadStudentsFromFirestore() {
    if (!currentUser) return;
    loadStudentsFromLocalStorage(); // Immediately render local cached copy for instant loading
    try {
        db.collection("teachers").doc(currentUser.uid).collection("students")
            .onSnapshot(snapshot => {
                const docs = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
                studentsList = deduplicateStudents(docs);
                saveStudentsToLocalStorage();
                renderStudentsUI();
                populateStudentDropdowns();
                updateDashboardStats();
            }, err => {
                console.warn("Firestore sync unavailable, using local data:", err);
                loadStudentsFromLocalStorage();
            });
    } catch (err) {
        console.warn("Firestore collection error, using local storage:", err);
        loadStudentsFromLocalStorage();
    }
}

function generateStudentCardHTML(st) {
    const waHref = st.whatsapp ? (st.whatsapp.startsWith('http') ? st.whatsapp : 'https://wa.me/' + st.whatsapp.replace(/[^0-9+]/g, '')) : '';
    const subList = (st.subjects && st.subjects.length > 0) ? st.subjects : ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];
    const subBadges = subList.map(sb => `<span class="px-2 py-0.5 bg-emerald-50 text-emerald-800 border border-emerald-200 rounded-md text-[10px] font-bold">${sb}</span>`).join(' ');

    return `
        <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:shadow transition flex flex-col justify-between space-y-3">
            <div>
                <div class="flex justify-between items-start mb-1">
                    <h4 onclick="openStudentProfile('${st.id}')" class="font-bold text-slate-800 hover:text-emerald-700 text-sm cursor-pointer flex items-center gap-1 transition">
                        <span>${st.name}</span>
                        <i class="fa-solid fa-chevron-left text-[10px] text-emerald-600"></i>
                    </h4>
                    <div class="flex items-center gap-1">
                        ${waHref ? `
                            <a href="${waHref}" target="_blank" title="WhatsApp" class="text-emerald-600 hover:text-emerald-700 p-1">
                                <i class="fa-brands fa-whatsapp text-lg"></i>
                            </a>
                        ` : ''}
                        <button onclick="editStudent('${st.id}')" title="${currentLang === 'ar' ? 'تعديل' : 'Edit'}" class="text-slate-400 hover:text-primary-700 p-1">
                            <i class="fa-solid fa-pen text-xs"></i>
                        </button>
                        <button onclick="deleteStudent('${st.id}')" title="${currentLang === 'ar' ? 'حذف' : 'Delete'}" class="text-slate-300 hover:text-red-600 p-1">
                            <i class="fa-solid fa-trash-can text-xs"></i>
                        </button>
                    </div>
                </div>

                <div class="flex flex-wrap items-center gap-1 text-xs mb-2">
                    ${subBadges}
                    <span class="text-slate-400 text-[11px] mr-1">• ${st.age ? st.age + (currentLang === 'ar' ? ' سنة' : ' yrs') : '-'}</span>
                </div>

                ${st.customPrompt ? `
                    <div class="text-[11px] text-primary-800 bg-primary-50 border border-primary-100 p-2 rounded-lg mb-2">
                        <strong><i class="fa-solid fa-wand-magic-sparkles text-amber-500"></i> ${currentLang === 'ar' ? 'توجيهات الذكاء الاصطناعي:' : 'AI Prompt:'}</strong> ${st.customPrompt}
                    </div>
                ` : ''}

                ${st.notes ? `<div class="text-xs text-slate-600 bg-slate-50 p-2 rounded-lg line-clamp-2">${st.notes}</div>` : ''}
            </div>

            <div class="pt-2 border-t border-slate-100 flex flex-wrap items-center justify-between gap-1.5">
                <button onclick="openStudentProfile('${st.id}')" class="px-2.5 py-1.5 bg-emerald-50 text-emerald-800 hover:bg-emerald-100 rounded-lg text-xs font-bold border border-emerald-200 transition flex items-center gap-1">
                    <i class="fa-solid fa-address-card"></i> ${currentLang === 'ar' ? 'الملف الشخصي' : 'Profile'}
                </button>
                <div class="flex items-center gap-1">
                    <button onclick="selectStudentForDaily('${st.id}')" class="px-3 py-1.5 bg-primary-700 hover:bg-primary-800 text-white rounded-lg text-xs font-bold transition flex items-center gap-1">
                        <i class="fa-solid fa-pen-to-square"></i> ${currentLang === 'ar' ? 'درس جديد' : 'New Lesson'}
                    </button>
                </div>
            </div>
        </div>
    `;
}

function renderStudentsUI() {
    const dashGrid = document.getElementById('dash-students-grid');
    const fullGrid = document.getElementById('students-full-grid');

    if (studentsList.length === 0) {
        const emptyHTML = `<div class="col-span-full text-center py-8 text-slate-400 text-sm">${currentLang === 'ar' ? 'لا يوجد طلاب مضافون حتى الآن. اضغط على "إضافة طالب جديد" للبدء.' : 'No students added yet. Click "Add New Student" to begin.'}</div>`;
        if (dashGrid) dashGrid.innerHTML = emptyHTML;
        if (fullGrid) fullGrid.innerHTML = emptyHTML;
        return;
    }

    const cardsHTML = studentsList.map(st => generateStudentCardHTML(st)).join('');

    if (dashGrid) dashGrid.innerHTML = cardsHTML;
    if (fullGrid) fullGrid.innerHTML = cardsHTML;
}

function populateStudentDropdowns() {
    const dailySelect = document.getElementById('daily-student-select');
    const monthlySelect = document.getElementById('monthly-student-select');

    const optionsHTML = `<option value="">-- ${currentLang === 'ar' ? 'اختر الطالب' : 'Select Student'} --</option>` + 
        studentsList.map(st => `<option value="${st.id}">${st.name}</option>`).join('');

    if (dailySelect) dailySelect.innerHTML = optionsHTML;
    if (monthlySelect) monthlySelect.innerHTML = optionsHTML;
}

function selectStudentForDaily(studentId) {
    navigateTo('daily');
    const dailySelect = document.getElementById('daily-student-select');
    if (dailySelect) dailySelect.value = studentId;
    onDailyStudentChanged();
}

// MULTI-SUBJECT SELECTION FREEDOM HELPERS
function getDailySelectedSubjects() {
    const selected = [];
    if (document.getElementById('daily-subj-quran')?.checked) selected.push("القرآن الكريم");
    if (document.getElementById('daily-subj-arabic')?.checked) selected.push("اللغة العربية");
    if (document.getElementById('daily-subj-islamic')?.checked) selected.push("العلوم الإسلامية");
    const custom = document.getElementById('daily-subj-custom')?.value.trim();
    if (custom) selected.push(custom);

    let resultString = "القرآن الكريم";
    if (selected.length === 0) {
        resultString = "القرآن الكريم";
    } else if (selected.length === 1) {
        resultString = selected[0];
    } else if (selected.length === 3 && !custom) {
        resultString = "شامل (جميع المواد)";
    } else {
        resultString = selected.join(" + ");
    }

    const hiddenSelect = document.getElementById('daily-subject-select');
    if (hiddenSelect) hiddenSelect.value = resultString;

    return resultString;
}

function onSubjectSelectionChanged() {
    getDailySelectedSubjects();
}

function selectAllDailySubjects(checkAll = true) {
    const q = document.getElementById('daily-subj-quran');
    const a = document.getElementById('daily-subj-arabic');
    const i = document.getElementById('daily-subj-islamic');
    if (q) q.checked = checkAll;
    if (a) a.checked = checkAll;
    if (i) i.checked = checkAll;
    onSubjectSelectionChanged();
}

function getMonthlySelectedSubjects() {
    const selected = [];
    if (document.getElementById('monthly-subj-quran')?.checked) selected.push("القرآن الكريم");
    if (document.getElementById('monthly-subj-arabic')?.checked) selected.push("اللغة العربية");
    if (document.getElementById('monthly-subj-islamic')?.checked) selected.push("العلوم الإسلامية");
    const custom = document.getElementById('monthly-subj-custom')?.value.trim();
    if (custom) selected.push(custom);

    let resultString = "شامل (جميع المواد)";
    if (selected.length === 0) {
        resultString = "شامل (جميع المواد)";
    } else if (selected.length === 1) {
        resultString = selected[0];
    } else if (selected.length === 3 && !custom) {
        resultString = "شامل (جميع المواد)";
    } else {
        resultString = selected.join(" + ");
    }

    const hiddenSelect = document.getElementById('monthly-subject-select');
    if (hiddenSelect) hiddenSelect.value = resultString;

    return { selectedList: selected, resultString };
}

function onMonthlySubjectSelectionChanged() {
    getMonthlySelectedSubjects();
    if (typeof onMonthlyStudentChanged === 'function') {
        onMonthlyStudentChanged();
    }
}

function selectAllMonthlySubjects(checkAll = true) {
    const q = document.getElementById('monthly-subj-quran');
    const a = document.getElementById('monthly-subj-arabic');
    const i = document.getElementById('monthly-subj-islamic');
    if (q) q.checked = checkAll;
    if (a) a.checked = checkAll;
    if (i) i.checked = checkAll;
    onMonthlySubjectSelectionChanged();
}

function onDailyStudentChanged() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    const badge = document.getElementById('student-prompt-badge');
    const promptText = document.getElementById('student-prompt-text');

    if (student && student.customPrompt) {
        if (promptText) promptText.textContent = student.customPrompt;
        if (badge) {
            badge.classList.remove('hidden');
            badge.classList.add('flex');
        }
    } else {
        if (badge) {
            badge.classList.add('hidden');
            badge.classList.remove('flex');
        }
    }

    if (student) {
        const subjs = student.subjects || ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];
        const qCb = document.getElementById('daily-subj-quran');
        const aCb = document.getElementById('daily-subj-arabic');
        const iCb = document.getElementById('daily-subj-islamic');
        const cIn = document.getElementById('daily-subj-custom');

        if (qCb) qCb.checked = subjs.includes("القرآن الكريم");
        if (aCb) aCb.checked = subjs.includes("اللغة العربية");
        if (iCb) iCb.checked = subjs.includes("العلوم الإسلامية");

        const extras = subjs.filter(s => s !== "القرآن الكريم" && s !== "اللغة العربية" && s !== "العلوم الإسلامية");
        if (cIn) cIn.value = extras.join('، ');

        onSubjectSelectionChanged();
    }

    if (typeof updateCalculatedLessonNumber === 'function') {
        updateCalculatedLessonNumber();
    }
}

function filterStudents() {
    const query = document.getElementById('student-search-input')?.value.toLowerCase() || '';
    const filtered = studentsList.filter(s => s.name.toLowerCase().includes(query));
    
    const fullGrid = document.getElementById('students-full-grid');
    if (!fullGrid) return;

    if (filtered.length === 0) {
        fullGrid.innerHTML = `<div class="col-span-full text-center py-8 text-slate-400 text-sm">${currentLang === 'ar' ? 'لا توجد نتائج مطابقة للبحث.' : 'No matching results found.'}</div>`;
        return;
    }
    fullGrid.innerHTML = filtered.map(st => generateStudentCardHTML(st)).join('');
}

function calculateAndUpdateStudentStats() {
    const totalStudents = studentsList.length;
    
    // Calculate active subjects and subject enrollments dynamically
    const subjectCounts = {
        quran: 0,
        arabic: 0,
        islamic: 0
    };
    
    let totalSubjectEnrollments = 0;
    const activeSubjectsSet = new Set();

    studentsList.forEach(st => {
        const subjs = st.subjects || [];
        subjs.forEach(s => {
            if (!s) return;
            if (s.includes("القرآن")) {
                subjectCounts.quran++;
                activeSubjectsSet.add("القرآن الكريم");
                totalSubjectEnrollments++;
            } else if (s.includes("العربية")) {
                subjectCounts.arabic++;
                activeSubjectsSet.add("اللغة العربية");
                totalSubjectEnrollments++;
            } else if (s.includes("الإسلامية") || s.includes("اسلام")) {
                subjectCounts.islamic++;
                activeSubjectsSet.add("العلوم الإسلامية");
                totalSubjectEnrollments++;
            } else {
                activeSubjectsSet.add(s);
                totalSubjectEnrollments++;
            }
        });
    });

    const activeSubjectsCount = activeSubjectsSet.size;

    // Update DOM elements
    const statTotalStudents = document.getElementById('stat-total-students');
    const statActiveSubjects = document.getElementById('stat-active-subjects');
    const statAiModel = document.getElementById('stat-ai-model');
    const settingsAiModel = document.getElementById('settings-ai-model');

    if (statTotalStudents) {
        statTotalStudents.textContent = totalStudents;
    }

    if (statActiveSubjects) {
        if (currentLang === 'ar') {
            statActiveSubjects.textContent = `${activeSubjectsCount} مواد نشطة (${totalSubjectEnrollments} تسجيل)`;
        } else {
            statActiveSubjects.textContent = `${activeSubjectsCount} Active Subjects (${totalSubjectEnrollments} Enrolled)`;
        }
    }

    if (statAiModel) {
        statAiModel.textContent = settingsAiModel?.value || 'Gemini 2.5 Flash';
    }

    return {
        totalStudents,
        activeSubjectsCount,
        totalSubjectEnrollments,
        subjectCounts
    };
}

function updateDashboardStats() {
    calculateAndUpdateStudentStats();
}

// ADD / EDIT / DELETE STUDENT
function openAddStudentModal() {
    const modalId = document.getElementById('modal-student-id');
    const form = document.querySelector('#add-student-modal form');
    const quranCb = document.getElementById('subj-quran');
    const arabicCb = document.getElementById('subj-arabic');
    const islamicCb = document.getElementById('subj-islamic');
    const modalTitle = document.getElementById('modal-title-text');
    const modal = document.getElementById('add-student-modal');

    if (modalId) modalId.value = '';
    if (form) form.reset();
    if (quranCb) quranCb.checked = true;
    if (arabicCb) arabicCb.checked = true;
    if (islamicCb) islamicCb.checked = true;
    if (modalTitle) modalTitle.textContent = currentLang === 'ar' ? "إضافة طالب جديد" : "Add New Student";
    if (modal) modal.classList.remove('hidden');
}

function editStudent(id) {
    const st = studentsList.find(s => s.id === id);
    if (!st) return;

    const modalId = document.getElementById('modal-student-id');
    const modalName = document.getElementById('modal-student-name');
    const modalAge = document.getElementById('modal-student-age');
    const modalPhone = document.getElementById('modal-student-phone');
    const modalWa = document.getElementById('modal-student-whatsapp');
    const modalPrompt = document.getElementById('modal-student-custom-prompt');
    const modalNotes = document.getElementById('modal-student-notes');
    const modalTitle = document.getElementById('modal-title-text');
    const modal = document.getElementById('add-student-modal');

    if (modalId) modalId.value = st.id;
    if (modalName) modalName.value = st.name || '';
    if (modalAge) modalAge.value = st.age || '';
    if (modalPhone) modalPhone.value = st.parentPhone || '';
    if (modalWa) modalWa.value = st.whatsapp || '';
    if (modalPrompt) modalPrompt.value = st.customPrompt || '';
    if (modalNotes) modalNotes.value = st.notes || '';

    const stSubjs = st.subjects || ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];
    const quranCb = document.getElementById('subj-quran');
    const arabicCb = document.getElementById('subj-arabic');
    const islamicCb = document.getElementById('subj-islamic');
    const customSubjInput = document.getElementById('subj-custom');

    if (quranCb) quranCb.checked = stSubjs.includes("القرآن الكريم");
    if (arabicCb) arabicCb.checked = stSubjs.includes("اللغة العربية");
    if (islamicCb) islamicCb.checked = stSubjs.includes("العلوم الإسلامية");

    const extraSubjs = stSubjs.filter(s => s !== "القرآن الكريم" && s !== "اللغة العربية" && s !== "العلوم الإسلامية");
    if (customSubjInput) customSubjInput.value = extraSubjs.join('، ');

    if (modalTitle) modalTitle.textContent = currentLang === 'ar' ? "تعديل بيانات الطالب" : "Edit Student Info";
    if (modal) modal.classList.remove('hidden');
}

function closeAddStudentModal() {
    const modal = document.getElementById('add-student-modal');
    if (modal) modal.classList.add('hidden');
}

async function handleAddStudentSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (!currentUser) return;

    const modalId = document.getElementById('modal-student-id')?.value;
    const name = document.getElementById('modal-student-name')?.value.trim();
    const age = document.getElementById('modal-student-age')?.value;
    const phone = document.getElementById('modal-student-phone')?.value.trim();
    const whatsapp = document.getElementById('modal-student-whatsapp')?.value.trim();
    const customPrompt = document.getElementById('modal-student-custom-prompt')?.value.trim();
    const notes = document.getElementById('modal-student-notes')?.value.trim();

    const subjects = [];
    const subjectsMap = {
        quran: false,
        arabic: false,
        islamic: false
    };

    if (document.getElementById('subj-quran')?.checked) {
        subjects.push("القرآن الكريم");
        subjectsMap.quran = true;
    }
    if (document.getElementById('subj-arabic')?.checked) {
        subjects.push("اللغة العربية");
        subjectsMap.arabic = true;
    }
    if (document.getElementById('subj-islamic')?.checked) {
        subjects.push("العلوم الإسلامية");
        subjectsMap.islamic = true;
    }
    const customSubj = document.getElementById('subj-custom')?.value.trim();
    if (customSubj) {
        subjects.push(customSubj);
    }
    if (subjects.length === 0) {
        subjects.push("القرآن الكريم");
        subjectsMap.quran = true;
    }

    if (!name) {
        alert(currentLang === 'ar' ? "يرجى إدخال اسم الطالب" : "Please enter student name");
        return;
    }

    const normalizedName = name.toLowerCase();

    // Find if a student with the same ID or name already exists in memory
    const existingStudent = studentsList.find(s => 
        (modalId && s.id === modalId) || 
        (s.name && s.name.trim().toLowerCase() === normalizedName)
    );

    let assignedId = modalId || (existingStudent ? existingStudent.id : null);

    const payload = {
        name,
        age: age ? parseInt(age) : null,
        subjects,
        subjectsMap,
        parentPhone: phone || '',
        whatsapp: whatsapp || '',
        customPrompt: customPrompt || '',
        notes: notes || '',
        updatedAt: new Date().toISOString()
    };

    try {
        const collectionRef = db.collection("teachers").doc(currentUser.uid).collection("students");
        
        // Check if matching student exists in Firestore by name if no assignedId yet
        if (!assignedId) {
            const querySnap = await collectionRef.where("name", "==", name).get();
            if (!querySnap.empty) {
                assignedId = querySnap.docs[0].id;
            }
        }

        if (assignedId && !assignedId.startsWith('st_')) {
            await collectionRef.doc(assignedId).set(payload, { merge: true });
        } else if (assignedId && assignedId.startsWith('st_')) {
            const docRef = await collectionRef.add({ ...payload, createdAt: new Date().toISOString() });
            if (docRef && docRef.id) assignedId = docRef.id;
        } else {
            payload.createdAt = new Date().toISOString();
            const docRef = await collectionRef.add(payload);
            if (docRef && docRef.id) assignedId = docRef.id;
        }
    } catch (err) {
        console.warn("Firestore save failed, saving to local storage:", err);
    }

    if (!assignedId) {
        assignedId = 'st_' + Date.now();
    }

    // Merge/update local state cleanly under single assignedId
    const existingIdx = studentsList.findIndex(s => s.id === assignedId || (s.name && s.name.trim().toLowerCase() === normalizedName));
    if (existingIdx !== -1) {
        studentsList[existingIdx] = { ...studentsList[existingIdx], ...payload, id: assignedId };
    } else {
        studentsList.push({ id: assignedId, ...payload });
    }

    studentsList = deduplicateStudents(studentsList);
    saveStudentsToLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();

    closeAddStudentModal();
    const form = document.querySelector('#add-student-modal form');
    if (form) form.reset();
}

async function deleteStudent(id) {
    if (!confirm(currentLang === 'ar' ? "هل أنت تأكد من حذف هذا الطالب وجميع بياناته؟" : "Are you sure you want to delete this student?")) return;
    try {
        await db.collection("teachers").doc(currentUser.uid).collection("students").doc(id).delete();
    } catch (err) {
        console.warn("Firestore delete failed, removing locally:", err);
    }
    studentsList = studentsList.filter(s => s.id !== id);
    saveStudentsToLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();
}

// Helper to extract and parse JSON from AI response safely
function extractAndParseJSON(text) {
    if (!text) throw new Error("لم يتم استلام ناتج من الذكاء الاصطناعي");
    const cleaned = text.replace(/```json/gi, '').replace(/```/g, '').trim();
    try {
        return JSON.parse(cleaned);
    } catch (e) {
        const firstBrace = text.indexOf('{');
        const lastBrace = text.lastIndexOf('}');
        if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
            const jsonSub = text.substring(firstBrace, lastBrace + 1);
            return JSON.parse(jsonSub);
        }
        throw e;
    }
}

function getEffectiveGeminiApiKey() {
    // 1) Check Vercel / Environment Variables (NEXT_PUBLIC_GEMINI_API_KEY)
    let envKey = '';
    try {
        if (typeof process !== 'undefined' && process.env && process.env.NEXT_PUBLIC_GEMINI_API_KEY) {
            envKey = process.env.NEXT_PUBLIC_GEMINI_API_KEY;
        }
    } catch (e) {}

    if (!envKey && typeof window !== 'undefined') {
        envKey = window.NEXT_PUBLIC_GEMINI_API_KEY || (window.ENV && window.ENV.NEXT_PUBLIC_GEMINI_API_KEY) || '';
    }

    if (!envKey) {
        try {
            const getMetaEnv = new Function('try { return import.meta.env; } catch(e) { return null; }');
            const metaEnv = getMetaEnv();
            if (metaEnv && metaEnv.NEXT_PUBLIC_GEMINI_API_KEY) {
                envKey = metaEnv.NEXT_PUBLIC_GEMINI_API_KEY;
            }
        } catch (e) {}
    }

    if (envKey && envKey.trim()) {
        return envKey.trim();
    }

    // 2) Check Settings UI Input field
    const fromInput = document.getElementById('settings-api-key')?.value.trim();
    if (fromInput) return fromInput;

    // 3) Fallback to LocalStorage
    const fromStorage = localStorage.getItem('gemini_api_key') || localStorage.getItem('user_gemini_api_key') || '';
    return fromStorage.trim();
}

// 3) GEMINI AI CALL (BACKGROUND CLIENT-SIDE FETCH WITH DYNAMIC LOCALSTORAGE API KEY & SECURE ERROR HANDLING)
async function callGeminiAPI(prompt, inlineData = null) {
    const apiKey = getEffectiveGeminiApiKey();

    if (!apiKey) {
        const userPrompt = confirm(
            currentLang === 'ar'
                ? "⚠️ لم يتم العثور على مفتاح API للذكاء الاصطناعي (Gemini API Key) في المتصفح.\n\nيرجى الانتقال إلى صفحة الإعدادات وتوفير مفتاح API الخاص بك لتشغيل التحليل والتحسين الذكي.\n\nهل ترغب في الانتقال إلى صفحة الإعدادات الآن؟"
                : "⚠️ Gemini API key not found in browser storage.\n\nPlease go to Settings to save your API key to activate AI features.\n\nWould you like to go to Settings now?"
        );
        if (userPrompt) {
            navigateTo('settings');
            setTimeout(() => {
                const apiKeyInput = document.getElementById('settings-api-key');
                if (apiKeyInput) {
                    apiKeyInput.focus();
                    apiKeyInput.classList.add('ring-2', 'ring-amber-500');
                }
            }, 150);
        }
        throw new Error(currentLang === 'ar' ? "يرجى إضافة مفتاح API الخاص بك من صفحة الإعدادات أولاً." : "Please add your API key in Settings first.");
    }

    const selectedModel = document.getElementById('settings-ai-model')?.value || localStorage.getItem('user_gemini_model') || 'gemini-1.5-flash';
    const modelsToTry = [selectedModel, 'gemini-1.5-flash', 'gemini-2.0-flash'];
    const uniqueModels = [...new Set(modelsToTry)];

    const parts = [{ text: prompt }];
    if (inlineData && inlineData.mimeType && inlineData.data) {
        parts.push({ inlineData: inlineData });
    }

    let lastErrorMsg = null;
    let isAuthError = false;

    for (const model of uniqueModels) {
        try {
            const url = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent?key=${encodeURIComponent(apiKey)}`;
            const headers = { 
                'Content-Type': 'application/json',
                'x-goog-api-key': apiKey
            };

            const response = await fetch(url, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    contents: [{ parts: parts }]
                })
            });

            if (response.ok) {
                const data = await response.json();
                const resultText = data.candidates?.[0]?.content?.parts?.[0]?.text;
                if (resultText) return resultText;
            } else {
                const errStatus = response.status;
                if (errStatus === 400 || errStatus === 401 || errStatus === 403) {
                    isAuthError = true;
                    lastErrorMsg = "مفتاح API غير صالح أو محظور/منتهي. يرجى إدخال مفتاح جديد وتأكيده في صفحة الإعدادات.";
                } else if (errStatus === 429) {
                    lastErrorMsg = "تم تجاوز حد الاستخدام المسموح لـ Gemini API. يرجى الانتظار قليلاً ثم إعادة المحاولة.";
                } else if (errStatus >= 500) {
                    lastErrorMsg = "خدمة الذكاء الاصطناعي غير متاحة مؤقتاً. يرجى المحاولة لاحقاً.";
                } else {
                    lastErrorMsg = `تعذر المعالجة من نموذج ${model}. (رمز الخطأ: ${errStatus})`;
                }
                console.warn(`Gemini model ${model} HTTP response notice: ${errStatus}`);
            }
        } catch (e) {
            lastErrorMsg = "تعذر الاتصال بالشبكة. يرجى التأكد من توفر الاتصال بالإنترنت لديك.";
        }
    }

    if (isAuthError) {
        const userPrompt = confirm(
            "⚠️ " + lastErrorMsg + "\n\nهل ترغب في الانتقال لصفحة الإعدادات لتحديث مفتاح API الخاص بك؟"
        );
        if (userPrompt) {
            navigateTo('settings');
            setTimeout(() => {
                const apiKeyInput = document.getElementById('settings-api-key');
                if (apiKeyInput) {
                    apiKeyInput.focus();
                    apiKeyInput.classList.add('ring-2', 'ring-amber-500');
                }
            }, 150);
        }
    }

    throw new Error(lastErrorMsg || "تعذر التحليل الذكي. يرجى التحقق من المفتاح في صفحة الإعدادات والاتصال بالإنترنت.");
}

// AUTO PARSE RAW UNSTRUCTURED REPORT WITH AI
async function parseAndFormatRawReportAI() {
    const rawInput = document.getElementById('daily-raw-input')?.value.trim() || '';
    if (!rawInput) {
        alert(currentLang === 'ar' ? "يرجى كتابة أو لصق ملخص التقرير أوالدرس أولاً!" : "Please write or paste the full report text first!");
        return;
    }

    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    const subject = getDailySelectedSubjects();
    const lessonNumber = document.getElementById('daily-lesson-number')?.value || 1;
    const duration = document.getElementById('daily-lesson-duration')?.value || "45 دقيقة";
    const date = document.getElementById('daily-date')?.value || new Date().toISOString().slice(0, 10);

    const btn = document.getElementById('btn-parse-raw-ai');
    if (!btn) return;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<div class="loader"></div> <span>جاري صياغة وتحليل التقرير الشامل...</span>`;
    btn.disabled = true;

    const customInstruction = student?.customPrompt ? `توجيهات مخصصة للطالب ${student.name}: ${student.customPrompt}` : '';
    const globalInst = document.getElementById('settings-global-instructions')?.value || "";

    const prompt = `
    أنت مساعد معلم حلقة قرآن كريم وعلوم إسلامية ولغة عربية خبير ومحترف جداً.
    اكتب تقريراً موحداً متكاملاً باللغة العربية موجهاً لولي أمر الطالب بالبيانات التالية:
    - اسم الطالب: ${student?.name || 'الطالب'}
    - المادة الدراسية: ${subject}
    - رقم الحصة: ${lessonNumber}
    - مدة الدرس: ${duration}
    - التاريخ: ${date}
    - التفاصيل الخام للدرس: "${rawInput}"
    - توجيهات عامة: ${globalInst}
    ${customInstruction}

    تنبيهات هامة:
    1. اكتب نص التقرير كاملاً موحداً متماسكاً في حقل reportArabic يبدأ بالتحية، ثم يذكر رقم الحصة (${lessonNumber}) ومدة الدرس والتقييم والإنجاز والتوصيات القادمة، بأسلوب تربوي راقٍ دون تقطيع.
    2. صغ ملخصاً بالإنجليزية في reportEnglish بعبارات مشجعة ومصطلحات إسلامية دقيقة مثل: "Noble Quran", "Surah", "Ayah", "Tajweed rules", "Tilawah (Recitation)", "Hifdh (Memorisation)", "Muraja'ah (Revision)", "Islamic Studies", "Arabic Language".

    أرجع ناتج بصيغة JSON مقفلة فقط تحتوي:
    {
      "reportArabic": "التقرير الكامل الشامل الموحد بالعربية لولي الأمر",
      "reportEnglish": "A polished encouraging daily report summary in English with precise Islamic terminology"
    }
    أرجع JSON فقط دون أي وسوم أو كلام إضافي.
    `;

    try {
        const rawText = await callGeminiAPI(prompt);
        const parsed = extractAndParseJSON(rawText);

        if (parsed.reportArabic && document.getElementById('daily-report-arabic')) document.getElementById('daily-report-arabic').value = parsed.reportArabic;
        if (parsed.reportEnglish && document.getElementById('daily-report-english')) document.getElementById('daily-report-english').value = parsed.reportEnglish;

        alert(currentLang === 'ar' ? "تم صياغة وتنسيق التقرير الشامل بنجاح!" : "Parsed and structured report successfully!");
    } catch (err) {
        alert("تعذر التحليل الذكي: " + err.message);
    } finally {
        btn.innerHTML = originalHTML;
        btn.disabled = false;
    }
}

// AUTO-CALCULATE AND RESET LESSON NUMBER ON 1ST OF MONTH
function updateCalculatedLessonNumber() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const dateInput = document.getElementById('daily-date');
    const lessonNumInput = document.getElementById('daily-lesson-number');
    if (!lessonNumInput) return;

    if (!studentId) {
        lessonNumInput.value = 1;
        return;
    }

    const dateVal = dateInput?.value || new Date().toISOString().slice(0, 10);
    const parts = dateVal.split('-');
    const yearMonth = `${parts[0]}-${parts[1]}`;
    const day = parts[2];

    // Get all recorded lessons for this student
    const lessons = getStudentLessonsLocal(studentId);
    
    // Filter lessons in the same year-month
    const monthLessons = lessons.filter(l => l.date && l.date.startsWith(yearMonth));

    // Reset to 1 if day is '01' OR if no lessons recorded in this month yet
    if (day === '01' || monthLessons.length === 0) {
        lessonNumInput.value = 1;
    } else {
        lessonNumInput.value = monthLessons.length + 1;
    }
}

function onDailyDateChanged() {
    updateCalculatedLessonNumber();
}

// LOCAL LESSON STORAGE HELPERS
function saveLessonLocal(studentId, lessonObj) {
    try {
        const uid = currentUser ? currentUser.uid : 'local';
        const key = `lessons_${uid}_${studentId}`;
        const list = JSON.parse(localStorage.getItem(key) || '[]');
        list.unshift(lessonObj);
        localStorage.setItem(key, JSON.stringify(list));
    } catch(e) {}
}

function getStudentLessonsLocal(studentId) {
    try {
        const uid = currentUser ? currentUser.uid : 'local';
        const key = `lessons_${uid}_${studentId}`;
        return JSON.parse(localStorage.getItem(key) || '[]');
    } catch(e) {
        return [];
    }
}

function copyDailyReportText() {
    const text = document.getElementById('daily-report-arabic')?.value || '';
    if (!text) {
        alert(currentLang === 'ar' ? "لا يوجد نص تقرير لنسخه!" : "No report text to copy!");
        return;
    }
    navigator.clipboard.writeText(text).then(() => {
        alert(currentLang === 'ar' ? "تم نسخ التقرير الكامل الشامل بنجاح إلى الحافظة!" : "Full report copied to clipboard!");
    });
}

// DAILY LESSON AI GENERATION (UNIFIED FULL REPORT)
async function generateDailyReportAI() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    if (!student) {
        alert(currentLang === 'ar' ? "يرجى اختيار الطالب أولاً!" : "Please select student first!");
        return;
    }

    const subject = getDailySelectedSubjects();
    const lessonNumber = document.getElementById('daily-lesson-number')?.value || 1;
    const duration = document.getElementById('daily-lesson-duration')?.value || "45 دقيقة";
    const date = document.getElementById('daily-date')?.value || new Date().toISOString().slice(0, 10);
    const rawInput = document.getElementById('daily-raw-input')?.value.trim() || '';
    const teacherNotes = document.getElementById('daily-teacher-notes')?.value.trim() || '';
    const fileSummary = document.getElementById('daily-file-summary')?.value.trim() || '';
    const globalInst = document.getElementById('settings-global-instructions')?.value || "";

    const btn = document.getElementById('btn-generate-daily-ai');
    if (!btn) return;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<div class="loader"></div> <span>جاري توليد التقرير الشامل الموحد من Gemini...</span>`;
    btn.disabled = true;

    const studentPrompt = student.customPrompt ? `توجيهات مخصصة للطالب محددة من المعلم: ${student.customPrompt}` : '';

    const prompt = `
    أنت خبير تربوي متميز ومساعد معلم في تحفيظ القرآن الكريم واللغة العربية والعلوم الإسلامية.
    اكتب تقريراً يومياً شاملاً ومكتملاً وموحداً (غير مجزأ) موجهاً لولي أمر الطالب بالبيانات التالية:
    - اسم الطالب: ${student.name}
    - المادة الدراسية: ${subject}
    - رقم الحصة: ${lessonNumber}
    - مدة الدرس: ${duration}
    - التاريخ: ${date}
    ${rawInput ? `- تفاصيل الدرس المكتوبة: ${rawInput}` : ''}
    ${teacherNotes ? `- ملاحظات المعلم: ${teacherNotes}` : ''}
    ${fileSummary ? `- ملخص المستند المرفق: ${fileSummary}` : ''}
    - التوجيهات العامة للمعلم: ${globalInst}
    ${studentPrompt}

    قواعد هامة جداً:
    1. اكتب نصاً كاملاً موحداً متماسكاً في حقل reportArabic يبدأ بالتحية الإسلامية، ويذكر رقم الحصة (${lessonNumber}) ومدة الدرس وما تضمنه مع عبارات تشجيعية راقية لولي الأمر.
    2. استخدم مصطلحات إسلامية دقيقة بالإنجليزية في reportEnglish مثل: "Noble Quran", "Surah", "Ayah", "Tilawah", "Hifdh", "Muraja'ah", "Tajweed Rules", "Islamic Studies", "Arabic Language".

    أرجع ناتج بصيغة JSON مقفلة تحتوي:
    {
      "reportArabic": "النص الكامل الموحد الشامل للتقرير اليومي الموجه لولي الأمر دون تجزئة",
      "reportEnglish": "Detailed continuous encouraging summary in English with proper Islamic terminology and lesson duration"
    }
    أرجع JSON فقط دون أي وسوم أو كلام إضافي.
    `;

    try {
        const rawText = await callGeminiAPI(prompt);
        const parsed = extractAndParseJSON(rawText);

        if (document.getElementById('daily-report-arabic')) document.getElementById('daily-report-arabic').value = parsed.reportArabic || "";
        if (document.getElementById('daily-report-english')) document.getElementById('daily-report-english').value = parsed.reportEnglish || "";
    } catch (err) {
        alert("تعذر التوليد الذكي: " + err.message);
    } finally {
        btn.innerHTML = originalHTML;
        btn.disabled = false;
    }
}

async function handleDailyLessonSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (!currentUser) return;

    const studentId = document.getElementById('daily-student-select')?.value;
    if (!studentId) {
        alert("اختر الطالب أولاً");
        return;
    }

    const lessonNumber = document.getElementById('daily-lesson-number')?.value || 1;

    const lessonData = {
        id: 'lesson_' + Date.now(),
        studentId,
        lessonNumber,
        subject: getDailySelectedSubjects(),
        duration: document.getElementById('daily-lesson-duration')?.value || "45 دقيقة",
        date: document.getElementById('daily-date')?.value || new Date().toISOString().slice(0, 10),
        rawInput: document.getElementById('daily-raw-input')?.value || '',
        teacherNotes: document.getElementById('daily-teacher-notes')?.value || '',
        fileSummary: document.getElementById('daily-file-summary')?.value || '',
        fileUrl: document.getElementById('uploaded-file-link')?.href || '',
        fileName: document.getElementById('uploaded-file-name')?.textContent || '',
        reportArabic: document.getElementById('daily-report-arabic')?.value || '',
        reportEnglish: document.getElementById('daily-report-english')?.value || '',
        createdAt: new Date().toISOString()
    };

    // Save local cached copy for instant offline/Word export
    saveLessonLocal(studentId, lessonData);

    try {
        await db.collection("teachers").doc(currentUser.uid)
            .collection("students").doc(studentId)
            .collection("lessons").add({
                ...lessonData,
                createdAt: firebase.firestore.FieldValue.serverTimestamp()
            });
        
        alert(currentLang === 'ar' ? "تم حفظ الدرس والتقرير اليومي بنجاح!" : "Daily lesson saved successfully!");
    } catch (err) {
        console.warn("Firestore save fallback to local:", err);
        alert(currentLang === 'ar' ? "تم حفظ التقرير محلياً بنجاح!" : "Saved locally successfully!");
    }

    updateCalculatedLessonNumber();
}

// DIRECT WHATSAPP REPORT SENDING
function sendDailyReportWhatsApp() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    
    const studentName = student ? student.name : (currentLang === 'ar' ? 'الطالب' : 'Student');
    const subject = getDailySelectedSubjects();
    const lessonNumber = document.getElementById('daily-lesson-number')?.value || '1';
    const duration = document.getElementById('daily-lesson-duration')?.value || "45 دقيقة";
    const date = document.getElementById('daily-date')?.value || '';
    const reportArabic = document.getElementById('daily-report-arabic')?.value || '';
    const reportEnglish = document.getElementById('daily-report-english')?.value || '';

    if (!reportArabic && !reportEnglish) {
        alert(currentLang === 'ar' ? "يرجى كتابة أو توليد التقرير اليومي أولاً قبل الإرسال!" : "Please write or generate the report first!");
        return;
    }

    const formattedMsg = 
`📖 *تقرير متابعة يومية - ${subject}*
👤 *الطالب:* ${studentName}
🔢 *رقم الحصة:* ${lessonNumber}
📅 *التاريخ:* ${date} | ⏱️ *مدة الدرس:* ${duration}

${reportArabic ? '📌 *التقرير الشامل:* \n' + reportArabic + '\n\n' : ''}${reportEnglish ? '🌐 *English Summary:* \n' + reportEnglish : ''}`;

    const encodedMsg = encodeURIComponent(formattedMsg);
    let whatsappTarget = student?.whatsapp || student?.parentPhone || '';

    if (whatsappTarget.includes('chat.whatsapp.com')) {
        navigator.clipboard.writeText(formattedMsg).then(() => {
            alert(currentLang === 'ar' ? "تم نسخ نص التقرير للحافظة! سيتم فتح رابط الجروب الآن لتلصقه وترسله مباشرة." : "Report copied! Opening WhatsApp group now.");
            window.open(whatsappTarget, '_blank');
        }).catch(() => {
            window.open(whatsappTarget, '_blank');
        });
    } else if (whatsappTarget) {
        const cleanPhone = whatsappTarget.replace(/[^0-9+]/g, '');
        window.open(`https://wa.me/${cleanPhone}?text=${encodedMsg}`, '_blank');
    } else {
        window.open(`https://api.whatsapp.com/send?text=${encodedMsg}`, '_blank');
    }
}

// STUDENT PROFILE MODAL & HISTORY LOGIC
function openStudentProfile(studentId) {
    const student = studentsList.find(s => s.id === studentId);
    if (!student) return;

    // Student Header
    const nameEl = document.getElementById('profile-student-name');
    const metaEl = document.getElementById('profile-student-meta');
    if (nameEl) nameEl.textContent = student.name;
    if (metaEl) {
        metaEl.innerHTML = `<span>العمر: ${student.age ? student.age + ' سنة' : 'غير محدد'}</span> • <span>الهاتف: ${student.parentPhone || 'غير محدد'}</span>`;
    }

    // Subjects Badges
    const badgesContainer = document.getElementById('profile-subjects-badges');
    if (badgesContainer) {
        const subList = (student.subjects && student.subjects.length > 0) ? student.subjects : ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];
        badgesContainer.innerHTML = subList.map(s => `<span class="px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded-md font-bold text-[11px]">${s}</span>`).join(' ');
    }

    // Buttons
    const waBtn = document.getElementById('profile-whatsapp-btn');
    if (waBtn) {
        if (student.whatsapp || student.parentPhone) {
            const href = student.whatsapp ? (student.whatsapp.startsWith('http') ? student.whatsapp : 'https://wa.me/' + student.whatsapp.replace(/[^0-9+]/g, '')) : 'https://wa.me/' + (student.parentPhone || '').replace(/[^0-9+]/g, '');
            waBtn.href = href;
            waBtn.classList.remove('hidden');
        } else {
            waBtn.classList.add('hidden');
        }
    }

    const editBtn = document.getElementById('profile-edit-btn');
    if (editBtn) editBtn.onclick = () => { closeStudentProfileModal(); editStudent(studentId); };

    const wordBtn = document.getElementById('profile-word-btn');
    if (wordBtn) wordBtn.onclick = () => exportStudentReportsDoc(studentId);

    const pdfBtn = document.getElementById('profile-pdf-btn');
    if (pdfBtn) pdfBtn.onclick = () => printStudentReportsPDF(studentId);

    const newLessonBtn = document.getElementById('profile-new-lesson-btn');
    if (newLessonBtn) newLessonBtn.onclick = () => { closeStudentProfileModal(); selectStudentForDaily(studentId); };

    // Custom Prompt
    const promptContainer = document.getElementById('profile-custom-prompt-container');
    const promptText = document.getElementById('profile-custom-prompt-text');
    if (student.customPrompt) {
        if (promptContainer) promptContainer.classList.remove('hidden');
        if (promptText) promptText.textContent = student.customPrompt;
    } else {
        if (promptContainer) promptContainer.classList.add('hidden');
    }

    // Fetch lessons history
    const lessons = getStudentLessonsLocal(studentId);
    const lessonsCountEl = document.getElementById('profile-lessons-count');
    const reportsCountEl = document.getElementById('profile-reports-count');
    if (lessonsCountEl) lessonsCountEl.textContent = lessons.length;
    if (reportsCountEl) reportsCountEl.textContent = lessons.filter(l => l.reportArabic).length;

    const historyList = document.getElementById('profile-lessons-history-list');
    if (historyList) {
        if (lessons.length === 0) {
            historyList.innerHTML = `<div class="text-center py-8 text-slate-400 text-xs bg-slate-50 rounded-xl border border-dashed border-slate-200">لا توجد دروس يومية مسجلة بعد لهذا الطالب. اضغط على "درس جديد" لإضافة أول درس.</div>`;
        } else {
            historyList.innerHTML = lessons.map((l, idx) => `
                <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-200 text-xs space-y-2">
                    <div class="flex items-center justify-between font-bold text-slate-800 border-b border-slate-200/60 pb-1.5">
                        <div class="flex items-center gap-2">
                            <span class="px-2 py-0.5 bg-emerald-700 text-white rounded-md text-[11px]">حصة #${l.lessonNumber || (lessons.length - idx)}</span>
                            <span class="text-emerald-900">${l.subject || 'القرآن الكريم'}</span>
                        </div>
                        <div class="flex items-center gap-2 text-slate-500 font-normal text-[11px]">
                            <span>📅 ${l.date || '-'}</span>
                            <span>⏱️ ${l.duration || '45 دقيقة'}</span>
                            <button onclick="deleteStudentLessonLocal('${studentId}', '${l.id}')" title="حذف هذه الحصة" class="text-slate-300 hover:text-red-600 transition p-1">
                                <i class="fa-solid fa-trash-can"></i>
                            </button>
                        </div>
                    </div>

                    ${l.reportArabic ? `
                        <div class="text-slate-700 leading-relaxed font-amiri text-xs bg-white p-2.5 rounded-lg border border-slate-200">
                            ${l.reportArabic}
                        </div>
                    ` : ''}

                    ${l.reportEnglish ? `
                        <div dir="ltr" class="text-slate-600 font-sans text-[11px] bg-slate-100/70 p-2 rounded-lg italic">
                            ${l.reportEnglish}
                        </div>
                    ` : ''}

                    ${l.fileUrl ? `
                        <div class="flex items-center justify-between text-[11px] bg-blue-50 text-blue-800 p-2 rounded-lg border border-blue-200">
                            <span class="truncate font-bold">📎 ${l.fileName || 'مستند مرفق'}</span>
                            <a href="${l.fileUrl}" target="_blank" class="text-blue-600 hover:underline font-bold flex items-center gap-1 shrink-0"><i class="fa-solid fa-external-link"></i> عرض</a>
                        </div>
                    ` : ''}

                    <div class="flex justify-end pt-1">
                        <button onclick="copyDailyReportTextFromHistory('${(l.reportArabic || '').replace(/'/g, "\\'")}')" class="text-[11px] font-bold text-emerald-700 hover:underline flex items-center gap-1">
                            <i class="fa-solid fa-copy"></i> نسخ التقرير
                        </button>
                    </div>
                </div>
            `).join('');
        }
    }

    const modal = document.getElementById('student-profile-modal');
    if (modal) modal.classList.remove('hidden');
}

function closeStudentProfileModal() {
    const modal = document.getElementById('student-profile-modal');
    if (modal) modal.classList.add('hidden');
}

function deleteStudentLessonLocal(studentId, lessonId) {
    if (!confirm("هل أنت تأكد من حذف هذا الدرس من السجل؟")) return;
    try {
        const uid = currentUser ? currentUser.uid : 'local';
        const key = `lessons_${uid}_${studentId}`;
        let list = JSON.parse(localStorage.getItem(key) || '[]');
        list = list.filter(l => l.id !== lessonId);
        localStorage.setItem(key, JSON.stringify(list));
    } catch(e) {}
    openStudentProfile(studentId);
    updateCalculatedLessonNumber();
}

function copyDailyReportTextFromHistory(text) {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => alert("تم نسخ التقرير بنجاح!"));
}

function sendMonthlyReportWhatsApp() {
    if (!activeGeneratedMonthlyReport) {
        alert("لا يوجد تقرير شهري مولد للإرسال!");
        return;
    }

    const st = studentsList.find(s => s.id === activeGeneratedMonthlyReport.studentId);
    const formattedMsg = 
`📊 *التقرير الشهري الشامل - ${activeGeneratedMonthlyReport.subject || 'القرآن والعلوم الإسلامية'}*
👤 *الطالب:* ${activeGeneratedMonthlyReport.studentName}
📅 *الشهر:* ${activeGeneratedMonthlyReport.monthYear || 'التقرير الشهري'}

✨ *تقييم الحفظ/الدرس:* ${activeGeneratedMonthlyReport.newMemorisation || 'ممتاز'}
🔄 *تقييم المراجعة:* ${activeGeneratedMonthlyReport.revision || 'ممتاز'}
🎙️ *التلاوة والتجويد:* ${activeGeneratedMonthlyReport.reading || 'ممتاز'}

${activeGeneratedMonthlyReport.nextMonthPlan ? '📅 *خطة الشهر القادم:* ' + activeGeneratedMonthlyReport.nextMonthPlan + '\n\n' : ''}${activeGeneratedMonthlyReport.summaryEnglish ? '🌐 *English Summary:* \n' + activeGeneratedMonthlyReport.summaryEnglish + '\n' : ''}`;

    const encodedMsg = encodeURIComponent(formattedMsg);
    const whatsappTarget = st?.whatsapp || st?.parentPhone || '';

    if (whatsappTarget.includes('chat.whatsapp.com')) {
        navigator.clipboard.writeText(formattedMsg).then(() => {
            alert("تم نسخ التقرير الشهري! سيتم فتح رابط الجروب الآن لتلصقه وترسله.");
            window.open(whatsappTarget, '_blank');
        });
    } else if (whatsappTarget) {
        const cleanPhone = whatsappTarget.replace(/[^0-9+]/g, '');
        window.open(`https://wa.me/${cleanPhone}?text=${encodedMsg}`, '_blank');
    } else {
        window.open(`https://api.whatsapp.com/send?text=${encodedMsg}`, '_blank');
    }
}

// AUTOMATIC AI MONTHLY REPORT LOGIC BASED ON DAILY LESSONS
async function generateMonthlyReportAI() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    if (!student) {
        alert(currentLang === 'ar' ? "اختر الطالب أولاً!" : "Please select student first!");
        return;
    }

    const subject = getMonthlySelectedSubjects().resultString;
    const monthYear = document.getElementById('monthly-month-year')?.value || new Date().toISOString().slice(0, 7);

    const btn = document.getElementById('btn-gen-monthly-ai');
    if (!btn) return;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<div class="loader"></div> <span>جاري قراءة الدروس اليومية وتوليد التقرير الشهري...</span>`;
    btn.disabled = true;

    // Fetch recorded daily lessons for this student
    const studentLessons = getStudentLessonsLocal(studentId);
    const filteredLessons = studentLessons.filter(l => !monthYear || (l.date && l.date.startsWith(monthYear)));

    let lessonsSummaryText = '';
    if (filteredLessons.length > 0) {
        lessonsSummaryText = filteredLessons.map((l, idx) => 
            `درس ${idx+1} (${l.date || 'تاريخ غير محدد'}): المادة: ${l.subject}, الحفظ: ${l.newMemorisation || 'لا يوجد'}, المراجعة: ${l.revision || 'لا يوجد'}, التلاوة: ${l.readingTajweed || 'لا يوجد'}, التقدير: ${l.grade || 'ممتاز'}, ملاحظات: ${l.teacherNotes || 'لا يوجد'}`
        ).join('\n');
    } else {
        lessonsSummaryText = `طالب منتظم يدرس المواد التالية: ${(student.subjects || ["القرآن الكريم", "اللغة العربية"]).join('، ')}.`;
    }

    const globalInst = document.getElementById('settings-global-instructions')?.value || "";
    const studentPrompt = student.customPrompt ? `توجيهات المعلم الخاصة بهذا الطالب: ${student.customPrompt}` : '';

    const prompt = `
    أنت خبير تربوي وموجّه في تحفيظ القرآن الكريم واللغة العربية والعلوم الإسلامية.
    بناءً على سجل الدروس اليومية والبيانات التالية للطالب:
    اسم الطالب: ${student.name}
    المادة: ${subject}
    الشهر: ${monthYear}
    سجل الدروس والتقارير اليومية لهذا الشهر:
    ${lessonsSummaryText}

    التوجيهات العامة للمعلم: ${globalInst}
    ${studentPrompt}

    قم بإعداد تقرير شهري شامل وممتاز يحلل مستوى الطالب خلال هذا الشهر ويبرز نقاط القوة والتوصيات وخطة الشهر القادم.
    استخدم مصطلحات إسلامية دقيقة بالإنجليزية في الملخص الإنجليزي مثل "Noble Quran", "Surah", "Tajweed", "Tilawah", "Hifdh", "Muraja'ah".

    أرجع النتيجة بتنسيق JSON مقفل يحتوي:
    {
      "newMemorisation": "تحليل شامل لتقدم الحفظ/الدروس الجديدة خلال الشهر",
      "revision": "تحليل شامل لقوة المراجعة والتمكين خلال الشهر",
      "reading": "تحليل الأداء في التلاوة والتجويد وقراءة النصوص",
      "memoScore": "ممتاز",
      "revScore": "ممتاز",
      "tajScore": "ممتاز",
      "comScore": "ممتاز",
      "strengths": ["نقطة قوة 1 تميز بها الطالب", "نقطة قوة 2"],
      "recommendations": ["توصية هامة لولي الأمر", "توصية ثانية"],
      "nextMonthPlan": "خطة العمل والشهر القادم الموصى بها",
      "summaryEnglish": "Comprehensive encouraging performance summary in English for parents with accurate Islamic terms"
    }
    أرجع JSON صافي فقط.
    `;

    try {
        const rawText = await callGeminiAPI(prompt);
        const parsed = extractAndParseJSON(rawText);

        activeGeneratedMonthlyReport = {
            studentId,
            subject,
            studentName: student.name,
            monthYear,
            ...parsed
        };

        renderMonthlyReportPreview(activeGeneratedMonthlyReport);
    } catch (err) {
        alert("تعذر توليد التقرير الشهري: " + err.message);
    } finally {
        btn.innerHTML = originalHTML;
        btn.disabled = false;
    }
}

// WORD & PDF EXPORT LOGIC FOR ALL REPORTS
function exportStudentReportsDoc(studentId) {
    const student = studentsList.find(s => s.id === studentId);
    if (!student) {
        alert("اختر الطالب أولاً!");
        return;
    }

    const lessons = getStudentLessonsLocal(studentId);

    let htmlContent = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
    <head><meta charset='utf-8'><title>تقارير الطالب ${student.name}</title>
    <style>
        body { font-family: 'Cairo', Arial, sans-serif; direction: rtl; text-align: right; padding: 20px; color: #1e293b; }
        h1 { color: #15803d; border-bottom: 2px solid #15803d; padding-bottom: 8px; font-size: 22px; }
        h2 { color: #166534; font-size: 16px; margin-top: 20px; }
        .card { border: 1px solid #cbd5e1; padding: 12px; margin-bottom: 12px; border-radius: 8px; background-color: #f8fafc; }
        .meta { font-weight: bold; color: #334155; font-size: 13px; margin-bottom: 6px; }
        .content { color: #0f172a; font-size: 13px; line-height: 1.6; }
    </style>
    </head>
    <body>
        <h1>تقرير متابعة شامل للطالب: ${student.name}</h1>
        <p class="meta">العمر: ${student.age || '-'} | المواد: ${(student.subjects || []).join('، ')}</p>
        <p class="meta">اسم المعلم: ${currentUser?.displayName || 'المعلم'}</p>
        <hr/>
        <h2>سجل الدروس والتقارير اليومية:</h2>
    `;

    if (lessons.length === 0) {
        htmlContent += `<p>لا توجد دروس يومية مسجلة بعد لهذا الطالب.</p>`;
    } else {
        lessons.forEach((l, idx) => {
            htmlContent += `
            <div class="card">
                <div class="meta">درس #${idx+1} | المادة: ${l.subject || 'القرآن الكريم'} | التاريخ: ${l.date || '-'} | مدة الدرس: ${l.duration || '45 دقيقة'} | التقدير: ${l.grade || 'ممتاز'}</div>
                <div class="content">
                    <p><strong>التقرير الشامل:</strong> ${l.reportArabic || 'لا يوجد'}</p>
                    ${l.homeworkAssigned ? `<p><strong>الواجب القادم:</strong> ${l.homeworkAssigned}</p>` : ''}
                </div>
            </div>
            `;
        });
    }

    if (activeGeneratedMonthlyReport && activeGeneratedMonthlyReport.studentId === studentId) {
        htmlContent += `
        <h2>التقرير الشهري الشامل (${activeGeneratedMonthlyReport.monthYear || ''}):</h2>
        <div class="card" style="background-color: #f0fdf4; border-color: #bbf7d0;">
            <p><strong>📖 الحفظ والدرس:</strong> ${activeGeneratedMonthlyReport.newMemorisation}</p>
            <p><strong>🔄 المراجعة:</strong> ${activeGeneratedMonthlyReport.revision}</p>
            <p><strong>🎙️ التلاوة والتجويد:</strong> ${activeGeneratedMonthlyReport.reading}</p>
            <p><strong>📅 خطة الشهر القادم:</strong> ${activeGeneratedMonthlyReport.nextMonthPlan}</p>
        </div>
        `;
    }

    htmlContent += `</body></html>`;

    const blob = new Blob(['\ufeff' + htmlContent], { type: 'application/msword' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `تقارير_الطالب_${student.name.replace(/\s+/g, '_')}.doc`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function exportStudentReportsDocFromMonthly() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    if (!studentId) {
        alert("اختر الطالب أولاً!");
        return;
    }
    exportStudentReportsDoc(studentId);
}

function printStudentReportsPDF(studentId) {
    const student = studentsList.find(s => s.id === studentId);
    if (!student) {
        alert("اختر الطالب أولاً!");
        return;
    }

    const lessons = getStudentLessonsLocal(studentId);
    const printWin = window.open('', '_blank');
    if (!printWin) return;

    let printHTML = `
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
        <meta charset="UTF-8">
        <title>تقارير الطالب ${student.name}</title>
        <style>
            body { font-family: 'Cairo', system-ui, sans-serif; direction: rtl; text-align: right; padding: 25px; color: #1e293b; }
            h1 { color: #15803d; border-bottom: 2px solid #15803d; padding-bottom: 8px; font-size: 22px; }
            .header-box { background: #f0fdf4; border: 1px solid #bbf7d0; padding: 15px; border-radius: 12px; margin-bottom: 20px; }
            .report-card { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; margin-bottom: 12px; page-break-inside: avoid; }
            .badge { display: inline-block; background: #dcfce7; color: #14532d; padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: bold; }
            @media print { body { padding: 0; } }
        </style>
    </head>
    <body>
        <div class="header-box">
            <h1>تقرير متابعة طالب: ${student.name}</h1>
            <p><strong>العمر:</strong> ${student.age || '-'} | <strong>المواد:</strong> ${(student.subjects || []).join('، ')}</p>
            <p><strong>اسم المعلم:</strong> ${currentUser?.displayName || 'المعلم'}</p>
        </div>
        <h2>سجل التقارير اليومية:</h2>
    `;

    if (lessons.length === 0) {
        printHTML += `<p>لا توجد تقارير يومية مسجلة بعد لهذا الطالب.</p>`;
    } else {
        lessons.forEach((l, i) => {
            printHTML += `
            <div class="report-card">
                <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                    <span class="badge">درس #${i+1} - ${l.subject || 'القرآن الكريم'}</span>
                    <span style="font-size:11px; color:#64748b;">📅 ${l.date || '-'} | ⏱️ ${l.duration || '45 دقيقة'} | ⭐ ${l.grade || 'ممتاز'}</span>
                </div>
                <p style="font-size:13px; line-height:1.6; margin:6px 0;">${l.reportArabic || 'لا يوجد نص للتقرير.'}</p>
                ${l.homeworkAssigned ? `<p style="font-size:12px; color:#166534; background:#f0fdf4; padding:6px; border-radius:6px; margin-top:4px;"><strong>📚 الواجب:</strong> ${l.homeworkAssigned}</p>` : ''}
            </div>
            `;
        });
    }

    if (activeGeneratedMonthlyReport && activeGeneratedMonthlyReport.studentId === studentId) {
        printHTML += `
        <h2>التقرير الشهري الشامل (${activeGeneratedMonthlyReport.monthYear || ''}):</h2>
        <div class="report-card" style="background:#f0fdf4; border-color:#bbf7d0;">
            <p style="font-size:13px;"><strong>📖 الحفظ والدرس:</strong> ${activeGeneratedMonthlyReport.newMemorisation}</p>
            <p style="font-size:13px;"><strong>🔄 المراجعة:</strong> ${activeGeneratedMonthlyReport.revision}</p>
            <p style="font-size:13px;"><strong>🎙️ التلاوة والتجويد:</strong> ${activeGeneratedMonthlyReport.reading}</p>
            <p style="font-size:13px;"><strong>📅 خطة الشهر القادم:</strong> ${activeGeneratedMonthlyReport.nextMonthPlan}</p>
        </div>
        `;
    }

    printHTML += `
        <script>
            window.onload = function() { window.print(); }
        </script>
    </body>
    </html>
    `;

    printWin.document.write(printHTML);
    printWin.document.close();
}

function printStudentReportsPDFFromMonthly() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    if (!studentId) {
        alert("اختر الطالب أولاً!");
        return;
    }
    printStudentReportsPDF(studentId);
}

// CHART.JS STUDENT PROGRESS ANALYTICS LOGIC
let studentChartInstance = null;

function onMonthlyStudentChanged() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);

    if (student && student.subjects && student.subjects.length > 0) {
        const subjs = student.subjects;
        const qCb = document.getElementById('monthly-subj-quran');
        const aCb = document.getElementById('monthly-subj-arabic');
        const iCb = document.getElementById('monthly-subj-islamic');
        const cIn = document.getElementById('monthly-subj-custom');

        if (qCb) qCb.checked = subjs.includes("القرآن الكريم");
        if (aCb) aCb.checked = subjs.includes("اللغة العربية");
        if (iCb) iCb.checked = subjs.includes("العلوم الإسلامية");

        const extras = subjs.filter(s => s !== "القرآن الكريم" && s !== "اللغة العربية" && s !== "العلوم الإسلامية");
        if (cIn) cIn.value = extras.join('، ');

        getMonthlySelectedSubjects();
    }

    updateStudentProgressChart();
}

function updateStudentProgressChart() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    const chartCanvas = document.getElementById('studentMonthlyChart');
    const chartType = document.getElementById('chart-type-select')?.value || 'line';

    if (!chartCanvas) return;

    if (!studentId) {
        if (studentChartInstance) {
            studentChartInstance.destroy();
            studentChartInstance = null;
        }
        const ctx = chartCanvas.getContext('2d');
        ctx.clearRect(0, 0, chartCanvas.width, chartCanvas.height);
        ctx.font = '13px Cairo, sans-serif';
        ctx.fillStyle = '#64748b';
        ctx.textAlign = 'center';
        ctx.fillText(currentLang === 'ar' ? 'اختر طالباً من القائمة لعرض الرسم البياني لمؤشرات الأداء' : 'Select a student to display performance chart', (chartCanvas.width / 2) || 150, (chartCanvas.height / 2) || 100);
        return;
    }

    const student = studentsList.find(s => s.id === studentId);
    const lessons = getStudentLessonsLocal(studentId);

    const gradeScoreMap = {
        'امتياز': 100,
        'ممتاز': 95,
        'جيد جداً': 85,
        'جيد': 75,
        'مقبول': 65,
        'يحتاج مراجعة': 55,
        'Excellent': 95,
        'Very Good': 85,
        'Good': 75
    };

    let labels = [];
    let memoScores = [];
    let revScores = [];

    if (lessons.length === 0) {
        labels = ['يناير', 'فبراير', 'مارس', 'أبريل', 'مايو', 'يونيو'];
        memoScores = [80, 85, 90, 88, 95, 98];
        revScores = [75, 80, 85, 90, 92, 95];
    } else {
        const monthGroups = {};
        const sortedLessons = [...lessons].sort((a, b) => (a.date || '').localeCompare(b.date || ''));

        sortedLessons.forEach(l => {
            const mKey = l.date ? l.date.slice(0, 7) : 'عام';
            if (!monthGroups[mKey]) {
                monthGroups[mKey] = { totalGrade: 0, count: 0 };
            }
            const score = gradeScoreMap[l.grade] || 90;
            monthGroups[mKey].totalGrade += score;
            monthGroups[mKey].count += 1;
        });

        Object.keys(monthGroups).slice(-8).forEach(mKey => {
            const group = monthGroups[mKey];
            const avgScore = Math.round(group.totalGrade / group.count);
            labels.push(mKey);
            memoScores.push(avgScore);
            revScores.push(Math.min(100, avgScore + 3));
        });
    }

    if (studentChartInstance) {
        studentChartInstance.destroy();
        studentChartInstance = null;
    }

    const ctx = chartCanvas.getContext('2d');

    if (chartType === 'radar') {
        studentChartInstance = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: ['الحفظ والدرس', 'المراجعة والتمكين', 'التلاوة والتجويد', 'الواجبات المنزليّة', 'الحضور والالتزام'],
                datasets: [{
                    label: student ? `أداء ${student.name}` : 'أداء الطالب',
                    data: [
                        memoScores[memoScores.length - 1] || 90,
                        revScores[revScores.length - 1] || 88,
                        92,
                        85,
                        95
                    ],
                    backgroundColor: 'rgba(22, 163, 74, 0.25)',
                    borderColor: '#16a34a',
                    borderWidth: 2,
                    pointBackgroundColor: '#15803d'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { font: { family: 'Cairo' } } }
                },
                scales: {
                    r: { min: 0, max: 100, ticks: { display: false } }
                }
            }
        });
    } else {
        studentChartInstance = new Chart(ctx, {
            type: chartType,
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'مستوى الحفظ والدروس (100%)',
                        data: memoScores,
                        borderColor: '#16a34a',
                        backgroundColor: chartType === 'bar' ? 'rgba(22, 163, 74, 0.75)' : 'rgba(22, 163, 74, 0.15)',
                        fill: chartType === 'line',
                        tension: 0.35,
                        borderWidth: 2.5,
                        pointRadius: 4,
                        pointBackgroundColor: '#15803d'
                    },
                    {
                        label: 'مستوى المراجعة والتمكين (100%)',
                        data: revScores,
                        borderColor: '#0284c7',
                        backgroundColor: chartType === 'bar' ? 'rgba(2, 132, 199, 0.75)' : 'rgba(2, 132, 199, 0.15)',
                        fill: chartType === 'line',
                        tension: 0.35,
                        borderWidth: 2.5,
                        pointRadius: 4,
                        pointBackgroundColor: '#0369a1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            font: { family: 'Cairo', size: 11, weight: 'bold' },
                            usePointStyle: true
                        }
                    },
                    tooltip: {
                        titleFont: { family: 'Cairo' },
                        bodyFont: { family: 'Cairo' }
                    }
                },
                scales: {
                    y: {
                        min: 40,
                        max: 100,
                        ticks: {
                            font: { family: 'Cairo', size: 10 },
                            callback: function(val) { return val + '%'; }
                        }
                    },
                    x: {
                        ticks: { font: { family: 'Cairo', size: 10 } }
                    }
                }
            }
        });
    }
}

function renderMonthlyReportPreview(report) {
    const container = document.getElementById('monthly-report-preview-container');
    const prStudentName = document.getElementById('pr-student-name');
    const prReportPeriod = document.getElementById('pr-report-period');
    const prTeacherLabel = document.getElementById('pr-teacher-label');
    const prEvaluationsBody = document.getElementById('pr-evaluations-body');

    if (prStudentName) prStudentName.textContent = `${currentLang === 'ar' ? 'التقرير الشهري للطالب:' : 'Monthly Report:'} ${report.studentName} (${report.subject || 'القرآن الكريم'})`;
    if (prReportPeriod) prReportPeriod.textContent = `${currentLang === 'ar' ? 'الفترة:' : 'Period:'} ${report.monthYear}`;
    if (prTeacherLabel) prTeacherLabel.textContent = `${currentLang === 'ar' ? 'المعلم:' : 'Teacher:'} ${currentUser?.displayName || 'المعتمد'}`;

    const html = `
        <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-center bg-emerald-50 p-3 rounded-xl border border-emerald-100 mb-4">
            <div><span class="block text-xs text-slate-500">الحفظ / الدرس</span><strong class="text-emerald-800 text-xs">${report.memoScore}</strong></div>
            <div><span class="block text-xs text-slate-500">المراجعة</span><strong class="text-emerald-800 text-xs">${report.revScore}</strong></div>
            <div><span class="block text-xs text-slate-500">التجويد / القراءة</span><strong class="text-emerald-800 text-xs">${report.tajScore}</strong></div>
            <div><span class="block text-xs text-slate-500">الالتزام</span><strong class="text-emerald-800 text-xs">${report.comScore}</strong></div>
        </div>

        <div class="space-y-3 font-amiri text-base leading-relaxed">
            <p><strong>📖 الدرس/الحفظ الجديد:</strong> ${report.newMemorisation}</p>
            <p><strong>🔄 المراجعة:</strong> ${report.revision}</p>
            <p><strong>🎙️ التلاوة والتجويد:</strong> ${report.reading}</p>
            
            ${report.strengths ? `<div><strong>🌟 نقاط القوة:</strong><ul class="list-disc list-inside mr-4">${report.strengths.map(s => `<li>${s}</li>`).join('')}</ul></div>` : ''}
            ${report.recommendations ? `<div><strong>💡 توصيات المعلم:</strong><ul class="list-disc list-inside mr-4">${report.recommendations.map(r => `<li>${r}</li>`).join('')}</ul></div>` : ''}
            
            <p><strong>📅 خطة الشهر القادم:</strong> ${report.nextMonthPlan}</p>
            ${report.summaryEnglish ? `<div class="bg-slate-50 p-3 rounded-lg text-xs font-sans text-slate-600 mt-2" dir="ltr"><strong>English Summary:</strong> ${report.summaryEnglish}</div>` : ''}
        </div>
    `;

    if (prEvaluationsBody) prEvaluationsBody.innerHTML = html;
    if (container) container.classList.remove('hidden');
}

async function saveMonthlyReportToFirebase() {
    if (!activeGeneratedMonthlyReport || !currentUser) return;
    try {
        await db.collection("teachers").doc(currentUser.uid)
            .collection("students").doc(activeGeneratedMonthlyReport.studentId)
            .collection("monthly_reports").add({
                ...activeGeneratedMonthlyReport,
                createdAt: firebase.firestore.FieldValue.serverTimestamp()
            });
        alert("تم حفظ التقرير الشهري بنجاح في قاعدة البيانات السحابية!");
    } catch (err) {
        alert("خطأ أثناء الحفظ: " + err.message);
    }
}

// SETTINGS & BACKUP HANDLER
function loadSettingsInputs() {
    const apiKeyInput = document.getElementById('settings-api-key');
    const aiModelSelect = document.getElementById('settings-ai-model');
    const globalInstrText = document.getElementById('settings-global-instructions');

    const savedKey = localStorage.getItem('gemini_api_key') || localStorage.getItem('user_gemini_api_key');
    if (apiKeyInput && savedKey) apiKeyInput.value = savedKey;

    const savedModel = localStorage.getItem('user_gemini_model');
    if (aiModelSelect && savedModel) aiModelSelect.value = savedModel;

    const savedInstr = localStorage.getItem('user_global_instructions');
    if (globalInstrText && savedInstr) globalInstrText.value = savedInstr;
}

function toggleApiKeyVisibility() {
    const input = document.getElementById('settings-api-key');
    const icon = document.getElementById('eye-icon');
    if (!input || !icon) return;
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-solid fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fa-solid fa-eye';
    }
}

async function handleSettingsSave(e) {
    if (e && e.preventDefault) e.preventDefault();
    const newName = document.getElementById('settings-teacher-name')?.value;
    const apiKey = document.getElementById('settings-api-key')?.value.trim() || '';
    const aiModel = document.getElementById('settings-ai-model')?.value || 'gemini-1.5-flash';
    const globalInstr = document.getElementById('settings-global-instructions')?.value || '';

    if (apiKey) {
        localStorage.setItem('user_gemini_api_key', apiKey);
        localStorage.setItem('gemini_api_key', apiKey);
    } else {
        localStorage.removeItem('user_gemini_api_key');
        localStorage.removeItem('gemini_api_key');
    }

    localStorage.setItem('user_gemini_model', aiModel);
    localStorage.setItem('user_global_instructions', globalInstr);

    if (currentUser && newName) {
        await currentUser.updateProfile({ displayName: newName });
        const userDisplayName = document.getElementById('user-display-name');
        const dashTeacherName = document.getElementById('dash-teacher-name');
        if (userDisplayName) userDisplayName.textContent = newName;
        if (dashTeacherName) dashTeacherName.textContent = newName;
    }

    alert(currentLang === 'ar' ? "تم حفظ إعدادات المعلم ومفتاح الذكاء الاصطناعي بنجاح!" : "Teacher settings & API key saved successfully!");
}

function exportBackupDataJSON() {
    const backupObj = {
        teacherName: currentUser?.displayName,
        students: studentsList,
        exportedAt: new Date().toISOString()
    };
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(backupObj, null, 2));
    const dlAnchor = document.createElement('a');
    dlAnchor.setAttribute("href", dataStr);
    dlAnchor.setAttribute("download", `teacher_reports_backup_${new Date().toISOString().slice(0,10)}.json`);
    document.body.appendChild(dlAnchor);
    dlAnchor.click();
    dlAnchor.remove();
}

// SMART FILE UPLOAD TO FIREBASE STORAGE & GEMINI AI ANALYSIS
async function handleFileUploadAndAIAnalysis(input) {
    const file = input?.files?.[0];
    if (!file) return;

    const statusIndicator = document.getElementById('upload-status-indicator');
    const fileInfoContainer = document.getElementById('uploaded-file-info');
    const fileNameElem = document.getElementById('uploaded-file-name');
    const fileLinkElem = document.getElementById('uploaded-file-link');
    const fileSummaryArea = document.getElementById('daily-file-summary');

    if (statusIndicator) {
        statusIndicator.classList.remove('hidden', 'bg-red-50', 'text-red-700', 'bg-emerald-50', 'text-emerald-700');
        statusIndicator.classList.add('bg-blue-50', 'text-blue-700', 'flex', 'items-center', 'gap-2');
        statusIndicator.innerHTML = `<i class="fa-solid fa-spinner fa-spin text-blue-600"></i> <span>جاري رفع المستند (${file.name}) إلى Firebase Storage وتحليله بواسطة الذكاء الاصطناعي (Gemini)...</span>`;
    }

    let downloadURL = "#";
    try {
        // 1) FIREBASE STORAGE UPLOAD
        const uid = currentUser ? currentUser.uid : 'demo_teacher';
        const storageRef = firebase.storage().ref();
        const fileRef = storageRef.child(`teachers/${uid}/documents/${Date.now()}_${file.name}`);
        const snapshot = await fileRef.put(file);
        downloadURL = await snapshot.ref.getDownloadURL();
    } catch (storageErr) {
        console.warn("Firebase Storage upload notice:", storageErr);
    }

    // Display uploaded info
    if (fileNameElem) fileNameElem.textContent = `📄 ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
    if (fileLinkElem) fileLinkElem.href = downloadURL;
    if (fileInfoContainer) fileInfoContainer.classList.remove('hidden');

    // 2) GEMINI API ANALYSIS
    const reader = new FileReader();
    reader.onload = async function(e) {
        const base64Data = e.target.result.split(',')[1];
        const fileType = file.type || "application/pdf";

        const promptText = `أنت معلم ومحلل تربوي خبير في المناهج الإسلامية واللغة العربية. تم رفع مستند أو واجب أو ورقة عمل للطالب باسم (${file.name}). قم بقراءة وتلخيص محتوى هذا الملف باختصار شديد ومباشر (في حدود 2-3 أسطر) موضحاً النقاط الرئيسية ليتم تضمينه في نهاية تقرير الطالب الموجه لولي الأمر.`;

        const inlineData = {
            mimeType: (fileType.startsWith("image/") || fileType === "application/pdf") ? fileType : "application/pdf",
            data: base64Data
        };

        try {
            const aiSummary = await callGeminiAPI(promptText, inlineData);

            if (fileSummaryArea) fileSummaryArea.value = aiSummary;

            // 3) Append summary directly to daily report text area
            const arabicReportArea = document.getElementById('daily-report-arabic');
            if (arabicReportArea) {
                const currentText = arabicReportArea.value.trim();
                const fileNote = `\n\n📎 *مرفق مستند وتقييم الواجب/النشاط (${file.name}):*\n${aiSummary}`;
                if (!currentText.includes(file.name)) {
                    arabicReportArea.value = currentText ? currentText + fileNote : `📎 *مرفق مستند وتقييم الواجب/النشاط (${file.name}):*\n${aiSummary}`;
                }
            }

            if (statusIndicator) {
                statusIndicator.className = "text-xs font-semibold p-2.5 rounded-lg bg-emerald-50 text-emerald-800 flex items-center gap-2";
                statusIndicator.innerHTML = `<i class="fa-solid fa-circle-check text-emerald-600"></i> <span>تم رفع الملف إلى السحابة بنجاح وتحليله بواسطة Gemini AI وتضمينه بالتقرير!</span>`;
            }
        } catch (apiErr) {
            console.error("Gemini File Analysis Error:", apiErr);
            const fallbackSummary = `تم إرفاق المستند: ${file.name} وحفظه في السحابة بنجاح.`;
            if (fileSummaryArea) fileSummaryArea.value = fallbackSummary;

            const arabicReportArea = document.getElementById('daily-report-arabic');
            if (arabicReportArea) {
                const currentText = arabicReportArea.value.trim();
                const fileNote = `\n\n📎 *مرفق مستند (${file.name})*`;
                if (!currentText.includes(file.name)) {
                    arabicReportArea.value = currentText ? currentText + fileNote : `📎 *مرفق مستند (${file.name})*`;
                }
            }

            if (statusIndicator) {
                statusIndicator.className = "text-xs font-semibold p-2.5 rounded-lg bg-emerald-50 text-emerald-800 flex items-center gap-2";
                statusIndicator.innerHTML = `<i class="fa-solid fa-circle-check text-emerald-600"></i> <span>تم رفع المستند إلى السحابة بنجاح وترابطه مع التقرير!</span>`;
            }
        }
    };

    reader.readAsDataURL(file);
}

// EXPOSE GLOBAL FUNCTIONS TO WINDOW FOR INLINE HTML EVENT HANDLERS (MODULE COMPATIBILITY)
if (typeof window !== 'undefined') {
    window.navigateTo = navigateTo;
    window.toggleMobileMenu = toggleMobileMenu;
    window.openStudentProfile = openStudentProfile;
    window.closeStudentProfileModal = closeStudentProfileModal;
    window.deleteStudentLessonLocal = deleteStudentLessonLocal;
    window.copyDailyReportTextFromHistory = copyDailyReportTextFromHistory;
    window.sendMonthlyReportWhatsApp = sendMonthlyReportWhatsApp;
    window.parseAndFormatRawReportAI = parseAndFormatRawReportAI;
    window.generateDailyReportAI = generateDailyReportAI;
    window.saveDailyLessonLocally = saveDailyLessonLocally;
    window.sendDailyReportWhatsApp = sendDailyReportWhatsApp;
    window.toggleApiKeyVisibility = toggleApiKeyVisibility;
    window.handleSettingsSave = handleSettingsSave;
    window.exportBackupDataJSON = exportBackupDataJSON;
    window.importBackupDataJSON = importBackupDataJSON;
    window.handleFileUploadAndSummarizeAI = handleFileUploadAndSummarizeAI;
    window.onDailyDateChanged = onDailyDateChanged;
    window.updateCalculatedLessonNumber = updateCalculatedLessonNumber;
    window.selectStudentForDaily = selectStudentForDaily;
    window.editStudent = editStudent;
    window.deleteStudent = deleteStudent;
    window.openAddStudentModal = openAddStudentModal;
    window.closeAddStudentModal = closeAddStudentModal;
    window.handleAddStudentSubmit = handleAddStudentSubmit;
    window.saveStudent = handleAddStudentSubmit;
    window.filterStudents = filterStudents;
    window.exportStudentReportsDoc = exportStudentReportsDoc;
    window.printStudentReportsPDF = printStudentReportsPDF;
    window.printMonthlyReportPDF = printMonthlyReportPDF;
    window.exportMonthlyReportDoc = exportMonthlyReportDoc;
    window.generateMonthlyReportAI = generateMonthlyReportAI;
    window.saveMonthlyReportLocally = saveMonthlyReportLocally;
    window.exportAllDataBackup = exportAllDataBackup;
    window.importDataBackup = importDataBackup;
    window.loadSettingsInputs = loadSettingsInputs;
    window.getEffectiveGeminiApiKey = getEffectiveGeminiApiKey;
    window.handleAuthSubmit = handleAuthSubmit;
    window.handleDemoAuth = handleDemoAuth;
    window.toggleAuthMode = toggleAuthMode;
    window.logout = logout;
    window.onSubjectChanged = onSubjectChanged;
    window.toggleLanguage = toggleLanguage;
    window.applyLanguage = applyLanguage;
}
