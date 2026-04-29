package com.afquintana.buycheaper.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afquintana.buycheaper.R
import com.afquintana.buycheaper.domain.model.CurrencyUnit
import com.afquintana.buycheaper.domain.model.QuantityUnit
import com.afquintana.buycheaper.domain.model.parseQuantityInput
import com.afquintana.buycheaper.domain.model.pricePerQuantityDisplayText
import java.util.Locale

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
    var currencyExpanded by remember { mutableStateOf(false) }
    var quantityUnitExpanded by remember { mutableStateOf(false) }
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
        Text(
            text = stringResource(R.string.title_edit_product),
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text(stringResource(R.string.label_product)) },
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
                label = { Text(stringResource(R.string.label_supermarket)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supermarketExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = supermarketExpanded,
                onDismissRequest = { supermarketExpanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                sortedSupermarkets.forEach { market ->
                    val marketColor = parseColor(market.colorHex)
                    val marketContentColor = contentColorFor(marketColor)
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ColorSwatch(
                                    colorHex = market.colorHex,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = market.name,
                                    color = marketContentColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = marketContentColor,
                            trailingIconColor = marketContentColor
                        ),
                        trailingIcon = {
                            IconButton(onClick = { supermarketIdPendingDelete = market.id }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.content_description_delete_supermarket),
                                    tint = marketContentColor
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                            .background(marketColor),
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
                value = sortedSections.firstOrNull { it.id == state.sectionId }?.title.orEmpty().uppercase(Locale.getDefault()),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_section)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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
                                text = section.title.uppercase(Locale.getDefault()),
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
                                    contentDescription = stringResource(R.string.content_description_delete_section)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.price,
                onValueChange = { viewModel.onPriceChanged(sanitizePriceInput(it)) },
                label = { Text(stringResource(R.string.label_price)) },
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = !currencyExpanded },
                modifier = Modifier.weight(0.7f)
            ) {
                OutlinedTextField(
                    value = state.currency.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_currency)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    CurrencyUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayLabel) },
                            onClick = {
                                viewModel.onCurrencyChanged(unit)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.quantity,
                onValueChange = { viewModel.onQuantityChanged(sanitizeQuantityInput(it)) },
                label = { Text(stringResource(R.string.label_quantity)) },
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuBox(
                expanded = quantityUnitExpanded,
                onExpandedChange = { quantityUnitExpanded = !quantityUnitExpanded },
                modifier = Modifier.weight(0.7f)
            ) {
                OutlinedTextField(
                    value = state.quantityUnit.displayLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_unit)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quantityUnitExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = quantityUnitExpanded,
                    onDismissRequest = { quantityUnitExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    QuantityUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayLabel) },
                            onClick = {
                                viewModel.onQuantityUnitChanged(unit)
                                quantityUnitExpanded = false
                            }
                        )
                    }
                }
            }
        }
        DisableSelection {
            OutlinedTextField(
                value = pricePerQuantityText(state.price, state.quantity, state.quantityUnit, state.currency),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(stringResource(R.string.label_price_per_quantity)) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                    disabledLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        state.error?.let { Text(it) }

        Button(
            onClick = viewModel::save,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) { Text(stringResource(R.string.action_save)) }
        Button(
            onClick = { confirmProductDelete = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) { Text(stringResource(R.string.action_delete)) }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_title)) },
        text = { Text(stringResource(R.string.dialog_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private fun pricePerQuantityText(
    price: String,
    quantity: String,
    quantityUnit: QuantityUnit,
    currency: CurrencyUnit
): String {
    val priceValue = price.normalizedDecimal().toDoubleOrNull()
    val quantityValue = parseQuantityInput(quantity)
    if (priceValue == null || quantityValue == null) return ""
    return pricePerQuantityDisplayText(priceValue, quantityValue, quantityUnit, currency)
}

private fun String.normalizedDecimal(): String = replace(',', '.')

private fun sanitizePriceInput(value: String): String =
    value.filter { it.isDigit() || it == ',' || it == '.' }

private fun sanitizeQuantityInput(value: String): String =
    value
        .replace('X', 'x')
        .replace('×', 'x')
        .filter { it.isDigit() || it == 'x' || it == ',' || it == '.' }

@Composable
private fun ColorSwatch(
    colorHex: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.small)
            .background(parseColor(colorHex))
    )
}

private fun parseColor(colorHex: String?): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex ?: "#2D2D2D")) }
        .getOrDefault(Color(0xFF2D2D2D))
}

private fun contentColorFor(background: Color): Color {
    return if (background.luminance() > 0.5f) Color.Black else Color.White
}
