package com.example.data

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class UserProfile(
    val userId: String = "",
    val displayName: String = "Alex",
    val email: String? = null,
    val inviteCode: String = "",
    val moodEmoji: String = "💖",
    val moodText: String = "Happy & Loved",
    val avatarColor: Long = 0xFFFFB3C6
)

data class ChatRoom(
    val roomId: String = "",
    val name: String = "Private Space",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PartnerMood(
    val id: String,
    val emoji: String,
    val label: String,
    val colorHex: Long
)

val DEFAULT_MOODS = listOf(
    PartnerMood("happy", "💖", "Happy & Loved", 0xFFFFB3C6),
    PartnerMood("loving", "🥰", "In Love", 0xFFFFC2D1),
    PartnerMood("attention", "🥺", "Need Attention", 0xFFFFD1BA),
    PartnerMood("busy", "⏳", "Busy Working", 0xFFFFF1BD),
    PartnerMood("tired", "😴", "Tired & Sleepy", 0xFFD7C0FF),
    PartnerMood("missing", "💭", "Missing You", 0xFFC1E7E3),
    PartnerMood("cozy", "☕", "Cozy & Relaxed", 0xFFFFE5D9),
    PartnerMood("excited", "🎉", "Excited", 0xFFFFCCD5)
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: String = "",
    val reaction: String? = null,
    val isDailyPromptReply: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class StickyNote(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String = "",
    val authorName: String = "Me",
    val content: String = "",
    val dateString: String = "",
    val colorIndex: Int = 0, // 0: Pink, 1: Lavender, 2: Yellow, 3: Peach, 4: Mint
    val createdAt: Long = System.currentTimeMillis()
)

val PASTEL_NOTE_COLORS = listOf(
    Color(0xFFFFE0E9), // Pastel Pink
    Color(0xFFEDE4FF), // Pastel Lavender
    Color(0xFFFFF7D1), // Pastel Butter Yellow
    Color(0xFFFFE8DC), // Pastel Peach
    Color(0xFFE0F4F2)  // Pastel Mint
)

val ROMANTIC_PROMPTS = listOf(
    "What was the exact moment you realized you had feelings for me?",
    "If we could escape to anywhere in the world for 48 hours right now, where are we going?",
    "What is your absolute favorite memory of us from this year?",
    "What is a small, everyday habit of mine that secretly makes you smile?",
    "If our love story was a movie, what song would play during the opening credits?",
    "What is something new you would love for us to try together this month?",
    "When was the last time I made your heart skip a beat?",
    "What is one dream you haven't told many people about that you want us to achieve?",
    "What food or dish always reminds you of our sweetest dates?",
    "What is the most comforting thing I can do for you when you've had a tough day?",
    "What is your favorite picture or video of the two of us?",
    "What compliment from me meant the absolute most to you?",
    "If you could relive any single date we've ever been on, which one would it be?",
    "What three words best describe the way I make you feel?",
    "What is a silly or funny inside joke of ours that still makes you laugh out loud?"
)
