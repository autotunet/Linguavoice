package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.PronunciationResult
import com.example.data.model.WordAccuracy

@Composable
fun PronunciationResultSheet(
    result: PronunciationResult?,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    remainingLives: Int? = null,
    maxLives: Int = 5,
    onReplayFeedbackSound: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = result != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (result == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            PronunciationFeedbackCard(
                result = result,
                onWordClick = onWordClick,
                onRetry = onRetry,
                onContinue = onContinue,
                remainingLives = remainingLives,
                maxLives = maxLives,
                onReplayFeedbackSound = onReplayFeedbackSound,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Keep WordChip available for any other callers
@Composable
fun WordChip(
    wordAccuracy: WordAccuracy,
    onClick: () -> Unit
) {
    ColorCodedWordChip(
        wordAccuracy = wordAccuracy,
        isSelected = false,
        onClick = onClick,
        testTag = "word_chip_${wordAccuracy.targetWord}"
    )
}
