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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    if (isLoggedIn) onLoggedIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
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
            onClick = viewModel::login,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Login")
        }

        Button(
            onClick = viewModel::register,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Registrarse")
        }
    }
}
