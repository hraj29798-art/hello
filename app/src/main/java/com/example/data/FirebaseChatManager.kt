package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatManager(private val context: Context) {

    private val auth: FirebaseAuth by lazy {
        ensureFirebaseInitialized()
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        ensureFirebaseInitialized()
        FirebaseFirestore.getInstance()
    }

    private fun ensureFirebaseInitialized() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyB0mhZsobsGWiDl1fu09CducoE85ftgDMI")
                    .setApplicationId("1:657243130603:web:6d8fc4abcf56c667080cae")
                    .setProjectId("chat-502f2")
                    .setStorageBucket("chat-502f2.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            } catch (e: Exception) {
                Log.e("FirebaseChatManager", "Failed to initialize Firebase with options: ${e.message}")
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { null }

    suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Anonymous sign-in error: ${e.message}")
            null
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Email sign-in error: ${e.message}")
            // Try creating user if does not exist
            try {
                val createResult = auth.createUserWithEmailAndPassword(email, pass).await()
                createResult.user
            } catch (e2: Exception) {
                Log.e("FirebaseChatManager", "Create user error: ${e2.message}")
                null
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        try {
            firestore.collection("users").document(profile.userId).set(
                mapOf(
                    "userId" to profile.userId,
                    "displayName" to profile.displayName,
                    "email" to (profile.email ?: ""),
                    "inviteCode" to profile.inviteCode,
                    "moodEmoji" to profile.moodEmoji,
                    "moodText" to profile.moodText,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error saving user profile: ${e.message}")
        }
    }

    suspend fun joinOrCreateRoom(roomId: String, userId: String, userName: String) {
        try {
            val roomRef = firestore.collection("rooms").document(roomId)
            val doc = roomRef.get().await()
            if (!doc.exists()) {
                roomRef.set(
                    mapOf(
                        "roomId" to roomId,
                        "name" to roomId,
                        "participants" to listOf(userId),
                        "participantNames" to mapOf(userId to userName),
                        "createdAt" to System.currentTimeMillis(),
                        "lastActive" to System.currentTimeMillis()
                    )
                ).await()
            } else {
                roomRef.update(
                    "participantNames.$userId", userName,
                    "lastActive", System.currentTimeMillis()
                ).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error joining room $roomId: ${e.message}")
        }
    }

    fun observeMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection("rooms")
                .document(roomId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(100)
                .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.e("FirebaseChatManager", "Messages listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    val msgs = snapshot?.documents?.mapNotNull { doc: DocumentSnapshot ->
                        ChatMessage(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "User",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getString("timestamp") ?: "",
                            reaction = doc.getString("reaction"),
                            isDailyPromptReply = doc.getBoolean("isDailyPromptReply") ?: false,
                            isRead = doc.getBoolean("isRead") ?: false,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } ?: emptyList()
                    trySend(msgs)
                }
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Failed to attach message listener: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    suspend fun sendMessage(roomId: String, message: ChatMessage) {
        try {
            firestore.collection("rooms")
                .document(roomId)
                .collection("messages")
                .add(
                    mapOf(
                        "senderId" to message.senderId,
                        "senderName" to message.senderName,
                        "text" to message.text,
                        "timestamp" to message.timestamp,
                        "reaction" to (message.reaction ?: ""),
                        "isDailyPromptReply" to message.isDailyPromptReply,
                        "isRead" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

            firestore.collection("rooms").document(roomId).update(
                "lastMessage", message.text,
                "lastMessageTime", message.timestamp,
                "lastActive", System.currentTimeMillis()
            ).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error sending message: ${e.message}")
        }
    }

    suspend fun updateReaction(roomId: String, messageId: String, reaction: String?) {
        try {
            firestore.collection("rooms")
                .document(roomId)
                .collection("messages")
                .document(messageId)
                .update("reaction", reaction)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error updating reaction: ${e.message}")
        }
    }

    fun observeStickyNotes(roomId: String): Flow<List<StickyNote>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection("rooms")
                .document(roomId)
                .collection("notes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) return@addSnapshotListener
                    val notes = snapshot?.documents?.mapNotNull { doc: DocumentSnapshot ->
                        StickyNote(
                            id = doc.id,
                            authorId = doc.getString("authorId") ?: "",
                            authorName = doc.getString("authorName") ?: "Partner",
                            content = doc.getString("content") ?: "",
                            dateString = doc.getString("dateString") ?: "",
                            colorIndex = (doc.getLong("colorIndex") ?: 0L).toInt(),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } ?: emptyList()
                    trySend(notes)
                }
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Sticky notes error: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }

    suspend fun addStickyNote(roomId: String, note: StickyNote) {
        try {
            firestore.collection("rooms")
                .document(roomId)
                .collection("notes")
                .add(
                    mapOf(
                        "authorId" to note.authorId,
                        "authorName" to note.authorName,
                        "content" to note.content,
                        "dateString" to note.dateString,
                        "colorIndex" to note.colorIndex,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error adding note: ${e.message}")
        }
    }

    suspend fun deleteStickyNote(roomId: String, noteId: String) {
        try {
            firestore.collection("rooms")
                .document(roomId)
                .collection("notes")
                .document(noteId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error deleting note: ${e.message}")
        }
    }

    fun observePartnerMood(roomId: String, currentUserId: String): Flow<Map<String, Any>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection("rooms")
                .document(roomId)
                .collection("moods")
                .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    snapshot.documents.forEach { doc: DocumentSnapshot ->
                        if (doc.id != currentUserId) {
                            val data = doc.data
                            if (data != null) {
                                trySend(data)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Partner mood listener error: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }

    suspend fun updateMood(roomId: String, userId: String, userName: String, emoji: String, text: String) {
        try {
            firestore.collection("rooms")
                .document(roomId)
                .collection("moods")
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "userName" to userName,
                        "emoji" to emoji,
                        "text" to text,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error updating mood: ${e.message}")
        }
    }

    suspend fun sendLoveTap(roomId: String, senderId: String, senderName: String) {
        try {
            firestore.collection("rooms").document(roomId).set(
                mapOf(
                    "loveTap" to mapOf(
                        "senderId" to senderId,
                        "senderName" to senderName,
                        "time" to System.currentTimeMillis()
                    )
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Error sending love tap: ${e.message}")
        }
    }

    fun observeLoveTap(roomId: String, currentUserId: String): Flow<String?> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection("rooms").document(roomId)
                .addSnapshotListener { doc: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null || doc == null) return@addSnapshotListener
                    val loveTap = doc.get("loveTap") as? Map<*, *>
                    if (loveTap != null) {
                        val sId = loveTap["senderId"] as? String
                        val sName = loveTap["senderName"] as? String ?: "Partner"
                        val time = (loveTap["time"] as? Long) ?: 0L
                        if (sId != currentUserId && (System.currentTimeMillis() - time < 6000)) {
                            trySend("💖 $sName sent you a warm heart squeeze!")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseChatManager", "Love tap observer error: ${e.message}")
        }
        awaitClose { listener?.remove() }
    }
}
