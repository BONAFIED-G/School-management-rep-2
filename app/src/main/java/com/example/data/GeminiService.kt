package com.example.data

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Direct REST API using gemini-3.5-flash as default model per gemini-api skill instructions
    private const val MODEL = "gemini-3.5-flash"
    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder.")
            return@withContext "Error: Gemini API Key is missing. Please set your GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        try {
            val requestJson = JSONObject()
            
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstObj)
            }

            val configObj = JSONObject()
            configObj.put("temperature", 0.7)
            requestJson.put("generationConfig", configObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$ENDPOINT?key=$apiKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API Request failed: ${response.code} - $responseBody")
                    return@withContext "Error details: API Request failed with HTTP ${response.code}. Please ensure your API key is correct."
                }
                
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text content found.")
                        }
                    }
                }
                return@withContext "No response candidates returned from Gemini."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Call", e)
            return@withContext "Error calling Gemini: ${e.localizedMessage}"
        }
    }

    suspend fun draftAINotification(
        student: Student,
        courseName: String,
        type: String, // Attendance, Grade, Behavior
        details: String
    ): String {
        val systemPrompt = """
            You are an elegant Academic Counselor. 
            Format the response as a friendly email to the parent advising them of an event.
            Sign off as "School Academic Counseling Team".
        """.trimIndent()

        val prompt = """
            Draft an official, personalized Email notification to a parent.
            Student Name: ${student.name}
            Grade: ${student.gradeLevel}
            Parent Name: ${student.parentName}
            Course Involved: $courseName
            Category of Notification: $type
            Event/Record Particulars: $details

            Make it sound very narrative, professional, helpful, objective, and supportive. Ensure it stays concise (maximum 150 words). Do not include system text/subject blocks, just draft the direct email content starting with a polite greeting.
        """.trimIndent()

        return generateContent(prompt, systemPrompt)
    }

    suspend fun generateComprehensiveAcademicReport(
        student: Student,
        courses: List<Course>,
        grades: List<Grade>,
        attendances: List<Attendance>,
        behaviors: List<BehaviorLog>
    ): String {
        val systemPrompt = """
            You are a Senior Academic Advisor.
            Your role is to write a supportive, insightful, and motivating Academic Performance Report Card Review.
            Synthesize available records constructively. Use markdown sections properly.
        """.trimIndent()

        val courseMap = courses.associateBy { it.id }
        
        val gradesStr = if (grades.isEmpty()) "No grades recorded." else grades.joinToString("\n") { 
            val cName = courseMap[it.courseId]?.name ?: "General"
            "- Course: $cName | Assessment: ${it.assessmentName} -> Score: ${it.score}/${it.maxScore} (${it.date})"
        }
        val attendanceStr = if (attendances.isEmpty()) "Perfect Attendance." else attendances.joinToString("\n") {
            val cName = courseMap[it.courseId]?.name ?: "General"
            "- Course: $cName | Status: ${it.status} on ${it.date} (${it.remarks})"
        }
        val behaviorStr = if (behaviors.isEmpty()) "Excellent behavioral record (No specific incidents reported)." else behaviors.joinToString("\n") {
            "- ${it.category} Indicator: ${it.behaviorType} (${it.pointsDelta} points) - ${it.description} on ${it.date}"
        }

        val prompt = """
            Please analyze the records for:
            Student: ${student.name}
            Grade Level: ${student.gradeLevel}

            --- Enrolled Course Progress & Grades ---
            $gradesStr

            --- Student Attendance Records ---
            $attendanceStr

            --- Student Behavior Logs ---
            $behaviorStr

            Generate a highly structured report covering:
            1. **Executive Performance Narrative**: Brief summary of the student's status.
            2. **Academic Trends & Strengths**: Detailed view of specific accomplishments.
            3. **Growth Opportunities**: Advice for areas that need improvement.
            4. **Actionable Recommendations**: 3 creative, high-impact suggestions for the student and parent.
            
            Use a warm, motivating, clear, and professional language.
        """.trimIndent()

        return generateContent(prompt, systemPrompt)
    }
}
