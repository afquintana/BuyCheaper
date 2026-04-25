package com.afquintana.buycheaper.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket

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

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Anadir producto"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            onCheckedChange = { checked ->
                                viewModel.toggleProductChecked(product, checked)
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
                    SectionHeader(sectionTitle = "Sin seccion")
                }
                items(uncategorizedProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        colorHex = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.colorHex,
                        onClick = { onProductClick(product.id) },
                        onCheckedChange = { checked ->
                            viewModel.toggleProductChecked(product, checked)
                        }
                    )
                }
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
        onAdd = { name, supermarketId, sectionId, price, quantity ->
            viewModel.addProduct(name, supermarketId, sectionId, price, quantity)
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
        text = sectionTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ProductCard(
    product: Product,
    colorHex: String?,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val color = parseColor(colorHex)
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black)
                    .clickable { onCheckedChange(!product.checked) }
            ) {
                if (product.checked) {
                    Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.Center)
                            .size(16.64.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp, end = 40.dp, bottom = 1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Precio: ${product.price} | Cantidad: ${product.quantity}")
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

    FormScreen(title = "Nueva seccion") {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titulo") },
            modifier = Modifier.fillMaxWidth()
        )
        FormActions(
            onSubmit = { onAdd(title) },
            submitLabel = "Anadir",
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
    FormScreen(title = "Nuevo supermercado") {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        ColorPickerField(
            colorHex = colorHex,
            onClick = onPickColor
        )
        FormActions(
            onSubmit = onAdd,
            submitLabel = "Anadir",
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductForm(
    supermarketOptions: List<Supermarket>,
    sectionOptions: List<Section>,
    onAdd: (String, String, String, Double, Double) -> Unit,
    onDeleteSection: (String) -> Unit,
    onDeleteSupermarket: (String) -> Unit,
    onAddSection: () -> Unit,
    onAddSupermarket: () -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var supermarketId by rememberSaveable { mutableStateOf("") }
    var sectionId by rememberSaveable { mutableStateOf("") }
    var supermarketExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
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

    FormScreen(title = "Nuevo producto") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Producto") },
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
                                    contentDescription = "Eliminar supermercado",
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
                            text = "Añadir supermercado",
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
                value = sectionOptions.firstOrNull { it.id == sectionId }?.title.orEmpty(),
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
                sectionOptions.forEach { section ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = section.title,
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
                                    contentDescription = "Eliminar seccion"
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
                            text = "Añadir sección",
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

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pricePerQuantityText(price, quantity),
            onValueChange = {},
            readOnly = true,
            label = { Text("Precio/Cantidad") },
            modifier = Modifier.fillMaxWidth()
        )

        FormActions(
            onSubmit = {
                onAdd(
                    name,
                    supermarketId,
                    sectionId,
                    price.toDoubleOrNull() ?: 0.0,
                    quantity.toDoubleOrNull() ?: 0.0
                )
            },
            submitLabel = "Anadir",
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
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(submitLabel)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
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
            text = "Color",
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

    FormScreen(title = "Elegir color") {
        Text("Previsualizacion")
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
            label = { Text("Color seleccionado") },
            modifier = Modifier.fillMaxWidth()
        )
        ColorSlider(
            label = "Rojo",
            value = red,
            onValueChange = { red = it }
        )
        ColorSlider(
            label = "Verde",
            value = green,
            onValueChange = { green = it }
        )
        ColorSlider(
            label = "Azul",
            value = blue,
            onValueChange = { blue = it }
        )
        Text("Colores rapidos")
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
            submitLabel = "Usar color",
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
        Text("$label: $value")
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

private fun pricePerQuantityText(price: String, quantity: String): String {
    val priceValue = price.toDoubleOrNull()
    val quantityValue = quantity.toDoubleOrNull()
    if (priceValue == null || quantityValue == null || quantityValue == 0.0) return ""
    return "%.3f".format(priceValue / quantityValue)
}

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
