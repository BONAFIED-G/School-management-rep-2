package com.example.data

import kotlinx.coroutines.flow.Flow

class SchoolRepository(db: AppDatabase) {
    val studentDao = db.studentDao()
    val courseDao = db.courseDao()
    val attendanceDao = db.attendanceDao()
    val gradeDao = db.gradeDao()
    val behaviorLogDao = db.behaviorLogDao()
    val parentNotificationDao = db.parentNotificationDao()
    val courseEnrollmentDao = db.courseEnrollmentDao()

    // Students
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)
    suspend fun getStudentByIdSuspend(id: Long): Student? = studentDao.getStudentByIdSuspend(id)
    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    // Course Enrollments
    val allEnrollments: Flow<List<CourseEnrollment>> = courseEnrollmentDao.getAllEnrollments()
    fun getEnrollmentsForStudent(studentId: Long): Flow<List<CourseEnrollment>> = courseEnrollmentDao.getEnrollmentsForStudent(studentId)
    suspend fun getEnrollmentsForStudentSuspend(studentId: Long) = courseEnrollmentDao.getEnrollmentsForStudentSuspend(studentId)
    suspend fun insertEnrollment(enrollment: CourseEnrollment): Long = courseEnrollmentDao.insertEnrollment(enrollment)
    suspend fun deleteEnrollment(studentId: Long, courseId: Long) = courseEnrollmentDao.deleteEnrollment(studentId, courseId)

    // Courses
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    suspend fun getCourseByIdSuspend(id: Long): Course? = courseDao.getCourseByIdSuspend(id)
    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)

    // Attendance
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>> = attendanceDao.getAttendanceForStudent(studentId)
    suspend fun getAttendanceForStudentSuspend(studentId: Long) = attendanceDao.getAttendanceForStudentSuspend(studentId)
    suspend fun insertAttendance(attendance: Attendance): Long = attendanceDao.insertAttendance(attendance)
    suspend fun deleteAttendance(attendance: Attendance) = attendanceDao.deleteAttendance(attendance)

    // Grades
    val allGrades: Flow<List<Grade>> = gradeDao.getAllGrades()
    fun getGradesForStudent(studentId: Long): Flow<List<Grade>> = gradeDao.getGradesForStudent(studentId)
    suspend fun getGradesForStudentSuspend(studentId: Long) = gradeDao.getGradesForStudentSuspend(studentId)
    suspend fun insertGrade(grade: Grade): Long = gradeDao.insertGrade(grade)
    suspend fun deleteGrade(grade: Grade) = gradeDao.deleteGrade(grade)

    // Behavior
    val allBehaviorLogs: Flow<List<BehaviorLog>> = behaviorLogDao.getAllBehaviorLogs()
    fun getBehaviorLogsForStudent(studentId: Long): Flow<List<BehaviorLog>> = behaviorLogDao.getBehaviorLogsForStudent(studentId)
    suspend fun getBehaviorLogsForStudentSuspend(studentId: Long) = behaviorLogDao.getBehaviorLogsForStudentSuspend(studentId)
    suspend fun insertBehaviorLog(log: BehaviorLog): Long = behaviorLogDao.insertBehaviorLog(log)
    suspend fun deleteBehaviorLog(log: BehaviorLog) = behaviorLogDao.deleteBehaviorLog(log)

    // Notifications
    val allNotifications: Flow<List<ParentNotification>> = parentNotificationDao.getAllNotifications()
    fun getNotificationsForStudent(studentId: Long): Flow<List<ParentNotification>> = parentNotificationDao.getNotificationsForStudent(studentId)
    suspend fun insertNotification(notification: ParentNotification): Long = parentNotificationDao.insertNotification(notification)
    suspend fun deleteNotification(notification: ParentNotification) = parentNotificationDao.deleteNotification(notification)
}
