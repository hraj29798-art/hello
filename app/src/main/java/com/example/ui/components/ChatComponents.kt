package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.UserProfile
import com.example.ui.theme.ChatBubbleReceiver
import com.example.ui.theme.ChatBubbleSender
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelLavenderDark
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPinkDark
import com.example.ui.theme.PastelPinkLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReactionMenu by remember { mutableStateOf(false) }

    val bubbleColor = if (isCurrentUser) ChatBubbleSender else ChatBubbleReceiver
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Sender label for receiver messages
        if (!isCurrentUser) {
            Text(
                text = message.senderName.ifBlank { "Partner" },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }

        Box {
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clickable { showReactionMenu = !showReactionMenu }
                    .testTag("chat_bubble_${message.id}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (message.isDailyPromptReply) {
                        Text(
                            text = "✨ Daily Prompt Answer",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PastelPinkDark,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = message.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.8f)
                        )
                        if (isCurrentUser) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = PastelPinkDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Attached reaction badge
            if (message.reaction != null) {
                Box(
                    modifier = Modifier
                        .align(if (isCurrentUser) Alignment.BottomStart else Alignment.BottomEnd)
                        .offset(x = if (isCurrentUser) (-6).dp else 6.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, PastelPink.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clickable { onReact(message.reaction) }
                        .testTag("reaction_badge_${message.id}")
                ) {
                    Text(text = message.reaction, fontSize = 12.sp)
                }
            }
        }

        // Quick reaction popup
        AnimatedVisibility(
            visible = showReactionMenu,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .border(1.dp, PastelPink.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("reaction_picker_menu")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("💖", "🥰", "🥺", "✨", "🔥", "🌸").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onReact(emoji)
                                    showReactionMenu = false
                                }
                                .padding(4.dp)
                                .testTag("react_with_$emoji")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onLoveTap: () -> Unit,
    onQuickEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.95f))
            .border(width = 1.dp, color = PastelPinkLight, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Quick emoji reaction bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("💖", "🥺", "😴", "🌸", "💌", "🧸", "🍰", "💍").forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onQuickEmoji(emoji) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("quick_emoji_$emoji")
                )
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Send Heart / Love Tap Button
            IconButton(
                onClick = onLoveTap,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PastelPinkLight)
                    .border(1.dp, PastelPink, CircleShape)
                    .testTag("love_tap_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Send Love Tap",
                    tint = PastelPinkDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Input Field
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Write a sweet message...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PastelPinkDark,
                    unfocusedBorderColor = PastelLavender,
                    focusedContainerColor = Color(0xFFFCF9FA),
                    unfocusedContainerColor = Color(0xFFFCF9FA)
                ),
                maxLines = 3
            )

            // Send Button
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank()) PastelPinkDark else PastelPink.copy(alpha = 0.4f))
                    .testTag("send_msg_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    onSaveName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(userProfile.displayName) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PastelPinkDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Profile & Invite Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Display Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Display Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )

                // Unique Invite Code Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelPinkLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Your Unique Invite Code:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userProfile.inviteCode,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PastelPinkDark
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Invite Code", userProfile.inviteCode)
                                    clipboard.setPrimaryClip(clip)
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_invite_code_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = PastelPinkDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Share this code with your partner so they can join your private chat space!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = "User ID: ${userProfile.userId.take(12)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveName(name) },
                colors = ButtonDefaults.buttonColors(containerColor = PastelPinkDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
            ) {
                Text("Close")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun JoinRoomDialog(
    currentRoomId: String,
    onJoinRoom: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredRoomId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = PastelPinkDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect to Chat Space",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your partner's Invite Code or a custom Room ID to pair:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = enteredRoomId,
                    onValueChange = { enteredRoomId = it.uppercase() },
                    placeholder = { Text("e.g. ALEX4821 or LOVE2026") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("room_id_input")
                )

                Text(
                    text = "Current Chat Space: #$currentRoomId",
                    style = MaterialTheme.typography.labelSmall,
                    color = PastelPinkDark,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onJoinRoom(enteredRoomId) },
                enabled = enteredRoomId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PastelPinkDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_join_room_btn")
            ) {
                Text("Connect Space ✨")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SettingsDialog(
    currentPasscode: String,
    onSavePasscode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf(currentPasscode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PastelPinkDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Secret Passcode Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Set a 4-digit PIN to keep your secret space locked and private:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("4-Digit Passcode") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("passcode_settings_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSavePasscode(pin) },
                enabled = pin.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = PastelPinkDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_settings_btn")
            ) {
                Text("Update Passcode")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
