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
    val profileId: String = "", // 10-digit unique Profile ID
    val aadharNumber: String = "", // 12-digit Aadhar card number
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
    val nriCountry: String = "",
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
    @field:DrawableRes val photoRes: Int = 0,
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
    val prefEducation: String = "",
    val prefOccupation: String = "",
    val prefCity: String = "",
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
    val blockedUserIds: List<String> = emptyList(),
    val isVipSubscribed: Boolean = false,
    val subscriptionPlan: String = "",
    val subscriptionTxnId: String = "",
    val subscriptionStartDate: String = "",
    val subscriptionStartTimestamp: Long = 0L,
    val subscriptionExpiryDate: String = "",
    val subscriptionExpiryTimestamp: Long = 0L,
    val isFreeSchemeUsed: Boolean = false
) {
    fun getEffectiveProfileImageUrl(): String {
        val cleanUrl = profileImageUrl.trim()
        if (cleanUrl.isNotBlank() && (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://"))) {
            return cleanUrl
        }
        return ""
    }
}
