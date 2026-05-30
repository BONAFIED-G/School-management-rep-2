package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdSuspend(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseByIdSuspend(id: Long): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Delete
    suspend fun deleteCourse(course: Course)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    suspend fun getAttendanceForStudentSuspend(studentId: Long): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAllGrades(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY date DESC")
    fun getGradesForStudent(studentId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY date DESC")
    suspend fun getGradesForStudentSuspend(studentId: Long): List<Grade>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade): Long

    @Delete
    suspend fun deleteGrade(grade: Grade)
}

@Dao
interface BehaviorLogDao {
    @Query("SELECT * FROM behavior_logs ORDER BY date DESC")
    fun getAllBehaviorLogs(): Flow<List<BehaviorLog>>

    @Query("SELECT * FROM behavior_logs WHERE studentId = :studentId ORDER BY date DESC")
    fun getBehaviorLogsForStudent(studentId: Long): Flow<List<BehaviorLog>>

    @Query("SELECT * FROM behavior_logs WHERE studentId = :studentId ORDER BY date DESC")
    suspend fun getBehaviorLogsForStudentSuspend(studentId: Long): List<BehaviorLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehaviorLog(log: BehaviorLog): Long

    @Delete
    suspend fun deleteBehaviorLog(log: BehaviorLog)
}

@Dao
interface ParentNotificationDao {
    @Query("SELECT * FROM parent_notifications ORDER BY sentAt DESC")
    fun getAllNotifications(): Flow<List<ParentNotification>>

    @Query("SELECT * FROM parent_notifications WHERE studentId = :studentId ORDER BY sentAt DESC")
    fun getNotificationsForStudent(studentId: Long): Flow<List<ParentNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ParentNotification): Long

    @Delete
    suspend fun deleteNotification(notification: ParentNotification)
}

@Dao
interface CourseEnrollmentDao {
    @Query("SELECT * FROM course_enrollments")
    fun getAllEnrollments(): Flow<List<CourseEnrollment>>

    @Query("SELECT * FROM course_enrollments WHERE studentId = :studentId")
    fun getEnrollmentsForStudent(studentId: Long): Flow<List<CourseEnrollment>>

    @Query("SELECT * FROM course_enrollments WHERE studentId = :studentId")
    suspend fun getEnrollmentsForStudentSuspend(studentId: Long): List<CourseEnrollment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: CourseEnrollment): Long

    @Query("DELETE FROM course_enrollments WHERE studentId = :studentId AND courseId = :courseId")
    suspend fun deleteEnrollment(studentId: Long, courseId: Long)
}

