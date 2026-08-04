package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    profileId: String,
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val profiles by viewModel.allProfiles.collectAsState()
    val matchProfile = profiles.find { it.id == profileId }

    val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
    val myId = myProfile.id.ifBlank { authUid ?: "USER_ME" }
    val chatRoomId = remember(myId, profileId) { if (myId < profileId) "${myId}_${profileId}" else "${profileId}_${myId}" }

    val chatMessagesFlow = remember(chatRoomId) { viewModel.repository.getChatMessages(chatRoomId) }
    val messages by chatMessagesFlow.collectAsState(initial = emptyList())

    val userInterests by viewModel.userInterests.collectAsState(initial = emptyList())
    val isBlocked = viewModel.isUserBlocked(profileId)
    val canChat = remember(profileId, userInterests, isBlocked) { !isBlocked && viewModel.canChatWith(profileId) }

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var msgToDeleteId by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val icebreakers = listOf(
        "જય શ્રી કૃષ્ણા! / રામ રામ જી",
        "શું આપણે પરિવાર અને ગોત્ર વિગતો શેર કરી શકીએ?",
        "અમારા પરિવારને આપની બાયોડેટા અને વતન પસંદ આવ્યા.",
        "શું આપણે ફોન કોલ પર વાત કરી શકીએ?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, RoyalGold, CircleShape)
                        ) {
                            val imgUrl = matchProfile?.getEffectiveProfileImageUrl() ?: ""
                            if (imgUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = matchProfile?.fullName ?: "",
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                    error = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (matchProfile != null && matchProfile.photoRes != 0) {
                                Image(
                                    painter = painterResource(id = matchProfile.photoRes),
                                    contentDescription = matchProfile.fullName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = matchProfile?.fullName ?: "ચૌધરી મિલન સાથી",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${matchProfile?.subCaste} • ગોત્ર: ${matchProfile?.gotra}",
                                fontSize = 11.sp,
                                color = WarmSaffron
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "પાછા જાઓ")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "વિકલ્પો")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isBlocked) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("સભ્યને અનબ્લોક કરો", color = VerifiedGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.unblockUser(profileId)
                                    android.widget.Toast.makeText(context, "સભ્ય અનબ્લોક કરવામાં આવ્યો છે.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("સભ્યને બ્લોક કરો અને રસ-પ્રસ્તાવ અટકાવો", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    showBlockDialog = true
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ચેટ ક્લિયર કરો (Clear Chat)", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                showClearChatDialog = true
                            }
                        )
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
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = false
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("આદરપૂર્વક કૌટુંબિક વાતચીત શરૂ કરો", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ગોત્ર, સંસ્કાર અને લગ્ન પ્રસ્તાવ અંગે વાતચીત કરો.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        myId = myId,
                        onDeleteClick = { msgToDeleteId = msg.id }
                    )
                }
            }

            if (isBlocked) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "આ સભ્યને બ્લોક કરવામાં આવ્યા છે",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "આ સભ્ય તમને રસ-પ્રસ્તાવ અથવા ચેટ મેસેજ મોકલી શકશે નહીં.",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.unblockUser(profileId)
                                android.widget.Toast.makeText(context, "સભ્ય અનબ્લોક કરવામાં આવ્યો છે.", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerifiedGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("સભ્યને અનબ્લોક કરો", fontWeight = FontWeight.Bold, color = VerifiedGreen)
                        }
                    }
                }
            } else if (!canChat) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmSaffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ચેટ સુવિધા લૉક છે",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RoyalMaroon
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "બંને સભ્યોએ એકબીજાના રસ-પ્રસ્તાવ (Interest Request) સ્વીકાર્યા બાદ જ સક્રિય ચેટ શક્ય બનશે.",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.sendInterest(profileId) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("રસ-પ્રસ્તાવ મોકલો", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Quick Icebreaker Chips Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icebreakers) { text ->
                        SuggestionChip(
                            onClick = { viewModel.sendChatMessage(profileId, text) },
                            label = { Text(text, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = SoftGold,
                                labelColor = RoyalMaroon
                            )
                        )
                    }
                }

                // Text Input Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 6.dp,
                    color = SurfaceCream
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.sendChatMessage(profileId, "🎙️ [વોઇસ મેસેજ મોકલ્યો]", isVoice = true) }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "વોઇસ ઇનપુટ", tint = WarmSaffron)
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("સંદેશ ટાઇપ કરો...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalMaroon)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendChatMessage(profileId, textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(RoyalMaroon, CircleShape)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            icon = {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("રસ-પ્રસ્તાવ રદ કરો અને સંપર્ક બ્લોક કરો", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "શું તમે ખરેખર ${matchProfile?.fullName ?: "આ સભ્ય"} નો રસ-પ્રસ્તાવ રદ કરવા અને ચેટ/સંપર્ક અવરોધિત (Block) કરવા માંગો છો? આનાથી સંદેશાઓ મોકલવાનું તથા વાલીનો સંપર્ક નંબર જોવાનું બંધ થઈ જશે.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeInterestAndBlock(profileId)
                        showBlockDialog = false
                        android.widget.Toast.makeText(
                            context,
                            "રસ-પ્રસ્તાવ રદ કરાયો અને સભ્ય બ્લોક થયો છે.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("હા, રદ અને બ્લોક કરો", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockDialog = false }) {
                    Text("રદ કરો")
                }
            }
        )
    }

    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = {
                Text("ચેટ ક્લિયર કરો (Clear Chat)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon)
            },
            text = {
                Text(
                    "શું તમે ખરેખર આ સભ્ય સાથેની તમામ ચેટ મેસેજ કાઢી નાખવા માંગો છો? આ પ્રક્રિયા પછી મેસેજ પાછા મેળવી શકાશે નહીં.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearChatRoom(profileId)
                        showClearChatDialog = false
                        android.widget.Toast.makeText(context, "તમામ ચેટ ક્લિયર કરવામાં આવી છે.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("હા, ચેટ ક્લિયર કરો", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearChatDialog = false }) {
                    Text("રદ કરો")
                }
            }
        )
    }

    if (msgToDeleteId != null) {
        val targetMsgId = msgToDeleteId!!
        AlertDialog(
            onDismissRequest = { msgToDeleteId = null },
            title = {
                Text("મેસેજ કાઢી નાખો (Delete Message)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon)
            },
            text = {
                Text("શું તમે આ મેસેજ કાઢી નાખવા માંગો છો?", fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChatMessage(targetMsgId)
                        msgToDeleteId = null
                        android.widget.Toast.makeText(context, "મેસેજ કાઢી નાખવામાં આવ્યો છે.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ડિલીટ કરો", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { msgToDeleteId = null }) {
                    Text("રદ કરો")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    myId: String,
    onDeleteClick: () -> Unit
) {
    val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
    val isMe = message.isUserSender ||
            (message.senderId.isNotBlank() && (
                message.senderId == myId ||
                (authUid != null && message.senderId == authUid) ||
                (myId == "USER_ME" && message.senderId == "USER_ME")
            ))

    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isMe) Color(0xFFDCF8C6) else Color(0xFFFFFFFF)
    val borderColor = if (isMe) Color(0xFFC8E6C9) else Color(0xFFE0E0E0)
    val textColor = Color(0xFF1F2937)
    val timestampColor = if (isMe) Color(0xFF556B2F) else Color(0xFF757575)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMe) 48.dp else 0.dp,
                end = if (isMe) 0.dp else 48.dp
            ),
        contentAlignment = alignment
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (message.isVoiceNote) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = RoyalMaroon)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Note (0:06 sec)", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        color = timestampColor,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Sent",
                            tint = Color(0xFF34B7F1),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Message",
                            tint = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
