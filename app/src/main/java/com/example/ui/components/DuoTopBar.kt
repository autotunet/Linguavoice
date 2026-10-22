package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed

@Composable
fun DuoTopBar(
    userProfile: UserProfile,
    onLanguageClick: () -> Unit,
    onHeartsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    .clip(RoundedCornerShape(14.dp))
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
}
