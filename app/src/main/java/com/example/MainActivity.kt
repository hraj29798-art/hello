package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CoupleChatViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.PasscodeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: CoupleChatViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(dynamicColor = false) {
        val state by viewModel.uiState.collectAsState()

        Surface(modifier = Modifier.fillMaxSize()) {
          AnimatedContent(
            targetState = state.isUnlocked,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_screen_transition"
          ) { isUnlocked ->
            if (isUnlocked) {
              ChatScreen(state = state, viewModel = viewModel)
            } else {
              PasscodeScreen(
                enteredPin = state.enteredPin,
                isError = state.isPinError,
                onDigitClick = { digit -> viewModel.onPinDigitEntered(digit) },
                onDeleteClick = { viewModel.onPinDelete() },
                onClearClick = { viewModel.onPinClear() }
              )
            }
          }
        }
      }
    }
  }
}
