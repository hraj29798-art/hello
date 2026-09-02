package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CoupleRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("couple_chat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSCODE = "passcode_pin"
        private const val KEY_USER_ID = "current_user_id"
        private const val KEY_DISPLAY_NAME = "current_display_name"
        private const val KEY_INVITE_CODE = "current_invite_code"
        private const val KEY_ROOM_ID = "current_room_id"
        private const val KEY_USER_MOOD = "current_user_mood"
        private const val KEY_PROMPT_INDEX = "current_prompt_index"
        private const val DEFAULT_PIN = "1234"
        private const val DEFAULT_ROOM = "LOVE2026"
    }

    fun getPasscode(): String {
        return prefs.getString(KEY_PASSCODE, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setPasscode(newPin: String) {
        prefs.edit().putString(KEY_PASSCODE, newPin).apply()
    }

    fun getUserId(): String {
        var id = prefs.getString(KEY_USER_ID, null)
        if (id == null) {
            id = "user_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString(KEY_USER_ID, id).apply()
        }
        return id
    }

    fun setUserId(id: String) {
        prefs.edit().putString(KEY_USER_ID, id).apply()
    }

    fun getDisplayName(): String {
        return prefs.getString(KEY_DISPLAY_NAME, "Alex") ?: "Alex"
    }

    fun setDisplayName(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
    }

    fun getInviteCode(): String {
        var code = prefs.getString(KEY_INVITE_CODE, null)
        if (code == null) {
            val randomSuffix = (1000..9999).random()
            val nameClean = getDisplayName().uppercase().filter { it.isLetter() }.take(4).ifBlank { "PAIR" }
            code = "$nameClean$randomSuffix"
            prefs.edit().putString(KEY_INVITE_CODE, code).apply()
        }
        return code
    }

    fun setInviteCode(code: String) {
        prefs.edit().putString(KEY_INVITE_CODE, code).apply()
    }

    fun getRoomId(): String {
        return prefs.getString(KEY_ROOM_ID, DEFAULT_ROOM) ?: DEFAULT_ROOM
    }

    fun setRoomId(roomId: String) {
        prefs.edit().putString(KEY_ROOM_ID, roomId).apply()
    }

    fun getMood(): PartnerMood {
        val moodId = prefs.getString(KEY_USER_MOOD, "happy") ?: "happy"
        return DEFAULT_MOODS.find { it.id == moodId } ?: DEFAULT_MOODS[0]
    }

    fun setMood(mood: PartnerMood) {
        prefs.edit().putString(KEY_USER_MOOD, mood.id).apply()
    }

    fun getPromptIndex(): Int {
        return prefs.getInt(KEY_PROMPT_INDEX, 0)
    }

    fun setPromptIndex(index: Int) {
        prefs.edit().putInt(KEY_PROMPT_INDEX, index).apply()
    }

    fun getInitialMessages(): List<ChatMessage> {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val now = System.currentTimeMillis()
        val time1 = timeFormat.format(Date(now - 1000 * 60 * 35))
        val time2 = timeFormat.format(Date(now - 1000 * 60 * 20))
        val time3 = timeFormat.format(Date(now - 1000 * 60 * 5))

        return listOf(
            ChatMessage(
                senderId = "partner_1",
                senderName = "Sam",
                text = "Hey my love! ❤️ How's your day going so far?",
                timestamp = time1
            ),
            ChatMessage(
                senderId = getUserId(),
                senderName = getDisplayName(),
                text = "Much better now that I'm talking to you! ✨ Just finishing up a few things.",
                timestamp = time2,
                reaction = "🥰"
            ),
            ChatMessage(
                senderId = "partner_1",
                senderName = "Sam",
                text = "Don't forget we have our cozy dinner date tonight! Can't wait 🍰💖",
                timestamp = time3,
                reaction = "💖"
            )
        )
    }

    fun getInitialNotes(): List<StickyNote> {
        val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val now = System.currentTimeMillis()
        val d1 = dateFormat.format(Date(now - 1000 * 60 * 60 * 3))
        val d2 = dateFormat.format(Date(now - 1000 * 60 * 60 * 24))

        return listOf(
            StickyNote(
                authorId = "partner_1",
                authorName = "Sam",
                content = "Reminder: You are the sweetest person in the universe! Drink some water and have an amazing day ✨",
                dateString = d1,
                colorIndex = 0
            ),
            StickyNote(
                authorId = getUserId(),
                authorName = getDisplayName(),
                content = "Saved a slice of that strawberry shortcake in the fridge just for you 🍓",
                dateString = d2,
                colorIndex = 1
            ),
            StickyNote(
                authorId = "partner_1",
                authorName = "Sam",
                content = "Song of the day for us: 'Until I Found You' 🎶 Play it whenever you miss me!",
                dateString = d1,
                colorIndex = 2
            )
        )
    }
}
