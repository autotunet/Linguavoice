package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark

enum class DuoButtonColor(
    val topColor: Color,
    val bottomColor: Color,
    val contentColor: Color = Color.White
) {
    GREEN(topColor = DuoGreen, bottomColor = DuoGreenDark),
    BLUE(topColor = Color(0xFF1CB0F6), bottomColor = Color(0xFF1899D6)),
    RED(topColor = Color(0xFFFF4B4B), bottomColor = Color(0xFFEA2B2B)),
    ORANGE(topColor = Color(0xFFFF9600), bottomColor = Color(0xFFE58700)),
    WHITE(topColor = Color.White, bottomColor = Color(0xFFE5E5E5), contentColor = Color(0xFF4B4B4B)),
    GRAY(topColor = Color(0xFFE5E5E5), bottomColor = Color(0xFFCECECE), contentColor = Color(0xFF777777))
}

@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorStyle: DuoButtonColor = DuoButtonColor.GREEN,
    elevation: Dp = 4.dp,
    icon: (@Composable () -> Unit)? = null,
    testTag: String = "duo_button"
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentElevation by animateDpAsState(
        targetValue = if (isPressed || !enabled) 0.dp else elevation,
        label = "button_elevation"
    )

    val actualStyle = if (enabled) colorStyle else DuoButtonColor.GRAY

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(54.dp + elevation)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = {
                            onClick()
                        }
                    )
                }
            }
    ) {
        // Bottom 3D shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp + elevation)
                .background(
                    color = actualStyle.bottomColor,
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Top face layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .offset(y = if (isPressed) elevation else 0.dp)
                .background(
                    color = actualStyle.topColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Box(modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = actualStyle.contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
