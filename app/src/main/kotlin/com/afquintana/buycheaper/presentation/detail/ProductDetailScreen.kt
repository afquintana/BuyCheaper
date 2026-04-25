package com.afquintana.buycheaper.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val sortedSupermarkets = state.supermarkets.sortedBy { it.name.lowercase() }
    val sortedSections = state.sections.sortedBy { it.title.lowercase() }

    LaunchedEffect(productId) { viewModel.load(productId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    var supermarketExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var supermarketIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var sectionIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var confirmProductDelete by remember { mutableStateOf(false) }

    supermarketIdPendingDelete?.let { pendingId ->
        ConfirmDeleteDialog(
            onDismiss = { supermarketIdPendingDelete = null },
            onConfirm = {
                viewModel.deleteSupermarket(pendingId)
                supermarketIdPendingDelete = null
            }
        )
    }

    sectionIdPendingDelete?.let { pendingId ->
        ConfirmDeleteDialog(
            onDismiss = { sectionIdPendingDelete = null },
            onConfirm = {
                viewModel.deleteSection(pendingId)
                sectionIdPendingDelete = null
            }
        )
    }

    if (confirmProductDelete) {
        ConfirmDeleteDialog(
            onDismiss = { confirmProductDelete = false },
            onConfirm = {
                viewModel.delete()
                confirmProductDelete = false
            }
        )
    }

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

        ExposedDropdownMenuBox(
            expanded = supermarketExpanded,
            onExpandedChange = { supermarketExpanded = !supermarketExpanded }
        ) {
            OutlinedTextField(
                value = sortedSupermarkets.firstOrNull { it.id == state.supermarketId }?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Supermercado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supermarketExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = supermarketExpanded,
                onDismissRequest = { supermarketExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                sortedSupermarkets.forEach { market ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = market.name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            trailingIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { supermarketIdPendingDelete = market.id }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar supermercado"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.onSupermarketChanged(market.id)
                            supermarketExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = sectionExpanded,
            onExpandedChange = { sectionExpanded = !sectionExpanded }
        ) {
            OutlinedTextField(
                value = sortedSections.firstOrNull { it.id == state.sectionId }?.title.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Seccion") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = sectionExpanded,
                onDismissRequest = { sectionExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                sortedSections.forEach { section ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = section.title,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            trailingIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = { sectionIdPendingDelete = section.id }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar seccion"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.onSectionChanged(section.id)
                            sectionExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(value = state.price, onValueChange = viewModel::onPriceChanged, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.quantity, onValueChange = viewModel::onQuantityChanged, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = pricePerQuantityText(state.price, state.quantity),
            onValueChange = {},
            readOnly = true,
            label = { Text("Precio/Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let { Text(it) }

        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        Button(onClick = { confirmProductDelete = true }, modifier = Modifier.fillMaxWidth()) { Text("Borrar") }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar borrado") },
        text = { Text("Estas seguro de querer borrarlo?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Borrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun pricePerQuantityText(price: String, quantity: String): String {
    val priceValue = price.toDoubleOrNull()
    val quantityValue = quantity.toDoubleOrNull()
    if (priceValue == null || quantityValue == null || quantityValue == 0.0) return ""
    return "%.3f".format(priceValue / quantityValue)
}
