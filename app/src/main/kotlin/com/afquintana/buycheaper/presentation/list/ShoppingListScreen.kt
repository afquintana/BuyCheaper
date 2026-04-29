package com.afquintana.buycheaper.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afquintana.buycheaper.R
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.CurrencyUnit
import com.afquintana.buycheaper.domain.model.QuantityUnit
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.model.pricePerQuantityDisplayText
import com.afquintana.buycheaper.domain.model.priceDisplayText
import com.afquintana.buycheaper.domain.model.quantityDisplayText
import com.afquintana.buycheaper.domain.model.parseQuantityInput
import com.afquintana.buycheaper.domain.model.formatDecimal
import com.afquintana.buycheaper.presentation.theme.Blue
import java.util.Locale

@Composable
fun ShoppingListRoute(
    onProductClick: (String) -> Unit,
    onAddProduct: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sortedSections = state.sections.sortedBy { it.title.lowercase() }
    val sortedProducts = state.products.sortedBy { it.name.lowercase() }
    val checkedProducts = state.products.filter { it.checkCount > 0 }
    val checkedTotal = checkedProducts.sumOf { it.price * it.checkCount }
    val checkedCurrencies = checkedProducts.map { it.currency }.distinct()

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = Blue,
                contentColor = Color.White
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_description_add_product)
                )
            }
        }
    ) { paddingValues ->
        if (state.products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_products),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            sortedSections.forEach { section ->
                val productsForSection = sortedProducts.filter { it.sectionId == section.id }
                if (productsForSection.isNotEmpty()) {
                    item(key = section.id) {
                        SectionHeader(section = section)
                    }
                    items(productsForSection, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            colorHex = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.colorHex,
                            onClick = { onProductClick(product.id) },
                            onToggleChecked = {
                                val nextCount = if (product.checkCount > 0) 0 else 1
                                viewModel.setProductCheckCount(product, nextCount)
                            },
                            onIncreaseCheckCount = {
                                val nextCount = if (product.checkCount <= 1) 2 else product.checkCount + 1
                                viewModel.setProductCheckCount(product, nextCount)
                            }
                        )
                    }
                }
            }

            val uncategorizedProducts = state.products.filter { product ->
                state.sections.none { it.id == product.sectionId }
            }.sortedBy { it.name.lowercase() }

            if (uncategorizedProducts.isNotEmpty()) {
                item(key = "uncategorized") {
                    SectionHeader(sectionTitle = stringResource(R.string.section_uncategorized))
                }
                items(uncategorizedProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        colorHex = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.colorHex,
                        onClick = { onProductClick(product.id) },
                        onToggleChecked = {
                            val nextCount = if (product.checkCount > 0) 0 else 1
                            viewModel.setProductCheckCount(product, nextCount)
                        },
                        onIncreaseCheckCount = {
                            val nextCount = if (product.checkCount <= 1) 2 else product.checkCount + 1
                            viewModel.setProductCheckCount(product, nextCount)
                        }
                    )
                }
            }

            item(key = "checked-total") {
                CheckedTotalRow(
                    total = checkedTotal,
                    currencyLabel = checkedCurrencies.singleOrNull()?.displayLabel
                )
            }
        }
    }
}

@Composable
fun AddSectionRoute(
    onBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    SectionForm(
        onAdd = {
            viewModel.addSection(it)
            onBack()
        },
        onBack = onBack
    )
}

@Composable
fun AddSupermarketRoute(
    selectedColorHex: String,
    onBack: () -> Unit,
    onPickColor: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    SupermarketForm(
        name = name,
        colorHex = selectedColorHex,
        onNameChange = { name = it },
        onPickColor = onPickColor,
        onAdd = {
            viewModel.addSupermarket(name, selectedColorHex)
            onBack()
        },
        onBack = onBack
    )
}

@Composable
fun AddSupermarketColorRoute(
    initialColorHex: String,
    onBack: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    ColorPickerScreen(
        initialColorHex = initialColorHex,
        onBack = onBack,
        onColorSelected = onColorSelected
    )
}

@Composable
fun AddProductRoute(
    onBack: () -> Unit,
    onAddSection: () -> Unit,
    onAddSupermarket: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ProductForm(
        supermarketOptions = state.supermarkets.sortedBy { it.name.lowercase() },
        sectionOptions = state.sections.sortedBy { it.title.lowercase() },
        initialCurrency = state.preferredCurrency,
        onAdd = { name, supermarketId, sectionId, price, quantity, quantityInput, quantityUnit, currency ->
            viewModel.addProduct(name, supermarketId, sectionId, price, quantity, quantityInput, quantityUnit, currency)
            onBack()
        },
        onDeleteSection = viewModel::deleteSection,
        onDeleteSupermarket = viewModel::deleteSupermarket,
        onAddSection = onAddSection,
        onAddSupermarket = onAddSupermarket,
        onBack = onBack
    )
}

@Composable
private fun SectionHeader(section: Section) {
    SectionHeader(sectionTitle = section.title)
}

@Composable
private fun SectionHeader(sectionTitle: String) {
    Text(
        text = sectionTitle.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun CheckedTotalRow(
    total: Double,
    currencyLabel: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_total),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = buildString {
                append(formatDecimal(total, 2))
                if (currencyLabel != null) {
                    append(" ")
                    append(currencyLabel)
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ProductCard(
    product: Product,
    colorHex: String?,
    onClick: () -> Unit,
    onToggleChecked: () -> Unit,
    onIncreaseCheckCount: () -> Unit
) {
    val color = parseColor(colorHex)
    val textColor = contentColorFor(color)
    val shape = RoundedCornerShape(18.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.8.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .offset(x = 4.dp, y = (-2).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (product.checkCount >= 2) {
                    Text(
                        text = stringResource(R.string.quantity_multiplier, product.checkCount),
                        color = textColor,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black)
                        .combinedClickable(
                            onClick = onToggleChecked,
                            onLongClick = onIncreaseCheckCount
                        )
                ) {
                    if (product.checkCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.Center)
                                .size(16.64.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.35.dp, end = 70.dp, bottom = 0.35.dp),
                verticalArrangement = Arrangement.spacedBy(0.35.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.priceDisplayText()}  ${product.quantityDisplayText()}  ${product.pricePerQuantityDisplayText()}",
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FormScreen(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun SectionForm(
    onAdd: (String) -> Unit,
    onBack: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }

    FormScreen(title = stringResource(R.string.title_new_section)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.label_title)) },
            modifier = Modifier.fillMaxWidth()
        )
        FormActions(
            onSubmit = { onAdd(title) },
            submitLabel = stringResource(R.string.action_add),
            onBack = onBack
        )
    }
}

@Composable
private fun SupermarketForm(
    name: String,
    colorHex: String,
    onNameChange: (String) -> Unit,
    onPickColor: () -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    FormScreen(title = stringResource(R.string.title_new_supermarket)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.label_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        ColorPickerField(
            colorHex = colorHex,
            onClick = onPickColor
        )
        FormActions(
            onSubmit = onAdd,
            submitLabel = stringResource(R.string.action_add),
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductForm(
    supermarketOptions: List<Supermarket>,
    sectionOptions: List<Section>,
    initialCurrency: CurrencyUnit,
    onAdd: (String, String, String, Double, Double, String, QuantityUnit, CurrencyUnit) -> Unit,
    onDeleteSection: (String) -> Unit,
    onDeleteSupermarket: (String) -> Unit,
    onAddSection: () -> Unit,
    onAddSupermarket: () -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable(initialCurrency.storageValue) {
        mutableStateOf(initialCurrency.storageValue)
    }
    var quantity by rememberSaveable { mutableStateOf("") }
    var quantityUnit by rememberSaveable { mutableStateOf(QuantityUnit.UNIT.storageValue) }
    var supermarketId by rememberSaveable { mutableStateOf("") }
    var sectionId by rememberSaveable { mutableStateOf("") }
    var supermarketExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var quantityUnitExpanded by remember { mutableStateOf(false) }
    var supermarketIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var sectionIdPendingDelete by remember { mutableStateOf<String?>(null) }

    supermarketIdPendingDelete?.let { pendingId ->
        ConfirmDeleteDialog(
            onDismiss = { supermarketIdPendingDelete = null },
            onConfirm = {
                if (supermarketId == pendingId) supermarketId = ""
                onDeleteSupermarket(pendingId)
                supermarketIdPendingDelete = null
            }
        )
    }

    sectionIdPendingDelete?.let { pendingId ->
        ConfirmDeleteDialog(
            onDismiss = { sectionIdPendingDelete = null },
            onConfirm = {
                if (sectionId == pendingId) sectionId = ""
                onDeleteSection(pendingId)
                sectionIdPendingDelete = null
            }
        )
    }

    FormScreen(title = stringResource(R.string.title_new_product)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.label_product)) },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = supermarketExpanded,
            onExpandedChange = { supermarketExpanded = !supermarketExpanded }
        ) {
            OutlinedTextField(
                value = supermarketOptions.firstOrNull { it.id == supermarketId }?.name.orEmpty(),
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
                supermarketOptions.forEach { market ->
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
                                    shape = MaterialTheme.shapes.small,
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
                            IconButton(onClick = {
                                supermarketIdPendingDelete = market.id
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.content_description_delete_supermarket),
                                    tint = marketContentColor
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(marketColor),
                        onClick = {
                            supermarketId = market.id
                            supermarketExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.action_add_supermarket),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        supermarketExpanded = false
                        onAddSupermarket()
                    }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = sectionExpanded,
            onExpandedChange = { sectionExpanded = !sectionExpanded }
        ) {
            OutlinedTextField(
                value = sectionOptions.firstOrNull { it.id == sectionId }?.title.orEmpty().uppercase(Locale.getDefault()),
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
                sectionOptions.forEach { section ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = section.title.uppercase(Locale.getDefault()),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface,
                            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                sectionIdPendingDelete = section.id
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.content_description_delete_section)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            sectionId = section.id
                            sectionExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.action_add_section),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        sectionExpanded = false
                        onAddSection()
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = sanitizePriceInput(it) },
                label = { Text(stringResource(R.string.label_price)) },
                modifier = Modifier.weight(1f)
            )

            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = !currencyExpanded },
                modifier = Modifier.weight(0.7f)
            ) {
                OutlinedTextField(
                    value = CurrencyUnit.fromStorage(currency).displayLabel,
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
                    CurrencyUnit.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.displayLabel) },
                            onClick = {
                                currency = item.storageValue
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
                value = quantity,
                onValueChange = { quantity = sanitizeQuantityInput(it) },
                label = { Text(stringResource(R.string.label_quantity)) },
                modifier = Modifier.weight(1f)
            )

            ExposedDropdownMenuBox(
                expanded = quantityUnitExpanded,
                onExpandedChange = { quantityUnitExpanded = !quantityUnitExpanded },
                modifier = Modifier.weight(0.7f)
            ) {
                OutlinedTextField(
                    value = QuantityUnit.fromStorage(quantityUnit).displayLabel,
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
                                quantityUnit = unit.storageValue
                                quantityUnitExpanded = false
                            }
                        )
                    }
                }
            }
        }

        DisableSelection {
            OutlinedTextField(
                value = pricePerQuantityText(
                    price = price,
                    quantity = quantity,
                    quantityUnit = QuantityUnit.fromStorage(quantityUnit),
                    currency = CurrencyUnit.fromStorage(currency)
                ),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(stringResource(R.string.label_price_per_quantity)) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        FormActions(
            onSubmit = {
                onAdd(
                    name,
                    supermarketId,
                    sectionId,
                    price.normalizedDecimal().toDoubleOrNull() ?: 0.0,
                    parseQuantityInput(quantity) ?: 0.0,
                    quantity,
                    QuantityUnit.fromStorage(quantityUnit),
                    CurrencyUnit.fromStorage(currency)
                )
            },
            submitLabel = stringResource(R.string.action_add),
            onBack = onBack
        )
    }
}

@Composable
private fun FormActions(
    onSubmit: () -> Unit,
    submitLabel: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            Text(submitLabel)
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            Text(stringResource(R.string.action_back))
        }
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

private fun parseColor(colorHex: String?): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex ?: "#2D2D2D")) }
        .getOrDefault(Color(0xFF2D2D2D))
}

@Composable
private fun ColorPickerField(
    colorHex: String,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.label_color),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorSwatch(
                    colorHex = colorHex,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = colorHex,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ColorPickerScreen(
    initialColorHex: String,
    onBack: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var red by rememberSaveable { mutableStateOf(colorComponent(initialColorHex, 0)) }
    var green by rememberSaveable { mutableStateOf(colorComponent(initialColorHex, 1)) }
    var blue by rememberSaveable { mutableStateOf(colorComponent(initialColorHex, 2)) }

    val selectedColorHex = rgbToHex(red, green, blue)

    FormScreen(title = stringResource(R.string.title_pick_color)) {
        Text(stringResource(R.string.label_preview))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(parseColor(selectedColorHex), RoundedCornerShape(20.dp))
        )
        OutlinedTextField(
            value = selectedColorHex,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_selected_color)) },
            modifier = Modifier.fillMaxWidth()
        )
        ColorSlider(
            label = stringResource(R.string.color_red),
            value = red,
            onValueChange = { red = it }
        )
        ColorSlider(
            label = stringResource(R.string.color_green),
            value = green,
            onValueChange = { green = it }
        )
        ColorSlider(
            label = stringResource(R.string.color_blue),
            value = blue,
            onValueChange = { blue = it }
        )
        Text(stringResource(R.string.label_quick_colors))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            supermarketPalette.forEach { colorHex ->
                ColorSwatch(
                    colorHex = colorHex,
                    shape = if (colorHex == selectedColorHex) MaterialTheme.shapes.large else MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            red = colorComponent(colorHex, 0)
                            green = colorComponent(colorHex, 1)
                            blue = colorComponent(colorHex, 2)
                        }
                )
            }
        }
        FormActions(
            onSubmit = { onColorSelected(selectedColorHex) },
            submitLabel = stringResource(R.string.label_use_color),
            onBack = onBack
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.slider_value, label, value))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f
        )
    }
}

private fun colorComponent(colorHex: String, componentIndex: Int): Int {
    val colorInt = runCatching { android.graphics.Color.parseColor(colorHex) }
        .getOrDefault(android.graphics.Color.parseColor("#3B82F6"))

    return when (componentIndex) {
        0 -> android.graphics.Color.red(colorInt)
        1 -> android.graphics.Color.green(colorInt)
        else -> android.graphics.Color.blue(colorInt)
    }
}

private fun rgbToHex(red: Int, green: Int, blue: Int): String {
    return String.format("#%02X%02X%02X", red, green, blue)
}

private fun contentColorFor(background: Color): Color {
    return if (background.luminance() > 0.5f) Color.Black else Color.White
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
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(parseColor(colorHex), shape)
            .aspectRatio(1f)
    )
}

private val supermarketPalette = listOf(
    "#EF4444",
    "#F97316",
    "#EAB308",
    "#22C55E",
    "#14B8A6",
    "#3B82F6",
    "#6366F1",
    "#A855F7",
    "#EC4899",
    "#78716C",
    "#64748B",
    "#0F172A"
)
