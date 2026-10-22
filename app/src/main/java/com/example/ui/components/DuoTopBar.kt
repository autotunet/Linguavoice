package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import kotlin.math.roundToInt

@Composable
fun DuoTopBar(
    userProfile: UserProfile,
    onLanguageClick: () -> Unit,
    onHeartsClick: () -> Unit,
    onStreakClick: () -> Unit = {},
    levelInfo: UserLevelInfo? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language Selector Pill
            Row(
                modifier = Modifier
                    .testTag("language_selector_pill")
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, DuoCardBorder, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onLanguageClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = userProfile.selectedLanguage.flagEmoji,
                    fontSize = 18.sp
                )
                Text(
                    text = userProfile.selectedLanguage.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                Row(
                    modifier = Modifier
                        .testTag("streak_top_bar_pill")
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onStreakClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Racha",
                        tint = DuoOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "${userProfile.streakDays}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DuoOrange
                    )
                }

                // Gems / Lingots
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Gemas",
                        tint = DuoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${userProfile.gems}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DuoBlue
                    )
                }

                // Hearts
                Row(
                    modifier = Modifier
                        .testTag("hearts_pill")
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onHeartsClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Vidas",
                        tint = DuoRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${userProfile.hearts}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DuoRed
                    )
                }
            }
        }

        // Visual progress bar at the top of the interface showing current level completion %
        if (levelInfo != null) {
            val percentage = ((levelInfo.progressFraction * 100f).roundToInt()).coerceIn(0, 100)
            val animatedFraction by animateFloatAsState(
                targetValue = levelInfo.progressFraction.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                label = "topBarLevelProgressAnim"
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_bar_level_progress_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${levelInfo.badgeEmoji} Nv. ${levelInfo.level}",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = DuoPurple
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DuoCardBorder.copy(alpha = 0.65f))
                        .testTag("top_bar_level_progress_indicator")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction.coerceAtLeast(if (percentage == 0) 0.03f else 0f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(DuoPurple, DuoGreen)
                                )
                            )
                    )
                }

                Text(
                    text = "$percentage%",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = DuoPurple,
                    modifier = Modifier.testTag("top_bar_level_percentage_text")
                )
            }
        }
    }
}
