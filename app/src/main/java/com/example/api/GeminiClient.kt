package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Request & Response Data Models ---

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class SystemInstruction(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerationConfig(val temperature: Float = 0.7f)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction,
    val generationConfig: GenerationConfig = GenerationConfig()
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<Candidate>?)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    /**
     * Helper function to execute AI Travel Planner prompt
     */
    suspend fun planTrip(userPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is invalid or empty in BuildConfig!")
            return "ไม่สามารถเชี่อมต่อ AI ได้เนื่องจากไม่ได้ตั้งค่า API Key กรุณาตรวจสอบแท็บ Secrets ใน AI Studio"
        }

        val systemPrompt = """
            คุณคือ "ผู้เชี่ยวชาญด้านการท่องเที่ยวและสไตล์ท้องถิ่นประจำตำบลหนองลู อำเภอสังขละบุรี จังหวัดกาญจนบุรี" บุคลิกภาพอบอุ่น เป็นกันเอง สุภาพ มีความรอบรู้ในพื้นที่อย่างลึกซึ้ง
            
            ขอบเขตความรับผิดชอบของคุณ:
            1. ช่วยนักท่องเที่ยววางแผนทริปท่องเที่ยว แนะนำที่จัดทริป 2 วัน 1 คืน หรือ 3 วัน 2 คืน ในเขตหนองลู (สะพานมอญ, เจดีย์พุทธคยา, วัดใต้น้ำสามประสบ, สตรีทฟู้ดฝั่งมอญ, ตลาดเช้า)
            2. แนะนำที่พัก แนะนำร้านอาหารท้องถิ่น เช่น แกงฮังเลมอญ ขนมจีนหยวกกล้วย หมูจุ่มพม่า ชาพม่า ยอดมะพร้าวอ่อน
            3. ให้คำปรึกษาด้านการแต่งกายตักบาตรมอญ วิธีปฏิบัติ มารยาท วัฒนธรรม
            4. ให้คำแนะนำเรื่องเส้นทาง ทางหลวงหมายเลข 323 โค้งชัน จุดจอดรถ จุดเติมน้ำมัน และการขับขี่รถอย่างปลอดภัยในสายหมอก
            
            เกณฑ์การตอบกลับ:
            - ตอบกลับเป็นภาษาไทยที่สุภาพ อ่านง่ายเป็นกันเอง ย่อหน้าเป็นระเบียบ และมีหัวข้อที่ชัดเจน (ใช้ Markdown ในการจัดระเบียบและทำหัวข้อตัวหนา)
            - หลีกเลี่ยงคำตอบที่สั้นเกินไป พยายามลงรายละเอียดที่เป็นประโยชน์เฉพาะเจาะจงให้เห็นภาพชัดเจนที่สุด
            - แนะนำให้นักท่องเที่ยวไปกราบสักการะสังขีระรูปและตามรอยหลวงพ่ออุตตมะเพื่อความเป็นสิริมงคล
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userPrompt)))
            ),
            systemInstruction = SystemInstruction(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "ขออภัย พลังงานสมงองของ AI ขัดข้องชั่วคราว ลองส่งข้อความใหม่อีกครั้งครับ"
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            "ข้อผิดพลาดในการเชื่อมต่อ: ${e.localizedMessage ?: "กรุณาลองใหม่อีกครั้ง"}"
        }
    }
}
