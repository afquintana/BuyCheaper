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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.ShoppingItem

@Composable
fun ShoppingListScreen(
    onProductClick: (String) -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SectionInput(onAdd = viewModel::addSection)
            SupermarketInput(onAdd = viewModel::addSupermarket)
            ProductInput(supermarketOptions = state.supermarkets, onAdd = viewModel::addProduct)

            Text(
                text = "Total lista: ${"%.2f".format(state.grandTotal)} €",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
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
                Text("Precio: ${product.price} · Cantidad: ${product.quantity}")
                Text("Total: ${"%.2f".format(product.total)}")
            }
            Text("Borrar", modifier = Modifier.clickable { onDelete(product.id) })
        }
    }
}

@Composable
private fun SectionInput(onAdd: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nueva sección") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { onAdd(title); title = "" }) { Text("Añadir") }
    }
}

@Composable
private fun SupermarketInput(onAdd: (name: String, colorHex: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#3B82F6") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Supermercado") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.weight(1f))
        Button(onClick = { onAdd(name, color); name = "" }) { Text("Crear") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductInput(
    supermarketOptions: List<com.afquintana.buycheaper.domain.model.Supermarket>,
    onAdd: (String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var supermarketId by remember { mutableStateOf("") }

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
        androidx.compose.material3.ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Producto") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad") }, modifier = Modifier.weight(1f))
    }
    Button(
        onClick = {
            onAdd(name, supermarketId, price.toDoubleOrNull() ?: 0.0, quantity.toDoubleOrNull() ?: 0.0)
            name = ""
            price = ""
            quantity = ""
        },
        modifier = Modifier.padding(top = 8.dp)
    ) { Text("Añadir producto") }
}

private fun parseColor(colorHex: String?): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex ?: "#2D2D2D")) }
        .getOrDefault(Color(0xFF2D2D2D))
}
