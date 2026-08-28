package com.ehshero.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.CommandNavy
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/**
 * The login screen (spec section 3). Section 3 asks for an illustrated
 * anime-style safety-hero character in helmet/vest/harness - this
 * environment can only produce code and vector graphics, not painted
 * character art, so that's represented here as a glowing emblem badge
 * instead. It's a deliberately obvious slot (see [HeroEmblem]) to drop
 * commissioned or AI-generated artwork into later without touching any
 * other code - see README "What's simplified".
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = remember { LoginViewModel() },
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginSucceeded) {
        if (state.loginSucceeded) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CommandNavy, SteelPanel))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            HeroEmblem()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "EHS HERO",
                style = MaterialTheme.typography.displayMedium,
                color = TextHigh,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Every Safe Action Makes You A Hero.",
                style = MaterialTheme.typography.bodyLarge,
                color = SignalCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = state.identifier,
                onValueChange = viewModel::onIdentifierChange,
                label = { Text("Employee ID / Email") },
                singleLine = true,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextMedium
                        )
                    }
                },
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = viewModel::onRememberMeChange,
                        colors = CheckboxDefaults.colors(checkedColor = GuardianAmber, checkmarkColor = OnAmber)
                    )
                    Text("Remember Me", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
                }
                TextButton(onClick = { viewModel.sendPasswordReset() }) {
                    Text("Forgot Password?", color = SignalCyan)
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.resetEmailSent) {
                Text(
                    text = "Password reset email sent - check your inbox.",
                    color = com.ehshero.app.ui.theme.ClearanceGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.login() },
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = OnAmber, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeroEmblem() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(
                Brush.radialGradient(listOf(GuardianAmberDim, SteelPanelElevated)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Engineering,
            contentDescription = "EHS Hero",
            tint = GuardianAmber,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextHigh,
    unfocusedTextColor = TextHigh,
    focusedBorderColor = GuardianAmber,
    unfocusedBorderColor = com.ehshero.app.ui.theme.SteelOutline,
    focusedLabelColor = GuardianAmber,
    unfocusedLabelColor = TextMedium,
    cursorColor = GuardianAmber
)
