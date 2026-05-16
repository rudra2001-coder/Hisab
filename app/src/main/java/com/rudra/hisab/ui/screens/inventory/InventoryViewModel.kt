package com.rudra.hisab.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.CategoryEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.repository.CategoryRepository
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val showAddDialog: Boolean = false,
    val showStockDialog: Boolean = false,
    val stockDialogProduct: ProductEntity? = null,
    val stockDialogIsAdd: Boolean = true,
    val stockQuantity: String = "",
    val stockNote: String = "",
    val isSaving: Boolean = false,
    val showDeleteConfirm: ProductEntity? = null,
    val deletedProduct: ProductEntity? = null,
    val showPriceHistory: ProductEntity? = null,
    val priceHistory: List<TransactionEntity> = emptyList(),
    val productImageUri: String = "",
    val isBangla: Boolean = true
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val database: HisabDatabase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                categoryRepository.getAllCategories(),
                appPreferences.settings
            ) { products, categories, settings ->
                Pair(Pair(products, categories), settings.isBangla)
            }.collect { (first, isBangla) ->
                val (products, categories) = first
                _state.value = _state.value.copy(
                    products = products,
                    categories = categories,
                    isBangla = isBangla
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(id: Long?) {
        _state.value = _state.value.copy(selectedCategoryId = id)
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun showStockDialog(product: ProductEntity, isAdd: Boolean) {
        _state.value = _state.value.copy(
            showStockDialog = true,
            stockDialogProduct = product,
            stockDialogIsAdd = isAdd,
            stockQuantity = "",
            stockNote = ""
        )
    }

    fun hideStockDialog() {
        _state.value = _state.value.copy(
            showStockDialog = false,
            stockDialogProduct = null,
            stockQuantity = "",
            stockNote = ""
        )
    }

    fun setStockQuantity(qty: String) {
        _state.value = _state.value.copy(stockQuantity = qty)
    }

    fun setStockNote(note: String) {
        _state.value = _state.value.copy(stockNote = note)
    }

    fun setProductImageUri(uri: String) {
        _state.value = _state.value.copy(productImageUri = uri)
    }

    fun addProduct(name: String, nameBangla: String, unit: String, buyPrice: Double, sellPrice: Double, stock: Double, lowStock: Double, categoryId: Long?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            productRepository.insert(
                ProductEntity(
                    name = name,
                    nameBangla = nameBangla,
                    unit = unit,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    currentStock = stock,
                    lowStockThreshold = lowStock,
                    categoryId = categoryId
                )
            )
            _state.value = _state.value.copy(isSaving = false, showAddDialog = false)
        }
    }

    fun confirmStockUpdate() {
        val s = _state.value
        val product = s.stockDialogProduct ?: return
        val qty = s.stockQuantity.toDoubleOrNull() ?: return
        if (qty <= 0) return
        if (s.stockNote.isBlank()) return

        viewModelScope.launch {
            try {
                database.withTransaction {
                    if (s.stockDialogIsAdd) {
                        productRepository.addStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type = TransactionType.PURCHASE,
                                productId = product.id,
                                quantity = qty,
                                unitPrice = product.buyPrice,
                                totalAmount = product.buyPrice * qty,
                                notes = s.stockNote
                            )
                        )
                    } else {
                        productRepository.removeStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type = TransactionType.STOCK_LOSS,
                                productId = product.id,
                                quantity = qty,
                                unitPrice = 0.0,
                                totalAmount = 0.0,
                                notes = s.stockNote
                            )
                        )
                    }
                }
            } catch (_: Exception) {
            }
            hideStockDialog()
        }
    }

    fun requestDeleteProduct(product: ProductEntity) {
        _state.value = _state.value.copy(showDeleteConfirm = product)
    }

    fun confirmDelete() {
        val product = _state.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            productRepository.delete(product)
            _state.value = _state.value.copy(
                showDeleteConfirm = null,
                deletedProduct = product
            )
        }
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(showDeleteConfirm = null)
    }

    fun undoDelete() {
        val product = _state.value.deletedProduct ?: return
        viewModelScope.launch {
            productRepository.insert(product)
            _state.value = _state.value.copy(deletedProduct = null)
        }
    }

    fun showPriceHistory(product: ProductEntity) {
        viewModelScope.launch {
            val history = transactionRepository.getTransactionsByProductOnce(product.id)
            _state.value = _state.value.copy(
                showPriceHistory = product,
                priceHistory = history
            )
        }
    }

    fun hidePriceHistory() {
        _state.value = _state.value.copy(showPriceHistory = null, priceHistory = emptyList())
    }

    fun getFilteredProducts(): List<ProductEntity> {
        val s = _state.value
        return s.products.filter { product ->
            val matchesCategory = s.selectedCategoryId == null || product.categoryId == s.selectedCategoryId
            val matchesSearch = s.searchQuery.isBlank() ||
                    product.name.contains(s.searchQuery, ignoreCase = true) ||
                    product.nameBangla.contains(s.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }
}
