package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPinkDark
import com.example.ui.theme.PastelPinkLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun PasscodeScreen(
    enteredPin: String,
    isError: Boolean,
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0 using FastOutSlowInEasing
                    -25f at 50
                    25f at 100
                    -20f at 150
                    20f at 200
                    -10f at 250
                    10f at 300
                    0f at 400
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PastelPinkLight,
                        Color(0xFFFFF7FA),
                        Color(0xFFF3EDFF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Romantic Icon Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, PastelPink, CircleShape)
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Secret Space",
                    tint = PastelPinkDark,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Our Secret Space",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter our 4-digit passcode to enter 💕",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Default PIN: 1234",
                style = MaterialTheme.typography.labelSmall,
                color = PastelPinkDark.copy(alpha = 0.8f),
                modifier = Modifier
                    .background(PastelPinkLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator with Shake
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 12.dp)
                    .testTag("pin_dots_row")
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor = if (isError) Color(0xFFE57373) else if (isFilled) PastelPinkDark else Color.Transparent
                    val borderColor = if (isError) Color(0xFFE57373) else if (isFilled) PastelPinkDark else PastelLavender

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(2.dp, borderColor, CircleShape)
                    )
                }
            }

            // Error Label
            AnimatedVisibility(
                visible = isError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Incorrect passcode. Please try again! 🥺",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad
            PasscodeKeypad(
                onDigitClick = onDigitClick,
                onDeleteClick = onDeleteClick,
                onClearClick = onClearClick
            )
        }
    }
}

@Composable
fun PasscodeKeypad(
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('C', '0', '<')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    when (key) {
                        'C' -> {
                            KeypadButton(
                                text = "Clear",
                                isAction = true,
                                onClick = onClearClick,
                                testTag = "keypad_clear"
                            )
                        }
                        '<' -> {
                            KeypadIconButton(
                                onClick = onDeleteClick,
                                testTag = "keypad_backspace"
                            )
                        }
                        else -> {
                            KeypadButton(
                                text = key.toString(),
                                isAction = false,
                                onClick = { onDigitClick(key) },
                                testTag = "keypad_digit_$key"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    isAction: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isAction) Color.Transparent else Color.White.copy(alpha = 0.95f),
        shadowElevation = if (isAction) 0.dp else 2.dp,
        modifier = modifier
            .size(72.dp)
            .testTag(testTag)
            .border(
                width = if (isAction) 0.dp else 1.5.dp,
                color = if (isAction) Color.Transparent else PastelPink.copy(alpha = 0.5f),
                shape = CircleShape
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = if (isAction) 14.sp else 24.sp,
                fontWeight = if (isAction) FontWeight.Medium else FontWeight.SemiBold,
                color = if (isAction) TextSecondary else TextPrimary
            )
        }
    }
}

@Composable
fun KeypadIconButton(
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = modifier
            .size(72.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
