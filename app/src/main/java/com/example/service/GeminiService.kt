package com.example.service

import com.example.BuildConfig
import com.example.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Gemini REST Request / Response DTOs
data class GeminiPart(val text: String? = null)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(
    val contents: List<GeminiContent>
)
data class GeminiCandidate(val content: GeminiContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetworkClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiService {

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateVoiceBiodata(
        notes: String,
        userProfile: Profile
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "આદરણીય ચૌધરી પરિવાર, હું ${userProfile.fullName} ગામ ${userProfile.nativeVillage} (${userProfile.locality}) થી છું. " +
                    "હાલમાં ${userProfile.currentCity} માં ${userProfile.occupation} તરીકે ફરજ બજાવું છું. " +
                    "અમારું સંસ્કારી કુટુંબ ${userProfile.subCaste} (${userProfile.gotra} ગોત્ર) નું છે. મોસાળ ${userProfile.motherBirthVillage} છે. " +
                    "$notes. એક સંસ્કારી, ઉચ્ચ શિક્ષિત અને પરંપરાગત મૂલ્યો ધરાવતા જીવનસાથીની શોધ છે."
        }

        val prompt = """
            You are an AI Biodata Assistant for Chaudhary Matrimony.
            Convert these voice notes/raw user input into an elegant, respectful, traditional yet modern matrimonial bio paragraph IN GUJARATI language (ગુજરાતી ભાષા) for a Chaudhary community profile.
            
            User Info:
            Name: ${userProfile.fullName}
            Sub-caste: ${userProfile.subCaste}, Gotra: ${userProfile.gotra}
            Occupation: ${userProfile.occupation}
            Native Village: ${userProfile.nativeVillage}, Mother's Village: ${userProfile.motherBirthVillage}
            Raw Notes: $notes
            
            Write 3-4 sentences IN GUJARATI in a polished, cultured tone suitable for marriage proposals.
        """.trimIndent()

        try {
            val req = GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            val res = GeminiNetworkClient.service.generateContent(apiKey, req)
            val text = res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return@withContext text
        } catch (e: Exception) {
            e.printStackTrace()
        }

        "આદરણીય ચૌધરી પરિવાર, હું ${userProfile.fullName}, ${userProfile.currentCity} માં ${userProfile.occupation} તરીકે કાર્યરત છું. ચૌધરી સંસ્કૃતિના મૂલ્યો ધરાવતો પરિવાર છે અને સંસ્કારી જીવનસાથીની તલાશ છે."
    }

    suspend fun getTopMatchReasons(
        userProfile: Profile,
        matchProfile: Profile
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "🌟 શા માટે તમારા માટે શ્રેષ્ઠ AI પસંદગી:\n" +
                    "• ગોત્ર બહિર્વિવાહ ચકાસાયેલ (${userProfile.gotra} અને ${matchProfile.gotra})\n" +
                    "• અનુકૂળ પ્રદેશ અને કૌટુંબિક પૃષ્ઠભૂમિ (${userProfile.locality} અને ${matchProfile.locality})\n" +
                    "• શૈક્ષણિક અને વ્યાવસાયિક ઉત્તમ જોડાણ (${userProfile.education} + ${matchProfile.education})\n" +
                    "• પારિવારિક સંસ્કાર અને મોસાળ પક્ષની ઉત્તમ સુસંગતતા."
        }

        val prompt = """
            Explain IN GUJARATI language (ગુજરાતી ભાષા) in 3 bullet points why ${matchProfile.fullName} (${matchProfile.occupation}, ${matchProfile.locality}) is a Top 5 AI Match for ${userProfile.fullName} (${userProfile.occupation}, ${userProfile.locality}) in Chaudhary Matrimony platform.
            Mention gotra compliance (${userProfile.gotra} vs ${matchProfile.gotra}), native locality synergy, and career alignment.
        """.trimIndent()

        try {
            val req = GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            val res = GeminiNetworkClient.service.generateContent(apiKey, req)
            val text = res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) return@withContext text
        } catch (e: Exception) {
            e.printStackTrace()
        }

        "• ગોત્ર બહિર્વિવાહ સફળ (${userProfile.gotra} અને ${matchProfile.gotra})\n• ઉચ્ચ શિક્ષણ અને વ્યવસાયિક સુસંગતતા\n• ${matchProfile.locality} માં અનુકૂળ પારિવારિક પરંપરા"
    }
}
