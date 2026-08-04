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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onBackClick: () -> Unit
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

    if (!isEditing && myProfile.fullName.isNotBlank()) {
        LockedBioDataView(
            profile = myProfile,
            onEditClick = { isEditing = true },
            onBackClick = onBackClick,
            onLogoutClick = {
                if (myProfile.fullName.isNotBlank()) {
                    onBackClick()
                } else {
                    isEditing = true
                    currentStep = 1
                }
            },
            onRefreshClick = {
                viewModel.refreshDashboard()
                Toast.makeText(context, "મંજૂરી સ્થિતિ તાજી કરી રહ્યા છીએ... (Refreshing Status)", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // Step 1: Registration Credentials, Personal Details & Birth Details for Kundli
    var regEmail by remember { mutableStateOf(if (myProfile.phoneContact.contains("@")) myProfile.phoneContact else "") }
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
    var phoneContact by remember { mutableStateOf(myProfile.phoneContact) }
    var parentPhoneContact by remember { mutableStateOf(myProfile.parentPhoneContact) }
    var bloodGroup by remember { mutableStateOf(if (myProfile.bloodGroup.isNotBlank()) myProfile.bloodGroup else "A+") }
    var isNri by remember { mutableStateOf(myProfile.isNri) }
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

    // Step 4: Family & Bio / Horoscope
    var familyDetails by remember { mutableStateOf(myProfile.familyDetails) }
    var aboutMe by remember { mutableStateOf(myProfile.aboutMe) }
    var voiceNotesInput by remember { mutableStateOf("") }
    var rashi by remember { mutableStateOf(if (myProfile.rashi.isNotBlank()) myProfile.rashi else "Mesha") }
    var manglikStatus by remember { mutableStateOf(if (myProfile.manglikStatus.isNotBlank()) myProfile.manglikStatus else "Non-Manglik") }

    var isSaving by remember { mutableStateOf(false) }

    var showStep1Errors by remember { mutableStateOf(false) }
    var showStep2Errors by remember { mutableStateOf(false) }
    var showStep3Errors by remember { mutableStateOf(false) }
    var showStep4Errors by remember { mutableStateOf(false) }

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
            if (myProfile.phoneContact.isNotBlank()) {
                phoneContact = myProfile.phoneContact
                if (myProfile.phoneContact.contains("@")) regEmail = myProfile.phoneContact
            }
            if (myProfile.parentPhoneContact.isNotBlank()) parentPhoneContact = myProfile.parentPhoneContact
            if (myProfile.bloodGroup.isNotBlank()) bloodGroup = myProfile.bloodGroup
            isNri = myProfile.isNri
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
                val isPhoneOk = regEmail.length == 10 && regEmail.all { it.isDigit() }
                if (regEmail.isBlank() || !isPhoneOk ||
                    (!viewModel.isLoggedIn.value && regPassword.length < 6) ||
                    fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                    age.isBlank() || age == "0" || birthDate.isBlank() ||
                    birthTime.isBlank() || birthPlace.isBlank() || height.isBlank() || currentCity.isBlank() ||
                    parentPhoneContact.isBlank() || parentPhoneContact.length != 10 ||
                    myProfile.profileImageUrl.isBlank() ||
                    myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank() ||
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
                        onClick = {
                            if (myProfile.fullName.isNotBlank()) {
                                onBackClick()
                            } else {
                                currentStep = 1
                            }
                        },
                        modifier = Modifier.testTag("registration_logout_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = RoyalMaroon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            onBackClick()
                        }
                    }) {
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
                        maritalStatus = maritalStatus, onMaritalStatusChange = { maritalStatus = it },
                        maritalStatusOther = maritalStatusOther, onMaritalStatusOtherChange = { maritalStatusOther = it },
                        viewModel = viewModel,
                        showErrors = showStep1Errors
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
                        rashi = rashi, onRashiChange = { rashi = it },
                        voiceNotesInput = voiceNotesInput, onVoiceNotesChange = { voiceNotesInput = it },
                        isGeneratingBio = isGeneratingBio,
                        onGenerateBio = { viewModel.generateVoiceBioFromNotes(voiceNotesInput) },
                        showErrors = showStep4Errors
                    )
                }
            }

            // NAVIGATION BUTTONS (Back / Next / Save to Firestore)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
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
                        when (currentStep) {
                            1 -> {
                                showStep1Errors = true
                                if (regEmail.isBlank() || !regEmail.contains("@") ||
                                    (!viewModel.isLoggedIn.value && regPassword.length < 6) ||
                                    fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                                    age.isBlank() || age == "0" || birthDate.isBlank() ||
                                    birthTime.isBlank() || birthPlace.isBlank() || height.isBlank() || currentCity.isBlank() ||
                                    parentPhoneContact.isBlank() || parentPhoneContact.length != 10 ||
                                    myProfile.profileImageUrl.isBlank() ||
                                    myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank() ||
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
                                if (regEmail.isBlank() || fullName.isBlank() || fatherName.isBlank() || grandfatherName.isBlank() || motherName.isBlank() ||
                                    myProfile.profileImageUrl.isBlank() ||
                                    myProfile.aadharFrontUrl.isBlank() || myProfile.aadharBackUrl.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "પ્રથમ ચરણની માહિતી અપૂર્ણ છે! કૃપા કરીને પ્રથમ ચરણ તપાસો.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentStep = 1
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
                                val updated = myProfile.copy(
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
                                    viewModel.updateMyProfile(updated)
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
                                    isSaving = false
                                    isEditing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("step_next_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("સાચવી રહ્યા છીએ...")
                    } else if (currentStep < 4) {
                        Text("આગળનું ચરણ", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
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
    maritalStatus: String, onMaritalStatusChange: (String) -> Unit,
    maritalStatusOther: String, onMaritalStatusOtherChange: (String) -> Unit,
    viewModel: MatrimonyViewModel,
    showErrors: Boolean = false
) {
    val context = LocalContext.current
    val myProfile by viewModel.myProfile.collectAsState()
    var isUploading by remember { mutableStateOf(false) }
    var isUploadingFront by remember { mutableStateOf(false) }
    var isUploadingBack by remember { mutableStateOf(false) }

    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var subDistrictExpanded by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            viewModel.uploadProfileImage(it) { uploadedUrl ->
                isUploading = false
                if (uploadedUrl != null) {
                    Toast.makeText(context, "તસવીર સફળતાપૂર્વક અપલોડ થઈ ગઈ!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "અપલોડ નિષ્ફળ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val aadharFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploadingFront = true
            viewModel.uploadAadharFrontImage(it) { uploadedUrl ->
                isUploadingFront = false
                if (uploadedUrl != null) {
                    Toast.makeText(context, "આધાર ફ્રન્ટ ફોટો અપલોડ થયો!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "આધાર ફ્રન્ટ અપલોડ નિષ્ફળ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val aadharBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploadingBack = true
            viewModel.uploadAadharBackImage(it) { uploadedUrl ->
                isUploadingBack = false
                if (uploadedUrl != null) {
                    Toast.makeText(context, "આધાર બેક ફોટો અપલોડ થયો!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "આધાર બેક અપલોડ નિષ્ફળ", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

            // Firebase Storage Profile Picture Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RoyalMaroon.copy(alpha = 0.15f))
                            .border(1.5.dp, RoyalGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (myProfile.profileImageUrl.isNotBlank()) {
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
                            text = "પ્રોફાઇલ તસવીર (ફાયરબેઝ સ્ટોરેજ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = RoyalMaroon
                        )
                        Text(
                            text = if (myProfile.profileImageUrl.isNotBlank()) "તસવીર લિંક થયેલ છે" else "ગેલરીમાંથી તસવીર ઉમેરો",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("અપલોડ", fontSize = 12.sp)
                        }
                    }
                }
            }

            // MANDATORY AADHAR PHOTOS SECTION
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
                            onUploadClick = { aadharFrontLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )

                        AadharPhotoUploadCard(
                            label = "આધાર બેક (પાછળ)",
                            imageUrl = myProfile.aadharBackUrl,
                            isUploading = isUploadingBack,
                            onUploadClick = { aadharBackLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Registration Account Credentials Section (Mobile Number & OTP)
            var regOtpSent by remember { mutableStateOf(false) }
            var regGeneratedOtp by remember { mutableStateOf("") }
            var regEnteredOtp by remember { mutableStateOf("") }
            var isRegOtpVerified by remember { mutableStateOf(viewModel.isLoggedIn.value) }

            OutlinedTextField(
                value = regEmail,
                onValueChange = {
                    onRegEmailChange(it)
                    isRegOtpVerified = false
                },
                label = { Text("લૉગિન મોબાઈલ નંબર (10 Digit Mobile Number - ફરજિયાત *)") },
                placeholder = { Text("૧૦ અંકનો મોબાઈલ નંબર દાખલ કરો") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_email_input"),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                singleLine = true,
                isError = showErrors && (regEmail.isBlank() || regEmail.length != 10 || !regEmail.all { it.isDigit() }),
                supportingText = {
                    if (showErrors && (regEmail.isBlank() || regEmail.length != 10 || !regEmail.all { it.isDigit() })) {
                        Text("કૃપા કરીને ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો", color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
            )

            // OTP Verification Box
            if (!isRegOtpVerified) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RoyalMaroon)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("મોબાઈલ નંબર ચકાસણી (OTP Verification)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalMaroon)
                        }

                        if (!regOtpSent) {
                            Button(
                                onClick = {
                                    if (regEmail.trim().length == 10 && regEmail.trim().all { it.isDigit() }) {
                                        Toast.makeText(context, "ફાયરબેઝ દ્વારા OTP મોકલાઈ રહ્યો છે...", Toast.LENGTH_SHORT).show()
                                        viewModel.sendFirebaseOtp(
                                            context = context,
                                            phoneNumber = regEmail.trim(),
                                            onOtpSent = { verId, testCode ->
                                                regGeneratedOtp = verId
                                                regOtpSent = true
                                                val msg = if (testCode != null) "OTP મોકલેલ છે: $testCode (અથવા 123456 વાપરો)" else "ફાયરબેઝ SMS દ્વારા OTP તમારા મોબાઈલ પર મોકલાઈ ગયો છે!"
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            },
                                            onInstantSuccess = {
                                                isRegOtpVerified = true
                                                Toast.makeText(context, "મોબાઈલ નંબર ઓટોમેટિક ચકાસાયો!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "કૃપા કરીને પ્રથમ ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OTP મોકલો (Send OTP)")
                            }
                        } else {
                            val activeCode = if (regGeneratedOtp.contains("_FB_")) regGeneratedOtp.substringAfter("_FB_") else if (regGeneratedOtp.startsWith("FALLBACK_")) regGeneratedOtp.removePrefix("FALLBACK_") else "123456"

                            Surface(
                                color = SurfaceCream,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "તમારો ચકાસણી OTP કોડ: $activeCode",
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "જો SMS ન મળે, તો ઉપર દર્શાવેલ કોડ ($activeCode) અથવા ટેસ્ટ કોડ '123456' દાખલ કરો.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = regEnteredOtp,
                                onValueChange = { regEnteredOtp = it },
                                label = { Text("૬ અંકનો OTP દાખલ કરો") },
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (regEnteredOtp.isNotBlank()) {
                                        viewModel.verifyFirebaseOtp(
                                            verificationId = regGeneratedOtp,
                                            enteredCode = regEnteredOtp,
                                            onSuccess = {
                                                isRegOtpVerified = true
                                                Toast.makeText(context, "મોબાઈલ નંબર સફળતાપૂર્વક ચકાસાયો! (Mobile verified)", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "કૃપા કરીને OTP દાખલ કરો", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OTP ચકાસો (Verify OTP)")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Birth Date Picker Field
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("જન્મ તારીખ") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val cal = Calendar.getInstance()
                            val dpd = DatePickerDialog(
                                context,
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
                        },
                    leadingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            val dpd = DatePickerDialog(
                                context,
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
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Birth Date")
                        }
                    }
                )

                // Birth Time Picker Field
                OutlinedTextField(
                    value = birthTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("જન્મ સમય") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val tpd = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                    val h12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                    val formatted = String.format("%02d:%02d %s", h12, minute, amPm)
                                    onBirthTimeChange(formatted)
                                },
                                8, 30, false
                            )
                            tpd.show()
                        },
                    leadingIcon = {
                        IconButton(onClick = {
                            val tpd = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                    val h12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                    val formatted = String.format("%02d:%02d %s", h12, minute, amPm)
                                    onBirthTimeChange(formatted)
                                },
                                8, 30, false
                            )
                            tpd.show()
                        }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick Birth Time")
                        }
                    }
                )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stateExpanded = true },
                            trailingIcon = {
                                IconButton(onClick = { stateExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select State")
                                }
                            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { districtExpanded = true },
                            trailingIcon = {
                                IconButton(onClick = { districtExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select District")
                                }
                            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { subDistrictExpanded = true },
                            trailingIcon = {
                                IconButton(onClick = { subDistrictExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Sub-District")
                                }
                            }
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
                        .clickable { bloodGroupExpanded = true }
                        .testTag("blood_group_dropdown_input"),
                    leadingIcon = {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = "Blood Group Icon",
                            tint = Color(0xFFD32F2F)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { bloodGroupExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Blood Group")
                        }
                    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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

// STEP 4: Family Details & AI Biodata
@Composable
fun Step4FamilyAndBio(
    familyDetails: String, onFamilyDetailsChange: (String) -> Unit,
    aboutMe: String, onAboutMeChange: (String) -> Unit,
    rashi: String, onRashiChange: (String) -> Unit,
    voiceNotesInput: String, onVoiceNotesChange: (String) -> Unit,
    isGeneratingBio: Boolean,
    onGenerateBio: () -> Unit,
    showErrors: Boolean = false
) {
    var rashiExpanded by remember { mutableStateOf(false) }
    val rashiOptions = listOf(
        "Mesha", "Vrishabha", "Mithuna", "Karka",
        "Simha", "Kanya", "Tula", "Vrishchika",
        "Dhanu", "Makara", "Kumbha", "Meena"
    )

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rashiExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { rashiExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Rashi")
                            }
                        }
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
}

@Composable
fun AadharPhotoUploadCard(
    label: String,
    imageUrl: String,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (imageUrl.isNotBlank()) Color(0xFFE8F5E9) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (imageUrl.isNotBlank()) VerifiedGreen else Color.LightGray
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
            if (imageUrl.isNotBlank()) {
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
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedGreen
                )
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
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("અપલોડ", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

