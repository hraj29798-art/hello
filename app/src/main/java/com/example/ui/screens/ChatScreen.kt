package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CoupleChatUiState
import com.example.ui.CoupleChatViewModel
import com.example.ui.components.ChatBubbleItem
import com.example.ui.components.ChatInputBar
import com.example.ui.components.DailyPromptCard
import com.example.ui.components.JoinRoomDialog
import com.example.ui.components.MoodHeaderBar
import com.example.ui.components.MoodSelectorDialog
import com.example.ui.components.NewNoteDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StickyNotesSheet
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPinkDark
import com.example.ui.theme.PastelPinkLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: CoupleChatUiState,
    viewModel: CoupleChatViewModel,
    modifier: Modifier = Modifier
) {
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto toast message listener
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Scroll to latest message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PastelPinkLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PastelPinkDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Couple Space 💕",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "#${state.currentRoomId} • ${state.currentUserProfile.displayName} & ${state.partnerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Profile / Invite Code button
                    IconButton(
                        onClick = { viewModel.toggleProfileDialog(true) },
                        modifier = Modifier.testTag("open_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile & Invite Code",
                            tint = PastelPinkDark
                        )
                    }

                    // Join / Pair Room button
                    IconButton(
                        onClick = { viewModel.toggleJoinRoomDialog(true) },
                        modifier = Modifier.testTag("open_join_room_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Pair Space",
                            tint = PastelPinkDark
                        )
                    }

                    // Sticky notes button with count badge
                    IconButton(
                        onClick = { viewModel.toggleStickyDrawer(true) },
                        modifier = Modifier.testTag("open_notes_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.stickyNotes.isNotEmpty()) {
                                    Badge(containerColor = PastelPinkDark) {
                                        Text(state.stickyNotes.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAlt,
                                contentDescription = "Secret Notes",
                                tint = PastelPinkDark
                            )
                        }
                    }

                    // Settings Dialog button
                    IconButton(
                        onClick = { viewModel.toggleSettingsDialog(true) },
                        modifier = Modifier.testTag("open_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }

                    // Lock Space button
                    IconButton(
                        onClick = { viewModel.lockSpace() },
                        modifier = Modifier.testTag("lock_space_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Space",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                messageText = messageInput,
                onMessageChange = { messageInput = it },
                onSendMessage = {
                    viewModel.sendMessage(messageInput)
                    messageInput = ""
                },
                onLoveTap = {
                    viewModel.sendLoveTap()
                },
                onQuickEmoji = { emoji ->
                    messageInput += emoji
                }
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF9FA),
                            Color(0xFFFCF6F8),
                            Color(0xFFF7F3FB)
                        )
                    )
                )
        ) {
            // 1. Mood Header Bar
            MoodHeaderBar(
                state = state,
                onMoodClick = { viewModel.toggleMoodDialog(true) },
                onPairRoomClick = { viewModel.toggleJoinRoomDialog(true) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            // 2. Daily Romantic Prompt Card
            DailyPromptCard(
                promptText = state.currentPrompt,
                promptIndex = state.currentPromptIndex,
                onNextPrompt = { viewModel.nextDailyPrompt() },
                onSendToChat = { viewModel.sharePromptToChat() },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            // 3. Real-time Messages Timeline
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    val isCurrent = (msg.senderId == state.currentUserProfile.userId) ||
                            (msg.senderName == state.currentUserProfile.displayName)

                    ChatBubbleItem(
                        message = msg,
                        isCurrentUser = isCurrent,
                        onReact = { emoji -> viewModel.reactToMessage(msg.id, emoji) }
                    )
                }
            }
        }
    }

    // Modal Profile & Invite Code Dialog
    if (state.showProfileDialog) {
        ProfileDialog(
            userProfile = state.currentUserProfile,
            onSaveName = { newName -> viewModel.updateDisplayName(newName) },
            onDismiss = { viewModel.toggleProfileDialog(false) }
        )
    }

    // Modal Join / Pair Room Dialog
    if (state.showJoinRoomDialog) {
        JoinRoomDialog(
            currentRoomId = state.currentRoomId,
            onJoinRoom = { roomId -> viewModel.joinRoom(roomId) },
            onDismiss = { viewModel.toggleJoinRoomDialog(false) }
        )
    }

    // Modal Sticky Notes Sheet
    if (state.showStickyDrawer) {
        StickyNotesSheet(
            notes = state.stickyNotes,
            onAddNoteClick = { viewModel.toggleNewNoteDialog(true) },
            onDeleteNote = { noteId -> viewModel.deleteStickyNote(noteId) },
            onDismiss = { viewModel.toggleStickyDrawer(false) }
        )
    }

    // Modal New Note Dialog
    if (state.showNewNoteDialog) {
        NewNoteDialog(
            authorName = state.currentUserProfile.displayName,
            onSaveNote = { content, colorIndex ->
                viewModel.addStickyNote(content, colorIndex)
                scope.launch {
                    snackbarHostState.showSnackbar("Secret note pinned! 📌")
                }
            },
            onDismiss = { viewModel.toggleNewNoteDialog(false) }
        )
    }

    // Mood Selector Dialog
    if (state.showMoodDialog) {
        MoodSelectorDialog(
            currentMoodEmoji = state.currentUserProfile.moodEmoji,
            onMoodSelected = { mood -> viewModel.updateMood(mood) },
            onDismiss = { viewModel.toggleMoodDialog(false) }
        )
    }

    // Settings Dialog
    if (state.showSettingsDialog) {
        SettingsDialog(
            currentPasscode = state.passcode,
            onSavePasscode = { pin -> viewModel.updatePasscode(pin) },
            onDismiss = { viewModel.toggleSettingsDialog(false) }
        )
    }
}
