package com.afquintana.buycheaper.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onSaved: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(productId) { viewModel.load(productId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Producto") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = state.supermarkets.firstOrNull { it.id == state.supermarketId }?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Supermercado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            androidx.compose.material3.ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                state.supermarkets.forEach { market ->
                    DropdownMenuItem(text = { Text(market.name) }, onClick = {
                        viewModel.onSupermarketChanged(market.id)
                        expanded = false
                    })
                }
            }
        }

        OutlinedTextField(value = state.price, onValueChange = viewModel::onPriceChanged, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.quantity, onValueChange = viewModel::onQuantityChanged, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())

        Text("Total: ${(state.price.toDoubleOrNull() ?: 0.0) * (state.quantity.toDoubleOrNull() ?: 0.0)}")
        state.error?.let { Text(it) }

        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
    }
}
