package com.example.model

import androidx.annotation.DrawableRes

data class PartnerPreferences(
    val ageMin: Int = 21,
    val ageMax: Int = 32,
    val heightMin: String = "5'2\"",
    val heightMax: String = "6'2\"",
    val preferredSubCastes: List<String> = listOf("All Sub-castes", "Anjana Chaudhary", "Patel Chaudhary", "Desai Chaudhary"),
    val preferredOccupations: List<String> = emptyList(),
    val preferredCities: List<String> = emptyList(),
    val minMonthlyIncome: String = "Any",
    val motherVillagePreference: String = "",
    val excludedGotras: List<String> = emptyList()
)

data class KundliDetails(
    val rashi: String = "Vrishabha (Taurus)",
    val nakshatra: String = "Rohini",
    val manglikStatus: String = "Non-Manglik",
    val birthTime: String = "08:30 AM",
    val birthPlace: String = "Bhiwani, Haryana"
)

data class Profile(
    val id: String,
    val fullName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val grandfatherName: String = "",
    val gender: String = "Groom", // "Groom" or "Bride"
    val age: Int = 0,
    val birthDate: String = "", // e.g. "15/08/1998"
    val height: String = "",
    val weight: String = "65 kg",
    val bloodGroup: String = "",
    val isNri: Boolean = false,
    val hasMaritalHistory: Boolean = false,
    val subCaste: String = "", // Jat Chaudhary, Kurmi Chaudhary, Anjana Chaudhary, Patel, etc.
    val gotra: String = "", // Self Gotra
    val motherGotra: String = "", // Mother's Gotra
    val locality: String = "", // Region: Haryana, Rajasthan, Western UP, Gujarat, Delhi NCR
    val nativeVillage: String = "", // Mool Gaon
    val motherBirthVillage: String = "", // Nanihal / Nanake Gaon
    val education: String = "",
    val occupation: String = "",
    val currentCity: String = "",
    val monthlyIncome: String = "",
    val maritalStatus: String = "Never Married",
    val aboutMe: String = "",
    val familyDetails: String = "",
    val isAadharVerified: Boolean = false,
    val aadharMasked: String = "XXXX-XXXX-4819",
    @DrawableRes val photoRes: Int = 0,
    val isShortlisted: Boolean = false,
    val interestStatus: String = "NONE", // NONE, SENT, ACCEPTED
    val phoneContact: String = "",
    val parentPhoneContact: String = "",
    val profileImageUrl: String = "",
    val aadharFrontUrl: String = "",
    val aadharBackUrl: String = "",
    val rashi: String = "Vrishabha (Taurus)",
    val nakshatra: String = "Rohini",
    val manglikStatus: String = "Non-Manglik",
    val birthTime: String = "08:30 AM",
    val birthPlace: String = "Bhiwani, Haryana",
    val prefAgeMin: Int = 21,
    val prefAgeMax: Int = 30,
    val prefHeightMin: String = "5'2\"",
    val prefHeightMax: String = "6'0\"",
    val prefMinIncome: String = "₹50,000 / month",
    val isApproved: Boolean = false,
    val isRejected: Boolean = false,
    val rejectionReason: String = "",
    val fatherOccupation: String = "",
    val motherOccupation: String = "",
    val numBrothers: Int = 0,
    val brothersNames: String = "",
    val numSisters: Int = 0,
    val sistersNames: String = "",
    val gol: String = "",
    val hobbies: String = "",
    val lastDeviceId: String = "",
    val lastLoginTimestamp: Long = 0L,
    val lastDeviceName: String = "",
    val blockedUserIds: List<String> = emptyList()
) {
    fun getEffectiveProfileImageUrl(): String {
        if (profileImageUrl.isNotBlank()) return profileImageUrl
        if (photoRes != 0) return ""
        val idHash = kotlin.math.abs((id + fullName).hashCode())
        val isBride = gender.equals("Bride", ignoreCase = true) || gender.equals("કન્યા", ignoreCase = true) || gender.contains("Female", ignoreCase = true) || gender.contains("Woman", ignoreCase = true)
        val femaleAvatars = listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=600&auto=format&fit=crop"
        )
        val maleAvatars = listOf(
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=600&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=600&auto=format&fit=crop"
        )
        return if (isBride) {
            femaleAvatars[idHash % femaleAvatars.size]
        } else {
            maleAvatars[idHash % maleAvatars.size]
        }
    }
}
