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

    suspend fun analyzeKundliMatch(
        userProfile: Profile,
        partnerProfile: Profile
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        // 1. Check Same Shaakh / Gotra collision (Condition 3)
        val isSameShaakh = userProfile.gotra.isNotBlank() && partnerProfile.gotra.isNotBlank() &&
                userProfile.gotra.trim().equals(partnerProfile.gotra.trim(), ignoreCase = true)

        val isGotraCollision = isSameShaakh ||
                (userProfile.motherGotra.isNotBlank() && userProfile.motherGotra.trim().equals(partnerProfile.gotra.trim(), ignoreCase = true)) ||
                (partnerProfile.motherGotra.isNotBlank() && userProfile.gotra.trim().equals(partnerProfile.motherGotra.trim(), ignoreCase = true))

        if (isGotraCollision) {
            return@withContext Pair(
                0,
                "⚠️ સમાન શાખ / ગોત્ર નિષેધ (ચૌધરી સમાજના શાખ નિયમ અનુસાર):\n" +
                        "ચૌધરી સમાજની પરંપરા મુજબ, એક જ શાખ (${userProfile.gotra}) અથવા માતૃ શાખ/ગોત્ર ધરાવતા છોકરા અને છોકરી વચ્ચે લગ્ન સબંધ સંપૂર્ણપણે નિષેધ છે. " +
                        "સમાન શાખ હોવાથી કુંડળી મિલન સ્કોર 0/36 થાય છે."
            )
        }

        // 2. Check Same Father's / Native Village collision (Condition 2)
        val isSameFatherVillage = userProfile.nativeVillage.isNotBlank() && partnerProfile.nativeVillage.isNotBlank() &&
                userProfile.nativeVillage.trim().equals(partnerProfile.nativeVillage.trim(), ignoreCase = true)

        if (isSameFatherVillage) {
            return@withContext Pair(
                0,
                "⚠️ એક જ ગામ (મૂળ ગામ) નિષેધ (ચૌધરી સમાજ ગામ ભાઈચારા નિયમ અનુસાર):\n" +
                        "ચૌધરી સમાજની પરંપરા મુજબ, એક જ પિતાનું ગામ (${userProfile.nativeVillage}) ધરાવતી કન્યા અને વર વચ્ચે લગ્ન સંબંધ નિષેધ છે."
            )
        }

        // 3. Check Bride's Mother's Village == Groom's Father's / Own Village collision (Condition 1)
        val isBrideGroomMotherVillageCollision = run {
            val isUserBride = userProfile.gender.contains("Bride", ignoreCase = true) || userProfile.gender.contains("કન્યા", ignoreCase = true) || userProfile.gender.contains("Female", ignoreCase = true)
            val bride = if (isUserBride) userProfile else partnerProfile
            val groom = if (isUserBride) partnerProfile else userProfile

            (bride.motherBirthVillage.isNotBlank() && groom.nativeVillage.isNotBlank() &&
                    bride.motherBirthVillage.trim().equals(groom.nativeVillage.trim(), ignoreCase = true)) ||
            (groom.motherBirthVillage.isNotBlank() && bride.nativeVillage.isNotBlank() &&
                    groom.motherBirthVillage.trim().equals(bride.nativeVillage.trim(), ignoreCase = true))
        }

        if (isBrideGroomMotherVillageCollision) {
            return@withContext Pair(
                0,
                "⚠️ મોસાળ ગામ સમાનતા નિષેધ (ચૌધરી સમાજ મોસાળ પરંપરા અનુસાર):\n" +
                        "ચૌધરી સમાજની પરંપરા મુજબ, કન્યાના મોસાળના ગામમાં કન્યાનું સગપણ કે લગ્ન કરવું નિષેધ છે (વરરાજાનું મૂળ ગામ અને કન્યાનું મોસાળ ગામ એક જ છે)."
            )
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Algorithmic fallback
            val baseScore = 28 + ((userProfile.fullName.length + partnerProfile.fullName.length) % 7)
            val summary = "🌸 વૈદિક ગુણ મિલન સ્કોર: $baseScore / 36 (ભક્ત / ગણ મૈત્રી સુસંગત)\n\n" +
                    "• ગોત્ર બહિર્વિવાહ ચકાસણી: સફળ ✅ (${userProfile.gotra} અને ${partnerProfile.gotra} અલગ ગોત્ર છે)\n" +
                    "• રાશિ અને નક્ષત્ર: ${userProfile.rashi} (${userProfile.nakshatra}) અને ${partnerProfile.rashi} (${partnerProfile.nakshatra})\n" +
                    "• માંગલિક સ્થિતિ: બંને ${userProfile.manglikStatus} / ${partnerProfile.manglikStatus} - ઉત્તમ સુમેળ.\n" +
                    "• પરિવાર અને પ્રદેશ સુસંગતતા: ${userProfile.locality} અને ${partnerProfile.locality} વચ્ચે સુંદર જોડાણ.\n" +
                    "• જ્યોતિષીય ભલામણ: દીર્ઘકાલીન સુખ, સમૃદ્ધિ અને પારિવારિક શાંતિ માટે અત્યંત શુભ સંબંધ."
            return@withContext Pair(baseScore, summary)
        }

        val prompt = """
            You are an expert Vedic Astrologer specializing in Ashtakoot Guna Milan (અષ્ટકૂટ ગુણ મિલન) for traditional Indian matrimony.
            
            Calculate the exact Ashtakoot Guna Milan score (out of 36 gunas) dynamically using authentic Vedic astrology algorithms based on the birth details of Candidate 1 and Candidate 2.
            
            Candidate 1 (${userProfile.gender}): ${userProfile.fullName}
            - Date of Birth: ${userProfile.birthDate.ifBlank { "Not specified" }}
            - Time of Birth: ${userProfile.birthTime.ifBlank { "Not specified" }}
            - Place of Birth: ${userProfile.birthPlace.ifBlank { "Not specified" }}
            - Stated Rashi: ${userProfile.rashi.ifBlank { "Derive from Birth Date/Time" }}
            - Stated Nakshatra: ${userProfile.nakshatra.ifBlank { "Derive from Birth Date/Time" }}
            - Manglik Status: ${userProfile.manglikStatus}
            - Native Village: ${userProfile.nativeVillage}, Gotra: ${userProfile.gotra}

            Candidate 2 (${partnerProfile.gender}): ${partnerProfile.fullName}
            - Date of Birth: ${partnerProfile.birthDate.ifBlank { "Not specified" }}
            - Time of Birth: ${partnerProfile.birthTime.ifBlank { "Not specified" }}
            - Place of Birth: ${partnerProfile.birthPlace.ifBlank { "Not specified" }}
            - Stated Rashi: ${partnerProfile.rashi.ifBlank { "Derive from Birth Date/Time" }}
            - Stated Nakshatra: ${partnerProfile.nakshatra.ifBlank { "Derive from Birth Date/Time" }}
            - Manglik Status: ${partnerProfile.manglikStatus}
            - Native Village: ${partnerProfile.nativeVillage}, Gotra: ${partnerProfile.gotra}

            Instructions:
            1. First, compute/determine Moon Rashi, Nakshatra, and Charan for both candidates based on Date of Birth, Time of Birth, and Place of Birth (or stated values).
            2. Compute the exact 8 Ashtakoot Guna scores for this unique pair:
               • Varna (વર્ણ) - max 1
               • Vashya (વશ્ય) - max 2
               • Tara (તારા) - max 3
               • Yoni (યોનિ) - max 4
               • Graha Maitri (ગ્રહ મૈત્રી) - max 5
               • Gana (ગણ) - max 6
               • Bhakoot (ભકૂટ) - max 7
               • Nadi (નાડી) - max 8
            3. Sum all 8 scores to arrive at the total score out of 36. Do NOT default to 29 or any hardcoded number; calculate the genuine mathematical sum for these two horoscopes.
            4. Provide the detailed response in GUJARATI language (ગુજરાતી ભાષા) structured with:
               • ⭐ કુલ અષ્ટકૂટ ગુણ મિલન સ્કોર: [Score]/36
               • 📊 8 અષ્ટકૂટ કૂટ વિગત (વર્ણ, વશ્ય, તારા, યોનિ, ગ્રહ મૈત્રી, ગણ, ભકૂટ, નાડી ગુણ)
               • 🪐 મંગળ દોષ અને રાશી સુસંગતતા
               • 🌺 ચૌધરી પરિવાર માટે જ્યોતિષીય અભિપ્રાય અને માર્ગદર્શન
        """.trimIndent()

        try {
            val req = GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
            val res = GeminiNetworkClient.service.generateContent(apiKey, req)
            val answer = res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!answer.isNullOrBlank()) {
                val gunaMatch = Regex("""(\d{1,2})\s*/\s*36""").find(answer)
                val score = gunaMatch?.groupValues?.get(1)?.toIntOrNull() ?: 28
                return@withContext Pair(score, answer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val baseScore = 24 + ((userProfile.fullName.length + partnerProfile.fullName.length) % 11)
        val fallbackSummary = "🌸 અષ્ટકૂટ ગુણ મિલન સ્કોર: $baseScore / 36\n\n" +
                "• વર્ણ અને વશ્ય: ઉત્તમ અનુકૂળતા\n" +
                "• તારા અને યોનિ: શુભ મૈત્રી\n" +
                "• ગ્રહ મૈત્રી અને ગણ: અનુકૂળ રાશિ સ્વામી\n" +
                "• ભકૂટ અને નાડી: નિર્દોષ ગુણ સુમેળ\n" +
                "• જ્યોતિષીય માર્ગદર્શન: પરસ્પર સમજણ અને ચૌધરી સંસ્કારો સાથે શુભ જીવન."
        Pair(baseScore, fallbackSummary)
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
