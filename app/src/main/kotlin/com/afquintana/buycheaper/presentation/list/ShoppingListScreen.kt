package com.afquintana.buycheaper.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.afquintana.buycheaper.domain.model.ShoppingItem
import com.afquintana.buycheaper.domain.model.Supermarket

@Composable
fun ShoppingListRoute(
    onProductClick: (String) -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeForm by rememberSaveable { mutableStateOf<ListForm?>(null) }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeForm == null) {
                ListActions(
                    onAddProduct = { activeForm = ListForm.Product },
                    onAddSection = { activeForm = ListForm.Section },
                    onAddSupermarket = { activeForm = ListForm.Supermarket }
                )

                Text(
                    text = "Total lista: ${"%.2f".format(state.grandTotal)} EUR",
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        when (item) {
                            is ShoppingItem.SectionItem -> SectionCard(item.section, viewModel::deleteSection)
                            is ShoppingItem.ProductItem -> ProductCard(
                                product = item.product,
                                colorHex = state.supermarkets.firstOrNull { it.id == item.product.supermarketId }?.colorHex,
                                onClick = { onProductClick(item.product.id) },
                                onDelete = viewModel::deleteProduct
                            )
                        }
                    }
                }
            } else {
                when (activeForm) {
                    ListForm.Product -> ProductForm(
                        supermarketOptions = state.supermarkets,
                        onAdd = { name, supermarketId, price, quantity ->
                            viewModel.addProduct(name, supermarketId, price, quantity)
                            activeForm = null
                        },
                        onBack = { activeForm = null }
                    )

                    ListForm.Section -> SectionForm(
                        onAdd = {
                            viewModel.addSection(it)
                            activeForm = null
                        },
                        onBack = { activeForm = null }
                    )

                    ListForm.Supermarket -> SupermarketForm(
                        onAdd = { name, colorHex ->
                            viewModel.addSupermarket(name, colorHex)
                            activeForm = null
                        },
                        onBack = { activeForm = null }
                    )

                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun ListActions(
    onAddProduct: () -> Unit,
    onAddSection: () -> Unit,
    onAddSupermarket: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onAddProduct, modifier = Modifier.fillMaxWidth()) {
            Text("Anadir producto")
        }
        Button(onClick = onAddSection, modifier = Modifier.fillMaxWidth()) {
            Text("Anadir seccion")
        }
        Button(onClick = onAddSupermarket, modifier = Modifier.fillMaxWidth()) {
            Text("Anadir supermercado")
        }
    }
}

@Composable
private fun SectionCard(section: Section, onDelete: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(section.title, fontWeight = FontWeight.Bold)
            Text("Borrar", modifier = Modifier.clickable { onDelete(section.id) })
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
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
            Column {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Precio: ${product.price} | Cantidad: ${product.quantity}")
                Text("Total: ${"%.2f".format(product.total)}")
            }
            Text("Borrar", modifier = Modifier.clickable { onDelete(product.id) })
        }
    }
}

@Composable
private fun SectionForm(
    onAdd: (String) -> Unit,
    onBack: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nueva seccion", fontWeight = FontWeight.Bold)
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nuevo supermercado", fontWeight = FontWeight.Bold)
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
    onAdd: (String, String, Double, Double) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var supermarketId by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nuevo producto", fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Producto") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = supermarketOptions.firstOrNull { it.id == supermarketId }?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Supermercado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                supermarketOptions.forEach { market ->
                    DropdownMenuItem(
                        text = { Text(market.name) },
                        onClick = {
                            supermarketId = market.id
                            expanded = false
                        }
                    )
                }
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

        FormActions(
            onSubmit = {
                onAdd(
                    name,
                    supermarketId,
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

private enum class ListForm {
    Product,
    Section,
    Supermarket
}
