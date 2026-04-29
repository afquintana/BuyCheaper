package com.afquintana.buycheaper.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afquintana.buycheaper.R

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var showRegisterForm by remember { mutableStateOf(false) }
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordVisible by passwordInteractionSource.collectIsPressedAsState()
    val isConfirmPasswordVisible by confirmPasswordInteractionSource.collectIsPressedAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_brand_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 64.sp
            ),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text(stringResource(R.string.label_email)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (showRegisterForm) 8.dp else 0.dp)
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text(stringResource(R.string.label_password)) },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {},
                    interactionSource = passwordInteractionSource
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = stringResource(R.string.content_description_show_password)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        if (!showRegisterForm) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = viewModel::onRememberMeChanged
                )
                Text(stringResource(R.string.label_remember_me))
            }
        }

        if (showRegisterForm) {
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = { Text(stringResource(R.string.label_confirm_password)) },
                visualTransformation = if (isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {},
                        interactionSource = confirmPasswordInteractionSource
                    ) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                            contentDescription = stringResource(R.string.content_description_show_confirm_password)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        if (state.error != null) {
            Text(text = state.error.orEmpty(), modifier = Modifier.padding(top = 8.dp))
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }

        Button(
            onClick = {
                if (showRegisterForm) {
                    showRegisterForm = false
                } else {
                    viewModel.login()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(
                if (showRegisterForm) {
                    stringResource(R.string.action_back_to_login)
                } else {
                    stringResource(R.string.action_login)
                }
            )
        }

        Button(
            onClick = {
                if (showRegisterForm) {
                    viewModel.register()
                } else {
                    showRegisterForm = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                if (showRegisterForm) {
                    stringResource(R.string.action_create_account)
                } else {
                    stringResource(R.string.action_register)
                }
            )
        }
    }
}
