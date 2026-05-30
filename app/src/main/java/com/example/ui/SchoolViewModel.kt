package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Login : Screen()
    object StudentsList : Screen()
    object AddStudent : Screen()
    object CoursesList : Screen()
    object AddCourse : Screen()
    data class StudentDetails(val studentId: Long) : Screen()
    object NotificationsLog : Screen()
    object StudentDashboard : Screen()
}

sealed class UserRole {
    object Admin : UserRole()
    data class StudentRole(val student: Student) : UserRole()
}

class SchoolViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SchoolRepository
    
    // Core states
    val students: StateFlow<List<Student>>
    val courses: StateFlow<List<Course>>
    val allAttendance: StateFlow<List<Attendance>>
    val allGrades: StateFlow<List<Grade>>
    val allBehaviorLogs: StateFlow<List<BehaviorLog>>
    val allNotifications: StateFlow<List<ParentNotification>>
    val allEnrollments: StateFlow<List<CourseEnrollment>>

    // Navigation and Session
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _loggedInUser = MutableStateFlow<UserRole?>(null)
    val loggedInUser: StateFlow<UserRole?> = _loggedInUser.asStateFlow()

    val loginMatric = MutableStateFlow("")
    val loginEmail = MutableStateFlow("")
    val loginError = MutableStateFlow<String?>(null)

    // Form states - Student
    var studentName = MutableStateFlow("")
    var studentGrade = MutableStateFlow("Grade 9")
    var studentEmail = MutableStateFlow("") // Added
    var studentMatric = MutableStateFlow("") // Added
    var parentName = MutableStateFlow("")
    var parentEmail = MutableStateFlow("")
    var parentPhone = MutableStateFlow("")
    var selectedColorHex = MutableStateFlow("#4F46E5") // Indigo default

    // Form states - Course
    var courseName = MutableStateFlow("")
    var teacherName = MutableStateFlow("")
    var courseSchedule = MutableStateFlow("")

    // Log detail entries form state (for Student Detail screens)
    var attCourseId = MutableStateFlow<Long?>(null)
    var attStatus = MutableStateFlow("Present")
    var attRemarks = MutableStateFlow("")
    var attSemester = MutableStateFlow("Spring 2026")
    
    var gradeCourseId = MutableStateFlow<Long?>(null)
    var gradeAssessmentName = MutableStateFlow("")
    var gradeScore = MutableStateFlow("")
    var gradeMaxScore = MutableStateFlow("100")
    var gradeSemester = MutableStateFlow("Spring 2026")

    var behCategory = MutableStateFlow("Positive")
    var behType = MutableStateFlow("Leadership")
    var behDescription = MutableStateFlow("")
    var behPoints = MutableStateFlow("5")

    // AI Processing States
    private val _isGeneratingNotification = MutableStateFlow(false)
    val isGeneratingNotification = _isGeneratingNotification.asStateFlow()

    private val _generatedNotificationDraft = MutableStateFlow<String?>(null)
    val generatedNotificationDraft = _generatedNotificationDraft.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport = _isGeneratingReport.asStateFlow()

    private val _generatedReport = MutableStateFlow<String?>(null)
    val generatedReport = _generatedReport.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SchoolRepository(db)

        // Flows
        students = repository.allStudents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        courses = repository.allCourses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allGrades = repository.allGrades.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allBehaviorLogs = repository.allBehaviorLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allEnrollments = repository.allEnrollments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Check and pre-populate database if empty and initialize form defaults
        viewModelScope.launch {
            val list = repository.allStudents.first()
            if (list.isEmpty()) {
                seedSampleData()
            }
        }
    }

    fun login(onSuccess: () -> Unit) {
        val matric = loginMatric.value.trim()
        val email = loginEmail.value.trim()

        if (matric.isEmpty() || email.isEmpty()) {
            loginError.value = "Please fill in all credentials."
            return
        }

        // Admin hardcoded check
        if (matric.equals("ADMIN", ignoreCase = true) && email.equals("admin@school.com", ignoreCase = true)) {
            _loggedInUser.value = UserRole.Admin
            loginError.value = null
            navigateTo(Screen.StudentsList)
            onSuccess()
            return
        }

        // Student query
        viewModelScope.launch {
            val matchedStudent = students.value.firstOrNull {
                it.matricNumber.equals(matric, ignoreCase = true) && 
                it.studentEmail.equals(email, ignoreCase = true)
            }

            if (matchedStudent != null) {
                _loggedInUser.value = UserRole.StudentRole(matchedStudent)
                loginError.value = null
                navigateTo(Screen.StudentDashboard)
                onSuccess()
            } else {
                loginError.value = "Invalid Student Credentials. Check matric and registered email."
            }
        }
    }

    fun logout() {
        _loggedInUser.value = null
        loginMatric.value = ""
        loginEmail.value = ""
        loginError.value = null
        navigateTo(Screen.Login)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        
        // Reset dynamic draft states on screen navigation
        _generatedNotificationDraft.value = null
        _generatedReport.value = null
    }

    fun getStudentById(id: Long): Flow<Student?> = repository.getStudentById(id)

    // --- Student Actions ---
    fun saveStudent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val name = studentName.value.trim()
            val grade = studentGrade.value.trim()
            val sEmail = studentEmail.value.trim()
            val sMatric = studentMatric.value.trim()
            val pName = parentName.value.trim()
            val pEmail = parentEmail.value.trim()
            val pPhone = parentPhone.value.trim()
            val color = selectedColorHex.value

            if (name.isNotEmpty() && pEmail.isNotEmpty() && sEmail.isNotEmpty() && sMatric.isNotEmpty()) {
                val student = Student(
                    name = name,
                    gradeLevel = grade,
                    studentEmail = sEmail,
                    matricNumber = sMatric,
                    parentName = pName,
                    parentEmail = pEmail,
                    parentPhone = pPhone,
                    photoColorHex = color
                )
                repository.insertStudent(student)
                resetStudentFields()
                onSuccess()
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    private fun resetStudentFields() {
        studentName.value = ""
        studentGrade.value = "Grade 9"
        studentEmail.value = ""
        studentMatric.value = ""
        parentName.value = ""
        parentEmail.value = ""
        parentPhone.value = ""
        selectedColorHex.value = "#4F46E5"
    }

    // --- Course Selection Actions ---
    fun enrollInCourse(studentId: Long, courseId: Long) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val enrollment = CourseEnrollment(
                studentId = studentId,
                courseId = courseId,
                dateEnrolled = dateStr
            )
            repository.insertEnrollment(enrollment)
        }
    }

    fun withdrawFromCourse(studentId: Long, courseId: Long) {
        viewModelScope.launch {
            repository.deleteEnrollment(studentId, courseId)
        }
    }

    // --- Course Actions ---
    fun saveCourse(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val name = courseName.value.trim()
            val teacher = teacherName.value.trim()
            val sched = courseSchedule.value.trim()

            if (name.isNotEmpty() && teacher.isNotEmpty()) {
                val course = Course(
                    name = name,
                    teacherName = teacher,
                    schedule = sched
                )
                repository.insertCourse(course)
                courseName.value = ""
                teacherName.value = ""
                courseSchedule.value = ""
                onSuccess()
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    // --- Log Detail Actions ---
    fun addAttendance(studentId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cId = attCourseId.value ?: return@launch
            val status = attStatus.value
            val remarks = attRemarks.value.trim()
            val sem = attSemester.value
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val attendance = Attendance(
                studentId = studentId,
                courseId = cId,
                date = dateStr,
                status = status,
                remarks = remarks,
                semester = sem
            )
            repository.insertAttendance(attendance)
            
            // Clean up
            attRemarks.value = ""
            onSuccess()

            // Trigger AI automated notification draft if Absent or Tardy
            if (status == "Absent" || status == "Tardy") {
                triggerAIAutomatedDraft(studentId, cId, "Attendance", "Marked $status. Remarks: $remarks")
            }
        }
    }

    fun deleteAttendance(attendance: Attendance) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
        }
    }

    fun addGrade(studentId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cId = gradeCourseId.value ?: return@launch
            val assess = gradeAssessmentName.value.trim()
            val scoreVal = gradeScore.value.toDoubleOrNull() ?: 0.0
            val maxVal = gradeMaxScore.value.toDoubleOrNull() ?: 100.0
            val sem = gradeSemester.value
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (assess.isNotEmpty()) {
                val gradeObj = Grade(
                    studentId = studentId,
                    courseId = cId,
                    assessmentName = assess,
                    score = scoreVal,
                    maxScore = maxVal,
                    date = dateStr,
                    semester = sem
                )
                repository.insertGrade(gradeObj)

                // Clean up
                gradeAssessmentName.value = ""
                gradeScore.value = ""
                onSuccess()

                // Trigger AI warning notification if parent notification is relevant (e.g. failing exam)
                val percentage = (scoreVal / maxVal) * 100
                if (percentage < 60.0) {
                    triggerAIAutomatedDraft(studentId, cId, "Grade Update", "Failing Grade Risk: Scored $scoreVal/$maxVal ($percentage%) on assessment '$assess'")
                } else if (percentage >= 95.0) {
                    triggerAIAutomatedDraft(studentId, cId, "Grade Update", "Exceptional Achievement: Scored $scoreVal/$maxVal ($percentage%) on assessment '$assess'")
                }
            }
        }
    }

    fun deleteGrade(grade: Grade) {
        viewModelScope.launch {
            repository.deleteGrade(grade)
        }
    }

    fun addBehaviorLog(studentId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cat = behCategory.value
            val bType = behType.value
            val desc = behDescription.value.trim()
            val pts = behPoints.value.toIntOrNull() ?: 5
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val behaviorLog = BehaviorLog(
                studentId = studentId,
                category = cat,
                behaviorType = bType,
                description = desc,
                pointsDelta = if (cat == "Positive") pts else -pts,
                date = dateStr
            )
            repository.insertBehaviorLog(behaviorLog)

            // Clean up
            behDescription.value = ""
            onSuccess()

            // Auto alert parent on behavior logs
            val pointsLabel = if (cat == "Positive") "+$pts" else "-$pts"
            triggerAIAutomatedDraft(studentId, 0L, "Behavior Alert", "Log category: $cat | Behavior type: $bType | Action: $desc | Points: $pointsLabel")
        }
    }

    fun deleteBehaviorLog(log: BehaviorLog) {
        viewModelScope.launch {
            repository.deleteBehaviorLog(log)
        }
    }

    fun deleteNotification(notification: ParentNotification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    // --- Intelligent Core Operations ---

    // Triggered automatically on critical changes
    private fun triggerAIAutomatedDraft(studentId: Long, courseId: Long, type: String, details: String) {
        viewModelScope.launch {
            val student = repository.getStudentByIdSuspend(studentId) ?: return@launch
            val courseName = if (courseId > 0) {
                repository.getCourseByIdSuspend(courseId)?.name ?: "Class"
            } else {
                "General Behavior"
            }

            _isGeneratingNotification.value = true
            val generatedText = GeminiService.draftAINotification(student, courseName, type, details)
            _isGeneratingNotification.value = false
            
            // Auto-save the sent message to database parent notifications list
            val title = "Report: AI $type - ${student.name}"
            val notification = ParentNotification(
                studentId = studentId,
                title = title,
                message = generatedText,
                type = type
            )
            repository.insertNotification(notification)
            _generatedNotificationDraft.value = generatedText
        }
    }

    // Manual Draft Trigger (from Chat pane or detailed logs)
    fun generateManualDraftNotification(studentId: Long, type: String, details: String) {
        viewModelScope.launch {
            val student = repository.getStudentByIdSuspend(studentId) ?: return@launch
            _isGeneratingNotification.value = true
            _generatedNotificationDraft.value = null
            
            val generatedText = GeminiService.draftAINotification(student, "General School Status", type, details)
            _generatedNotificationDraft.value = generatedText
            _isGeneratingNotification.value = false
            
            // Save as notification log
            val notification = ParentNotification(
                studentId = studentId,
                title = "Notification: Academic Advisory Report",
                message = generatedText,
                type = type
            )
            repository.insertNotification(notification)
        }
    }

    fun requestAcademicSummaryReport(studentId: Long) {
        viewModelScope.launch {
            val student = repository.getStudentByIdSuspend(studentId) ?: return@launch
            _isGeneratingReport.value = true
            _generatedReport.value = null

            val studentCourses = courses.value
            val studentGrades = repository.getGradesForStudentSuspend(studentId)
            val studentAttendance = repository.getAttendanceForStudentSuspend(studentId)
            val studentBehaviors = repository.getBehaviorLogsForStudentSuspend(studentId)

            val apiReport = GeminiService.generateComprehensiveAcademicReport(
                student = student,
                courses = studentCourses,
                grades = studentGrades,
                attendances = studentAttendance,
                behaviors = studentBehaviors
            )

            _generatedReport.value = apiReport
            _isGeneratingReport.value = false

            // Save report instance in notification log too for history
            val reportLog = ParentNotification(
                studentId = studentId,
                title = "Annual/Monthly Performance Audit",
                message = apiReport,
                type = "Academic Report Summary"
            )
            repository.insertNotification(reportLog)
        }
    }

    // --- Seeder ---
    private suspend fun seedSampleData() {
        // Students
        val st1 = Student(name = "Liam Jefferson", gradeLevel = "Grade 10", studentEmail = "liam.j@gmail.com", matricNumber = "STU-101", parentName = "Robert Jefferson", parentEmail = "robert.jefferson@example.com", parentPhone = "+1 555-0192", photoColorHex = "#4F46E5")
        val st2 = Student(name = "Sophia Martinez", gradeLevel = "Grade 10", studentEmail = "sophia.m@gmail.com", matricNumber = "STU-102", parentName = "Gabriella Martinez", parentEmail = "g.martinez@example.com", parentPhone = "+1 555-0144", photoColorHex = "#EC4899")
        val st3 = Student(name = "Marcus Vance", gradeLevel = "Grade 11", studentEmail = "marcus.v@gmail.com", matricNumber = "STU-103", parentName = "Dianne Vance", parentEmail = "dianne.vance@example.com", parentPhone = "+1 555-0165", photoColorHex = "#10B981")
        val st4 = Student(name = "Chloe Dubois", gradeLevel = "Grade 9", studentEmail = "chloe.d@gmail.com", matricNumber = "STU-104", parentName = "Luc Dubois", parentEmail = "luc.dubois@example.com", parentPhone = "+1 555-0178", photoColorHex = "#F59E0B")

        val id1 = repository.insertStudent(st1)
        val id2 = repository.insertStudent(st2)
        val id3 = repository.insertStudent(st3)
        val id4 = repository.insertStudent(st4)

        // Courses
        val c1 = Course(name = "Mathematics AP", teacherName = "Ms. Grace Hopper", schedule = "Mon/Wed 9:00 AM")
        val c2 = Course(name = "Theoretical Physics", teacherName = "Dr. Richard Feynman", schedule = "Tue/Thu 11:00 AM")
        val c3 = Course(name = "English Literature", teacherName = "Mrs. Jane Austen", schedule = "Mon/Wed 1:00 PM")
        val c4 = Course(name = "Organic Chemistry", teacherName = "Dr. Marie Curie", schedule = "Fri 10:00 AM")

        val cid1 = repository.insertCourse(c1)
        val cid2 = repository.insertCourse(c2)
        val cid3 = repository.insertCourse(c3)
        val cid4 = repository.insertCourse(c4)

        // Course enrollments
        repository.insertEnrollment(CourseEnrollment(studentId = id1, courseId = cid1, dateEnrolled = "2026-05-10"))
        repository.insertEnrollment(CourseEnrollment(studentId = id1, courseId = cid2, dateEnrolled = "2026-05-10"))
        repository.insertEnrollment(CourseEnrollment(studentId = id2, courseId = cid1, dateEnrolled = "2026-05-11"))
        repository.insertEnrollment(CourseEnrollment(studentId = id2, courseId = cid3, dateEnrolled = "2026-05-11"))
        repository.insertEnrollment(CourseEnrollment(studentId = id3, courseId = cid2, dateEnrolled = "2026-05-12"))
        repository.insertEnrollment(CourseEnrollment(studentId = id3, courseId = cid4, dateEnrolled = "2026-05-12"))
        repository.insertEnrollment(CourseEnrollment(studentId = id4, courseId = cid1, dateEnrolled = "2026-05-13"))

        // Grades
        repository.insertGrade(Grade(studentId = id1, courseId = cid1, assessmentName = "Calculus Quiz 1", score = 94.0, maxScore = 100.0, date = "2026-05-12", semester = "Spring 2026"))
        repository.insertGrade(Grade(studentId = id1, courseId = cid2, assessmentName = "Mechanics midterm", score = 88.5, maxScore = 100.0, date = "2025-11-20", semester = "Fall 2025"))
        
        repository.insertGrade(Grade(studentId = id2, courseId = cid3, assessmentName = "Shakespeare Essay", score = 98.0, maxScore = 100.0, date = "2026-05-15", semester = "Spring 2026"))
        repository.insertGrade(Grade(studentId = id2, courseId = cid1, assessmentName = "Algebra Midterm", score = 76.0, maxScore = 100.0, date = "2025-11-18", semester = "Fall 2025"))

        repository.insertGrade(Grade(studentId = id3, courseId = cid4, assessmentName = "Reaction Pathways Quiz", score = 55.0, maxScore = 100.0, date = "2025-11-22", semester = "Fall 2025")) // Poor grade checks warnings AI
        repository.insertGrade(Grade(studentId = id3, courseId = cid2, assessmentName = "Laser physics lab", score = 91.0, maxScore = 100.0, date = "2026-05-24", semester = "Spring 2026"))

        // Attendance
        repository.insertAttendance(Attendance(studentId = id1, courseId = cid1, date = "2026-05-28", status = "Present", remarks = "", semester = "Spring 2026"))
        repository.insertAttendance(Attendance(studentId = id1, courseId = cid2, date = "2025-11-15", status = "Present", remarks = "", semester = "Fall 2025"))
        repository.insertAttendance(Attendance(studentId = id1, courseId = cid1, date = "2025-10-10", status = "Absent", remarks = "Sick leave", semester = "Fall 2025"))
        
        repository.insertAttendance(Attendance(studentId = id2, courseId = cid3, date = "2026-05-28", status = "Tardy", remarks = "Car broke down on commute", semester = "Spring 2026"))
        repository.insertAttendance(Attendance(studentId = id2, courseId = cid1, date = "2025-11-15", status = "Present", remarks = "", semester = "Fall 2025"))
        
        repository.insertAttendance(Attendance(studentId = id3, courseId = cid4, date = "2025-11-15", status = "Absent", remarks = "Medical appointment", semester = "Fall 2025"))
        repository.insertAttendance(Attendance(studentId = id3, courseId = cid2, date = "2026-05-28", status = "Present", remarks = "", semester = "Spring 2026"))
        repository.insertAttendance(Attendance(studentId = id4, courseId = cid1, date = "2026-05-28", status = "Present", remarks = "", semester = "Spring 2026"))

        // Behaviors
        repository.insertBehaviorLog(BehaviorLog(studentId = id1, category = "Positive", behaviorType = "Leadership", description = "Organized algebra study session for struggling classmates.", pointsDelta = 10, date = "2026-05-14"))
        repository.insertBehaviorLog(BehaviorLog(studentId = id2, category = "Positive", behaviorType = "Participation", description = "Extremely insightful contributions during poetry critique.", pointsDelta = 5, date = "2026-05-15"))
        repository.insertBehaviorLog(BehaviorLog(studentId = id3, category = "Negative", behaviorType = "Disruptive", description = "Repeated side conversations during lab instructions.", pointsDelta = -5, date = "2026-05-21"))
        repository.insertBehaviorLog(BehaviorLog(studentId = id4, category = "Positive", behaviorType = "Helpfulness", description = "Volunteered to clean up chemistry lab after hours.", pointsDelta = 5, date = "2026-05-25"))

        // Notifications
        repository.insertNotification(ParentNotification(
            studentId = id1,
            title = "Report: AI Behavior Alert - Liam Jefferson",
            message = "Dear Robert,\n\nWe would like to praise Liam for demonstrating exceptional team leadership in Math class today. He organized an extra algebra study circle to assist peers during lunch breaks. This level of supportive engagement is superb!\n\nWarm regards,\nSchool Academic Counseling Team",
            type = "Behavior Alert"
        ))
        
        repository.insertNotification(ParentNotification(
            studentId = id3,
            title = "Report: AI Grade Update - Marcus Vance",
            message = "Dear Dianne,\n\nThis is a quick counseling advisory regarding Marcus's Organic Chemistry performance. He recently scored 55/100 on his Reaction Pathways Quiz. We would like to collaborate on setting up remedial counseling sessions during home study.\n\nWarm regards,\nSchool Academic Counseling Team",
            type = "Grade Update"
        ))
    }
}
