package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import java.util.Calendar
import com.example.data.IndiaLocationData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val myProfile by viewModel.myProfile.collectAsState()
    val generatedVoiceBio by viewModel.generatedVoiceBio.collectAsState()
    val isGeneratingBio by viewModel.isGeneratingVoiceBio.collectAsState()

    var showApprovalPendingDialog by remember {
        mutableStateOf(myProfile.fullName.isNotBlank() && !myProfile.isApproved && !viewModel.isAdmin.value)
    }

    var currentStep by remember { mutableIntStateOf(1) } // Steps 1 to 4

    // Bio-data Lock & Edit Mode
    var isEditing by remember { mutableStateOf(!myProfile.fullName.isNotBlank()) }

    // Step 1: Registration Credentials, Personal Details & Birth Details for Kundli
    var regEmail by remember { mutableStateOf(myProfile.phoneContact.ifBlank { myProfile.parentPhoneContact }) }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf(myProfile.fullName) }
    var fatherName by remember { mutableStateOf(myProfile.fatherName) }
    var fatherOccupation by remember { mutableStateOf(myProfile.fatherOccupation) }
    var motherName by remember { mutableStateOf(myProfile.motherName) }
    var motherOccupation by remember { mutableStateOf(myProfile.motherOccupation) }
    var grandfatherName by remember { mutableStateOf(myProfile.grandfatherName) }
    var numBrothers by remember { mutableIntStateOf(myProfile.numBrothers) }
    var brothersNames by remember { mutableStateOf(myProfile.brothersNames) }
    var numSisters by remember { mutableIntStateOf(myProfile.numSisters) }
    var sistersNames by remember { mutableStateOf(myProfile.sistersNames) }
    var gender by remember { mutableStateOf(if (myProfile.gender.isNotBlank()) myProfile.gender else "Groom") }
    var age by remember { mutableStateOf(if (myProfile.age > 0) myProfile.age.toString() else "25") }
    var birthDate by remember { mutableStateOf(if (myProfile.birthDate.isNotBlank()) myProfile.birthDate else "15/08/1998") }
    var birthTime by remember { mutableStateOf(if (myProfile.birthTime.isNotBlank()) myProfile.birthTime else "08:30 AM") }
    var birthState by remember { mutableStateOf("Gujarat") }
    var birthDistrict by remember { mutableStateOf("Banaskantha") }
    var birthSubDistrict by remember { mutableStateOf("Palanpur") }
    var birthPlace by remember { mutableStateOf(if (myProfile.birthPlace.isNotBlank()) myProfile.birthPlace else "Palanpur, Banaskantha, Gujarat") }
    var height by remember { mutableStateOf(myProfile.height) }
    var currentCity by remember { mutableStateOf(myProfile.currentCity) }
    var aadharNumber by remember { mutableStateOf(myProfile.aadharNumber) }
    var phoneContact by remember { mutableStateOf(myProfile.phoneContact) }
    var parentPhoneContact by remember { mutableStateOf(myProfile.parentPhoneContact) }
    var isMobileDuplicate by remember { mutableStateOf(false) }
    var isAadharDuplicate by remember { mutableStateOf(false) }
    var bloodGroup by remember { mutableStateOf(if (myProfile.bloodGroup.isNotBlank()) myProfile.bloodGroup else "A+") }
    var isNri by remember { mutableStateOf(myProfile.isNri) }
    var nriCountry by remember { mutableStateOf(if (myProfile.nriCountry.isNotBlank()) myProfile.nriCountry else "United States (USA)") }
    var maritalStatus by remember { mutableStateOf(if (myProfile.maritalStatus.isNotBlank()) myProfile.maritalStatus else "Unmarried") }
    var maritalStatusOther by remember { mutableStateOf("") }

    // Step 2: Community & Gotra / Village Exogamy & Gol
    var subCaste by remember { mutableStateOf(if (myProfile.subCaste.isNotBlank()) myProfile.subCaste else "આંજણા ચૌધરી") }
    var gol by remember { mutableStateOf(myProfile.gol) }
    var gotra by remember { mutableStateOf(myProfile.gotra) }
    var motherGotra by remember { mutableStateOf(myProfile.motherGotra) }
    var nativeVillage by remember { mutableStateOf(myProfile.nativeVillage) }
    var motherBirthVillage by remember { mutableStateOf(myProfile.motherBirthVillage) }
    var locality by remember { mutableStateOf(if (myProfile.locality.isNotBlank()) myProfile.locality else "Gujarat") }

    // Step 3: Career & Income & Hobbies
    var education by remember { mutableStateOf(myProfile.education) }
    var occupation by remember { mutableStateOf(myProfile.occupation) }
    var monthlyIncome by remember { mutableStateOf(myProfile.monthlyIncome) }
    var hobbies by remember { mutableStateOf(myProfile.hobbies) }

    // Step 4: Family & Bio / Horoscope & Partner Preferences
    var familyDetails by remember { mutableStateOf(myProfile.familyDetails) }
    var aboutMe by remember { mutableStateOf(myProfile.aboutMe) }
    var voiceNotesInput by remember { mutableStateOf("") }

    // Expected Matrimonial Partner Preferences (ઈચ્છિત પાત્ર અપેક્ષા)
    var prefAgeMin by remember { mutableStateOf(if (myProfile.prefAgeMin > 0) myProfile.prefAgeMin.toString() else "21") }
    var prefAgeMax by remember { mutableStateOf(if (myProfile.prefAgeMax > 0) myProfile.prefAgeMax.toString() else "32") }
    var prefHeightMin by remember { mutableStateOf(if (myProfile.prefHeightMin.isNotBlank()) myProfile.prefHeightMin else "5'2\"") }
    var prefHeightMax by remember { mutableStateOf(if (myProfile.prefHeightMax.isNotBlank()) myProfile.prefHeightMax else "6'2\"") }
    var prefMinIncome by remember { mutableStateOf(if (myProfile.prefMinIncome.isNotBlank()) myProfile.prefMinIncome else "₹25,000+ / મહિનો") }
    var prefEducation by remember { mutableStateOf(if (myProfile.prefEducation.isNotBlank()) myProfile.prefEducation else "કોઈપણ / સ્નાતક (Graduate)") }
    var prefOccupation by remember { mutableStateOf(if (myProfile.prefOccupation.isNotBlank()) myProfile.prefOccupation else "નોકરી / વ્યવસાય (Job/Business)") }
    var prefCity by remember { mutableStateOf(if (myProfile.prefCity.isNotBlank()) myProfile.prefCity else "ગુજરાત / કોઈપણ શહેર") }
    var rashi by remember { mutableStateOf(if (myProfile.rashi.isNotBlank()) myProfile.rashi else "Mesha") }
    var manglikStatus by remember { mutableStateOf(if (myProfile.manglikStatus.isNotBlank()) myProfile.manglikStatus else "Non-Manglik") }

    var isSaving by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    var agreeTerms by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeDisclaimer by remember { mutableStateOf(false) }
    var agreeRefund by remember { mutableStateOf(false) }
    var showPolicyDetailDialogIndex by remember { mutableStateOf<Int?>(null) }

    var showStep1Errors by remember { mutableStateOf(false) }
    var showStep2Errors by remember { mutableStateOf(false) }
    var showStep3Errors by remember { mutableStateOf(false) }
    var showStep4Errors by remember { mutableStateOf(false) }

    // Upload & Progress States for Step 1
    var isUploadingProfile by remember { mutableStateOf(false) }
    var profileUploadProgress by remember { mutableIntStateOf(0) }
    var isUploadingFront by remember { mutableStateOf(false) }
    var frontUploadProgress by remember { mutableIntStateOf(0) }
    var isUploadingBack by remember { mutableStateOf(false) }
    var backUploadProgress by remember { mutableIntStateOf(0) }

    val startUploadProfile: (Uri) -> Unit = { uri ->
        isUploadingProfile = true
        profileUploadProgress = 0
        viewModel.uploadProfileImage(
            uri = uri,
            onProgress = { pct -> profileUploadProgress = pct },
            onResult = { uploadedUrl ->
                isUploadingProfile = false
                if (uploadedUrl != null) {
                    profileUploadProgress = 100
                    Toast.makeText(context, "પ્રોફાઇલ તસવીર ૧૦૦% સફળતાપૂર્વક અપલોડ થઈ ગઈ!", Toast.LENGTH_SHORT).show()
                } else {
                    profileUploadProgress = 0
                    Toast.makeText(context, "તસવીર અપલોડ નિષ્ફળ! કૃપા કરીને ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    val startUploadFront: (Uri) -> Unit = { uri ->
        isUploadingFront = true
        frontUploadProgress = 0
        viewModel.uploadAadharFrontImage(
            uri = uri,
            onProgress = { pct -> frontUploadProgress = pct },
            onResult = { uploadedUrl ->
                isUploadingFront = false
                if (uploadedUrl != null) {
                    frontUploadProgress = 100
                    Toast.makeText(context, "આધાર ફ્રન્ટ ફોટો ૧૦૦% સફળતાપૂર્વક અપલોડ થયો!", Toast.LENGTH_SHORT).show()
                } else {
                    frontUploadProgress = 0
                    Toast.makeText(context, "આધાર ફ્રન્ટ અપલોડ નિષ્ફળ! ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    val startUploadBack: (Uri) -> Unit = { uri ->
        isUploadingBack = true
        backUploadProgress = 0
        viewModel.uploadAadharBackImage(
            uri = uri,
            onProgress = { pct -> backUploadProgress = pct },
            onResult = { uploadedUrl ->
                isUploadingBack = false
                if (uploadedUrl != null) {
                    backUploadProgress = 100
                    Toast.makeText(context, "આધાર બેક ફોટો ૧૦૦% સફળતાપૂર્વક અપલોડ થયો!", Toast.LENGTH_SHORT).show()
                } else {
                    backUploadProgress = 0
                    Toast.makeText(context, "આધાર બેક અપલોડ નિષ્ફળ! ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Profile Edit Mode OTP Verification States
    var editOtpSent by remember { mutableStateOf(false) }
    var editVerificationId by remember { mutableStateOf("") }
    var editResendToken by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }
    var editEnteredOtp by remember { mutableStateOf("") }
    var isEditOtpVerified by remember { mutableStateOf(false) }
    var isSendingEditOtp by remember { mutableStateOf(false) }
    var isVerifyingEditOtp by remember { mutableStateOf(false) }
    var editTimerSeconds by remember { mutableIntStateOf(60) }
    var editOtpError by remember { mutableStateOf("") }

    LaunchedEffect(editOtpSent, editTimerSeconds) {
        if (editOtpSent && !isEditOtpVerified && editTimerSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            editTimerSeconds -= 1
        }
    }

    // Draft save & sign-out helper
    val performLogout = {
        if (fullName.isNotBlank() || fatherName.isNotBlank() || parentPhoneContact.isNotBlank() || regEmail.isNotBlank()) {
            val finalMaritalStatus = if (maritalStatus == "Other") {
                if (maritalStatusOther.isNotBlank()) "Other: $maritalStatusOther" else "Other"
            } else {
                maritalStatus
            }
            val draftProfile = myProfile.copy(
                fullName = fullName,
                fatherName = fatherName,
                fatherOccupation = fatherOccupation,
                grandfatherName = grandfatherName,
                motherName = motherName,
                motherOccupation = motherOccupation,
                numBrothers = numBrothers,
                brothersNames = brothersNames,
                numSisters = numSisters,
                sistersNames = sistersNames,
                gender = gender,
                age = age.toIntOrNull() ?: myProfile.age,
                birthDate = birthDate,
                birthTime = birthTime,
                birthPlace = birthPlace,
                height = height,
                currentCity = currentCity,
                phoneContact = regEmail.ifBlank { parentPhoneContact },
                parentPhoneContact = parentPhoneContact,
                bloodGroup = bloodGroup,
                isNri = isNri,
                nriCountry = if (isNri) nriCountry else "",
                hasMaritalHistory = (maritalStatus != "Unmarried"),
                maritalStatus = finalMaritalStatus,
                subCaste = subCaste,
                gol = gol,
                gotra = gotra,
                motherGotra = motherGotra,
                nativeVillage = nativeVillage,
                motherBirthVillage = motherBirthVillage,
                locality = locality,
                education = education,
                occupation = occupation,
                monthlyIncome = monthlyIncome,
                hobbies = hobbies,
                familyDetails = familyDetails,
                aboutMe = aboutMe,
                rashi = rashi,
                manglikStatus = manglikStatus
            )
            viewModel.saveDraftLocally(draftProfile)
        }
        viewModel.logout()
        if (onLogoutClick != null) {
            onLogoutClick()
        } else {
            onBackClick()
        }
    }

    if (!isEditing && myProfile.fullName.isNotBlank()) {
        LockedBioDataView(
            profile = myProfile,
            viewModel = viewModel,
            onEditClick = { isEditing = true },
            onBackClick = onBackClick,
            onLogoutClick = { performLogout() },
            onRefreshClick = {
                viewModel.refreshDashboard()
                Toast.makeText(context, "મંજૂરી સ્થિતિ તાજી કરી રહ્યા છીએ... (Refreshing Status)", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // Synchronize form fields whenever myProfile is updated/loaded from Firestore or local DB
    LaunchedEffect(myProfile) {
        if (myProfile.fullName.isNotBlank()) {
            fullName = myProfile.fullName
            if (myProfile.fatherName.isNotBlank()) fatherName = myProfile.fatherName
            if (myProfile.fatherOccupation.isNotBlank()) fatherOccupation = myProfile.fatherOccupation
            if (myProfile.motherName.isNotBlank()) motherName = myProfile.motherName
            if (myProfile.motherOccupation.isNotBlank()) motherOccupation = myProfile.motherOccupation
            if (myProfile.grandfatherName.isNotBlank()) grandfatherName = myProfile.grandfatherName
            numBrothers = myProfile.numBrothers
            if (myProfile.brothersNames.isNotBlank()) brothersNames = myProfile.brothersNames
            numSisters = myProfile.numSisters
            if (myProfile.sistersNames.isNotBlank()) sistersNames = myProfile.sistersNames
            if (myProfile.gender.isNotBlank()) gender = myProfile.gender
            if (myProfile.age > 0) age = myProfile.age.toString()
            if (myProfile.birthDate.isNotBlank()) birthDate = myProfile.birthDate
            if (myProfile.birthTime.isNotBlank()) birthTime = myProfile.birthTime
            if (myProfile.birthPlace.isNotBlank()) birthPlace = myProfile.birthPlace
            if (myProfile.height.isNotBlank()) height = myProfile.height
            if (myProfile.currentCity.isNotBlank()) currentCity = myProfile.currentCity
            if (myProfile.aadharNumber.isNotBlank()) aadharNumber = myProfile.aadharNumber
            if (myProfile.phoneContact.isNotBlank()) {
                phoneContact = myProfile.phoneContact
                regEmail = myProfile.phoneContact
            } else if (myProfile.parentPhoneContact.isNotBlank()) {
                if (regEmail.isBlank()) regEmail = myProfile.parentPhoneContact
            }
            if (myProfile.parentPhoneContact.isNotBlank()) parentPhoneContact = myProfile.parentPhoneContact
            if (myProfile.bloodGroup.isNotBlank()) bloodGroup = myProfile.bloodGroup
            isNri = myProfile.isNri
            if (myProfile.nriCountry.isNotBlank()) nriCountry = myProfile.nriCountry
            if (myProfile.maritalStatus.isNotBlank()) {
                val ms = myProfile.maritalStatus
                val knownOptions = listOf("Unmarried", "Widow", "Awaiting Divorce", "Divorced")
                if (ms in knownOptions) {
                    maritalStatus = ms
                    maritalStatusOther = ""
                } else if (ms.startsWith("Other: ")) {
                    maritalStatus = "Other"
                    maritalStatusOther = ms.removePrefix("Other: ")
                } else {
                    maritalStatus = "Other"
                    maritalStatusOther = ms
                }
            }
            if (myProfile.subCaste.isNotBlank()) subCaste = myProfile.subCaste
            if (myProfile.gol.isNotBlank()) gol = myProfile.gol
            if (myProfile.gotra.isNotBlank()) gotra = myProfile.gotra
            if (myProfile.motherGotra.isNotBlank()) motherGotra = myProfile.motherGotra
            if (myProfile.nativeVillage.isNotBlank()) nativeVillage = myProfile.nativeVillage
            if (myProfile.motherBirthVillage.isNotBlank()) motherBirthVillage = myProfile.motherBirthVillage
            if (myProfile.locality.isNotBlank()) locality = myProfile.locality
            if (myProfile.education.isNotBlank()) education = myProfile.education
            if (myProfile.occupation.isNotBlank()) occupation = myProfile.occupation
            if (myProfile.monthlyIncome.isNotBlank()) monthlyIncome = myProfile.monthlyIncome
            if (myProfile.hobbies.isNotBlank()) hobbies = myProfile.hobbies
            if (myProfile.familyDetails.isNotBlank()) familyDetails = myProfile.familyDetails
            if (myProfile.aboutMe.isNotBlank()) aboutMe = myProfile.aboutMe
            if (myProfile.rashi.isNotBlank()) rashi = myProfile.rashi
            if (myProfile.manglikStatus.isNotBlank()) manglikStatus = myProfile.manglikStatus

            if (!myProfile.isApproved && !viewModel.isAdmin.value) {
                showApprovalPendingDialog = true
            }
        }
    }

    LaunchedEffect(regEmail, phoneContact, parentPhoneContact, myProfile.id) {
        val phoneToCheck = regEmail.ifBlank { phoneContact }.ifBlank { parentPhoneContact }
        val cleanPhone = phoneToCheck.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length >= 10) {
            isMobileDuplicate = viewModel.isMobileRegistered(cleanPhone, myProfile.id)
        } else {
            isMobileDuplicate = false
        }
    }

    LaunchedEffect(aadharNumber, myProfile.id) {
        val cleanAadhar = aadharNumber.replace(Regex("[^0-9]"), "")
        if (cleanAadhar.length == 12) {
            isAadharDuplicate = viewModel.isAadharRegistered(cleanAadhar, myProfile.id)
        } else {
            isAadharDuplicate = false
        }
    }

    LaunchedEffect(generatedVoiceBio) {
        if (generatedVoiceBio.isNotBlank()) {
            aboutMe = generatedVoiceBio
        }
    }

    val stepTitles = listOf(
        "મૂળભૂત માહિતી",
        "શાખ અને મોસાળ",
        "શિક્ષણ અને વ્યવસાય",
        "પરિવાર અને બાયોડેટા"
    )

    fun validateStepForJump(step: Int): Boolean {
        when (step) {
            1 -> {
                showStep1Errors = true
                if (isUploadingProfile || isUploadingFront || isUploadingBack) {
                    Toast.makeText(
                        context,
                        "⏳ ફોટો / આધાર અપલોડ થઈ રહ્યો છે. કૃપા કરીને ૧૦૦% પૂર્ણ થાય ત્યાં સુધી રાહ જુઓ!",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                if (myProfile.profileImageUrl.isBlank() || myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank()) {
                    Toast.makeText(
                        context,
                        "પ્રોફાઇલ ફોટો, આધાર ફ્રન્ટ અને આધાર બેક ફોટો અપલોડ કરવો ફરજિયાત છે! (Profile photo, Aadhar front and back photos are required)",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                val cleanAadharNum = aadharNumber.replace(Regex("[^0-9]"), "")
                if (cleanAadharNum.length != 12) {
                    Toast.makeText(
                        context,
                        "૧૨ અંકનો સાચો આધાર કાર્ડ નંબર દાખલ કરવો ફરજિયાત છે! (Valid 12-digit Aadhar Card Number is required)",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                if (isAadharDuplicate) {
                    Toast.makeText(
                        context,
                        "આ આધાર કાર્ડ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (This Aadhar Card Number is already registered)",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                if (isMobileDuplicate) {
                    Toast.makeText(
                        context,
                        "આ મોબાઈલ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (This Mobile Number is already registered)",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                val isEditMode = viewModel.isLoggedIn.value || myProfile.fullName.isNotBlank()
                val targetPhone = regEmail.ifBlank { myProfile.phoneContact }.ifBlank { parentPhoneContact }
                val isPhoneOk = isEditMode || (targetPhone.length == 10 && targetPhone.all { it.isDigit() }) || (targetPhone.contains("@") && targetPhone.length >= 5)
                if ((!isEditMode && targetPhone.isBlank()) || !isPhoneOk ||
                    (!isEditMode && regPassword.length < 6) ||
                    fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                    age.isBlank() || age == "0" || birthDate.isBlank() ||
                    birthTime.isBlank() || birthPlace.isBlank() || height.isBlank() || currentCity.isBlank() ||
                    parentPhoneContact.isBlank() || parentPhoneContact.length != 10 ||
                    (maritalStatus == "Other" && maritalStatusOther.isBlank())) {
                    Toast.makeText(context, "પ્રથમ ચરણની તમામ ફરજિયાત લાલ ચિહ્નિત વિગતો ભરો!", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
            2 -> {
                showStep2Errors = true
                if (subCaste.isBlank() || gol.isBlank() || gotra.isBlank() || motherGotra.isBlank() ||
                    nativeVillage.isBlank() || motherBirthVillage.isBlank() || locality.isBlank()) {
                    Toast.makeText(context, "બીજા ચરણની તમામ ફરજિયાત લાલ ચિહ્નિત વિગતો ભરો!", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
            3 -> {
                showStep3Errors = true
                if (education.isBlank() || occupation.isBlank() || monthlyIncome.isBlank()) {
                    Toast.makeText(context, "ત્રીજા ચરણની તમામ ફરજિયાત લાલ ચિહ્નિત વિગતો ભરો!", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
            4 -> {
                showStep4Errors = true
                if (familyDetails.isBlank() || aboutMe.isBlank() || rashi.isBlank()) {
                    Toast.makeText(context, "ચોથા ચરણની તમામ ફરજિયાત લાલ ચિહ્નિત વિગતો ભરો!", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
            else -> return true
        }
    }

    fun jumpToStep(targetStep: Int) {
        if (isUploadingProfile || isUploadingFront || isUploadingBack) {
            Toast.makeText(
                context,
                "⏳ ફોટો / આધાર અપલોડ પ્રક્રિયા ચાલુ છે. કૃપા કરીને ૧૦૦% પૂર્ણ થાય ત્યાં સુધી રાહ જુઓ!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (targetStep <= currentStep) {
            currentStep = targetStep
            return
        }
        for (s in 1 until targetStep) {
            if (!validateStepForJump(s)) {
                currentStep = s
                return
            }
        }
        currentStep = targetStep
    }

    // Dynamic Profile Completeness Calculation
    data class MissingFieldInfo(
        val name: String,
        val isCompleted: Boolean,
        val targetStep: Int
    )

    val profileCompletenessFields = listOf(
        MissingFieldInfo("પૂરું નામ", fullName.isNotBlank(), 1),
        MissingFieldInfo("પિતાનું નામ", fatherName.isNotBlank(), 1),
        MissingFieldInfo("દાદાનું નામ (Grandfather's Name)", grandfatherName.isNotBlank(), 1),
        MissingFieldInfo("માતાનું નામ", motherName.isNotBlank(), 1),
        MissingFieldInfo("વાલીનો મોબાઈલ નંબર", parentPhoneContact.isNotBlank(), 1),
        MissingFieldInfo("પ્રોફાઇલ ફોટો", myProfile.profileImageUrl.isNotBlank(), 1),
        MissingFieldInfo("આધાર ફ્રન્ટ ફોટો", myProfile.aadharFrontUrl.isNotBlank(), 1),
        MissingFieldInfo("આધાર બેક ફોટો", myProfile.aadharBackUrl.isNotBlank(), 1),
        MissingFieldInfo("આધાર કાર્ડ નંબર (૧૨ અંક)", aadharNumber.replace(" ", "").length == 12, 1),
        MissingFieldInfo("પોતાની શાખ", gotra.isNotBlank(), 2),
        MissingFieldInfo("માતાની શાખ", motherGotra.isNotBlank(), 2),
        MissingFieldInfo("મૂળ ગામ", nativeVillage.isNotBlank(), 2),
        MissingFieldInfo("માતાનું વતન (મોસાળ)", motherBirthVillage.isNotBlank(), 2),
        MissingFieldInfo("શિક્ષણ", education.isNotBlank(), 3),
        MissingFieldInfo("વ્યવસાય", occupation.isNotBlank(), 3),
        MissingFieldInfo("માસિક આવક", monthlyIncome.isNotBlank(), 3),
        MissingFieldInfo("પરિવારની વિગત", familyDetails.isNotBlank(), 4),
        MissingFieldInfo("પોતાના વિશે", aboutMe.isNotBlank(), 4),
        MissingFieldInfo("રાશિ / કુંડળી", rashi.isNotBlank(), 4)
    )

    val completedFieldsCount = profileCompletenessFields.count { it.isCompleted }
    val totalFields = profileCompletenessFields.size
    val completionPercentage = (completedFieldsCount * 100) / totalFields
    val missingFields = profileCompletenessFields.filter { !it.isCompleted }

    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

    if (showApprovalPendingDialog && myProfile.fullName.isNotBlank() && !myProfile.isApproved && !viewModel.isAdmin.value) {
        AlertDialog(
            onDismissRequest = { showApprovalPendingDialog = false },
            icon = {
                Icon(
                    imageVector = if (myProfile.isRejected) Icons.Default.Cancel else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = if (myProfile.isRejected) Color(0xFFD32F2F) else RoyalMaroon,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (myProfile.isRejected) "પ્રોફાઇલ અસ્વીકૃત થયેલ છે\n(Profile Rejected)" else "એડમિન મંજૂરી માટે પેન્ડિંગ\n(Waiting for Admin Approval)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (myProfile.isRejected) Color(0xFFD32F2F) else RoyalMaroon,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = if (myProfile.isRejected) Color(0xFFFFEBEE) else SoftGold.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (myProfile.isRejected) Color(0xFFFFCDD2) else RoyalGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (myProfile.isRejected) {
                                Text(
                                    text = "અસ્વીકારનું કારણ (Rejection Reason):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB71C1C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = myProfile.rejectionReason.ifBlank { "પ્રોફાઇલ વિગતો અથવા આપેલ દસ્તાવેજો/ફોટો અપૂર્ણ છે." },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121),
                                    lineHeight = 18.sp
                                )
                            } else {
                                Text(
                                    text = "તમારું રજીસ્ટ્રેશન ફોર્મ સફળતાપૂર્વક મેળવેલ છે. એડમિન દ્વારા ચકાસણી પૂરતી થયા બાદ તમારી પ્રોફાઇલ અન્ય સભ્યોને પ્રદર્શિત થશે.",
                                    fontSize = 12.5.sp,
                                    color = DarkMaroon,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("ઉમેદવાર: ${myProfile.fullName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (myProfile.parentPhoneContact.isNotBlank()) {
                                Text("વાલીનો નંબર: ${myProfile.parentPhoneContact}", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    // Pulldown Refresh status button after application submission
                    Button(
                        onClick = {
                            viewModel.refreshDashboard()
                            Toast.makeText(context, "મંજૂરી સ્થિતિ ચકાસી રહ્યા છીએ... (Refreshing Status)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("refresh_approval_dialog_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("સ્થિતિ રીફ્રેશ કરો (Refresh Status)", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApprovalPendingDialog = false
                        isEditing = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("રજીસ્ટ્રેશન ફોર્મ જુઓ / સુધારો", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showApprovalPendingDialog = false
                        isEditing = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("બાયોડેટા કાર્ડ જુઓ")
                }
            },
            containerColor = CreamBackground,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showPolicyDetailDialogIndex != null) {
        val selectedIdx = showPolicyDetailDialogIndex!!
        val policies = listOf(
            Triple(if (appLanguage == "gu") "પ્રાઇવસી પોલિસી" else "Privacy Policy", Icons.Default.Security,
                "Chaudhary Vivah Privacy Policy:\n\n1. Information Collection: We collect bio-data, Gothra details, contact info, and optional Aadhar ID strictly for matrimonial verification within Chaudhary community.\n\n2. Data Usage: Used solely for Gothra exogamy validation, 36-Guna Kundli matching, and profile discovery.\n\n3. Match Complete Deletion: You can permanently delete your profile, bio-data, and photos with 1-click once your match is finalized.\n\n4. Contact Support: info@chaudharyvivah.in | 9016607483"),
            Triple(if (appLanguage == "gu") "રિફંડ પોલિસી" else "Refund Policy", Icons.Default.Payments,
                "Chaudhary Vivah Refund & Cancellation Policy:\n\n1. 7-Day Refund Window: Full refund for unutilized paid subscriptions before viewing contact details or profile rejection.\n\n2. Non-Refundable: Once contact phone numbers are unlocked or after match completion account deletion.\n\n3. Processing Time: Approved refunds credited back within 5 to 7 business days to original payment method.\n\n4. Contact Support: info@chaudharyvivah.in | 9016607483"),
            Triple(if (appLanguage == "gu") "નિયમો અને શરતો" else "Terms & Conditions", Icons.Default.Gavel,
                "Chaudhary Vivah Terms & Conditions:\n\n1. Legal Marriageable Age: Minimum 18 years for females and 21 years for males under Indian Marriage Act.\n\n2. Verified Chaudhary Profiles: Exclusive platform for Chaudhary community families.\n\n3. Authenticity: Submitting false bio-data or fake documents is prohibited.\n\n4. Admin Verification: All profiles undergo manual admin approval before public discovery."),
            Triple(if (appLanguage == "gu") "ડિસ્ક્લેમર" else "Disclaimer", Icons.Default.Info,
                "Chaudhary Vivah Disclaimer:\n\n1. Independent Check: Platform acts as matchmaking introductory portal. Families are advised to perform independent background checks.\n\n2. Kundli Milan: 36-Guna Kundli scores are for reference based on traditional astrological calculations.\n\n3. No Guaranteed Marriage Outcome: Platform facilitates introductions but does not guarantee matrimonial outcomes.")
        )
        val urls = listOf(
            "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/privacy-policy.html",
            "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/refund-policy.html",
            "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/terms-conditions.html",
            "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/disclaimer.html"
        )
        val currentPolicy = policies.getOrNull(selectedIdx) ?: policies[0]
        val currentUrl = urls.getOrNull(selectedIdx) ?: urls[0]

        AlertDialog(
            onDismissRequest = { showPolicyDetailDialogIndex = null },
            icon = {
                Icon(currentPolicy.second, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = currentPolicy.first,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalMaroon
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightRoseContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = currentPolicy.third,
                                fontSize = 11.5.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (appLanguage == "gu") "વેબસાઇટ પેજ ઓપન કરો" else "Open Web Page", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPolicyDetailDialogIndex = null }) {
                    Text(if (appLanguage == "gu") "બંધ કરો" else "Close", fontSize = 12.sp)
                }
            }
        )
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        if (isSaving || isUploadingProfile || isUploadingFront || isUploadingBack) {
            showExitConfirmDialog = true
        } else if (currentStep > 1) {
            currentStep--
        } else {
            showExitConfirmDialog = true
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) showExitConfirmDialog = false
            },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("ચેતવણી: ડેટા ગુમાવવાનું જોખમ", fontWeight = FontWeight.Bold, color = DarkMaroon)
            },
            text = {
                Text(
                    "સ્ક્રીન છોડવાથી ભરેલી માહિતી અથવા અપલોડ પ્રક્રિયા અધૂરી રહી શકે છે. શું તમે ખરેખર બહાર નીકળવા માંગો છો?\n\n(Leaving the screen may result in data loss or abort active uploads. Are you sure you want to leave the screen?)"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("હા, બહાર નીકળો (Yes, Exit)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text("ના, અહીં જ રહો (Stay)", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (isSaving) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* strictly wait */ },
            properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold),
                modifier = Modifier.padding(16.dp).fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = RoyalMaroon,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "માહિતી ફાયરબેઝમાં સુરક્ષિત સાચવી રહ્યા છીએ...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoyalMaroon,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "કૃપા કરીને રાહ જુઓ, ડેટા સેવ પૂર્ણ થાય ત્યાં સુધી સ્ક્રીન બંધ કે બેક ન કરો.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.myBioSetupTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Step $currentStep / 4: ${stepTitles[currentStep - 1]}",
                            fontSize = 11.sp,
                            color = DarkMaroon
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { performLogout() },
                        modifier = Modifier.testTag("registration_logout_icon")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = RoyalMaroon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSaving || isUploadingProfile || isUploadingFront || isUploadingBack) {
                                showExitConfirmDialog = true
                            } else if (currentStep > 1) {
                                currentStep--
                            } else {
                                showExitConfirmDialog = true
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "પાછા જાઓ")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Approval Status Banner
            if (myProfile.fullName.isNotBlank()) {
                if (myProfile.isApproved) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("પ્રોફાઇલ એડમિન મંજૂર થયેલ છે", fontWeight = FontWeight.Bold, color = VerifiedGreen, fontSize = 14.sp)
                                Text("તમારો બાયોડેટા સમાજના અન્ય સભ્યોના ડેશબોર્ડ પર સક્રિય અને દ્રશ્યમાન છે.", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                } else if (myProfile.isRejected) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("પ્રોફાઇલ અસ્વીકૃત થયેલ છે (Profile Rejected)", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 15.sp)
                                        Text("એડમિન દ્વારા બાયોડેટા નામંજૂર કરવામાં આવ્યો છે.", fontSize = 12.sp, color = Color(0xFFB71C1C))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.refreshDashboard()
                                        Toast.makeText(context, "મંજૂરી સ્થિતિ અપડેટ થઈ રહી છે... (Refreshing Status)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("refresh_rejection_card_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Status", tint = Color(0xFFD32F2F))
                                }
                            }

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("અસ્વીકારનું કારણ (Rejection Reason):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB71C1C))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = myProfile.rejectionReason.ifBlank { "પ્રોફાઇલ વિગતો અથવા આપેલ ફોટો/આધાર અપૂર્ણ છે." },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF212121),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isEditing = true
                                    currentStep = 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("પ્રોફાઇલ સુધારો અને ફરી સબમિટ કરો (Edit Profile)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("એડમિન મંજૂરી માટે પેન્ડિંગ (Pending Admin Approval)", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
                                    Text("તમારો બાયોડેટા ચકાસણી હેઠળ છે. એડમિન મંજૂર કર્યા બાદ જ અન્ય સભ્યોને ડેશબોર્ડ પર દેખાશે.", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.refreshDashboard()
                                    Toast.makeText(context, "મંજૂરી સ્થિતિ અપડેટ થઈ રહી છે... (Refreshing Status)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("refresh_pending_approval_card_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Status", tint = Color(0xFFF57F17))
                            }
                        }
                    }
                }
            }

            // Visual Profile Completeness Progress Bar Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_completeness_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = when {
                                    completionPercentage >= 90 -> VerifiedGreen
                                    completionPercentage >= 65 -> WarmSaffron
                                    else -> RoyalMaroon
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "પ્રોફાઇલ પૂર્ણતા ટકાવારી",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = RoyalMaroon
                            )
                        }

                        Surface(
                            color = when {
                                completionPercentage >= 90 -> VerifiedGreen
                                completionPercentage >= 65 -> WarmSaffron
                                else -> RoyalMaroon
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$completionPercentage% પૂર્ણ",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (completionPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = when {
                            completionPercentage >= 90 -> VerifiedGreen
                            completionPercentage >= 65 -> WarmSaffron
                            else -> RoyalMaroon
                        },
                        trackColor = Color.LightGray.copy(alpha = 0.4f)
                    )

                    // Encouragement Message & Missing Detail Suggestions
                    if (completionPercentage == 100) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = null,
                                tint = WarmSaffron,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "૧૦૦% સંપૂર્ણ પ્રોફાઇલ! શ્રેષ્ઠ સંબંધ માટે તમારી બાયોડેટા તૈયાર છે.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VerifiedGreen
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "💡 વધુ સંબંધ પ્રસ્તાવ મેળવવા માટે બાકી વિગતો ઉમેરો:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkMaroon
                            )

                            // Missing Field Quick-Jump Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                missingFields.take(3).forEach { field ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { jumpToStep(field.targetStep) },
                                        label = {
                                            Text(
                                                text = "+ ${field.name}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalMaroon
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = SoftGold.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Multi-step Progress Bar Indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (step in 1..4) {
                            val isActive = step == currentStep
                            val isCompleted = step < currentStep

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> VerifiedGreen
                                            isActive -> RoyalMaroon
                                            else -> Color.LightGray.copy(alpha = 0.4f)
                                        }
                                    )
                                    .clickable { jumpToStep(step) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$step",
                                        color = if (isActive) Color.White else Color.DarkGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (step < 4) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    color = if (step < currentStep) VerifiedGreen else Color.LightGray.copy(alpha = 0.5f),
                                    thickness = 2.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ચરણ $currentStep: ${stepTitles[currentStep - 1]}",
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon,
                        fontSize = 14.sp
                    )
                }
            }

            // STEP CONTENT
            AnimatedContent(
                targetState = currentStep,
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> Step1BasicInfo(
                        regEmail = regEmail, onRegEmailChange = { regEmail = it },
                        regPassword = regPassword, onRegPasswordChange = { regPassword = it },
                        regPasswordVisible = regPasswordVisible, onRegPasswordVisibleToggle = { regPasswordVisible = !regPasswordVisible },
                        fullName = fullName, onFullNameChange = { fullName = it },
                        fatherName = fatherName, onFatherNameChange = { fatherName = it },
                        fatherOccupation = fatherOccupation, onFatherOccupationChange = { fatherOccupation = it },
                        grandfatherName = grandfatherName, onGrandfatherNameChange = { grandfatherName = it },
                        motherName = motherName, onMotherNameChange = { motherName = it },
                        motherOccupation = motherOccupation, onMotherOccupationChange = { motherOccupation = it },
                        numBrothers = numBrothers, onNumBrothersChange = { numBrothers = it },
                        brothersNames = brothersNames, onBrothersNamesChange = { brothersNames = it },
                        numSisters = numSisters, onNumSistersChange = { numSisters = it },
                        sistersNames = sistersNames, onSistersNamesChange = { sistersNames = it },
                        gender = gender, onGenderChange = { gender = it },
                        age = age, onAgeChange = { age = it },
                        birthDate = birthDate, onBirthDateChange = { birthDate = it },
                        birthTime = birthTime, onBirthTimeChange = { birthTime = it },
                        birthState = birthState, onBirthStateChange = { birthState = it },
                        birthDistrict = birthDistrict, onBirthDistrictChange = { birthDistrict = it },
                        birthSubDistrict = birthSubDistrict, onBirthSubDistrictChange = { birthSubDistrict = it },
                        birthPlace = birthPlace, onBirthPlaceChange = { birthPlace = it },
                        height = height, onHeightChange = { height = it },
                        currentCity = currentCity, onCurrentCityChange = { currentCity = it },
                        parentPhoneContact = parentPhoneContact, onParentPhoneChange = { raw -> parentPhoneContact = raw.filter { it.isDigit() }.take(10) },
                        bloodGroup = bloodGroup, onBloodGroupChange = { bloodGroup = it },
                        isNri = isNri, onIsNriChange = { isNri = it },
                        nriCountry = nriCountry, onNriCountryChange = { nriCountry = it },
                        maritalStatus = maritalStatus, onMaritalStatusChange = { maritalStatus = it },
                        maritalStatusOther = maritalStatusOther, onMaritalStatusOtherChange = { maritalStatusOther = it },
                        aadharNumber = aadharNumber, onAadharNumberChange = { aadharNumber = it },
                        isAadharDuplicate = isAadharDuplicate,
                        isMobileDuplicate = isMobileDuplicate,
                        viewModel = viewModel,
                        showErrors = showStep1Errors,
                        isUploadingProfile = isUploadingProfile,
                        profileUploadProgress = profileUploadProgress,
                        isUploadingFront = isUploadingFront,
                        frontUploadProgress = frontUploadProgress,
                        isUploadingBack = isUploadingBack,
                        backUploadProgress = backUploadProgress,
                        onUploadProfile = startUploadProfile,
                        onUploadFront = startUploadFront,
                        onUploadBack = startUploadBack
                    )

                    2 -> Step2CommunityAndVillage(
                        subCaste = subCaste, onSubCasteChange = { subCaste = it },
                        gol = gol, onGolChange = { gol = it },
                        gotra = gotra, onGotraChange = { gotra = it },
                        motherGotra = motherGotra, onMotherGotraChange = { motherGotra = it },
                        nativeVillage = nativeVillage, onNativeVillageChange = { nativeVillage = it },
                        motherBirthVillage = motherBirthVillage, onMotherBirthVillageChange = { motherBirthVillage = it },
                        locality = locality, onLocalityChange = { locality = it },
                        showErrors = showStep2Errors
                    )

                    3 -> Step3CareerAndIncome(
                        education = education, onEducationChange = { education = it },
                        occupation = occupation, onOccupationChange = { occupation = it },
                        monthlyIncome = monthlyIncome, onMonthlyIncomeChange = { monthlyIncome = it },
                        hobbies = hobbies, onHobbiesChange = { hobbies = it },
                        showErrors = showStep3Errors
                    )

                    4 -> Step4FamilyAndBio(
                        familyDetails = familyDetails, onFamilyDetailsChange = { familyDetails = it },
                        aboutMe = aboutMe, onAboutMeChange = { aboutMe = it },
                        prefAgeMin = prefAgeMin, onPrefAgeMinChange = { prefAgeMin = it },
                        prefAgeMax = prefAgeMax, onPrefAgeMaxChange = { prefAgeMax = it },
                        prefHeightMin = prefHeightMin, onPrefHeightMinChange = { prefHeightMin = it },
                        prefHeightMax = prefHeightMax, onPrefHeightMaxChange = { prefHeightMax = it },
                        prefMinIncome = prefMinIncome, onPrefMinIncomeChange = { prefMinIncome = it },
                        prefEducation = prefEducation, onPrefEducationChange = { prefEducation = it },
                        prefOccupation = prefOccupation, onPrefOccupationChange = { prefOccupation = it },
                        prefCity = prefCity, onPrefCityChange = { prefCity = it },
                        rashi = rashi, onRashiChange = { rashi = it },
                        voiceNotesInput = voiceNotesInput, onVoiceNotesChange = { voiceNotesInput = it },
                        isGeneratingBio = isGeneratingBio,
                        onGenerateBio = { viewModel.generateVoiceBioFromNotes(voiceNotesInput) },
                        agreeTerms = agreeTerms, onAgreeTermsChange = { agreeTerms = it },
                        agreePrivacy = agreePrivacy, onAgreePrivacyChange = { agreePrivacy = it },
                        agreeDisclaimer = agreeDisclaimer, onAgreeDisclaimerChange = { agreeDisclaimer = it },
                        agreeRefund = agreeRefund, onAgreeRefundChange = { agreeRefund = it },
                        onOpenPolicyUrl = { policyType ->
                            openPolicyPageInBrowserOrDialog(context, policyType) { index ->
                                showPolicyDetailDialogIndex = index
                            }
                        },
                        showErrors = showStep4Errors,
                        appLanguage = appLanguage,
                        isEditMode = viewModel.isLoggedIn.value || myProfile.fullName.isNotBlank(),
                        registeredPhone = myProfile.phoneContact.ifBlank { parentPhoneContact }.ifBlank { regEmail },
                        editOtpSent = editOtpSent,
                        onSendEditOtp = {
                            val targetPhone = myProfile.phoneContact.ifBlank { parentPhoneContact }.ifBlank { regEmail }.trim()
                            if (targetPhone.length == 10 && targetPhone.all { it.isDigit() }) {
                                isSendingEditOtp = true
                                editOtpError = ""
                                viewModel.sendFirebaseOtp(
                                    context = context,
                                    phoneNumber = targetPhone,
                                    resendingToken = editResendToken,
                                    onOtpSent = { verId, token ->
                                        editVerificationId = verId
                                        editResendToken = token
                                        editOtpSent = true
                                        isSendingEditOtp = false
                                        editTimerSeconds = 60
                                        editOtpError = ""
                                        Toast.makeText(context, "નોંધાયેલ મોબાઈલ પર SMS OTP મોકલવામાં આવ્યો છે!", Toast.LENGTH_LONG).show()
                                    },
                                    onInstantSuccess = {
                                        isEditOtpVerified = true
                                        editOtpSent = true
                                        isSendingEditOtp = false
                                        editOtpError = ""
                                        Toast.makeText(context, "મોબાઈલ નંબર ઓટોમેટિક ચકાસાયો!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isSendingEditOtp = false
                                        editOtpError = err
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                editOtpError = "નોંધાયેલ મોબાઈલ નંબર અમાન્ય છે"
                            }
                        },
                        editEnteredOtp = editEnteredOtp,
                        onEditEnteredOtpChange = { editEnteredOtp = it },
                        isEditOtpVerified = isEditOtpVerified,
                        isSendingEditOtp = isSendingEditOtp,
                        isVerifyingEditOtp = isVerifyingEditOtp,
                        editTimerSeconds = editTimerSeconds,
                        editOtpError = editOtpError,
                        onVerifyEditOtp = {
                            if (editEnteredOtp.trim().length == 6) {
                                isVerifyingEditOtp = true
                                editOtpError = ""
                                viewModel.verifyFirebaseOtp(
                                    verificationId = editVerificationId,
                                    enteredCode = editEnteredOtp.trim(),
                                    onSuccess = {
                                        isEditOtpVerified = true
                                        isVerifyingEditOtp = false
                                        editOtpError = ""
                                        Toast.makeText(context, "મોબાઈલ OTP સફળતાપૂર્વક ચકાસાયો!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isVerifyingEditOtp = false
                                        editOtpError = err
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                editOtpError = "કૃપા કરીને ૬ અંકનો OTP દાખલ કરો"
                            }
                        }
                    )
                }
            }

            // NAVIGATION BUTTONS (Back / Next / Save to Firestore)
            // Active Upload Warning Banner
            val isUploadingAny = isUploadingProfile || isUploadingFront || isUploadingBack
            val highestUploadProgress = maxOf(profileUploadProgress, frontUploadProgress, backUploadProgress)

            if (isUploadingAny) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFE65100),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "⏳ ફોટો / આધાર અપલોડ થઈ રહ્યો છે ($highestUploadProgress%)...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "કૃપા કરીને ૧૦૦% પૂર્ણ થાય ત્યાં સુધી રાહ જુઓ. અપલોડ પૂર્ણ થયા પછી જ આગળ વધી શકાશે.",
                                fontSize = 11.sp,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = {
                            if (!isUploadingAny) {
                                currentStep--
                            } else {
                                Toast.makeText(context, "અપલોડ ચાલુ છે, કૃપા કરીને રાહ જુઓ!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSaving && !isUploadingAny,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalMaroon)
                    ) {
                        Text("પાછળ", color = RoyalMaroon, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (isUploadingAny) {
                            Toast.makeText(
                                context,
                                "⏳ ફોટો / આધાર અપલોડ પ્રક્રિયા ચાલુ છે ($highestUploadProgress%). કૃપા કરીને ૧૦૦% પૂર્ણ થાય ત્યાં સુધી રાહ જુઓ!",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }
                        when (currentStep) {
                            1 -> {
                                showStep1Errors = true
                                if (myProfile.profileImageUrl.isBlank() || myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "પ્રોફાઇલ ફોટો, આધાર ફ્રન્ટ અને આધાર બેક ફોટો અપલોડ કરવો ફરજિયાત છે! (Profile photo, Aadhar front & back required)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                val isEditProfile = viewModel.isLoggedIn.value || myProfile.fullName.isNotBlank()
                                val targetPhone = regEmail.ifBlank { myProfile.phoneContact }.ifBlank { parentPhoneContact }
                                val isPhoneOk = isEditProfile || (targetPhone.length == 10 && targetPhone.all { it.isDigit() }) || (targetPhone.contains("@") && targetPhone.length >= 5)
                                if ((!isEditProfile && targetPhone.isBlank()) || !isPhoneOk ||
                                    (!isEditProfile && regPassword.length < 6) ||
                                    fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                                    age.isBlank() || age == "0" || birthDate.isBlank() ||
                                    birthTime.isBlank() || birthPlace.isBlank() || height.isBlank() || currentCity.isBlank() ||
                                    parentPhoneContact.isBlank() || parentPhoneContact.length != 10 ||
                                    (maritalStatus == "Other" && maritalStatusOther.isBlank())) {
                                    Toast.makeText(
                                        context,
                                        "લાલ રંગથી ચિહ્નિત થયેલ તમામ ફરજિયાત ખાતા ભરો! (Please fill all required fields highlighted in red)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                currentStep = 2
                            }
                            2 -> {
                                showStep2Errors = true
                                if (subCaste.isBlank() || gol.isBlank() || gotra.isBlank() || motherGotra.isBlank() ||
                                    nativeVillage.isBlank() || motherBirthVillage.isBlank() || locality.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "લાલ રંગથી ચિહ્નિત થયેલ તમામ ફરજિયાત ખાતા ભરો! (Please fill all required fields highlighted in red)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                currentStep = 3
                            }
                            3 -> {
                                showStep3Errors = true
                                if (education.isBlank() || occupation.isBlank() || monthlyIncome.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "લાલ રંગથી ચિહ્નિત થયેલ તમામ ફરજિયાત ખાતા ભરો! (Please fill all required fields highlighted in red)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                currentStep = 4
                            }
                            4 -> {
                                showStep4Errors = true
                                if (familyDetails.isBlank() || aboutMe.isBlank() || rashi.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "લાલ રંગથી ચિહ્નિત થયેલ તમામ ફરજિયાત ખાતા ભરો! (Please fill all required fields highlighted in red)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                if (!agreeTerms || !agreePrivacy || !agreeDisclaimer || !agreeRefund) {
                                    Toast.makeText(
                                        context,
                                        if (appLanguage == "gu")
                                            "રજીસ્ટ્રેશન ફોર્મ સબમિટ કરવા માટે તમામ ૪ શરતો (નિયમો, પ્રાઇવસી પોલિસી, ડિસ્ક્લેમર અને રિફંડ પોલિસી) સ્વીકારવી ફરજિયાત છે!"
                                        else
                                            "Mandatory: Please tick all 4 checkboxes (Terms, Privacy, Disclaimer, Refund Policy) before submitting registration!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }
                                val isEditProfile = viewModel.isLoggedIn.value || myProfile.fullName.isNotBlank()
                                val checkPhone = regEmail.ifBlank { myProfile.phoneContact }.ifBlank { parentPhoneContact }
                                val cleanAadharNum = aadharNumber.replace(Regex("[^0-9]"), "")
                                if ((!isEditProfile && checkPhone.isBlank()) || fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                                    myProfile.profileImageUrl.isBlank() ||
                                    myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank() || cleanAadharNum.length != 12) {
                                    Toast.makeText(
                                        context,
                                        "પ્રથમ ચરણની માહિતી અથવા આધાર નંબર અપૂર્ણ છે! કૃપા કરીને પ્રથમ ચરણ તપાસો.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentStep = 1
                                    return@Button
                                }

                                if (isAadharDuplicate) {
                                    Toast.makeText(
                                        context,
                                        "આ આધાર કાર્ડ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (Aadhar card number is already registered)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentStep = 1
                                    return@Button
                                }

                                if (isMobileDuplicate) {
                                    Toast.makeText(
                                        context,
                                        "આ મોબાઈલ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (Mobile number is already registered)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentStep = 1
                                    return@Button
                                }

                                if (isEditProfile && !isEditOtpVerified && !viewModel.isAdmin.value) {
                                    Toast.makeText(
                                        context,
                                        "પ્રોફાઇલમાં સુધારો સબમિટ કરવા માટે તમારા નોંધાયેલ નંબર (+91 $checkPhone) પર SMS OTP મેળવીને ચકાસણી કરો!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }

                                val finalMaritalStatus = if (maritalStatus == "Other") {
                                    if (maritalStatusOther.isNotBlank()) "Other: $maritalStatusOther" else "Other"
                                } else {
                                    maritalStatus
                                }
                                val computedHasMaritalHistory = (maritalStatus != "Unmarried")

                                // Final Save to Cloud Firestore
                                isSaving = true
                                val currentAuthUid = com.example.service.FirebaseAuthService.currentUser?.uid
                                val effectiveUid = if (!currentAuthUid.isNullOrBlank()) currentAuthUid else myProfile.id
                                val updated = myProfile.copy(
                                    id = if (effectiveUid.isNotBlank() && effectiveUid != "USER_ME") effectiveUid else myProfile.id,
                                    profileId = myProfile.profileId,
                                    aadharNumber = cleanAadharNum,
                                    fullName = fullName,
                                    fatherName = fatherName,
                                    fatherOccupation = fatherOccupation,
                                    grandfatherName = grandfatherName,
                                    motherName = motherName,
                                    motherOccupation = motherOccupation,
                                    numBrothers = numBrothers,
                                    brothersNames = brothersNames,
                                    numSisters = numSisters,
                                    sistersNames = sistersNames,
                                    gender = gender,
                                    age = age.toIntOrNull() ?: 26,
                                    birthDate = birthDate,
                                    birthTime = birthTime,
                                    birthPlace = birthPlace,
                                    height = height,
                                    currentCity = currentCity,
                                    phoneContact = regEmail.ifBlank { parentPhoneContact },
                                    parentPhoneContact = parentPhoneContact,
                                    bloodGroup = bloodGroup,
                                    isNri = isNri,
                                    nriCountry = if (isNri) nriCountry else "",
                                    hasMaritalHistory = computedHasMaritalHistory,
                                    maritalStatus = finalMaritalStatus,
                                    subCaste = subCaste,
                                    gol = gol,
                                    gotra = gotra,
                                    motherGotra = motherGotra,
                                    nativeVillage = nativeVillage,
                                    motherBirthVillage = motherBirthVillage,
                                    locality = locality,
                                    education = education,
                                    occupation = occupation,
                                    monthlyIncome = monthlyIncome,
                                    hobbies = hobbies,
                                    familyDetails = familyDetails,
                                    aboutMe = aboutMe,
                                    prefAgeMin = prefAgeMin.toIntOrNull() ?: 21,
                                    prefAgeMax = prefAgeMax.toIntOrNull() ?: 32,
                                    prefHeightMin = prefHeightMin,
                                    prefHeightMax = prefHeightMax,
                                    prefMinIncome = prefMinIncome,
                                    prefEducation = prefEducation,
                                    prefOccupation = prefOccupation,
                                    prefCity = prefCity,
                                    rashi = rashi,
                                    manglikStatus = "Non-Manglik",
                                    isApproved = if (viewModel.isAdmin.value) true else false,
                                    isRejected = false,
                                    rejectionReason = ""
                                )

                                if (!viewModel.isLoggedIn.value) {
                                    viewModel.registerWithEmailAndPassword(
                                        email = regEmail.trim(),
                                        pass = regPassword.trim(),
                                        profileData = updated,
                                        onError = { err ->
                                            isSaving = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        },
                                        onSuccess = {
                                            isSaving = false
                                            isEditing = false
                                            Toast.makeText(
                                                context,
                                                "રજીસ્ટ્રેશન ફોર્મ સફળતાપૂર્વક સબમિટ થયું! એડમિન ચકાસણી સુધી બાયોડેટા લોક રહેશે.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                } else {
                                    viewModel.updateMyProfile(updated) { result ->
                                        isSaving = false
                                        if (result.isSuccess) {
                                            isEditing = false
                                            val toastMsg = if (viewModel.isAdmin.value) {
                                                "બાયોડેટા સફળતાપૂર્વક અપડેટ કરવામાં આવ્યો છે!"
                                            } else {
                                                "બાયોડેટા લોક થયો! એડમિનની મંજૂરી બાદ પ્રોફાઇલ અન્ય ડેશબોર્ડ પર પ્રદર્શિત થશે."
                                            }
                                            Toast.makeText(
                                                context,
                                                toastMsg,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "બાયોડેટા સાચવવામાં નિષ્ફળતા! કૃપા કરીને ફરી પ્રયાસ કરો.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("step_next_save_button"),
                    enabled = !isSaving && !isUploadingAny,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("સાચવી રહ્યા છીએ...")
                    } else if (isUploadingAny) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("અપલોડ ચાલુ છે ($highestUploadProgress%)", fontWeight = FontWeight.Bold)
                    } else if (currentStep < 4) {
                        Text("આગળનું ચરણ", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("બાયોડેટા સાચવો", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

val NriCountryList = listOf(
    "United States (USA)",
    "Canada",
    "United Kingdom (UK)",
    "Australia",
    "New Zealand",
    "United Arab Emirates (UAE)",
    "Qatar",
    "Saudi Arabia",
    "Kuwait",
    "Oman",
    "Bahrain",
    "Singapore",
    "Malaysia",
    "Germany",
    "Netherlands",
    "France",
    "Switzerland",
    "Sweden",
    "Ireland",
    "Japan",
    "South Africa",
    "Other Country"
)

// STEP 1: Basic Information
@Composable
fun Step1BasicInfo(
    regEmail: String, onRegEmailChange: (String) -> Unit,
    regPassword: String, onRegPasswordChange: (String) -> Unit,
    regPasswordVisible: Boolean, onRegPasswordVisibleToggle: () -> Unit,
    fullName: String, onFullNameChange: (String) -> Unit,
    fatherName: String, onFatherNameChange: (String) -> Unit,
    fatherOccupation: String, onFatherOccupationChange: (String) -> Unit,
    grandfatherName: String, onGrandfatherNameChange: (String) -> Unit,
    motherName: String, onMotherNameChange: (String) -> Unit,
    motherOccupation: String, onMotherOccupationChange: (String) -> Unit,
    numBrothers: Int, onNumBrothersChange: (Int) -> Unit,
    brothersNames: String, onBrothersNamesChange: (String) -> Unit,
    numSisters: Int, onNumSistersChange: (Int) -> Unit,
    sistersNames: String, onSistersNamesChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    birthDate: String, onBirthDateChange: (String) -> Unit,
    birthTime: String, onBirthTimeChange: (String) -> Unit,
    birthState: String, onBirthStateChange: (String) -> Unit,
    birthDistrict: String, onBirthDistrictChange: (String) -> Unit,
    birthSubDistrict: String, onBirthSubDistrictChange: (String) -> Unit,
    birthPlace: String, onBirthPlaceChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    currentCity: String, onCurrentCityChange: (String) -> Unit,
    parentPhoneContact: String, onParentPhoneChange: (String) -> Unit,
    bloodGroup: String, onBloodGroupChange: (String) -> Unit,
    isNri: Boolean, onIsNriChange: (Boolean) -> Unit,
    nriCountry: String, onNriCountryChange: (String) -> Unit,
    maritalStatus: String, onMaritalStatusChange: (String) -> Unit,
    maritalStatusOther: String, onMaritalStatusOtherChange: (String) -> Unit,
    aadharNumber: String = "", onAadharNumberChange: (String) -> Unit = {},
    isAadharDuplicate: Boolean = false,
    isMobileDuplicate: Boolean = false,
    viewModel: MatrimonyViewModel,
    showErrors: Boolean = false,
    isUploadingProfile: Boolean = false,
    profileUploadProgress: Int = 0,
    isUploadingFront: Boolean = false,
    frontUploadProgress: Int = 0,
    isUploadingBack: Boolean = false,
    backUploadProgress: Int = 0,
    onUploadProfile: (Uri) -> Unit = {},
    onUploadFront: (Uri) -> Unit = {},
    onUploadBack: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val myProfile by viewModel.myProfile.collectAsState()

    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var subDistrictExpanded by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadProfile(it) }
    }

    val aadharFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadFront(it) }
    }

    val aadharBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadBack(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ચરણ ૧: વૈયક્તિક માહિતી અને જન્મ વિગત (AI કુંડળી મિલન માટે)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = RoyalMaroon
            )

            // Registration Account Credentials Section (Mobile Number & Realtime SMS OTP - ON TOP)
            var regOtpSent by remember { mutableStateOf(false) }
            var regGeneratedOtp by remember { mutableStateOf("") }
            var regResendToken by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }
            var regEnteredOtp by remember { mutableStateOf("") }
            var isRegOtpVerified by remember { mutableStateOf(viewModel.isLoggedIn.value) }
            var isSendingRegOtp by remember { mutableStateOf(false) }
            var isVerifyingRegOtp by remember { mutableStateOf(false) }
            var regTimerSeconds by remember { mutableStateOf(60) }
            var regOtpError by remember { mutableStateOf("") }

            LaunchedEffect(regOtpSent, regTimerSeconds) {
                if (regOtpSent && !isRegOtpVerified && regTimerSeconds > 0) {
                    kotlinx.coroutines.delay(1000L)
                    regTimerSeconds -= 1
                }
            }

            val isEditProfileMode = viewModel.isLoggedIn.value || myProfile.fullName.isNotBlank()
            val displayMobile = regEmail.ifBlank { myProfile.phoneContact }.ifBlank { parentPhoneContact }

            OutlinedTextField(
                value = if (isEditProfileMode) displayMobile else regEmail,
                onValueChange = {
                    if (!isEditProfileMode) {
                        onRegEmailChange(it)
                        if (regOtpSent) {
                            regOtpSent = false
                            isRegOtpVerified = false
                            regEnteredOtp = ""
                            regOtpError = ""
                        }
                    }
                },
                label = {
                    if (isEditProfileMode) {
                        Text("નોંધાયેલ મોબાઈલ નંબર (લૉક થયેલ 🔒)")
                    } else {
                        Text("લૉગિન મોબાઈલ નંબર (10 Digit Mobile Number - ફરજિયાત *)")
                    }
                },
                placeholder = { Text("૧૦ અંકનો મોબાઈલ નંબર દાખલ કરો") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_email_input"),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                trailingIcon = {
                    if (isEditProfileMode) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray)
                    }
                },
                singleLine = true,
                enabled = !isEditProfileMode && !isRegOtpVerified && !isSendingRegOtp,
                isError = (showErrors && !isEditProfileMode && (regEmail.isBlank() || regEmail.length != 10 || !regEmail.all { it.isDigit() })) || isMobileDuplicate,
                supportingText = {
                    if (isMobileDuplicate) {
                        Text(
                            "આ મોબાઈલ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (Mobile number is already registered)",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    } else if (isEditProfileMode) {
                        Text("નોંધાયેલ મોબાઈલ નંબર બદલી શકાશે નહીં (Locked)", color = Color.Gray, fontSize = 11.sp)
                    } else if (showErrors && (regEmail.isBlank() || regEmail.length != 10 || !regEmail.all { it.isDigit() })) {
                        Text("કૃપા કરીને ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો", color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
            )

            // OTP Verification Box (For New Registration Only)
            if (!isEditProfileMode && !isRegOtpVerified) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RoyalMaroon)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("મોબાઈલ નંબર ચકાસણી (Realtime OTP)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalMaroon)
                        }

                        if (regOtpError.isNotBlank()) {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = regOtpError,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (!regOtpSent) {
                            Button(
                                onClick = {
                                    if (regEmail.trim().length == 10 && regEmail.trim().all { it.isDigit() }) {
                                        isSendingRegOtp = true
                                        regOtpError = ""
                                        viewModel.sendFirebaseOtp(
                                            context = context,
                                            phoneNumber = regEmail.trim(),
                                            resendingToken = regResendToken,
                                            onOtpSent = { verId, token ->
                                                regGeneratedOtp = verId
                                                regResendToken = token
                                                regOtpSent = true
                                                isSendingRegOtp = false
                                                regTimerSeconds = 60
                                                regOtpError = ""
                                                Toast.makeText(context, "SMS દ્વારા ૬ અંકનો સુરક્ષિત OTP મોકલવામાં આવ્યો છે!", Toast.LENGTH_LONG).show()
                                            },
                                            onInstantSuccess = {
                                                isRegOtpVerified = true
                                                regOtpSent = true
                                                isSendingRegOtp = false
                                                regOtpError = ""
                                                Toast.makeText(context, "મોબાઈલ નંબર ઓટોમેટિક ચકાસાયો!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                isSendingRegOtp = false
                                                regOtpError = err
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        regOtpError = "કૃપા કરીને પ્રથમ ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો"
                                        Toast.makeText(context, regOtpError, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isSendingRegOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                modifier = Modifier.fillMaxWidth().testTag("reg_send_otp_button")
                            ) {
                                if (isSendingRegOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SMS OTP મોકલાઈ રહ્યો છે...")
                                } else {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SMS OTP મોકલો (Send Realtime OTP)")
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+91 $regEmail પર OTP મોકલાયો છે",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = RoyalMaroon
                                        )
                                    }
                                    Text(
                                        text = "તમારા મોબાઈલ પર આવેલ ૬ અંકનો SMS કોડ નીચે દાખલ કરો.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = regEnteredOtp,
                                onValueChange = { if (it.length <= 6) regEnteredOtp = it },
                                label = { Text("૬ અંકનો SMS OTP (Enter 6-digit OTP)") },
                                placeholder = { Text("SMS કોડ દાખલ કરો") },
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("reg_otp_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (regTimerSeconds > 0) {
                                    Text(
                                        text = "ફરીથી મોકલો: ${regTimerSeconds}s",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            isSendingRegOtp = true
                                            regOtpError = ""
                                            viewModel.sendFirebaseOtp(
                                                context = context,
                                                phoneNumber = regEmail.trim(),
                                                resendingToken = regResendToken,
                                                onOtpSent = { verId, token ->
                                                    regGeneratedOtp = verId
                                                    regResendToken = token
                                                    isSendingRegOtp = false
                                                    regTimerSeconds = 60
                                                    Toast.makeText(context, "નવો SMS OTP મોકલવામાં આવ્યો છે!", Toast.LENGTH_SHORT).show()
                                                },
                                                onInstantSuccess = {
                                                    isRegOtpVerified = true
                                                    isSendingRegOtp = false
                                                },
                                                onError = { err ->
                                                    isSendingRegOtp = false
                                                    regOtpError = err
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        enabled = !isSendingRegOtp
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalMaroon)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ફરીથી OTP મોકલો (Resend)", fontSize = 12.sp, color = RoyalMaroon, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        regOtpSent = false
                                        regEnteredOtp = ""
                                        regOtpError = ""
                                    }
                                ) {
                                    Text("નંબર બદલો (Change Number)", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Button(
                                onClick = {
                                    if (regEnteredOtp.trim().length == 6) {
                                        isVerifyingRegOtp = true
                                        regOtpError = ""
                                        viewModel.verifyFirebaseOtp(
                                            verificationId = regGeneratedOtp,
                                            enteredCode = regEnteredOtp.trim(),
                                            onSuccess = {
                                                isRegOtpVerified = true
                                                isVerifyingRegOtp = false
                                                regOtpError = ""
                                                Toast.makeText(context, "મોબાઈલ નંબર સફળતાપૂર્વક ચકાસાયો! (Mobile verified)", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isVerifyingRegOtp = false
                                                regOtpError = err
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        regOtpError = "કૃપા કરીને SMS માં આવેલ ૬ અંકનો OTP દાખલ કરો"
                                        Toast.makeText(context, regOtpError, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isVerifyingRegOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                modifier = Modifier.fillMaxWidth().testTag("reg_verify_otp_button")
                            ) {
                                if (isVerifyingRegOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ચકાસણી ચાલુ છે...")
                                } else {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("OTP ચકાસો (Verify OTP)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VerifiedGreenContainer, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("મોબાઈલ નંબર ચકાસણી સફળ! (Mobile Number Verified)", color = VerifiedGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (!viewModel.isLoggedIn.value) {
                OutlinedTextField(
                    value = regPassword,
                    onValueChange = onRegPasswordChange,
                    label = { Text("લૉગિન પાસવર્ડ (Password - ફરજિયાત *)") },
                    placeholder = { Text("ઓછામાં ઓછા ૬ અક્ષરનો પાસવર્ડ લખો") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_password_input"),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon) },
                    trailingIcon = {
                        IconButton(onClick = onRegPasswordVisibleToggle) {
                            Icon(
                                imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (regPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    isError = showErrors && regPassword.length < 6,
                    supportingText = {
                        if (showErrors && regPassword.length < 6) {
                            Text("ઓછામાં ઓછા ૬ અક્ષરનો પાસવર્ડ જરૂરી છે", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    visualTransformation = if (regPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
            }

            // Firebase Storage Profile Picture Card with Real-time Percentage Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isUploadingProfile -> Color(0xFFFFF8E1)
                        myProfile.profileImageUrl.isNotBlank() -> Color(0xFFE8F5E9)
                        else -> SoftGold.copy(alpha = 0.3f)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isUploadingProfile -> WarmSaffron
                        myProfile.profileImageUrl.isNotBlank() -> VerifiedGreen
                        else -> RoyalGold
                    }
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RoyalMaroon.copy(alpha = 0.15f))
                                .border(
                                    1.5.dp,
                                    if (myProfile.profileImageUrl.isNotBlank()) VerifiedGreen else RoyalGold,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingProfile) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = RoyalMaroon,
                                    strokeWidth = 3.dp
                                )
                            } else if (myProfile.profileImageUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = myProfile.profileImageUrl,
                                    contentDescription = "Profile Photo",
                                    placeholder = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_matrimony_hero_1784990427738),
                                    error = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_matrimony_hero_1784990427738),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = RoyalMaroon,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "પ્રોફાઇલ તસવીર (ફરજિયાત *)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = when {
                                    isUploadingProfile -> "સર્વર પર અપલોડ થઈ રહી છે... ($profileUploadProgress%)"
                                    myProfile.profileImageUrl.isNotBlank() -> "સફળતાપૂર્વક અપલોડ થયેલ છે ✓"
                                    else -> "ગેલરીમાંથી સ્પષ્ટ ફોટો પસંદ કરો"
                                },
                                fontSize = 11.sp,
                                color = if (myProfile.profileImageUrl.isNotBlank()) VerifiedGreen else Color.Gray,
                                fontWeight = if (myProfile.profileImageUrl.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        Button(
                            onClick = { photoLauncher.launch("image/*") },
                            enabled = !isUploadingProfile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (myProfile.profileImageUrl.isNotBlank()) DarkMaroon else RoyalMaroon
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isUploadingProfile) {
                                Text("$profileUploadProgress%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else if (myProfile.profileImageUrl.isNotBlank()) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("બદલો", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("અપલોડ *", fontSize = 12.sp)
                            }
                        }
                    }

                    if (isUploadingProfile) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (profileUploadProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RoyalMaroon,
                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("અપલોડ સ્થિતિ", fontSize = 10.sp, color = Color.DarkGray)
                            Text("$profileUploadProgress%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                        }
                    }
                }
            }

            // MANDATORY AADHAR PHOTOS SECTION WITH PROGRESS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "આધાર કાર્ડ ફોટો અપલોડ (ફરજિયાત *)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = "એડમિન ચકાસણી માટે આગળનો અને પાછળનો ફોટો જરૂરી છે",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AadharPhotoUploadCard(
                            label = "આધાર ફ્રન્ટ (આગળ)",
                            imageUrl = myProfile.aadharFrontUrl,
                            isUploading = isUploadingFront,
                            uploadProgress = frontUploadProgress,
                            onUploadClick = { aadharFrontLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )

                        AadharPhotoUploadCard(
                            label = "આધાર બેક (પાછળ)",
                            imageUrl = myProfile.aadharBackUrl,
                            isUploading = isUploadingBack,
                            uploadProgress = backUploadProgress,
                            onUploadClick = { aadharBackLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = aadharNumber,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(12)
                    onAadharNumberChange(filtered)
                },
                label = { Text("૧૨ અંકનો આધાર કાર્ડ નંબર (12 Digit Aadhar Number - ફરજિયાત *)") },
                placeholder = { Text("દા.ત. 123456789012") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_aadhar_number_input"),
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalMaroon) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                isError = (showErrors && aadharNumber.replace(" ", "").length != 12) || isAadharDuplicate,
                supportingText = {
                    val clean = aadharNumber.replace(" ", "")
                    if (isAadharDuplicate) {
                        Text(
                            "આ આધાર કાર્ડ નંબર પહેલેથી જ રજીસ્ટર્ડ છે! (Aadhar card number is already registered)",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    } else if (showErrors && clean.length != 12) {
                        Text("કૃપા કરીને ૧૨ અંકનો સાચો આધાર નંબર દાખલ કરો", color = MaterialTheme.colorScheme.error)
                    } else if (clean.length == 12) {
                        Text("✓ ૧૨ અંકનો આધાર નંબર દાખલ કરેલ છે", color = VerifiedGreen)
                    }
                }
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("પોતાનું પૂરું નામ (ફરજિયાત *)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_full_name_input"),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = showErrors && fullName.isBlank(),
                supportingText = {
                    if (showErrors && fullName.isBlank()) {
                        Text("પોતાનું પૂરું નામ લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = fatherName,
                onValueChange = onFatherNameChange,
                label = { Text("પિતાનું પૂરું નામ (ફરજિયાત *)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_father_name_input"),
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                isError = showErrors && fatherName.isBlank(),
                supportingText = {
                    if (showErrors && fatherName.isBlank()) {
                        Text("પિતાનું પૂરું નામ લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = fatherOccupation,
                onValueChange = onFatherOccupationChange,
                label = { Text("પિતાનો વ્યવસાય (Father's Occupation)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_father_occupation_input"),
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                placeholder = { Text("દા.ત. સરકારી નોકરી / વેપાર / ખેતી / નિવૃત્ત") }
            )

            OutlinedTextField(
                value = grandfatherName,
                onValueChange = onGrandfatherNameChange,
                label = { Text("દાદાનું પૂરું નામ (Grandfather's Name) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_grandfather_name_input"),
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                isError = showErrors && grandfatherName.isBlank(),
                supportingText = {
                    if (showErrors && grandfatherName.isBlank()) {
                        Text(
                            text = "દાદાનું નામ લખવું ફરજિયાત છે (Mandatory field)",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            OutlinedTextField(
                value = motherName,
                onValueChange = onMotherNameChange,
                label = { Text("માતાનું પૂરું નામ (ફરજિયાત *)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_mother_name_input"),
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                isError = showErrors && motherName.isBlank(),
                supportingText = {
                    if (showErrors && motherName.isBlank()) {
                        Text("માતાનું પૂરું નામ લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = motherOccupation,
                onValueChange = onMotherOccupationChange,
                label = { Text("માતાનો વ્યવસાય (Mother's Occupation)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_mother_occupation_input"),
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                placeholder = { Text("દા.ત. ગૃહણી (Homemaker) / શિક્ષક / સરકારી સેવા") }
            )

            // Brothers & Sisters Section Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ભાઈ અને બહેનની વિગતો (Siblings Information):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoyalMaroon
                    )

                    // Brothers selection
                    Text(text = "ભાઈઓની સંખ્યા (Number of Brothers):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (0..5).forEach { count ->
                            FilterChip(
                                selected = numBrothers == count,
                                onClick = { onNumBrothersChange(count) },
                                label = { Text(if (count == 5) "5+" else count.toString()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (numBrothers > 0) {
                        val currentBList = brothersNames.split(",").map { it.trim() }.toMutableList()
                        while (currentBList.size < numBrothers) { currentBList.add("") }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            (0 until numBrothers.coerceAtMost(5)).forEach { idx ->
                                OutlinedTextField(
                                    value = currentBList.getOrElse(idx) { "" },
                                    onValueChange = { newVal ->
                                        val updatedList = currentBList.toMutableList()
                                        while (updatedList.size <= idx) { updatedList.add("") }
                                        updatedList[idx] = newVal
                                        onBrothersNamesChange(updatedList.filter { it.isNotBlank() }.joinToString(", "))
                                    },
                                    label = { Text("ભાઈ ${idx + 1} નું નામ (Brother ${idx + 1} Name)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    // Sisters selection
                    Text(text = "બહેનોની સંખ્યા (Number of Sisters):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (0..5).forEach { count ->
                            FilterChip(
                                selected = numSisters == count,
                                onClick = { onNumSistersChange(count) },
                                label = { Text(if (count == 5) "5+" else count.toString()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (numSisters > 0) {
                        val currentSList = sistersNames.split(",").map { it.trim() }.toMutableList()
                        while (currentSList.size < numSisters) { currentSList.add("") }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            (0 until numSisters.coerceAtMost(5)).forEach { idx ->
                                OutlinedTextField(
                                    value = currentSList.getOrElse(idx) { "" },
                                    onValueChange = { newVal ->
                                        val updatedList = currentSList.toMutableList()
                                        while (updatedList.size <= idx) { updatedList.add("") }
                                        updatedList[idx] = newVal
                                        onSistersNamesChange(updatedList.filter { it.isNotBlank() }.joinToString(", "))
                                    },
                                    label = { Text("બહેન ${idx + 1} નું નામ (Sister ${idx + 1} Name)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Gender Selection
            Text(
                text = "લિંગ પસંદગી (Gender Selection):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkMaroon
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = viewModel.isMaleGender(gender),
                    onClick = { onGenderChange("Groom") },
                    label = { Text("પુરુષ (Male)") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalMaroon, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = viewModel.isFemaleGender(gender),
                    onClick = { onGenderChange("Bride") },
                    label = { Text("સ્ત્રી (Female)") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalMaroon, selectedLabelColor = Color.White)
                )
            }

            // Birth Date & Birth Time Row (With Native Pickers)
            val openDatePicker = {
                val dpd = DatePickerDialog(
                    context,
                    com.example.R.style.LightDatePickerDialogTheme,
                    { _, year, month, dayOfMonth ->
                        val formatted = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                        onBirthDateChange(formatted)
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val calculatedAge = (currentYear - year).coerceIn(18, 80)
                        onAgeChange(calculatedAge.toString())
                    },
                    1998, 7, 15
                )
                dpd.show()
            }
            val openTimePicker = {
                val tpd = TimePickerDialog(
                    context,
                    com.example.R.style.LightTimePickerDialogTheme,
                    { _, hourOfDay, minute ->
                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                        val h12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                        val formatted = String.format("%02d:%02d %s", h12, minute, amPm)
                        onBirthTimeChange(formatted)
                    },
                    8, 30, false
                )
                tpd.show()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Birth Date Picker Field
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("જન્મ તારીખ") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Birth Date")
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { openDatePicker() }
                    )
                }

                // Birth Time Picker Field
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = birthTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("જન્મ સમય") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick Birth Time")
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { openTimePicker() }
                    )
                }
            }

            // Birth Place Selection (India States -> Districts -> Sub-districts)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = RoyalMaroon)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "જન્મ સ્થળ પસંદગી (AI કુંડળી જન્મ સ્થળ):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = RoyalMaroon
                        )
                    }

                    // State Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = birthState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("રાજ્ય (State)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select State")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { stateExpanded = true }
                        )
                        DropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(SurfaceCream)
                        ) {
                            IndiaLocationData.statesList.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        onBirthStateChange(state)
                                        val dists = IndiaLocationData.getDistricts(state)
                                        val firstDist = dists.firstOrNull() ?: ""
                                        onBirthDistrictChange(firstDist)
                                        val subDists = IndiaLocationData.getSubDistricts(firstDist)
                                        val firstSub = subDists.firstOrNull() ?: ""
                                        onBirthSubDistrictChange(firstSub)
                                        onBirthPlaceChange("$firstSub, $firstDist, $state")
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // District Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val districtList = IndiaLocationData.getDistricts(birthState)
                        OutlinedTextField(
                            value = birthDistrict,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("જિલ્લો (District)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select District")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { districtExpanded = true }
                        )
                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(SurfaceCream)
                        ) {
                            districtList.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district) },
                                    onClick = {
                                        onBirthDistrictChange(district)
                                        val subDists = IndiaLocationData.getSubDistricts(district)
                                        val firstSub = subDists.firstOrNull() ?: ""
                                        onBirthSubDistrictChange(firstSub)
                                        onBirthPlaceChange("$firstSub, $district, $birthState")
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sub-District Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val subDistrictList = IndiaLocationData.getSubDistricts(birthDistrict)
                        OutlinedTextField(
                            value = birthSubDistrict,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("તાલુકો / સબ-જિલ્લો (Sub-district)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Sub-District")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { subDistrictExpanded = true }
                        )
                        DropdownMenu(
                            expanded = subDistrictExpanded,
                            onDismissRequest = { subDistrictExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(SurfaceCream)
                        ) {
                            subDistrictList.forEach { subDist ->
                                DropdownMenuItem(
                                    text = { Text(subDist) },
                                    onClick = {
                                        onBirthSubDistrictChange(subDist)
                                        onBirthPlaceChange("$subDist, $birthDistrict, $birthState")
                                        subDistrictExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "પસંદ કરેલ સ્થાન: $birthPlace",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalMaroon
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("ઉંમર (વર્ષ) *") },
                    modifier = Modifier.weight(1f),
                    isError = showErrors && (age.isBlank() || age == "0")
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = onHeightChange,
                    label = { Text("ઊંચાઈ (દા.ત. 5'10\") *") },
                    modifier = Modifier.weight(1f),
                    isError = showErrors && height.isBlank()
                )
            }

            OutlinedTextField(
                value = currentCity,
                onValueChange = onCurrentCityChange,
                label = { Text("હાલનું શહેર / રહેઠાણ *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                isError = showErrors && currentCity.isBlank(),
                supportingText = {
                    if (showErrors && currentCity.isBlank()) {
                        Text("હાલનું શહેર લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = parentPhoneContact,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.filter { it.isDigit() }.take(10)
                    onParentPhoneChange(digitsOnly)
                },
                label = { Text("વાલીનો મોબાઈલ નંબર (Parent's Mobile Number) *") },
                placeholder = { Text("૧૦ આંકડાનો મોબાઈલ નંબર દાખલ કરો") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_phone_input"),
                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = RoyalMaroon) },
                singleLine = true,
                isError = (showErrors && (parentPhoneContact.isBlank() || parentPhoneContact.length != 10)) || (parentPhoneContact.isNotBlank() && parentPhoneContact.length != 10),
                supportingText = {
                    if (parentPhoneContact.isNotBlank() && parentPhoneContact.length != 10) {
                        Text(
                            text = "વાલીનો મોબાઈલ નંબર બરાબર ૧૦ આંકડાનો હોવો જોઈએ (${parentPhoneContact.length}/10 આંકડા)",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (parentPhoneContact.length == 10) {
                        Text(
                            text = "✓ ૧૦ આંકડા પૂર્ણ (10 digits entered)",
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        Text(text = "માત્ર અંક (0-9) અને બરાબર ૧૦ આંકડા (Only numeric 10 digits)")
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            // Blood Group Dropdown Selection
            var bloodGroupExpanded by remember { mutableStateOf(false) }
            val bloodGroupList = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = bloodGroup.ifBlank { "A+" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("બ્લડ ગ્રુપ પસંદ કરો (Blood Group Selection) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("blood_group_dropdown_input"),
                    leadingIcon = {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = "Blood Group Icon",
                            tint = Color(0xFFD32F2F)
                        )
                    },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Blood Group")
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { bloodGroupExpanded = true }
                )
                DropdownMenu(
                    expanded = bloodGroupExpanded,
                    onDismissRequest = { bloodGroupExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(SurfaceCream)
                ) {
                    bloodGroupList.forEach { bg ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(bg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            },
                            onClick = {
                                onBloodGroupChange(bg)
                                bloodGroupExpanded = false
                            }
                        )
                    }
                }
            }

            // Is Candidate NRI Toggle Icon Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.25f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = "NRI Icon",
                                tint = RoyalMaroon,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "શું ઉમેદવાર NRI છે? (Is Candidate NRI?)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DarkMaroon
                                )
                                Text(
                                    text = if (isNri) "હા - NRI ઉમેદવાર (Abroad)" else "ના - ભારતમાં નિવાસી (Resident)",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = isNri,
                                onClick = { onIsNriChange(true) },
                                label = { Text("હા (Yes)") },
                                modifier = Modifier.testTag("nri_yes_toggle"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isNri) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalMaroon,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = !isNri,
                                onClick = { onIsNriChange(false) },
                                label = { Text("ના (No)") },
                                modifier = Modifier.testTag("nri_no_toggle"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (!isNri) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalMaroon,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }

                    if (isNri) {
                        Spacer(modifier = Modifier.height(10.dp))
                        var countryExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = nriCountry.ifBlank { "United States (USA)" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("NRI દેશ પસંદ કરો (Select Country) *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nri_country_dropdown_input"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Country",
                                        tint = RoyalMaroon
                                    )
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Country")
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { countryExpanded = true }
                            )
                            DropdownMenu(
                                expanded = countryExpanded,
                                onDismissRequest = { countryExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                NriCountryList.forEach { countryOption ->
                                    DropdownMenuItem(
                                        text = { Text(countryOption) },
                                        onClick = {
                                            onNriCountryChange(countryOption)
                                            countryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Marital Status / History Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Marital Status Icon",
                            tint = RoyalMaroon,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "વૈવાહિક સ્થિતિ / અગાઉનો વૈવાહિક ઇતિહાસ (Marital Status) *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkMaroon
                        )
                    }

                    val maritalOptions = listOf("Unmarried", "Widow", "Awaiting Divorce", "Divorced", "Other")

                    // Row 1: Unmarried, Widow, Awaiting Divorce
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        maritalOptions.take(3).forEach { option ->
                            FilterChip(
                                selected = maritalStatus == option,
                                onClick = { onMaritalStatusChange(option) },
                                label = {
                                    Text(
                                        when (option) {
                                            "Unmarried" -> "Unmarried (અવિવાહિત)"
                                            "Widow" -> "Widow (વિધવા/ર)"
                                            "Awaiting Divorce" -> "Awaiting Divorce"
                                            else -> option
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reg_marital_status_$option"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalMaroon,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Row 2: Divorced, Other
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        maritalOptions.drop(3).forEach { option ->
                            FilterChip(
                                selected = maritalStatus == option,
                                onClick = { onMaritalStatusChange(option) },
                                label = {
                                    Text(
                                        when (option) {
                                            "Divorced" -> "Divorced (છૂટાછેડા)"
                                            "Other" -> "Other (અન્ય)"
                                            else -> option
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reg_marital_status_$option"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalMaroon,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    if (maritalStatus == "Other") {
                        OutlinedTextField(
                            value = maritalStatusOther,
                            onValueChange = onMaritalStatusOtherChange,
                            label = { Text("અન્ય વૈવાહિક સ્થિતિનું વર્ણન (Describe Other Marital Status) *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_marital_status_other_input"),
                            placeholder = { Text("દા.ત. Separated / Custom Status") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            singleLine = true,
                            isError = showErrors && maritalStatusOther.isBlank(),
                            supportingText = {
                                if (showErrors && maritalStatusOther.isBlank()) {
                                    Text("અન્ય સ્થિતિનું વર્ણન કરવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// STEP 2: Community & Village Exogamy (Sub-caste, Gotra, Mother's Birth Village)
@Composable
fun Step2CommunityAndVillage(
    subCaste: String, onSubCasteChange: (String) -> Unit,
    gol: String, onGolChange: (String) -> Unit,
    gotra: String, onGotraChange: (String) -> Unit,
    motherGotra: String, onMotherGotraChange: (String) -> Unit,
    nativeVillage: String, onNativeVillageChange: (String) -> Unit,
    motherBirthVillage: String, onMotherBirthVillageChange: (String) -> Unit,
    locality: String, onLocalityChange: (String) -> Unit,
    showErrors: Boolean = false
) {
    val subCasteOptions = listOf("આંજણા ચૌધરી", "પટેલ ચૌધરી", "દેસાઈ ચૌધરી")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ચરણ ૨: સમાજ શાખા, ગોળ અને વતન",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalMaroon
                )
            }

            Text(
                text = "સમાજ સબ-કાસ્ટ પસંદ કરો:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkMaroon
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subCasteOptions.forEach { option ->
                    FilterChip(
                        selected = subCaste == option,
                        onClick = { onSubCasteChange(option) },
                        label = { Text(option, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = gol,
                onValueChange = onGolChange,
                label = { Text("ગોળ / ઝાલા નું નામ *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_gol_input"),
                placeholder = { Text("દા.ત. ૨૭ ગોળ, ૪૨ ગોળ, ૫૬ ગોળ, ઝાલા") },
                isError = showErrors && gol.isBlank(),
                supportingText = {
                    if (showErrors && gol.isBlank()) {
                        Text("ગોળ / ઝાલા નું નામ લખવું ફરજિયાત છે (Mandatory field)", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = gotra,
                onValueChange = onGotraChange,
                label = { Text("પોતાની શાખ *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_gotra_input"),
                placeholder = { Text("દા.ત. ચૌધરી / પટેલ / સારણ / દહિયા") },
                isError = showErrors && gotra.isBlank(),
                supportingText = {
                    if (showErrors && gotra.isBlank()) {
                        Text("પોતાની શાખ લખવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = motherGotra,
                onValueChange = onMotherGotraChange,
                label = { Text("માતાની શાખ *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("દા.ત. પવાર / પંવાર / મલિક") },
                isError = showErrors && motherGotra.isBlank(),
                supportingText = {
                    if (showErrors && motherGotra.isBlank()) {
                        Text("માતાની શાખ લખવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = nativeVillage,
                onValueChange = onNativeVillageChange,
                label = { Text("મૂળ વતન / પિતૃ ગામ *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_native_village_input"),
                placeholder = { Text("દા.ત. વિસનગર / ઊંઝા / પાલનપુર") },
                isError = showErrors && nativeVillage.isBlank(),
                supportingText = {
                    if (showErrors && nativeVillage.isBlank()) {
                        Text("મૂળ વતન / પિતૃ ગામ લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = motherBirthVillage,
                onValueChange = onMotherBirthVillageChange,
                label = { Text("મોસાળ / માતાનું જન્મ વતન *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_mother_village_input"),
                placeholder = { Text("દા.ત. કડી / મહેસાણા / ખેરાલુ") },
                isError = showErrors && motherBirthVillage.isBlank(),
                supportingText = {
                    if (showErrors && motherBirthVillage.isBlank()) {
                        Text("મોસાળ / માતાનું વતન લખવું ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}

// STEP 3: Career & Income & Hobbies
@Composable
fun Step3CareerAndIncome(
    education: String, onEducationChange: (String) -> Unit,
    occupation: String, onOccupationChange: (String) -> Unit,
    monthlyIncome: String, onMonthlyIncomeChange: (String) -> Unit,
    hobbies: String, onHobbiesChange: (String) -> Unit,
    showErrors: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ચરણ ૩: શિક્ષણ, વ્યવસાય, આવક અને શોખ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RoyalMaroon
            )

            OutlinedTextField(
                value = education,
                onValueChange = onEducationChange,
                label = { Text("ઉચ્ચતમ શૈક્ષણિક ડિગ્રી *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_education_input"),
                placeholder = { Text("દા.ત. બી.ટેક, એમ.બી.બી.એસ, એમ.બી.એ, બી.કોમ") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                isError = showErrors && education.isBlank(),
                supportingText = {
                    if (showErrors && education.isBlank()) {
                        Text("શિક્ષણની વિગત ભરવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = occupation,
                onValueChange = onOccupationChange,
                label = { Text("હાલનો વ્યવસાય / નોકરી *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_occupation_input"),
                placeholder = { Text("દા.ત. સૉફ્ટવેર એન્જિનિયર, સરકારી સેવા, કૃષિ વ્યવસાય") },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                isError = showErrors && occupation.isBlank(),
                supportingText = {
                    if (showErrors && occupation.isBlank()) {
                        Text("વ્યવસાયની વિગત ભરવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = monthlyIncome,
                onValueChange = onMonthlyIncomeChange,
                label = { Text("માસિક આવક (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("દા.ત. ₹૭૫,૦૦૦ / મહિને") },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                isError = showErrors && monthlyIncome.isBlank(),
                supportingText = {
                    if (showErrors && monthlyIncome.isBlank()) {
                        Text("માસિક આવકની વિગત ભરવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = hobbies,
                onValueChange = onHobbiesChange,
                label = { Text("હોબીઝ / શોખ (Hobbies & Interests)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_hobbies_input"),
                placeholder = { Text("દા.ત. વાંચન, પ્રવાસ, ક્રિકેટ, સંગીત, યોગા, સ્પોર્ટ્સ") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
            )
        }
    }
}

// STEP 4: Family Details & AI Biodata & Mandatory Legal Agreements
@Composable
fun Step4FamilyAndBio(
    familyDetails: String, onFamilyDetailsChange: (String) -> Unit,
    aboutMe: String, onAboutMeChange: (String) -> Unit,
    prefAgeMin: String, onPrefAgeMinChange: (String) -> Unit,
    prefAgeMax: String, onPrefAgeMaxChange: (String) -> Unit,
    prefHeightMin: String, onPrefHeightMinChange: (String) -> Unit,
    prefHeightMax: String, onPrefHeightMaxChange: (String) -> Unit,
    prefMinIncome: String, onPrefMinIncomeChange: (String) -> Unit,
    prefEducation: String, onPrefEducationChange: (String) -> Unit,
    prefOccupation: String, onPrefOccupationChange: (String) -> Unit,
    prefCity: String, onPrefCityChange: (String) -> Unit,
    rashi: String, onRashiChange: (String) -> Unit,
    voiceNotesInput: String, onVoiceNotesChange: (String) -> Unit,
    isGeneratingBio: Boolean,
    onGenerateBio: () -> Unit,
    agreeTerms: Boolean, onAgreeTermsChange: (Boolean) -> Unit,
    agreePrivacy: Boolean, onAgreePrivacyChange: (Boolean) -> Unit,
    agreeDisclaimer: Boolean, onAgreeDisclaimerChange: (Boolean) -> Unit,
    agreeRefund: Boolean, onAgreeRefundChange: (Boolean) -> Unit,
    onOpenPolicyUrl: (String) -> Unit,
    showErrors: Boolean = false,
    appLanguage: String = "gu",
    isEditMode: Boolean = false,
    registeredPhone: String = "",
    editOtpSent: Boolean = false,
    onSendEditOtp: () -> Unit = {},
    editEnteredOtp: String = "",
    onEditEnteredOtpChange: (String) -> Unit = {},
    isEditOtpVerified: Boolean = false,
    isSendingEditOtp: Boolean = false,
    isVerifyingEditOtp: Boolean = false,
    editTimerSeconds: Int = 60,
    editOtpError: String = "",
    onVerifyEditOtp: () -> Unit = {}
) {
    var rashiExpanded by remember { mutableStateOf(false) }
    val rashiOptions = listOf(
        "Mesha", "Vrishabha", "Mithuna", "Karka",
        "Simha", "Kanya", "Tula", "Vrishchika",
        "Dhanu", "Makara", "Kumbha", "Meena"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ચરણ ૪: પારિવારિક પૃષ્ઠભૂમિ અને બાયોડેટા",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalMaroon
                )

                // Single Selection Rashi Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "રાશિ પસંદ કરો (Select Rashi):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkMaroon
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (rashi.isNotBlank()) rashi else "Mesha",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("રાશિ") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Rashi")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { rashiExpanded = true }
                        )

                        DropdownMenu(
                            expanded = rashiExpanded,
                            onDismissRequest = { rashiExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(SurfaceCream)
                        ) {
                            rashiOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = item,
                                            fontWeight = if (rashi.equals(item, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (rashi.equals(item, ignoreCase = true)) RoyalMaroon else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        onRashiChange(item)
                                        rashiExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "ઝડપી પસંદગી:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        rashiOptions.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { item ->
                                    FilterChip(
                                        selected = rashi.equals(item, ignoreCase = true),
                                        onClick = { onRashiChange(item) },
                                        label = { Text(item, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalMaroon,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = familyDetails,
                    onValueChange = onFamilyDetailsChange,
                    label = { Text("કૌટુંબિક પૃષ્ઠભૂમિ અને ખેતી / વ્યવસાય *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("દા.ત. સંયુક્ત પરિવાર, ૧૫ વીઘા જમીન, પિતા નિવૃત્ત અધિકારી...") },
                    isError = showErrors && familyDetails.isBlank(),
                    supportingText = {
                        if (showErrors && familyDetails.isBlank()) {
                            Text("કૌટુંબિક પૃષ્ઠભૂમિ લખવી ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = aboutMe,
                    onValueChange = onAboutMeChange,
                    label = { Text("પોતાના વિશે પરિચય / પરિચય બ્રીફ (ટાઇપ કરો) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_about_me_input"),
                    placeholder = { Text("તમારા વ્યક્તિત્વ, શોખ અને અપેક્ષાઓ વિશે ટૂંકમાં લખો...") },
                    maxLines = 5,
                    isError = showErrors && aboutMe.isBlank(),
                    supportingText = {
                        if (showErrors && aboutMe.isBlank()) {
                            Text("પોતાના વિશે પરિચય લખવો ફરજિયાત છે", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        }

        // Expected Matrimonial Partner Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ઈચ્છિત જીવનસાથીની અપેક્ષાઓ (Partner Preferences)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoyalMaroon
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prefAgeMin,
                        onValueChange = onPrefAgeMinChange,
                        label = { Text("ન્યૂનતમ ઉંમર") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = prefAgeMax,
                        onValueChange = onPrefAgeMaxChange,
                        label = { Text("મહત્તમ ઉંમર") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prefHeightMin,
                        onValueChange = onPrefHeightMinChange,
                        label = { Text("ઓછામાં ઓછી ઊંચાઈ") },
                        placeholder = { Text("5'0\"") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = prefHeightMax,
                        onValueChange = onPrefHeightMaxChange,
                        label = { Text("વધુમાં વધુ ઊંચાઈ") },
                        placeholder = { Text("6'2\"") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = prefEducation,
                    onValueChange = onPrefEducationChange,
                    label = { Text("અપેક્ષિત શિક્ષણ (Expected Education)") },
                    placeholder = { Text("દા.ત. સ્નાતક / અનુસ્નાતક / B.E. / B.Ed...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = prefOccupation,
                    onValueChange = onPrefOccupationChange,
                    label = { Text("અપેક્ષિત વ્યવસાય (Expected Occupation)") },
                    placeholder = { Text("દા.ત. સરકારી નોકરી / પ્રાઇવેટ જોબ / વ્યવસાય...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prefMinIncome,
                        onValueChange = onPrefMinIncomeChange,
                        label = { Text("અપેક્ષિત આવક (Min Income)") },
                        placeholder = { Text("₹25,000+ / મહિનો") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = prefCity,
                        onValueChange = onPrefCityChange,
                        label = { Text("અપેક્ષિત શહેર / રાજ્ય") },
                        placeholder = { Text("ગુજરાત / અમદાવાદ / સ્થાનિક") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // PROFILE EDIT MODE OTP VERIFICATION CARD
        if (isEditMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_edit_otp_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = RoyalMaroon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "પ્રોફાઇલ સુધારા SMS ચકાસણી (Firebase OTP Verification) *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RoyalMaroon
                        )
                    }

                    Text(
                        text = "તમારો નોંધાયેલ મોબાઈલ નંબર: +91 $registeredPhone\nપ્રોફાઇલમાં કોઈપણ સુધારો એડમિન મંજૂરી માટે મોકલતા પહેલા Firebase SMS OTP ચકાસણી ફરજિયાત છે.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    if (editOtpError.isNotBlank()) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = editOtpError,
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (isEditOtpVerified) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = VerifiedGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "મોબાઈલ OTP સફળતાપૂર્વક ચકાસાયો! હવે નીચેના સેવ બટનથી સુધારેલ પ્રોફાઇલ સબમિટ કરી શકો છો.",
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedGreen,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (!editOtpSent) {
                        Button(
                            onClick = onSendEditOtp,
                            enabled = !isSendingEditOtp,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                            modifier = Modifier.fillMaxWidth().testTag("send_edit_otp_button")
                        ) {
                            if (isSendingEditOtp) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SMS OTP મોકલાઈ રહ્યો છે...")
                            } else {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Firebase OTP મોકલો (Send OTP to +91 $registeredPhone)")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = editEnteredOtp,
                            onValueChange = onEditEnteredOtpChange,
                            label = { Text("૬ અંકનો SMS OTP (Enter 6-digit OTP)") },
                            placeholder = { Text("SMS કોડ દાખલ કરો") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = RoyalMaroon) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("edit_otp_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editTimerSeconds > 0) {
                                Text("ફરીથી મોકલો: ${editTimerSeconds}s", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                TextButton(
                                    onClick = onSendEditOtp,
                                    enabled = !isSendingEditOtp
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalMaroon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ફરીથી OTP મોકલો (Resend OTP)", fontSize = 12.sp, color = RoyalMaroon, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = onVerifyEditOtp,
                            enabled = !isVerifyingEditOtp,
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                            modifier = Modifier.fillMaxWidth().testTag("verify_edit_otp_button")
                        ) {
                            if (isVerifyingEditOtp) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ચકાસણી ચાલુ છે...")
                            } else {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OTP ચકાસો (Verify OTP)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // MANDATORY LEGAL POLICIES & AGREEMENT CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("registration_terms_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (appLanguage == "gu") "નિયમો, પોલિસી અને શરતોની મંજૂરી *" else "Legal Policies & Consent *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = RoyalMaroon
                        )
                    }

                    androidx.compose.material3.FilterChip(
                        selected = agreeTerms && agreePrivacy && agreeDisclaimer && agreeRefund,
                        onClick = {
                            val allChecked = agreeTerms && agreePrivacy && agreeDisclaimer && agreeRefund
                            onAgreeTermsChange(!allChecked)
                            onAgreePrivacyChange(!allChecked)
                            onAgreeDisclaimerChange(!allChecked)
                            onAgreeRefundChange(!allChecked)
                        },
                        label = {
                            Text(
                                text = if (agreeTerms && agreePrivacy && agreeDisclaimer && agreeRefund)
                                    (if (appLanguage == "gu") "બધાં દૂર કરો" else "Deselect All")
                                else
                                    (if (appLanguage == "gu") "બધા ટીક કરો" else "Select All"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalMaroon,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceCream,
                            labelColor = RoyalMaroon
                        ),
                        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = agreeTerms && agreePrivacy && agreeDisclaimer && agreeRefund,
                            borderColor = RoyalGold
                        )
                    )
                }

                Text(
                    text = if (appLanguage == "gu")
                        "રજીસ્ટ્રેશન ફોર્મ સબમિટ કરવા માટે નીચે આપેલી તમામ ૪ શરતો પર ટીક કરવું ફરજિયાત છે. સંબંધિત લિંક પર ક્લિક કરીને લાઈવ વેબપેજ જુઓ."
                    else
                        "You must tick all 4 checkboxes below before submitting registration. Click any policy link to view the official web page.",
                    fontSize = 11.5.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = RoyalGold.copy(alpha = 0.5f))

                // 1. Terms & Conditions
                PolicyCheckboxItem(
                    checked = agreeTerms,
                    onCheckedChange = onAgreeTermsChange,
                    prefixText = if (appLanguage == "gu") "હું " else "I accept the ",
                    linkText = if (appLanguage == "gu") "નિયમો અને શરતો (Terms & Conditions)" else "Terms & Conditions",
                    suffixText = if (appLanguage == "gu") " વાંચીને સ્વીકારું છું *" else " *",
                    onClickLink = { onOpenPolicyUrl("terms") },
                    isError = showErrors && !agreeTerms,
                    testTag = "reg_terms_checkbox"
                )

                // 2. Privacy Policy
                PolicyCheckboxItem(
                    checked = agreePrivacy,
                    onCheckedChange = onAgreePrivacyChange,
                    prefixText = if (appLanguage == "gu") "હું " else "I accept the ",
                    linkText = if (appLanguage == "gu") "પ્રાઇવસી પોલિસી (Privacy Policy)" else "Privacy Policy",
                    suffixText = if (appLanguage == "gu") " વાંચીને સ્વીકારું છું *" else " *",
                    onClickLink = { onOpenPolicyUrl("privacy") },
                    isError = showErrors && !agreePrivacy,
                    testTag = "reg_privacy_checkbox"
                )

                // 3. Disclaimer
                PolicyCheckboxItem(
                    checked = agreeDisclaimer,
                    onCheckedChange = onAgreeDisclaimerChange,
                    prefixText = if (appLanguage == "gu") "હું " else "I accept the ",
                    linkText = if (appLanguage == "gu") "ડિસ્ક્લેમર (Disclaimer)" else "Disclaimer",
                    suffixText = if (appLanguage == "gu") " સ્વીકારું છું *" else " *",
                    onClickLink = { onOpenPolicyUrl("disclaimer") },
                    isError = showErrors && !agreeDisclaimer,
                    testTag = "reg_disclaimer_checkbox"
                )

                // 4. Refund Policy
                PolicyCheckboxItem(
                    checked = agreeRefund,
                    onCheckedChange = onAgreeRefundChange,
                    prefixText = if (appLanguage == "gu") "હું " else "I accept the ",
                    linkText = if (appLanguage == "gu") "રિફંડ અને કેન્સલેશન પોલિસી (Refund Policy)" else "Refund Policy",
                    suffixText = if (appLanguage == "gu") " સ્વીકારું છું *" else " *",
                    onClickLink = { onOpenPolicyUrl("refund") },
                    isError = showErrors && !agreeRefund,
                    testTag = "reg_refund_checkbox"
                )

                if (showErrors && (!agreeTerms || !agreePrivacy || !agreeDisclaimer || !agreeRefund)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (appLanguage == "gu")
                                "⚠️ સબમિટ કરવા માટે તમામ ૪ શરતો પર ટીક કરવું ફરજિયાત છે!"
                            else
                                "⚠️ All 4 checkboxes must be ticked to submit registration!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PolicyCheckboxItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    prefixText: String,
    linkText: String,
    suffixText: String,
    onClickLink: () -> Unit,
    isError: Boolean,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isError) Color(0xFFFFEBEE) else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = RoyalMaroon,
                uncheckedColor = if (isError) MaterialTheme.colorScheme.error else Color.Gray
            ),
            modifier = Modifier
                .padding(top = 2.dp)
                .testTag(testTag)
        )

        FlowRow(
            modifier = Modifier
                .weight(1f)
                .clickable { onCheckedChange(!checked) }
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Center
        ) {
            if (prefixText.isNotBlank()) {
                Text(
                    text = prefixText,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF90CAF9)),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clickable { onClickLink() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = linkText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Web Page",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (suffixText.isNotBlank()) {
                Text(
                    text = suffixText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.DarkGray
                )
            }
        }
    }
}

private fun openPolicyPageInBrowserOrDialog(
    context: android.content.Context,
    policyType: String,
    onShowDialog: (Int) -> Unit
) {
    val pageMap = mapOf(
        "privacy" to Triple(0, "privacy-policy.html", "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/privacy-policy.html"),
        "refund" to Triple(1, "refund-policy.html", "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/refund-policy.html"),
        "terms" to Triple(2, "terms-conditions.html", "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/terms-conditions.html"),
        "disclaimer" to Triple(3, "disclaimer.html", "https://ais-dev-kbofivximmlj23x36jqlii-108666020810.asia-southeast1.run.app/app/src/main/assets/website/disclaimer.html")
    )
    val info = pageMap[policyType]
    if (info != null) {
        onShowDialog(info.first)
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(info.third))
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback gracefully
        }
    }
}

@Composable
fun AadharPhotoUploadCard(
    label: String,
    imageUrl: String,
    isUploading: Boolean,
    uploadProgress: Int = 0,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUploading -> Color(0xFFFFF8E1)
                imageUrl.isNotBlank() -> Color(0xFFE8F5E9)
                else -> Color.White
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isUploading -> WarmSaffron
                imageUrl.isNotBlank() -> VerifiedGreen
                else -> Color.LightGray
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = RoyalMaroon,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (uploadProgress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = RoyalMaroon,
                    trackColor = Color.LightGray.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$uploadProgress% અપલોડ...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon
                )
            } else if (imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Surface(
                        color = VerifiedGreen,
                        shape = RoundedCornerShape(bottomStart = 6.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerifiedGreen
                    )
                    TextButton(
                        onClick = onUploadClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text("બદલો", fontSize = 10.sp, color = RoyalMaroon, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onUploadClick,
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("અપલોડ કરો *", fontSize = 10.sp)
                }
            }
        }
    }
}

