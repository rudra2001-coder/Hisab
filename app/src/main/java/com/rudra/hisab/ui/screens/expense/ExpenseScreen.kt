package com.rudra.hisab.ui.screens.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "আজকের খরচ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "মোট: ${CurrencyFormatter.format(state.todayTotal)}",
            style = MaterialTheme.typography.titleLarge,
            color = RedExpense,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.todayExpenses.isEmpty()) {
            Text(
                text = "আজ কোনো খরচ নেই",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.todayExpenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddDialog() },
            title = { Text("খরচ যোগ") },
            text = {
                Column {
                    Text("বিভাগ", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExpenseCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { viewModel.setCategory(cat) },
                                label = {
                                    Text(
                                        when (cat) {
                                            ExpenseCategory.TRANSPORT -> "পরিবহন"
                                            ExpenseCategory.LABOR -> "শ্রমিক"
                                            ExpenseCategory.RENT -> "ভাড়া"
                                            ExpenseCategory.UTILITY -> "ইউটিলিটি"
                                            ExpenseCategory.PURCHASE -> "ক্রয়"
                                            ExpenseCategory.OTHER -> "অন্যান্য"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedExpense.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.amount,
                        onValueChange = viewModel::setAmount,
                        label = { Text("পরিমাণ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::setDescription,
                        label = { Text("বিবরণ (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addExpense() },
                    enabled = state.amount.toDoubleOrNull() ?: 0.0 > 0 && !state.isSaving
                ) { Text(if (state.isSaving) "..." else "সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddDialog() }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RedExpense.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (expense.categoryId) {
                        ExpenseCategory.TRANSPORT -> "পরিবহন"
                        ExpenseCategory.LABOR -> "শ্রমিক"
                        ExpenseCategory.RENT -> "ভাড়া"
                        ExpenseCategory.UTILITY -> "ইউটিলিটি"
                        ExpenseCategory.PURCHASE -> "ক্রয়"
                        ExpenseCategory.OTHER -> "অন্যান্য"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (expense.description.isNotBlank()) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(expense.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RedExpense
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedExpense)
            }
        }
    }
}
