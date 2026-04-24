package com.afquintana.buycheaper.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            item {
                Text(
                    text = "Total lista: ${"%.2f".format(state.grandTotal)} EUR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            state.sections.forEach { section ->
                val productsForSection = state.products.filter { it.sectionId == section.id }
                if (productsForSection.isNotEmpty()) {
                    item(key = section.id) {
                        SectionHeader(section = section)
                    }
                    items(productsForSection, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            supermarketName = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.name,
                            colorHex = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.colorHex,
                            onClick = { onProductClick(product.id) },
                            onDelete = viewModel::deleteProduct
                        )
                    }
                }
            }

            val uncategorizedProducts = state.products.filter { product ->
                state.sections.none { it.id == product.sectionId }
            }

            if (uncategorizedProducts.isNotEmpty()) {
                item(key = "uncategorized") {
                    SectionHeader(sectionTitle = "Sin seccion")
                }
                items(uncategorizedProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        supermarketName = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.name,
                        colorHex = state.supermarkets.firstOrNull { it.id == product.supermarketId }?.colorHex,
                        onClick = { onProductClick(product.id) },
                        onDelete = viewModel::deleteProduct
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
    onBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    SupermarketForm(
        onAdd = { name, colorHex ->
            viewModel.addSupermarket(name, colorHex)
            onBack()
        },
        onBack = onBack
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
        supermarketOptions = state.supermarkets,
        sectionOptions = state.sections,
        onAdd = { name, supermarketId, sectionId, price, quantity ->
            viewModel.addProduct(name, supermarketId, sectionId, price, quantity)
            onBack()
        },
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
    supermarketName: String?,
    colorHex: String?,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val color = parseColor(colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                supermarketName?.takeIf { it.isNotBlank() }?.let {
                    Text("Supermercado: $it")
                }
                Text("Precio: ${product.price} | Cantidad: ${product.quantity}")
                Text("Total: ${"%.2f".format(product.total)}")
            }
            Text("Borrar", modifier = Modifier.clickable { onDelete(product.id) })
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    onAdd: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("#3B82F6") }

    FormScreen(title = "Nuevo supermercado") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )
        FormActions(
            onSubmit = { onAdd(name, color) },
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
                onDismissRequest = { supermarketExpanded = false }
            ) {
                supermarketOptions.forEach { market ->
                    DropdownMenuItem(
                        text = { Text(market.name) },
                        onClick = {
                            supermarketId = market.id
                            supermarketExpanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = onAddSupermarket, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a anadir supermercado")
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
                onDismissRequest = { sectionExpanded = false }
            ) {
                sectionOptions.forEach { section ->
                    DropdownMenuItem(
                        text = { Text(section.title) },
                        onClick = {
                            sectionId = section.id
                            sectionExpanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = onAddSection, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a anadir seccion")
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onSubmit, modifier = Modifier.weight(1f)) {
            Text(submitLabel)
        }
        Button(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text("Volver")
        }
    }
}

private fun parseColor(colorHex: String?): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex ?: "#2D2D2D")) }
        .getOrDefault(Color(0xFF2D2D2D))
}
