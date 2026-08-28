package com.ehshero.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.components.BadgeCard
import com.ehshero.app.ui.components.HeroAvatar
import com.ehshero.app.ui.components.HeroShieldRing
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.StatChip
import com.ehshero.app.ui.components.XpProgressBar
import com.ehshero.app.ui.theme.AVATAR_OPTIONS
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.ui.theme.avatarOptionFor

@Composable
fun ProfileScreen(
    uid: String,
    viewModel: ProfileViewModel = remember(uid) { ProfileViewModel(uid) },
    onViewActivityHistory: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading || state.user == null) {
        LoadingState(message = "Loading profile...")
        return
    }
    val user = state.user!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroAvatar(option = avatarOptionFor(user.avatarId), size = 88.dp, selected = true)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = user.name, style = MaterialTheme.typography.headlineSmall, color = TextHigh, fontWeight = FontWeight.Bold)
        Text(text = "${user.designation} \u2022 ${user.employeeId}", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
        if (user.projectName.isNotBlank()) {
            Text(text = user.projectName, style = MaterialTheme.typography.bodySmall, color = SignalCyan)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HeroShieldRing(
            levelNumber = state.levelProgress?.currentLevel?.levelNumber ?: user.level,
            progress = state.levelProgress?.progress ?: 0f,
            diameter = 110.dp
        )
        Text(
            text = (state.levelProgress?.currentLevel?.title ?: "Safety Rookie").uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = GuardianAmber,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        XpProgressBar(
            progress = state.levelProgress?.progress ?: 0f,
            trailingLabel = "${user.totalXp} XP total",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip(
                icon = Icons.Filled.EmojiEvents,
                value = "${state.unlockedBadges.size}/${state.totalBadges}",
                label = "Badges",
                accentColor = GuardianAmber,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                icon = Icons.Filled.LocalFireDepartment,
                value = user.currentStreak.toString(),
                label = "Streak",
                accentColor = HazardCoral,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                icon = Icons.Filled.MilitaryTech,
                value = "Lv ${user.level}",
                label = "Level",
                accentColor = SignalCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "CHOOSE YOUR HERO", style = MaterialTheme.typography.labelMedium, color = TextMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AVATAR_OPTIONS, key = { it.id }) { option ->
                HeroAvatar(
                    option = option,
                    size = 56.dp,
                    selected = option.id == user.avatarId,
                    modifier = Modifier.clickable { viewModel.setAvatar(option.id) }
                )
            }
        }

        if (state.unlockedBadges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "YOUR BADGES", style = MaterialTheme.typography.labelMedium, color = TextMedium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.unlockedBadges, key = { it.badgeId }) { badge ->
                    Box(modifier = Modifier.width(130.dp)) {
                        BadgeCard(badge = badge, unlocked = true)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        OutlinedButton(onClick = onViewActivityHistory, modifier = Modifier.fillMaxWidth()) {
            Text("View Activity History")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                viewModel.logout()
                onLoggedOut()
            },
            colors = ButtonDefaults.buttonColors(containerColor = HazardCoral),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOG OUT")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
