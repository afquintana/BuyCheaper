package com.afquintana.buycheaper.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var showRegisterForm by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showRegisterForm) {
            OutlinedTextField(
                value = state.nick,
                onValueChange = viewModel::onNickChanged,
                label = { Text("Nick") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (showRegisterForm) 8.dp else 0.dp)
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

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
            Text(if (showRegisterForm) "Volver al login" else "Login")
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
            Text(if (showRegisterForm) "Crear cuenta" else "Registrarse")
        }
    }
}
