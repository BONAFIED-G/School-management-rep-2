package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gradeLevel: String,
    val studentEmail: String, // Dynamic login credential matching the registered Gmail
    val matricNumber: String,  // Unique student matric, also dynamic login credential
    val parentName: String,
    val parentEmail: String,
    val parentPhone: String,
    val photoColorHex: String // e.g. "#FF4F81" for beautiful custom M3 colored profile cards
)

@Entity(tableName = "course_enrollments")
data class CourseEnrollment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val dateEnrolled: String
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val teacherName: String,
    val schedule: String
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Tardy"
    val remarks: String = "",
    val semester: String = "Spring 2026"
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val assessmentName: String, // e.g. "Quiz 1", "Midterm"
    val score: Double,
    val maxScore: Double = 100.0,
    val date: String, // YYYY-MM-DD
    val semester: String = "Spring 2026"
)

@Entity(tableName = "behavior_logs")
data class BehaviorLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val category: String, // "Positive" or "Negative"
    val behaviorType: String, // e.g. "Participation", "Leadership", "Disruptive", "Late Assignment"
    val description: String,
    val pointsDelta: Int, // e.g. +5 or -5
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "parent_notifications")
data class ParentNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val message: String,
    val type: String, // "Attendance Alert", "Grade Update", "Behavior Alert", "Academic Summary"
    val sentAt: Long = System.currentTimeMillis(),
    val status: String = "Sent" // "Sent", "Draft"
)
