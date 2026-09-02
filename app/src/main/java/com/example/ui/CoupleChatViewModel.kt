package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.CoupleRepository
import com.example.data.DEFAULT_MOODS
import com.example.data.FirebaseChatManager
import com.example.data.PartnerMood
import com.example.data.ROMANTIC_PROMPTS
import com.example.data.StickyNote
import com.example.data.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CoupleChatUiState(
    val isUnlocked: Boolean = false,
    val passcode: String = "1234",
    val enteredPin: String = "",
    val isPinError: Boolean = false,
    val currentUserProfile: UserProfile = UserProfile(),
    val currentRoomId: String = "LOVE2026",
    val partnerName: String = "Partner",
    val partnerMoodEmoji: String = "🥰",
    val partnerMoodText: String = "In Love",
    val messages: List<ChatMessage> = emptyList(),
    val stickyNotes: List<StickyNote> = emptyList(),
    val currentPromptIndex: Int = 0,
    val showMoodDialog: Boolean = false,
    val showStickyDrawer: Boolean = false,
    val showNewNoteDialog: Boolean = false,
    val showProfileDialog: Boolean = false,
    val showJoinRoomDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val toastMessage: String? = null,
    val isConnectedToFirestore: Boolean = true
) {
    val currentPrompt: String
        get() = ROMANTIC_PROMPTS.getOrElse(currentPromptIndex) { ROMANTIC_PROMPTS.first() }
}

class CoupleChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CoupleRepository(application)
    private val firebaseManager = FirebaseChatManager(application)

    private val _uiState = MutableStateFlow(CoupleChatUiState())
    val uiState: StateFlow<CoupleChatUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val noteDateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    private var messagesJob: Job? = null
    private var notesJob: Job? = null
    private var moodJob: Job? = null
    private var loveTapJob: Job? = null

    init {
        initAuthAndData()
    }

    private fun initAuthAndData() {
        val passcode = repository.getPasscode()
        val userId = repository.getUserId()
        val displayName = repository.getDisplayName()
        val inviteCode = repository.getInviteCode()
        val currentRoom = repository.getRoomId()
        val mood = repository.getMood()
        val promptIdx = repository.getPromptIndex()
        val initialMsgs = repository.getInitialMessages()
        val initialNotes = repository.getInitialNotes()

        val profile = UserProfile(
            userId = userId,
            displayName = displayName,
            inviteCode = inviteCode,
            moodEmoji = mood.emoji,
            moodText = mood.label
        )

        _uiState.update {
            it.copy(
                passcode = passcode,
                currentUserProfile = profile,
                currentRoomId = currentRoom,
                currentPromptIndex = promptIdx,
                messages = initialMsgs,
                stickyNotes = initialNotes
            )
        }

        viewModelScope.launch {
            // Sign in anonymously with Firebase Auth if needed
            val fbUser = firebaseManager.currentUser ?: firebaseManager.signInAnonymously()
            if (fbUser != null) {
                repository.setUserId(fbUser.uid)
                val updatedProfile = profile.copy(userId = fbUser.uid, email = fbUser.email)
                _uiState.update { it.copy(currentUserProfile = updatedProfile) }
                firebaseManager.saveUserProfile(updatedProfile)
                joinRoom(currentRoom)
            } else {
                // Fallback to local user ID
                joinRoom(currentRoom)
            }
        }
    }

    fun joinRoom(newRoomId: String) {
        val cleanRoomId = newRoomId.trim().uppercase().ifBlank { "LOVE2026" }
        repository.setRoomId(cleanRoomId)
        _uiState.update { it.copy(currentRoomId = cleanRoomId, showJoinRoomDialog = false) }

        viewModelScope.launch {
            val user = _uiState.value.currentUserProfile
            firebaseManager.joinOrCreateRoom(cleanRoomId, user.userId, user.displayName)
            listenToRoomData(cleanRoomId)
        }
    }

    private fun listenToRoomData(roomId: String) {
        messagesJob?.cancel()
        notesJob?.cancel()
        moodJob?.cancel()
        loveTapJob?.cancel()

        // 1. Messages Listener
        messagesJob = viewModelScope.launch {
            firebaseManager.observeMessages(roomId).collect { remoteMsgs ->
                if (remoteMsgs.isNotEmpty()) {
                    _uiState.update { it.copy(messages = remoteMsgs) }
                }
            }
        }

        // 2. Sticky Notes Listener
        notesJob = viewModelScope.launch {
            firebaseManager.observeStickyNotes(roomId).collect { remoteNotes ->
                if (remoteNotes.isNotEmpty()) {
                    _uiState.update { it.copy(stickyNotes = remoteNotes) }
                }
            }
        }

        // 3. Partner Mood Listener
        val myUid = _uiState.value.currentUserProfile.userId
        moodJob = viewModelScope.launch {
            firebaseManager.observePartnerMood(roomId, myUid).collect { data ->
                val pName = data["userName"] as? String ?: "Partner"
                val pEmoji = data["emoji"] as? String ?: "🥰"
                val pText = data["text"] as? String ?: "Connected"
                _uiState.update {
                    it.copy(
                        partnerName = pName,
                        partnerMoodEmoji = pEmoji,
                        partnerMoodText = pText
                    )
                }
            }
        }

        // 4. Love Tap Listener
        loveTapJob = viewModelScope.launch {
            firebaseManager.observeLoveTap(roomId, myUid).collect { alert ->
                if (alert != null) {
                    showToast(alert)
                }
            }
        }
    }

    fun onPinDigitEntered(digit: Char) {
        val currentPin = _uiState.value.enteredPin
        if (currentPin.length < 4) {
            val updated = currentPin + digit
            _uiState.update { it.copy(enteredPin = updated, isPinError = false) }
            if (updated.length == 4) {
                verifyPin(updated)
            }
        }
    }

    fun onPinDelete() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = current.dropLast(1), isPinError = false) }
        }
    }

    fun onPinClear() {
        _uiState.update { it.copy(enteredPin = "", isPinError = false) }
    }

    private fun verifyPin(pin: String) {
        if (pin == _uiState.value.passcode) {
            _uiState.update { it.copy(isUnlocked = true, enteredPin = "", isPinError = false) }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isPinError = true) }
                delay(600)
                _uiState.update { it.copy(enteredPin = "", isPinError = false) }
            }
        }
    }

    fun lockSpace() {
        _uiState.update { it.copy(isUnlocked = false, enteredPin = "") }
    }

    fun updateDisplayName(newName: String) {
        if (newName.isBlank()) return
        val cleanName = newName.trim()
        repository.setDisplayName(cleanName)
        val updatedProfile = _uiState.value.currentUserProfile.copy(displayName = cleanName)
        _uiState.update { it.copy(currentUserProfile = updatedProfile, showProfileDialog = false) }

        viewModelScope.launch {
            firebaseManager.saveUserProfile(updatedProfile)
            firebaseManager.joinOrCreateRoom(_uiState.value.currentRoomId, updatedProfile.userId, cleanName)
            showToast("Display name updated to $cleanName")
        }
    }

    fun updateMood(mood: PartnerMood) {
        repository.setMood(mood)
        val currentProfile = _uiState.value.currentUserProfile.copy(
            moodEmoji = mood.emoji,
            moodText = mood.label
        )
        _uiState.update { it.copy(currentUserProfile = currentProfile, showMoodDialog = false) }

        viewModelScope.launch {
            firebaseManager.saveUserProfile(currentProfile)
            firebaseManager.updateMood(
                roomId = _uiState.value.currentRoomId,
                userId = currentProfile.userId,
                userName = currentProfile.displayName,
                emoji = mood.emoji,
                text = mood.label
            )
        }
    }

    fun nextDailyPrompt() {
        val nextIndex = (_uiState.value.currentPromptIndex + 1) % ROMANTIC_PROMPTS.size
        repository.setPromptIndex(nextIndex)
        _uiState.update { it.copy(currentPromptIndex = nextIndex) }
    }

    fun sharePromptToChat() {
        val prompt = _uiState.value.currentPrompt
        val text = "💌 Daily Question: \"$prompt\""
        sendMessage(text, isDailyPrompt = true)
    }

    fun sendMessage(text: String, isDailyPrompt: Boolean = false) {
        if (text.isBlank()) return
        val user = _uiState.value.currentUserProfile
        val roomId = _uiState.value.currentRoomId
        val newMsg = ChatMessage(
            senderId = user.userId,
            senderName = user.displayName,
            text = text.trim(),
            timestamp = timeFormat.format(Date()),
            isDailyPromptReply = isDailyPrompt
        )

        // Optimistic update
        _uiState.update { it.copy(messages = it.messages + newMsg) }

        viewModelScope.launch {
            firebaseManager.sendMessage(roomId, newMsg)
        }
    }

    fun sendLoveTap() {
        val user = _uiState.value.currentUserProfile
        val roomId = _uiState.value.currentRoomId
        showToast("💓 Sent a love squeeze!")
        viewModelScope.launch {
            firebaseManager.sendLoveTap(roomId, user.userId, user.displayName)
            sendMessage("💓 *Sent you a soft heart squeeze!*")
        }
    }

    fun reactToMessage(messageId: String, emoji: String) {
        val targetMsg = _uiState.value.messages.find { it.id == messageId } ?: return
        val newReaction = if (targetMsg.reaction == emoji) null else emoji

        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(reaction = newReaction) else msg
                }
            )
        }

        viewModelScope.launch {
            firebaseManager.updateReaction(_uiState.value.currentRoomId, messageId, newReaction)
        }
    }

    fun addStickyNote(content: String, colorIndex: Int) {
        if (content.isBlank()) return
        val user = _uiState.value.currentUserProfile
        val roomId = _uiState.value.currentRoomId
        val newNote = StickyNote(
            authorId = user.userId,
            authorName = user.displayName,
            content = content.trim(),
            dateString = noteDateFormat.format(Date()),
            colorIndex = colorIndex
        )

        _uiState.update {
            it.copy(
                stickyNotes = listOf(newNote) + it.stickyNotes,
                showNewNoteDialog = false
            )
        }

        viewModelScope.launch {
            firebaseManager.addStickyNote(roomId, newNote)
        }
    }

    fun deleteStickyNote(noteId: String) {
        val roomId = _uiState.value.currentRoomId
        _uiState.update {
            it.copy(stickyNotes = it.stickyNotes.filter { note -> note.id != noteId })
        }
        viewModelScope.launch {
            firebaseManager.deleteStickyNote(roomId, noteId)
        }
    }

    fun updatePasscode(newPin: String) {
        if (newPin.length == 4) {
            repository.setPasscode(newPin)
            _uiState.update { it.copy(passcode = newPin, showSettingsDialog = false) }
            showToast("Passcode updated successfully")
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(toastMessage = msg) }
            delay(2800)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    fun toggleMoodDialog(show: Boolean) { _uiState.update { it.copy(showMoodDialog = show) } }
    fun toggleStickyDrawer(show: Boolean) { _uiState.update { it.copy(showStickyDrawer = show) } }
    fun toggleNewNoteDialog(show: Boolean) { _uiState.update { it.copy(showNewNoteDialog = show) } }
    fun toggleProfileDialog(show: Boolean) { _uiState.update { it.copy(showProfileDialog = show) } }
    fun toggleJoinRoomDialog(show: Boolean) { _uiState.update { it.copy(showJoinRoomDialog = show) } }
    fun toggleSettingsDialog(show: Boolean) { _uiState.update { it.copy(showSettingsDialog = show) } }
}
