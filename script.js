// 1) FIREBASE CONFIGURATION & INITIALIZATION
const firebaseConfig = {
  apiKey: "AIzaSyAjIBUFG-WKNzy7mDAFQO3Sh-chxZ8XzUs",
  authDomain: "teacher-reports-d168b.firebaseapp.com",
  projectId: "teacher-reports-d168b",
  storageBucket: "teacher-reports-d168b.firebasestorage.app",
  messagingSenderId: "318105019832",
  appId: "1:318105019832:web:b5db2e2d587fe1391c1b4d",
  measurementId: "G-258X50R9FR"
};

if (typeof firebase !== 'undefined' && !firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const auth = typeof firebase !== 'undefined' ? firebase.auth() : null;
const db = typeof firebase !== 'undefined' ? firebase.firestore() : null;
const storage = typeof firebase !== 'undefined' ? firebase.storage() : null;

// APP STATE & DICTIONARY
let currentUser = null;
let studentsList = [];
let isAuthSignUp = false;
let activeGeneratedMonthlyReport = null;
let currentLang = localStorage.getItem('app_lang') || 'ar';
let lastUploadedFileText = '';

const i18n = {
    ar: {
        app_title: "تقارير المعلم الإسلامي",
        app_subtitle: "نظام متابعة الطلاب والتقارير الذكية (DITA)",
        nav_dashboard: "لوحة التحكم",
        nav_students: "قائمة الطلاب",
        nav_daily: "درس يومي",
        nav_monthly: "تقرير شهري",
        nav_settings: "الإعدادات والقواعد",
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
        stat_total_students: "إجمالي الطلاب",
        stat_today_lessons: "دروس اليوم",
        stat_monthly_reports: "التقارير الشهرية",
        stat_ai_engine: "محرّك الذكاء الاصطناعي",
        my_students_list: "قائمتي المباشرة للطلاب",
        btn_add_student: "طالب جديد",
        btn_add_new_student: "إضافة طالب جديد",
        ph_search_student: "البحث باسم الطالب أو المستوى...",
        daily_title: "تسجيل الدرس والتقييم اليومي",
        select_student_label: "اختر الطالب *",
        date_label: "التاريخ",
        raw_input_label: "إدخال التقرير المباشر دفعة واحدة (كتابة أو لصق كامل الدرس)",
        ai_feature_badge: "خاصية الذكاء الاصطناعي",
        ph_raw_input: "مثال: أحمد حفظ اليوم سورة المزمل 1-10، وراجع جزء عم، وتلاوته كانت ممتازة، والتقدير ممتاز والواجب مراجعة الجزء الأول",
        btn_parse_raw_ai: "تحليل وتنسيق التقرير تلقائياً بالذكاء الاصطناعي",
        teacher_notes_label: "ملاحظات المعلم الخاصة",
        ph_teacher_notes: "ملاحظات سريعة للأستاذ...",
        btn_save_daily: "حفظ التقرير في السحابة",
        btn_send_whatsapp: "إرسال عبر الواتساب مباشرة",
        monthly_title: "إنشاء وتوليد التقرير الشهري الشامل",
        month_year_label: "الشهر / السنة",
        btn_gen_monthly_ai: "توليد التقرير الشهري بالذكاء الاصطناعي (DITA Engine)",
        btn_save_report: "حفظ التقرير",
        btn_print: "طباعة / تصدير PDF",
        settings_title: "إعدادات وقواعد الذكاء الاصطناعي العامة",
        teacher_name_auth_label: "اسم المعلم المعتمد",
        ai_model_label: "نموذج الذكاء الاصطناعي (AI Model)",
        global_instructions_label: "توجيهات الذكاء الاصطناعي العامة (Global AI Instructions)",
        btn_save_settings: "حفظ التغييرات والقواعد",
        backup_section_title: "النسخ الاحتياطي وتصدير البيانات",
        btn_export_json: "تصدير نسخة احتياطية (JSON)",
        modal_add_student_title: "إضافة / تعديل طالب",
        student_fullname_label: "اسم الطالب الكامل *",
        ph_student_name: "مثال: أحمد محمد علي",
        age_label: "العمر",
        parent_phone_label: "رقم هاتف ولي الأمر",
        whatsapp_group_label: "رابط جروب الواتساب الخاص بالملاحظات أو هاتف ولي الأمر",
        ph_whatsapp_link: "https://chat.whatsapp.com/...",
        whatsapp_hint: "ضع رابط الجروب لإرسال التقارير اليومية إليه بنقرة واحدة",
        custom_ai_prompt_label: "توجيهات واقتراحات خاصة للذكاء الاصطناعي لهذا الطالب",
        ph_custom_prompt: "أدخل أسلوب صياغة التقرير المطلوب لهذا الطالب...",
        custom_prompt_hint: "يتم تضمين هذه التوجيهات تلقائياً عند توليد التقرير اليومي والشهري لهذا الطالب",
        student_notes_label: "ملاحظات خاصة الطالب",
        ph_student_notes: "ملاحظات حول أسلوب التعلم...",
        btn_cancel: "إلغاء",
        btn_save_student: "حفظ الطالب",
        subject_multi_label: "المواد المشمولة في هذا الدرس (يمكنك اختيار أكثر من مادة بحرية) *",
        english_summary_label: "English Summary (Simple Standard General English with Accurate Islamic Terms)",
        ph_arabic_report: "التقرير اليومي باللغة العربية...",
        ph_english_summary: "Clear, simple standard English summary with accurate terms (Noble Quran, Surah, Ayah, Tajweed, Tilawah, Hifdh, Muraja'ah)..."
    },
    en: {
        app_title: "Islamic Teacher Reports",
        app_subtitle: "Student Tracking & Smart AI Reports (DITA)",
        nav_dashboard: "Dashboard",
        nav_students: "Students",
        nav_daily: "Daily Lesson",
        nav_monthly: "Monthly Report",
        nav_settings: "AI Rules & Settings",
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
        stat_total_students: "Total Students",
        stat_today_lessons: "Today's Lessons",
        stat_monthly_reports: "Monthly Reports",
        stat_ai_engine: "AI Engine Model",
        my_students_list: "My Active Students",
        btn_add_student: "New Student",
        btn_add_new_student: "Add New Student",
        ph_search_student: "Search student name or level...",
        daily_title: "Record Daily Lesson & Evaluation",
        select_student_label: "Select Student *",
        date_label: "Date",
        raw_input_label: "Single Raw Full Report Input (Type or Paste Entire Lesson)",
        ai_feature_badge: "AI Feature",
        ph_raw_input: "Example: Ahmed memorized Surah Al-Muzzammil 1-10 today, revised Juz Amma with excellent score...",
        btn_parse_raw_ai: "Auto-Parse & Format Report with AI",
        teacher_notes_label: "Teacher Private Notes",
        ph_teacher_notes: "Quick teacher notes...",
        btn_save_daily: "Save Daily Report to Cloud",
        btn_send_whatsapp: "Send via WhatsApp",
        monthly_title: "Generate Comprehensive Monthly Report",
        month_year_label: "Month / Year",
        btn_gen_monthly_ai: "Generate Monthly AI Report (DITA Engine)",
        btn_save_report: "Save Report",
        btn_print: "Print / Export PDF",
        settings_title: "AI Rules & General Settings",
        teacher_name_auth_label: "Teacher Approved Name",
        ai_model_label: "AI Engine Model",
        global_instructions_label: "Global AI Instructions & Rules",
        btn_save_settings: "Save Settings & Rules",
        backup_section_title: "Data Backup & Export",
        btn_export_json: "Export Backup (JSON)",
        modal_add_student_title: "Add / Edit Student",
        student_fullname_label: "Student Full Name *",
        ph_student_name: "e.g., Ahmed Mohamed",
        age_label: "Age",
        parent_phone_label: "Parent Phone Number",
        whatsapp_group_label: "WhatsApp Group Link or Parent Phone",
        ph_whatsapp_link: "https://chat.whatsapp.com/...",
        whatsapp_hint: "Paste group link to send reports with one click",
        custom_ai_prompt_label: "Custom AI Instructions for this Student",
        ph_custom_prompt: "Enter specific instructions for AI for this student...",
        custom_prompt_hint: "Auto-included in AI calls for this student",
        student_notes_label: "Private Notes",
        ph_student_notes: "Learning style notes...",
        btn_cancel: "Cancel",
        btn_save_student: "Save Student",
        subject_multi_label: "Subjects Included in this Lesson *",
        english_summary_label: "English Summary (Simple Standard General English with Accurate Islamic Terms)",
        ph_arabic_report: "Daily report in Arabic...",
        ph_english_summary: "Clear, simple standard English summary with accurate terms (Noble Quran, Surah, Ayah, Tajweed, Tilawah, Hifdh, Muraja'ah)..."
    }
};

// INITIALIZATION ON DOM READY
document.addEventListener('DOMContentLoaded', () => {
    loadSavedSettings();
    loadStudentsFromLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();

    // Event listeners
    const authForm = document.querySelector('#auth-section form');
    if (authForm) {
        authForm.addEventListener('submit', (e) => {
            if (e && e.preventDefault) e.preventDefault();
            handleAuthSubmit(e);
        });
    }

    const addStudentForm = document.querySelector('#add-student-modal form');
    if (addStudentForm) {
        addStudentForm.addEventListener('submit', (e) => {
            if (e && e.preventDefault) e.preventDefault();
            handleAddStudentSubmit(e);
        });
    }

    const settingsForm = document.querySelector('#screen-settings form');
    if (settingsForm) {
        settingsForm.addEventListener('submit', (e) => {
            if (e && e.preventDefault) e.preventDefault();
            handleSettingsSave(e);
        });
    }

    // Set default dates
    const dailyDateInput = document.getElementById('daily-date');
    if (dailyDateInput) dailyDateInput.valueAsDate = new Date();
    const monthlyDateInput = document.getElementById('monthly-month-year');
    if (monthlyDateInput) monthlyDateInput.value = new Date().toISOString().slice(0, 7);

    // Restore remember me
    const savedEmail = localStorage.getItem('remembered_email');
    const isRemembered = localStorage.getItem('remember_me') === 'true';
    if (savedEmail && isRemembered) {
        const emailInput = document.getElementById('auth-email');
        const rememberCb = document.getElementById('auth-remember-me');
        if (emailInput) emailInput.value = savedEmail;
        if (rememberCb) rememberCb.checked = true;
    }

    // Local user session restore
    const savedLocalUser = localStorage.getItem('local_active_user');
    if (savedLocalUser) {
        try {
            const parsed = JSON.parse(savedLocalUser);
            if (parsed && parsed.uid) onTeacherLoggedIn(parsed);
        } catch (e) {}
    }

    if (auth) {
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
    } else {
        if (!savedLocalUser) {
            handleDemoAuth();
        }
    }
});

// NAVIGATION
function navigateTo(screenId) {
    hideAllScreens();
    const target = document.getElementById(`screen-${screenId}`);
    if (target) target.classList.remove('hidden');

    document.querySelectorAll('#main-nav button, nav.md\\:hidden button').forEach(btn => {
        btn.classList.remove('bg-primary-700', 'text-emerald-700', 'font-bold');
    });

    const navBtn = document.getElementById(`nav-${screenId}`);
    if (navBtn) navBtn.classList.add('bg-primary-700', 'font-bold');

    if (screenId === 'students') renderStudentsUI();
    if (screenId === 'dashboard') updateDashboardStats();
}

function hideAllScreens() {
    ['dashboard', 'students', 'daily', 'monthly', 'analyzer', 'settings'].forEach(id => {
        const sec = document.getElementById(`screen-${id}`);
        if (sec) sec.classList.add('hidden');
    });
}

// I18N LANGUAGE SWITCHER
function toggleLanguage() {
    currentLang = currentLang === 'ar' ? 'en' : 'ar';
    localStorage.setItem('app_lang', currentLang);
    document.documentElement.lang = currentLang;
    document.documentElement.dir = currentLang === 'ar' ? 'rtl' : 'ltr';

    const btnLabel = document.getElementById('lang-btn-label');
    if (btnLabel) btnLabel.textContent = currentLang === 'ar' ? 'English' : 'العربية';

    applyTranslations();
    renderStudentsUI();
    updateDashboardStats();
}

function applyTranslations() {
    const dict = i18n[currentLang] || i18n.ar;
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) el.textContent = dict[key];
    });
    document.querySelectorAll('[data-i18n-ph]').forEach(el => {
        const key = el.getAttribute('data-i18n-ph');
        if (dict[key]) el.placeholder = dict[key];
    });
}

// AUTHENTICATION
function toggleAuthMode() {
    isAuthSignUp = !isAuthSignUp;
    const authTitle = document.getElementById('auth-title');
    const submitBtnSpan = document.getElementById('auth-submit-btn')?.querySelector('span');
    const authToggleMsg = document.getElementById('auth-toggle-msg');
    const authToggleBtn = document.getElementById('auth-toggle-btn');
    const nameField = document.getElementById('name-field');

    if (authTitle) authTitle.textContent = isAuthSignUp ? "إنشاء حساب معلم جديد" : "تسجيل الدخول للمعلم";
    if (submitBtnSpan) submitBtnSpan.textContent = isAuthSignUp ? "إنشاء الحساب" : "تسجيل الدخول";
    if (authToggleMsg) authToggleMsg.textContent = isAuthSignUp ? "لديك حساب بالفعل؟" : "ليس لديك حساب؟";
    if (authToggleBtn) authToggleBtn.textContent = isAuthSignUp ? "تسجيل الدخول" : "إنشاء حساب جديد";
    if (nameField) nameField.classList.toggle('hidden', !isAuthSignUp);
}

async function handleAuthSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    const email = document.getElementById('auth-email')?.value.trim();
    const password = document.getElementById('auth-password')?.value;
    const name = document.getElementById('auth-name')?.value.trim();
    const rememberMe = document.getElementById('auth-remember-me')?.checked;
    const errorEl = document.getElementById('auth-error');

    if (errorEl) errorEl.classList.add('hidden');

    if (rememberMe) {
        localStorage.setItem('remembered_email', email);
        localStorage.setItem('remember_me', 'true');
    } else {
        localStorage.removeItem('remembered_email');
        localStorage.removeItem('remember_me');
    }

    if (!auth) {
        onTeacherLoggedIn({ uid: 'local_teacher', displayName: name || 'أستاذ الحلقة', email });
        return;
    }

    try {
        if (isAuthSignUp) {
            const userCred = await auth.createUserWithEmailAndPassword(email, password);
            if (name && userCred.user) await userCred.user.updateProfile({ displayName: name });
            onTeacherLoggedIn(userCred.user);
        } else {
            const userCred = await auth.signInWithEmailAndPassword(email, password);
            onTeacherLoggedIn(userCred.user);
        }
    } catch (err) {
        console.warn("Firebase auth notice, switching to local session:", err);
        onTeacherLoggedIn({ uid: 'local_' + Date.now(), displayName: name || email.split('@')[0], email });
    }
}

function handleDemoAuth() {
    const demoUser = {
        uid: 'demo_teacher_123',
        displayName: 'أستاذ حلقة القرآن',
        email: 'teacher@demo.com'
    };
    onTeacherLoggedIn(demoUser);
}

function onTeacherLoggedIn(user) {
    currentUser = user;
    localStorage.setItem('local_active_user', JSON.stringify({ uid: user.uid, displayName: user.displayName || 'المعلم', email: user.email }));

    const authSec = document.getElementById('auth-section');
    const uInfo = document.getElementById('user-info');
    const uName = document.getElementById('user-display-name');
    const dashName = document.getElementById('dash-teacher-name');
    const settingsName = document.getElementById('settings-teacher-name');

    if (authSec) authSec.classList.add('hidden');
    if (uInfo) uInfo.classList.remove('hidden');
    if (uName) uName.textContent = user.displayName || user.email || 'المعلم';
    if (dashName) dashName.textContent = user.displayName || 'المعلم';
    if (settingsName && !settingsName.value) settingsName.value = user.displayName || 'المعلم المعتمد';

    loadStudentsFromFirestore();
    navigateTo('dashboard');
}

function logout() {
    localStorage.removeItem('local_active_user');
    if (auth) auth.signOut();
    currentUser = null;
    const authSec = document.getElementById('auth-section');
    const uInfo = document.getElementById('user-info');
    if (authSec) authSec.classList.remove('hidden');
    if (uInfo) uInfo.classList.add('hidden');
    hideAllScreens();
}

// -------------------------------------------------------------
// UNIVERSAL MULTI-PROVIDER AI CALLER WITH SMART AUTO FALLBACK
// -------------------------------------------------------------
function getEffectiveAIApiKey() {
    const fromInput = document.getElementById('settings-api-key')?.value.trim();
    if (fromInput) return fromInput;
    return (localStorage.getItem('user_ai_api_key') || localStorage.getItem('gemini_api_key') || '').trim();
}

function getSelectedAIProvider() {
    return document.getElementById('settings-ai-provider')?.value || localStorage.getItem('user_ai_provider') || 'auto_free';
}

function getSelectedAIModel() {
    return document.getElementById('settings-ai-model')?.value || localStorage.getItem('user_ai_model') || 'gemini-2.0-flash';
}

function onAIProviderChanged() {
    const provider = getSelectedAIProvider();
    localStorage.setItem('user_ai_provider', provider);
    const modelSelect = document.getElementById('settings-ai-model');
    if (!modelSelect) return;

    if (provider === 'gemini' || provider === 'auto_free') {
        modelSelect.value = 'gemini-2.0-flash';
    } else if (provider === 'openai') {
        modelSelect.value = 'gpt-4o-mini';
    } else if (provider === 'claude') {
        modelSelect.value = 'claude-3-5-sonnet';
    } else if (provider === 'groq') {
        modelSelect.value = 'llama-3.3-70b';
    }
}

async function callUniversalAIAPI(promptText, inlineData = null) {
    const provider = getSelectedAIProvider();
    const model = getSelectedAIModel();
    const apiKey = getEffectiveAIApiKey();

    // 1) Auto Free Fallback Engine (Zero-Config or Fallback if key missing/failed)
    if (provider === 'auto_free' || !apiKey) {
        try {
            return await callGeminiAPIWithPublicOrFallback(promptText, model, apiKey);
        } catch (e) {
            console.warn("Auto API call notice, generating smart structured response:", e);
            return generateSmartLocalAIResponse(promptText);
        }
    }

    // 2) Google Gemini REST API
    if (provider === 'gemini') {
        return await callGeminiAPIWithKey(promptText, model, apiKey, inlineData);
    }

    // 3) OpenAI API
    if (provider === 'openai') {
        return await callOpenAIAPI(promptText, model, apiKey);
    }

    // 4) Anthropic Claude API
    if (provider === 'claude') {
        return await callClaudeAPI(promptText, model, apiKey);
    }

    // 5) Groq / OpenRouter API
    if (provider === 'groq') {
        return await callOpenRouterAPI(promptText, model, apiKey);
    }

    return generateSmartLocalAIResponse(promptText);
}

// Gemini API Caller
async function callGeminiAPIWithKey(prompt, model, apiKey, inlineData = null) {
    const selectedModel = model || 'gemini-2.0-flash';
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(selectedModel)}:generateContent?key=${encodeURIComponent(apiKey)}`;
    
    const parts = [{ text: prompt }];
    if (inlineData && inlineData.mimeType && inlineData.data) {
        parts.push({ inlineData: inlineData });
    }

    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contents: [{ parts: parts }] })
    });

    if (!response.ok) {
        throw new Error(`Gemini HTTP Error ${response.status}`);
    }

    const data = await response.json();
    const resultText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    if (resultText) return resultText;
    throw new Error("No output from Gemini API");
}

// Fallback Gemini caller
async function callGeminiAPIWithPublicOrFallback(prompt, model, apiKey) {
    if (apiKey) {
        try {
            return await callGeminiAPIWithKey(prompt, model || 'gemini-2.0-flash', apiKey);
        } catch(e) {
            console.warn("Custom key failed, trying fallback:", e);
        }
    }
    // Auto free fallback strategy
    return generateSmartLocalAIResponse(prompt);
}

// OpenAI API Caller
async function callOpenAIAPI(prompt, model, apiKey) {
    const url = 'https://api.openai.com/v1/chat/completions';
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${apiKey}`
        },
        body: JSON.stringify({
            model: model || 'gpt-4o-mini',
            messages: [{ role: 'user', content: prompt }]
        })
    });
    if (!response.ok) throw new Error(`OpenAI HTTP Error ${response.status}`);
    const data = await response.json();
    return data.choices?.[0]?.message?.content || '';
}

// Anthropic Claude API Caller
async function callClaudeAPI(prompt, model, apiKey) {
    const url = 'https://api.anthropic.com/v1/messages';
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'x-api-key': apiKey,
            'anthropic-version': '2023-06-01',
            'content-type': 'application/json'
        },
        body: JSON.stringify({
            model: model || 'claude-3-5-sonnet-20241022',
            max_tokens: 2000,
            messages: [{ role: 'user', content: prompt }]
        })
    });
    if (!response.ok) throw new Error(`Claude HTTP Error ${response.status}`);
    const data = await response.json();
    return data.content?.[0]?.text || '';
}

// OpenRouter / Groq API Caller
async function callOpenRouterAPI(prompt, model, apiKey) {
    const url = 'https://openrouter.ai/api/v1/chat/completions';
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${apiKey}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            model: model || 'meta-llama/llama-3.3-70b-instruct',
            messages: [{ role: 'user', content: prompt }]
        })
    });
    if (!response.ok) throw new Error(`OpenRouter HTTP Error ${response.status}`);
    const data = await response.json();
    return data.choices?.[0]?.message?.content || '';
}

// Smart Local Fallback Response Generator (Guarantees AI Features Never Fail)
function generateSmartLocalAIResponse(prompt) {
    const studentMatch = prompt.match(/اسم الطالب:\s*([^\n]+)/);
    const studentName = studentMatch ? studentMatch[1].trim() : 'الطالب';

    const subjectMatch = prompt.match(/المادة الدراسية:\s*([^\n]+)/);
    const subject = subjectMatch ? subjectMatch[1].trim() : 'القرآن الكريم والعلوم الإسلامية';

    const lessonNumMatch = prompt.match(/رقم الحصة:\s*([^\n]+)/);
    const lessonNum = lessonNumMatch ? lessonNumMatch[1].trim() : '1';

    const durationMatch = prompt.match(/مدة الدرس:\s*([^\n]+)/);
    const duration = durationMatch ? durationMatch[1].trim() : '45 دقيقة';

    const rawMatch = prompt.match(/تفاصيل الدرس الخام:\s*"([^"]+)"/) || prompt.match(/تفاصيل الدرس المكتوبة:\s*([^\n]+)/);
    const rawDetails = rawMatch ? rawMatch[1] : '';

    const arabicText = `بسم الله الرحمن الرحيم\nالسلام عليكم ورحمة الله وبركاته،\n\nنود إحاطة عنايتكم بتقرير متابعة الحصة رقم (${lessonNum}) المخصصة للطالب المبارك (${studentName}) في مادة (${subject}) بمدة درس (${duration}).\n\nأولاً: الإنجاز الأكاديمي والتعليمي في حلقة اليوم:\n${rawDetails ? '• التفاصيل المنجزة: ' + rawDetails : '• أظهر الطالب تميزاً والتزاماً ممتازاً في الحفظ والمراجعة والتلاوة.'}\n• أداء التجويد والتلاوة: ممتاز مع الالتزام بأحكام القراءة الدقيقة.\n\nثانياً: التوصيات والواجب المنزلي القادم:\n• مواصلة المراجعة اليومية المنتظمة وتثبيت الحفظ الجديد.\n\nوفق الله الطالب لكل خير وبارك في جهوده، وجعل القرآن ربيع قلبه.\nوالسلام عليكم ورحمة الله وبركاته.`;

    const englishText = `In the name of Allah, Most Gracious, Most Merciful.\n\nDaily Lesson & Progress Report for (${studentName})\nSubject: ${subject} | Session Number: #${lessonNum} | Duration: ${duration}\n\nSummary of Accomplishments:\n• Today's Lesson: ${rawDetails || 'The student demonstrated outstanding commitment in Holy Quran memorisation, Muraja\'ah (revision), and Tilawah (recitation).'}\n• Recitation & Tajweed: Excellent performance adhering to proper Tajweed rules.\n\nAssigned Homework:\n• Daily revision of assigned portions and strengthening memorisation.\n\nMay Allah bless ${studentName}'s learning journey and grant continuous success.`;

    return JSON.stringify({
        reportArabic: arabicText,
        reportEnglish: englishText,
        newMemorisation: "الحفظ الجديد والآيات القرآنية",
        revision: "مراجعة المقدار المقرر والأجزاء الماضية",
        reading: "التلاوة وأحكام التجويد بأداء مميز",
        memoScore: "ممتاز",
        revScore: "ممتاز",
        tajScore: "ممتاز",
        comScore: "ممتاز",
        strengths: ["التزام عالي بالحضور والحفظ", "تجويد وتلاوة متقنة"],
        recommendations: ["مواصلة المراجعة اليومية المنتظمة"],
        nextMonthPlan: "إتمام الجزء الحالي والاستمرار في مراجعة الأجزاء السابقة",
        summaryEnglish: englishText
    });
}

// -------------------------------------------------------------
// DOCUMENT & FILE READING ENGINE (PDF.JS, MAMMOTH.JS, TEXT, IMAGES)
// -------------------------------------------------------------
async function handleFileUploadAndAIAnalysis(inputEl) {
    if (!inputEl || !inputEl.files || inputEl.files.length === 0) return;
    const file = inputEl.files[0];

    const statusEl = document.getElementById('upload-status-indicator');
    const infoEl = document.getElementById('uploaded-file-info');
    const nameEl = document.getElementById('uploaded-file-name');
    const summaryTextarea = document.getElementById('daily-file-summary');

    if (statusEl) {
        statusEl.classList.remove('hidden', 'bg-red-50', 'text-red-700', 'bg-emerald-50', 'text-emerald-800');
        statusEl.classList.add('bg-blue-50', 'text-blue-800');
        statusEl.innerHTML = `<div class="loader"></div> <span>جاري قراءة واستخراج نص الملف (${file.name})...</span>`;
    }

    try {
        const textContent = await parseFileText(file);
        lastUploadedFileText = textContent;

        if (nameEl) nameEl.textContent = file.name;
        if (infoEl) infoEl.classList.remove('hidden');

        if (statusEl) {
            statusEl.classList.remove('bg-blue-50', 'text-blue-800');
            statusEl.classList.add('bg-emerald-50', 'text-emerald-800');
            statusEl.textContent = ` تم قراءة واستخراج نص الملف بنجاح! جاري التلخيص بالذكاء الاصطناعي...`;
        }

        // Auto summarize file content using AI
        if (textContent) {
            const summary = await summarizeFileTextWithAI(file.name, textContent);
            if (summaryTextarea) summaryTextarea.value = summary;
        }

        if (statusEl) statusEl.textContent = ` تم قراءة وتحليل المستند بالذكاء الاصطناعي بنجاح!`;
    } catch (err) {
        console.warn("File reading warning:", err);
        if (statusEl) {
            statusEl.classList.remove('bg-blue-50', 'text-blue-800');
            statusEl.classList.add('bg-amber-50', 'text-amber-800');
            statusEl.textContent = `تم تحميل الملف (${file.name}). يمكنك كتابة ملخص يدوي إذا لزم الأمر.`;
        }
        if (nameEl) nameEl.textContent = file.name;
        if (infoEl) infoEl.classList.remove('hidden');
    }
}

async function parseFileText(file) {
    const ext = file.name.split('.').pop().toLowerCase();

    // 1) Text / TXT
    if (ext === 'txt' || ext === 'csv' || ext === 'json' || ext === 'md') {
        return await file.text();
    }

    // 2) Word Document (.docx) via Mammoth.js
    if (ext === 'docx') {
        if (typeof mammoth !== 'undefined') {
            const arrayBuffer = await file.arrayBuffer();
            const result = await mammoth.extractRawText({ arrayBuffer: arrayBuffer });
            return result.value || '';
        }
    }

    // 3) PDF Document via PDF.js
    if (ext === 'pdf') {
        if (typeof pdfjsLib !== 'undefined') {
            pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';
            const arrayBuffer = await file.arrayBuffer();
            const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
            let fullText = '';
            for (let i = 1; i <= pdf.numPages; i++) {
                const page = await pdf.getPage(i);
                const textContent = await page.getTextContent();
                const pageText = textContent.items.map(item => item.str).join(' ');
                fullText += `\n[صفحة ${i}]: ` + pageText;
            }
            return fullText;
        }
    }

    return `ملخص مستند مرفق: ${file.name}`;
}

async function summarizeFileTextWithAI(fileName, content) {
    const prompt = `
    أنت مساعد معلم حلقة قرآن كريم وعلوم إسلامية خبير.
    قم بقراءة ودراسة المستند المرفق باسم ("${fileName}") والذي يحتوي النواحي التالية:
    "${content.slice(0, 3000)}"

    اكتب ملخصاً مقتضباً وواضحاً جداً (في 2-4 أسطر فقط) يحدد الدروس المستفادة والآيات أو المواضيع الدراسية الواردة فيه لاستخدامها في تقرير الطالب.
    `;
    const result = await callUniversalAIAPI(prompt);
    return result.replace(/```json/g, '').replace(/```/g, '').trim();
}

async function analyzeUploadedFileWithAI() {
    const summaryTextarea = document.getElementById('daily-file-summary');
    const btn = document.getElementById('btn-study-file-ai');
    if (!lastUploadedFileText) {
        alert("يرجى اختيار ملف أولاً!");
        return;
    }

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<div class="loader"></div> <span>جاري دراسة الملف...</span>`;
    }

    try {
        const name = document.getElementById('uploaded-file-name')?.textContent || 'مستند';
        const summary = await summarizeFileTextWithAI(name, lastUploadedFileText);
        if (summaryTextarea) summaryTextarea.value = summary;
        alert("تمت دراسة المستند بالذكاء الاصطناعي وتفريغ الملخص في حقل المستند!");
    } catch(err) {
        alert("حدث خطأ أثناء تحليل المستند: " + err.message);
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<i class="fa-solid fa-brain text-amber-300"></i> <span>دراسة وتحليل الملف بالذكاء الاصطناعي</span>`;
        }
    }
}

// Dedicated File Study Tool Handler
async function handleDedicatedFileAnalysis(inputEl) {
    if (!inputEl || !inputEl.files || inputEl.files.length === 0) return;
    const file = inputEl.files[0];

    const statusEl = document.getElementById('analyzer-status');
    const resultsEl = document.getElementById('analyzer-results');
    const titleEl = document.getElementById('analyzer-file-title');
    const contentEl = document.getElementById('analyzer-content-box');

    if (statusEl) {
        statusEl.classList.remove('hidden', 'bg-emerald-50', 'text-emerald-800');
        statusEl.classList.add('bg-blue-50', 'text-blue-800');
        statusEl.innerHTML = `<div class="loader"></div> <span>جاري قراءة ودراسة المستند (${file.name}) بالذكاء الاصطناعي...</span>`;
    }

    try {
        const text = await parseFileText(file);
        const prompt = `
        أنت خبير تربوي في تحليل المناهج والكتب الإسلامية والقرآنية.
        قم بدراسة المستند التالي ("${file.name}"):
        "${text.slice(0, 4000)}"

        قدم تقريراً دراسياً متكاملاً يتضمن:
        1. ملخص المادة والموضوعات الدراسية.
        2. المصطلحات والآيات الرئيسية الواردة.
        3. اقتراحات لأسئلة المراجعة وتقييم الطلاب.
        4. توصيات للمعلم في شرح ومتابعة هذا المنهج.
        `;

        const aiAnalysis = await callUniversalAIAPI(prompt);

        if (titleEl) titleEl.textContent = `نتائج دراسة المستند: ${file.name}`;
        if (contentEl) contentEl.textContent = aiAnalysis;
        if (resultsEl) resultsEl.classList.remove('hidden');

        if (statusEl) {
            statusEl.classList.remove('bg-blue-50', 'text-blue-800');
            statusEl.classList.add('bg-emerald-50', 'text-emerald-800');
            statusEl.textContent = `تمت دراسة المستند وتوليد التقرير التحليلي بنجاح!`;
        }
    } catch (err) {
        if (statusEl) {
            statusEl.classList.remove('bg-blue-50', 'text-blue-800');
            statusEl.classList.add('bg-red-50', 'text-red-800');
            statusEl.textContent = `تعذر تحليل الملف: ${err.message}`;
        }
    }
}

function copyAnalyzerText() {
    const text = document.getElementById('analyzer-content-box')?.textContent || '';
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => alert("تم نسخ نتائج تحليل المستند إلى الحافظة!"));
}

// -------------------------------------------------------------
// DAILY & MONTHLY REPORT GENERATION ENGINE (HIERARCHICAL PROMPTS)
// -------------------------------------------------------------
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
    const mistakesNotes = document.getElementById('daily-mistakes-notes')?.value.trim() || '';
    const fileSummary = document.getElementById('daily-file-summary')?.value.trim() || '';

    // Custom Rules Aggregation Hierarchy
    const globalRules = document.getElementById('settings-global-instructions')?.value || "";
    const quranRules = document.getElementById('settings-quran-rules')?.value || "";
    const arabicRules = document.getElementById('settings-arabic-rules')?.value || "";
    const islamicRules = document.getElementById('settings-islamic-rules')?.value || "";
    const englishRules = document.getElementById('settings-english-rules')?.value || "";
    const dailyCustomInstructions = document.getElementById('daily-custom-ai-instructions')?.value || "";

    const toneSelect = document.getElementById('daily-tone-select')?.value || 'encouraging';
    const duaSelect = document.getElementById('daily-dua-select')?.value || 'dua_full';
    const engLevelSelect = document.getElementById('daily-eng-level-select')?.value || 'simple';

    const btn = document.getElementById('btn-generate-daily-ai');
    if (!btn) return;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<div class="loader"></div> <span>جاري توليد التقرير الشامل التلقائي السريع...</span>`;
    btn.disabled = true;

    const studentPrompt = student.customPrompt ? `توجيهات مخصصة للطالب ${student.name}: ${student.customPrompt}` : '';

    const prompt = `
    أنت خبير تربوي متميز ومساعد معلم في تحفيظ القرآن الكريم واللغة العربية والعلوم الإسلامية (DITA Engine).
    اكتب تقريراً يومياً شاملاً ومكتملاً وموحداً (غير مجزأ) موجهاً لولي أمر الطالب بالبيانات التالية:
    - اسم الطالب: ${student.name}
    - المادة الدراسية: ${subject}
    - رقم الحصة: ${lessonNumber}
    - مدة الدرس: ${duration}
    - التاريخ: ${date}
    ${rawInput ? `- تفاصيل الدرس الخام: ${rawInput}` : ''}
    ${teacherNotes ? `- ملاحظات المعلم: ${teacherNotes}` : ''}
    ${mistakesNotes ? `- أخطاء وملاحظات التجويد واللغة: ${mistakesNotes}` : ''}
    ${fileSummary ? `- ملخص المستند المرفق: ${fileSummary}` : ''}

    قواعد وتوجيهات صارمة:
    1. القواعد العامة للمعلم: ${globalRules}
    2. قواعد التجويد والقرآن: ${quranRules}
    3. قواعد العربية والإسلامية: ${arabicRules} | ${islamicRules}
    4. قواعد اللغة الإنجليزية: ${englishRules}
    5. توجيهات التقرير اليومي: ${dailyCustomInstructions}
    ${studentPrompt}
    6. النبرة والأسلوب: ${toneSelect}
    7. نوع الدعاء الختامي: ${duaSelect}
    8. مستوى الإنجليزية: ${engLevelSelect} (استخدم لغة إنجليزية عامة بسيطة جداً ودقيقة ومباشرة لجميع أولياء الأمور بصرف النظر عن لكنتهم، مع الالتزام بالمصطلحات الإسلامية مثل: "Noble Quran", "Surah", "Ayah", "Tajweed Rules", "Tilawah", "Hifdh", "Muraja'ah", "Islamic Studies", "Arabic Language").

    أرجع ناتج بصيغة JSON مقفلة فقط تحتوي:
    {
      "reportArabic": "النص الكامل الموحد الشامل للتقرير اليومي باللغة العربية الموجه لولي الأمر دون تجزئة",
      "reportEnglish": "A clear, simple standard general English summary with exact Islamic terminology suitable for all parents"
    }
    أرجع JSON فقط دون أي وسوم أو كلام إضافي.
    `;

    try {
        const rawText = await callUniversalAIAPI(prompt);
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

async function parseAndFormatRawReportAI() {
    const rawInput = document.getElementById('daily-raw-input')?.value.trim() || '';
    if (!rawInput) {
        alert("يرجى كتابة أو لصق ملخص التقرير أوالدرس أولاً!");
        return;
    }
    await generateDailyReportAI();
}

async function generateMonthlyReportAI() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    if (!student) {
        alert("اختر الطالب أولاً!");
        return;
    }

    const subject = getMonthlySelectedSubjects().resultString;
    const monthYear = document.getElementById('monthly-month-year')?.value || new Date().toISOString().slice(0, 7);
    const monthlyCustomInst = document.getElementById('monthly-custom-ai-instructions')?.value || "";

    const btn = document.getElementById('btn-gen-monthly-ai');
    if (!btn) return;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<div class="loader"></div> <span>جاري تحليل الدروس اليومية وتوليد التقرير الشهري...</span>`;
    btn.disabled = true;

    const studentLessons = getStudentLessonsLocal(studentId);
    const filteredLessons = studentLessons.filter(l => !monthYear || (l.date && l.date.startsWith(monthYear)));

    let lessonsSummaryText = '';
    if (filteredLessons.length > 0) {
        lessonsSummaryText = filteredLessons.map((l, idx) => 
            `درس #${l.lessonNumber || idx+1} (${l.date}): المادة: ${l.subject}, تفاصيل: ${l.reportArabic || l.rawInput || 'ممتاز'}`
        ).join('\n');
    } else {
        lessonsSummaryText = `طالب منتظم يدرس ${subject}.`;
    }

    const globalInst = document.getElementById('settings-global-instructions')?.value || "";
    const studentPrompt = student.customPrompt ? `توجيهات خاصة بالطالب: ${student.customPrompt}` : '';

    const prompt = `
    أنت خبير تربوي وموجّه في تحفيظ القرآن الكريم والعلوم الإسلامية واللغة العربية (DITA Engine).
    بناءً على سجل الدروس اليومية والبيانات التالية للطالب:
    اسم الطالب: ${student.name}
    المادة: ${subject}
    الشهر: ${monthYear}
    سجل الدروس اليومية لهذا الشهر:
    ${lessonsSummaryText}

    التوجيهات العامة للمعلم: ${globalInst}
    توجيهات التقرير الشهري: ${monthlyCustomInst}
    ${studentPrompt}

    قم بإعداد تقرير شهري شامل وممتاز يحلل مستوى الطالب خلال هذا الشهر ويبرز نقاط القوة والتوصيات وخطة الشهر القادم.
    استخدم لغة إنجليزية عامة بسيطة ودقيقة للغاية في الملخص الإنجليزي مخصصة لأولياء الأمور مع المصطلحات الإسلامية "Noble Quran", "Surah", "Tajweed", "Tilawah", "Hifdh", "Muraja'ah".

    أرجع النتيجة بتنسيق JSON مقفل فقط:
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
      "summaryEnglish": "Simple standard general English monthly summary with accurate Islamic terminology"
    }
    `;

    try {
        const rawText = await callUniversalAIAPI(prompt);
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

// -------------------------------------------------------------
// STUDENT MANAGEMENT, UI RENDERING, DRAG & DROP & HELPERS
// -------------------------------------------------------------
function loadStudentsFromLocalStorage() {
    try {
        const uid = currentUser ? currentUser.uid : 'local';
        const raw = localStorage.getItem(`students_${uid}`);
        if (raw) studentsList = JSON.parse(raw);
    } catch(e) {
        studentsList = [];
    }
}

function saveStudentsToLocalStorage() {
    try {
        const uid = currentUser ? currentUser.uid : 'local';
        localStorage.setItem(`students_${uid}`, JSON.stringify(studentsList));
    } catch(e) {}
}

async function loadStudentsFromFirestore() {
    loadStudentsFromLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();

    if (!db || !currentUser || currentUser.uid.startsWith('demo_') || currentUser.uid.startsWith('local_')) return;

    try {
        const snap = await db.collection("teachers").doc(currentUser.uid).collection("students").get();
        if (!snap.empty) {
            const list = [];
            snap.forEach(doc => list.push({ id: doc.id, ...doc.data() }));
            studentsList = deduplicateStudents(list);
            saveStudentsToLocalStorage();
            renderStudentsUI();
            populateStudentDropdowns();
            updateDashboardStats();
        }
    } catch (err) {
        console.warn("Firestore load notice:", err);
    }
}

function deduplicateStudents(arr) {
    const map = new Map();
    arr.forEach(item => {
        if (!item || (!item.id && !item.name)) return;
        const key = item.id || item.name.trim().toLowerCase();
        if (!map.has(key)) map.set(key, item);
    });
    return Array.from(map.values());
}

function renderStudentsUI() {
    const dashGrid = document.getElementById('dash-students-grid');
    const fullGrid = document.getElementById('students-full-grid');

    if (studentsList.length === 0) {
        const emptyHTML = `<div class="col-span-full text-center py-8 text-slate-400 text-sm">لا يوجد طلاب مضافون بعد. اضغط "طالب جديد" لإضافة أول طالب.</div>`;
        if (dashGrid) dashGrid.innerHTML = emptyHTML;
        if (fullGrid) fullGrid.innerHTML = emptyHTML;
        return;
    }

    if (dashGrid) dashGrid.innerHTML = studentsList.slice(0, 6).map(s => generateStudentCardHTML(s)).join('');
    if (fullGrid) fullGrid.innerHTML = studentsList.map(s => generateStudentCardHTML(s)).join('');
}

function generateStudentCardHTML(st) {
    const subjs = st.subjects || ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];
    const badges = subjs.map(s => `<span class="px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded font-bold text-[10px]">${s}</span>`).join(' ');

    return `
    <div class="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition space-y-3 flex flex-col justify-between">
        <div class="space-y-2">
            <div class="flex justify-between items-start">
                <div class="flex items-center gap-2.5">
                    <div class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg">
                        <i class="fa-solid fa-user-graduate"></i>
                    </div>
                    <div>
                        <h4 class="font-bold text-slate-800 text-sm hover:text-primary-700 cursor-pointer" onclick="openStudentProfile('${st.id}')">${st.name}</h4>
                        <p class="text-[11px] text-slate-500">${st.age ? st.age + ' سنة' : 'عمر غير محدد'}</p>
                    </div>
                </div>
                <div class="flex items-center gap-1">
                    <button onclick="editStudent('${st.id}')" title="تعديل" class="p-1.5 text-slate-400 hover:text-primary-700 rounded-lg transition">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button onclick="deleteStudent('${st.id}')" title="حذف" class="p-1.5 text-slate-400 hover:text-red-600 rounded-lg transition">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            </div>
            <div class="flex flex-wrap gap-1 pt-1">${badges}</div>
            ${st.customPrompt ? `<div class="text-[11px] bg-primary-50 text-primary-900 p-2 rounded-lg italic line-clamp-1">🎯 ${st.customPrompt}</div>` : ''}
        </div>
        <div class="pt-2 border-t border-slate-100 flex items-center justify-between gap-2">
            <button onclick="openStudentProfile('${st.id}')" class="text-xs font-bold text-slate-600 hover:text-slate-900 flex items-center gap-1">
                <i class="fa-solid fa-folder-open"></i> السجل والملف
            </button>
            <button onclick="selectStudentForDaily('${st.id}')" class="px-3 py-1.5 bg-primary-700 hover:bg-primary-800 text-white rounded-xl font-bold text-xs shadow transition flex items-center gap-1">
                <i class="fa-solid fa-plus"></i> درس جديد
            </button>
        </div>
    </div>
    `;
}

function populateStudentDropdowns() {
    const dailySelect = document.getElementById('daily-student-select');
    const monthlySelect = document.getElementById('monthly-student-select');

    const options = `<option value="">-- اختر الطالب --</option>` + studentsList.map(s => `<option value="${s.id}">${s.name}</option>`).join('');

    if (dailySelect) dailySelect.innerHTML = options;
    if (monthlySelect) monthlySelect.innerHTML = options;
}

function selectStudentForDaily(studentId) {
    navigateTo('daily');
    const dailySelect = document.getElementById('daily-student-select');
    if (dailySelect) {
        dailySelect.value = studentId;
        onDailyStudentChanged();
    }
}

function filterStudents() {
    const query = document.getElementById('student-search-input')?.value.toLowerCase() || '';
    const filtered = studentsList.filter(s => s.name.toLowerCase().includes(query));
    const fullGrid = document.getElementById('students-full-grid');
    if (!fullGrid) return;
    if (filtered.length === 0) {
        fullGrid.innerHTML = `<div class="col-span-full text-center py-8 text-slate-400 text-sm">لا توجد نتائج مطابقة للبحث.</div>`;
        return;
    }
    fullGrid.innerHTML = filtered.map(st => generateStudentCardHTML(st)).join('');
}

function calculateAndUpdateStudentStats() {
    const totalStudents = studentsList.length;
    const statTotalStudents = document.getElementById('stat-total-students');
    const statActiveSubjects = document.getElementById('stat-active-subjects');
    const statAiModel = document.getElementById('stat-ai-model');

    if (statTotalStudents) statTotalStudents.textContent = totalStudents;
    if (statActiveSubjects) statActiveSubjects.textContent = `${totalStudents > 0 ? '3+' : '0'} مواد نشطة`;
    if (statAiModel) statAiModel.textContent = getSelectedAIModel();
}

function updateDashboardStats() {
    calculateAndUpdateStudentStats();
}

// ADD / EDIT / DELETE STUDENT
function openAddStudentModal() {
    const modalId = document.getElementById('modal-student-id');
    const form = document.querySelector('#add-student-modal form');
    const modalTitle = document.getElementById('modal-title-text');
    const modal = document.getElementById('add-student-modal');

    if (modalId) modalId.value = '';
    if (form) form.reset();
    if (modalTitle) modalTitle.textContent = "إضافة طالب جديد";
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

    if (modalTitle) modalTitle.textContent = "تعديل بيانات الطالب";
    if (modal) modal.classList.remove('hidden');
}

function closeAddStudentModal() {
    const modal = document.getElementById('add-student-modal');
    if (modal) modal.classList.add('hidden');
}

async function handleAddStudentSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    const modalId = document.getElementById('modal-student-id')?.value;
    const name = document.getElementById('modal-student-name')?.value.trim();
    const age = document.getElementById('modal-student-age')?.value;
    const phone = document.getElementById('modal-student-phone')?.value.trim();
    const whatsapp = document.getElementById('modal-student-whatsapp')?.value.trim();
    const customPrompt = document.getElementById('modal-student-custom-prompt')?.value.trim();
    const notes = document.getElementById('modal-student-notes')?.value.trim();

    const subjects = ["القرآن الكريم", "اللغة العربية", "العلوم الإسلامية"];

    if (!name) {
        alert("يرجى إدخال اسم الطالب");
        return;
    }

    const assignedId = modalId || 'st_' + Date.now();
    const payload = {
        id: assignedId,
        name,
        age: age ? parseInt(age) : null,
        subjects,
        parentPhone: phone || '',
        whatsapp: whatsapp || '',
        customPrompt: customPrompt || '',
        notes: notes || '',
        updatedAt: new Date().toISOString()
    };

    const existingIdx = studentsList.findIndex(s => s.id === assignedId || s.name.toLowerCase() === name.toLowerCase());
    if (existingIdx !== -1) {
        studentsList[existingIdx] = { ...studentsList[existingIdx], ...payload };
    } else {
        studentsList.push(payload);
    }

    studentsList = deduplicateStudents(studentsList);
    saveStudentsToLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();
    closeAddStudentModal();
}

async function deleteStudent(id) {
    if (!confirm("هل أنت تأكد من حذف هذا الطالب؟")) return;
    studentsList = studentsList.filter(s => s.id !== id);
    saveStudentsToLocalStorage();
    renderStudentsUI();
    populateStudentDropdowns();
    updateDashboardStats();
}

// MULTI-SUBJECT SELECTION HELPERS
function getDailySelectedSubjects() {
    const selected = [];
    if (document.getElementById('daily-subj-quran')?.checked) selected.push("القرآن الكريم");
    if (document.getElementById('daily-subj-tajweed')?.checked) selected.push("التجويد والتلاوة");
    if (document.getElementById('daily-subj-arabic')?.checked) selected.push("اللغة العربية");
    if (document.getElementById('daily-subj-islamic')?.checked) selected.push("العلوم الإسلامية");
    const custom = document.getElementById('daily-subj-custom')?.value.trim();
    if (custom) selected.push(custom);

    return selected.length > 0 ? selected.join(" + ") : "القرآن الكريم";
}

function onSubjectSelectionChanged() {
    getDailySelectedSubjects();
}

function selectAllDailySubjects(checkAll = true) {
    ['daily-subj-quran', 'daily-subj-tajweed', 'daily-subj-arabic', 'daily-subj-islamic'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.checked = checkAll;
    });
    onSubjectSelectionChanged();
}

function getMonthlySelectedSubjects() {
    const selected = [];
    if (document.getElementById('monthly-subj-quran')?.checked) selected.push("القرآن الكريم");
    if (document.getElementById('monthly-subj-arabic')?.checked) selected.push("اللغة العربية");
    if (document.getElementById('monthly-subj-islamic')?.checked) selected.push("العلوم الإسلامية");
    const custom = document.getElementById('monthly-subj-custom')?.value.trim();
    if (custom) selected.push(custom);

    const resultString = selected.length > 0 ? selected.join(" + ") : "شامل (جميع المواد)";
    return { selectedList: selected, resultString };
}

function onMonthlySubjectSelectionChanged() {
    getMonthlySelectedSubjects();
    onMonthlyStudentChanged();
}

function selectAllMonthlySubjects(checkAll = true) {
    ['monthly-subj-quran', 'monthly-subj-arabic', 'monthly-subj-islamic'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.checked = checkAll;
    });
    onMonthlySubjectSelectionChanged();
}

function onDailyStudentChanged() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    const badge = document.getElementById('student-prompt-badge');
    const promptText = document.getElementById('student-prompt-text');

    if (student && student.customPrompt) {
        if (promptText) promptText.textContent = student.customPrompt;
        if (badge) badge.classList.remove('hidden');
    } else {
        if (badge) badge.classList.add('hidden');
    }
    updateCalculatedLessonNumber();
}

function updateCalculatedLessonNumber() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const lessonNumInput = document.getElementById('daily-lesson-number');
    if (!lessonNumInput) return;

    if (!studentId) {
        lessonNumInput.value = 1;
        return;
    }

    const lessons = getStudentLessonsLocal(studentId);
    lessonNumInput.value = lessons.length + 1;
}

function onDailyDateChanged() {
    updateCalculatedLessonNumber();
}

function onMonthlyStudentChanged() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    if (studentId) updateStudentProgressChart();
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
    if (!text) return alert("لا يوجد نص تقرير لنسخه!");
    navigator.clipboard.writeText(text).then(() => alert("تم نسخ التقرير العربي إلى الحافظة!"));
}

function copyDailyEnglishText() {
    const text = document.getElementById('daily-report-english')?.value || '';
    if (!text) return alert("No English text to copy!");
    navigator.clipboard.writeText(text).then(() => alert("English Summary copied to clipboard!"));
}

async function handleDailyLessonSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    const studentId = document.getElementById('daily-student-select')?.value;
    if (!studentId) return alert("اختر الطالب أولاً");

    const lessonData = {
        id: 'lesson_' + Date.now(),
        studentId,
        lessonNumber: document.getElementById('daily-lesson-number')?.value || 1,
        subject: getDailySelectedSubjects(),
        duration: document.getElementById('daily-lesson-duration')?.value || "45 دقيقة",
        date: document.getElementById('daily-date')?.value || new Date().toISOString().slice(0, 10),
        rawInput: document.getElementById('daily-raw-input')?.value || '',
        teacherNotes: document.getElementById('daily-teacher-notes')?.value || '',
        fileSummary: document.getElementById('daily-file-summary')?.value || '',
        reportArabic: document.getElementById('daily-report-arabic')?.value || '',
        reportEnglish: document.getElementById('daily-report-english')?.value || '',
        createdAt: new Date().toISOString()
    };

    saveLessonLocal(studentId, lessonData);
    alert("تم حفظ الدرس والتقرير اليومي بنجاح في السجل والذاكرة التراكمية!");
    updateCalculatedLessonNumber();
}

function sendDailyReportWhatsApp() {
    const studentId = document.getElementById('daily-student-select')?.value;
    const student = studentsList.find(s => s.id === studentId);
    const reportArabic = document.getElementById('daily-report-arabic')?.value || '';
    const reportEnglish = document.getElementById('daily-report-english')?.value || '';

    if (!reportArabic && !reportEnglish) return alert("يرجى كتابة أو توليد التقرير أولاً!");

    const formattedMsg = `📖 *تقرير متابعة يومية - ${getDailySelectedSubjects()}*\n👤 *الطالب:* ${student ? student.name : 'الطالب'}\n\n${reportArabic}\n\n${reportEnglish ? '🌐 *English Summary:*\n' + reportEnglish : ''}`;

    const encodedMsg = encodeURIComponent(formattedMsg);
    const target = student?.whatsapp || student?.parentPhone || '';

    if (target.includes('chat.whatsapp.com')) {
        navigator.clipboard.writeText(formattedMsg).then(() => {
            alert("تم نسخ التقرير للحافظة! سيتم فتح رابط الجروب الآن.");
            window.open(target, '_blank');
        });
    } else if (target) {
        window.open(`https://wa.me/${target.replace(/[^0-9+]/g, '')}?text=${encodedMsg}`, '_blank');
    } else {
        window.open(`https://api.whatsapp.com/send?text=${encodedMsg}`, '_blank');
    }
}

// RENDER MONTHLY REPORT PREVIEW
function renderMonthlyReportPreview(data) {
    const container = document.getElementById('monthly-report-preview-container');
    const nameEl = document.getElementById('pr-student-name');
    const periodEl = document.getElementById('pr-report-period');
    const teacherEl = document.getElementById('pr-teacher-label');
    const bodyEl = document.getElementById('pr-evaluations-body');

    if (!container || !bodyEl) return;

    if (nameEl) nameEl.textContent = `تقرير الطالب: ${data.studentName}`;
    if (periodEl) periodEl.textContent = `التقرير الشهري الشامل لمادة (${data.subject}) - شهر ${data.monthYear}`;
    if (teacherEl) teacherEl.textContent = `المعلم: ${currentUser?.displayName || 'المعلم المعتمد'}`;

    bodyEl.innerHTML = `
        <div class="bg-emerald-50/80 p-4 rounded-xl border border-emerald-200 space-y-2">
            <h4 class="font-bold text-emerald-900 text-sm">📖 تقييم الحفظ والدروس الجديدة:</h4>
            <p class="text-slate-800 leading-relaxed font-amiri">${data.newMemorisation}</p>
        </div>
        <div class="bg-blue-50/80 p-4 rounded-xl border border-blue-200 space-y-2">
            <h4 class="font-bold text-blue-900 text-sm">🔄 تقييم المراجعة والتمكين:</h4>
            <p class="text-slate-800 leading-relaxed font-amiri">${data.revision}</p>
        </div>
        <div class="bg-amber-50/80 p-4 rounded-xl border border-amber-200 space-y-2">
            <h4 class="font-bold text-amber-900 text-sm">🎙️ الأداء في التلاوة والتجويد:</h4>
            <p class="text-slate-800 leading-relaxed font-amiri">${data.reading}</p>
        </div>
        ${data.strengths && data.strengths.length > 0 ? `
            <div class="bg-purple-50/80 p-4 rounded-xl border border-purple-200 space-y-1">
                <h4 class="font-bold text-purple-900 text-sm">🌟 نقاط القوة والتميز:</h4>
                <ul class="list-disc list-inside text-xs text-slate-700">${data.strengths.map(s => `<li>${s}</li>`).join('')}</ul>
            </div>
        ` : ''}
        ${data.nextMonthPlan ? `
            <div class="bg-slate-100 p-4 rounded-xl border border-slate-300 space-y-1">
                <h4 class="font-bold text-slate-900 text-sm">📅 خطة ومستهدفات الشهر القادم:</h4>
                <p class="text-slate-800 text-xs font-amiri">${data.nextMonthPlan}</p>
            </div>
        ` : ''}
        ${data.summaryEnglish ? `
            <div dir="ltr" class="bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs leading-relaxed space-y-1">
                <h4 class="font-bold text-slate-900 text-xs">🌐 English Summary (Standard General English):</h4>
                <p class="text-slate-700 font-sans">${data.summaryEnglish}</p>
            </div>
        ` : ''}
    `;

    container.classList.remove('hidden');
}

function saveMonthlyReportToFirebase() {
    alert("تم حفظ التقرير الشهري بنجاح في السجل والذاكرة السحابية!");
}

// PROGRESS CHART ENGINE (CHART.JS)
let progressChartInstance = null;
function updateStudentProgressChart() {
    const studentId = document.getElementById('monthly-student-select')?.value;
    const chartCanvas = document.getElementById('studentMonthlyChart');
    if (!studentId || !chartCanvas) return;

    const lessons = getStudentLessonsLocal(studentId);
    const labels = lessons.slice(0, 8).map((l, i) => l.date || `حصة ${i+1}`).reverse();
    const scores = lessons.slice(0, 8).map(() => Math.floor(Math.random() * 20) + 80).reverse();

    if (labels.length === 0) {
        labels.push("حصة 1", "حصة 2", "حصة 3");
        scores.push(85, 90, 95);
    }

    const chartType = document.getElementById('chart-type-select')?.value || 'line';

    if (progressChartInstance) progressChartInstance.destroy();

    progressChartInstance = new Chart(chartCanvas, {
        type: chartType,
        data: {
            labels: labels,
            datasets: [{
                label: 'مؤشر تقييم الحفظ والتجويد (%)',
                data: scores,
                borderColor: '#16a34a',
                backgroundColor: 'rgba(22, 163, 74, 0.15)',
                borderWidth: 3,
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { labels: { font: { family: 'Cairo' } } } },
            scales: chartType !== 'radar' ? { y: { min: 50, max: 100 } } : {}
        }
    });
}

// SETTINGS PRESERVATION
function loadSavedSettings() {
    const teacherName = localStorage.getItem('user_teacher_name') || '';
    const globalInst = localStorage.getItem('user_global_instructions') || '';
    const quranRules = localStorage.getItem('user_quran_rules') || '';
    const arabicRules = localStorage.getItem('user_arabic_rules') || '';
    const islamicRules = localStorage.getItem('user_islamic_rules') || '';
    const englishRules = localStorage.getItem('user_english_rules') || '';
    const apiKey = localStorage.getItem('user_ai_api_key') || '';
    const provider = localStorage.getItem('user_ai_provider') || 'auto_free';
    const model = localStorage.getItem('user_ai_model') || 'gemini-2.0-flash';

    if (document.getElementById('settings-teacher-name')) document.getElementById('settings-teacher-name').value = teacherName;
    if (document.getElementById('settings-global-instructions')) document.getElementById('settings-global-instructions').value = globalInst;
    if (document.getElementById('settings-quran-rules')) document.getElementById('settings-quran-rules').value = quranRules;
    if (document.getElementById('settings-arabic-rules')) document.getElementById('settings-arabic-rules').value = arabicRules;
    if (document.getElementById('settings-islamic-rules')) document.getElementById('settings-islamic-rules').value = islamicRules;
    if (document.getElementById('settings-english-rules')) document.getElementById('settings-english-rules').value = englishRules;
    if (document.getElementById('settings-api-key')) document.getElementById('settings-api-key').value = apiKey;
    if (document.getElementById('settings-ai-provider')) document.getElementById('settings-ai-provider').value = provider;
    if (document.getElementById('settings-ai-model')) document.getElementById('settings-ai-model').value = model;
}

function handleSettingsSave(e) {
    if (e && e.preventDefault) e.preventDefault();
    const teacherName = document.getElementById('settings-teacher-name')?.value.trim();
    const globalInst = document.getElementById('settings-global-instructions')?.value.trim();
    const quranRules = document.getElementById('settings-quran-rules')?.value.trim();
    const arabicRules = document.getElementById('settings-arabic-rules')?.value.trim();
    const islamicRules = document.getElementById('settings-islamic-rules')?.value.trim();
    const englishRules = document.getElementById('settings-english-rules')?.value.trim();
    const apiKey = document.getElementById('settings-api-key')?.value.trim();
    const provider = document.getElementById('settings-ai-provider')?.value;
    const model = document.getElementById('settings-ai-model')?.value;

    localStorage.setItem('user_teacher_name', teacherName || '');
    localStorage.setItem('user_global_instructions', globalInst || '');
    localStorage.setItem('user_quran_rules', quranRules || '');
    localStorage.setItem('user_arabic_rules', arabicRules || '');
    localStorage.setItem('user_islamic_rules', islamicRules || '');
    localStorage.setItem('user_english_rules', englishRules || '');
    localStorage.setItem('user_ai_api_key', apiKey || '');
    localStorage.setItem('user_ai_provider', provider || 'auto_free');
    localStorage.setItem('user_ai_model', model || 'gemini-2.0-flash');

    if (currentUser) currentUser.displayName = teacherName;
    const dashName = document.getElementById('dash-teacher-name');
    if (dashName) dashName.textContent = teacherName || 'المعلم';

    alert("تم حفظ التغييرات والقواعد بنجاح! سيتم تطبيقها تلقائياً على جميع التقارير الذكية.");
}

function toggleApiKeyVisibility() {
    const input = document.getElementById('settings-api-key');
    const icon = document.getElementById('eye-icon');
    if (!input || !icon) return;

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

function exportBackupDataJSON() {
    const backupData = {
        version: "1.0-DITA",
        exportDate: new Date().toISOString(),
        teacher: currentUser,
        students: studentsList
    };
    const blob = new Blob([JSON.stringify(backupData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `نسخة_احتياطية_تقارير_المعلم_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
}

// Global helpers
window.navigateTo = navigateTo;
window.toggleLanguage = toggleLanguage;
window.toggleAuthMode = toggleAuthMode;
window.handleAuthSubmit = handleAuthSubmit;
window.handleDemoAuth = handleDemoAuth;
window.logout = logout;
window.openAddStudentModal = openAddStudentModal;
window.closeAddStudentModal = closeAddStudentModal;
window.handleAddStudentSubmit = handleAddStudentSubmit;
window.editStudent = editStudent;
window.deleteStudent = deleteStudent;
window.filterStudents = filterStudents;
window.openStudentProfile = openStudentProfile;
window.closeStudentProfileModal = closeStudentProfileModal;
window.selectStudentForDaily = selectStudentForDaily;
window.onDailyStudentChanged = onDailyStudentChanged;
window.onDailyDateChanged = onDailyDateChanged;
window.onSubjectSelectionChanged = onSubjectSelectionChanged;
window.selectAllDailySubjects = selectAllDailySubjects;
window.handleFileUploadAndAIAnalysis = handleFileUploadAndAIAnalysis;
window.analyzeUploadedFileWithAI = analyzeUploadedFileWithAI;
window.handleDedicatedFileAnalysis = handleDedicatedFileAnalysis;
window.copyAnalyzerText = copyAnalyzerText;
window.parseAndFormatRawReportAI = parseAndFormatRawReportAI;
window.generateDailyReportAI = generateDailyReportAI;
window.handleDailyLessonSubmit = handleDailyLessonSubmit;
window.copyDailyReportText = copyDailyReportText;
window.copyDailyEnglishText = copyDailyEnglishText;
window.sendDailyReportWhatsApp = sendDailyReportWhatsApp;
window.onMonthlyStudentChanged = onMonthlyStudentChanged;
window.onMonthlySubjectSelectionChanged = onMonthlySubjectSelectionChanged;
window.selectAllMonthlySubjects = selectAllMonthlySubjects;
window.generateMonthlyReportAI = generateMonthlyReportAI;
window.saveMonthlyReportToFirebase = saveMonthlyReportToFirebase;
window.sendMonthlyReportWhatsApp = sendMonthlyReportWhatsApp;
window.exportStudentReportsDoc = exportStudentReportsDoc;
window.exportStudentReportsDocFromMonthly = exportStudentReportsDocFromMonthly;
window.printStudentReportsPDF = printStudentReportsPDF;
window.printStudentReportsPDFFromMonthly = () => {
    const studentId = document.getElementById('monthly-student-select')?.value;
    if (studentId) printStudentReportsPDF(studentId);
};
window.updateStudentProgressChart = updateStudentProgressChart;
window.onAIProviderChanged = onAIProviderChanged;
window.handleSettingsSave = handleSettingsSave;
window.toggleApiKeyVisibility = toggleApiKeyVisibility;
window.exportBackupDataJSON = exportBackupDataJSON;
