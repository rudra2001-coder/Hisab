package com.rudra.hisab.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.CategoryEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CategoryRepository
import com.rudra.hisab.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 1,
    val shopName: String = "",
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
)

data class PresetCategory(
    val id: Long,
    val name: String,
    val nameBangla: String,
    val icon: String
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    val presetCategories = listOf(
        PresetCategory(1, "Coal", "কয়লা", "whatshot"),
        PresetCategory(2, "Rice", "চাল", "grain"),
        PresetCategory(3, "Grocery", "মুদি", "shopping_cart"),
        PresetCategory(4, "Cloth", "কাপড়", "checkroom"),
        PresetCategory(5, "Hardware", "হার্ডওয়্যার", "hardware"),
        PresetCategory(6, "Pharmacy", "ফার্মেসি", "medical_services"),
        PresetCategory(7, "Stationery", "স্টেশনারি", "edit"),
        PresetCategory(8, "Other", "অন্যান্য", "more_horiz")
    )

    fun setShopName(name: String) {
        _state.value = _state.value.copy(shopName = name)
    }

    fun selectCategory(id: Long) {
        _state.value = _state.value.copy(selectedCategoryId = id)
    }

    fun nextStep() {
        val current = _state.value
        _state.value = current.copy(currentStep = current.currentStep + 1)
    }

    fun previousStep() {
        val current = _state.value
        _state.value = current.copy(currentStep = current.currentStep - 1)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            appPreferences.setShopName(_state.value.shopName)
            appPreferences.setShopCategory(
                presetCategories.find { it.id == _state.value.selectedCategoryId }?.name ?: "Other"
            )

            val categoryId = categoryRepository.insert(
                CategoryEntity(
                    name = presetCategories.find { it.id == _state.value.selectedCategoryId }?.name ?: "Other",
                    nameBangla = presetCategories.find { it.id == _state.value.selectedCategoryId }?.nameBangla ?: "অন্যান্য"
                )
            )

            val starterProducts = getStarterProducts(categoryId)
            productRepository.insertAll(starterProducts)

            appPreferences.completeOnboarding()
            _state.value = _state.value.copy(isLoading = false, isComplete = true)
        }
    }

    private fun getStarterProducts(categoryId: Long): List<ProductEntity> {
        return when (_state.value.selectedCategoryId) {
            1L -> listOf(
                ProductEntity(name = "Bituminous Coal", nameBangla = "বিটুমিনাস কয়লা", unit = "kg", buyPrice = 15.0, sellPrice = 22.0, currentStock = 500.0, lowStockThreshold = 50.0, categoryId = categoryId),
                ProductEntity(name = "Anthracite Coal", nameBangla = "অ্যানথ্রাসাইট কয়লা", unit = "kg", buyPrice = 25.0, sellPrice = 35.0, currentStock = 300.0, lowStockThreshold = 30.0, categoryId = categoryId),
                ProductEntity(name = "Firewood", nameBangla = "জ্বালানি কাঠ", unit = "kg", buyPrice = 8.0, sellPrice = 12.0, currentStock = 1000.0, lowStockThreshold = 100.0, categoryId = categoryId)
            )
            2L -> listOf(
                ProductEntity(name = "Miniket Rice", nameBangla = "মিনিকেট চাল", unit = "kg", buyPrice = 50.0, sellPrice = 58.0, currentStock = 200.0, lowStockThreshold = 20.0, categoryId = categoryId),
                ProductEntity(name = "Najirshail Rice", nameBangla = "নাজিরশাইল চাল", unit = "kg", buyPrice = 60.0, sellPrice = 70.0, currentStock = 150.0, lowStockThreshold = 15.0, categoryId = categoryId),
                ProductEntity(name = "Puffed Rice", nameBangla = "মুড়ি", unit = "kg", buyPrice = 40.0, sellPrice = 48.0, currentStock = 80.0, lowStockThreshold = 10.0, categoryId = categoryId)
            )
            3L -> listOf(
                ProductEntity(name = "Cooking Oil", nameBangla = "রান্নার তেল", unit = "litre", buyPrice = 160.0, sellPrice = 180.0, currentStock = 50.0, lowStockThreshold = 10.0, categoryId = categoryId),
                ProductEntity(name = "Sugar", nameBangla = "চিনি", unit = "kg", buyPrice = 90.0, sellPrice = 100.0, currentStock = 100.0, lowStockThreshold = 15.0, categoryId = categoryId),
                ProductEntity(name = "Salt", nameBangla = "লবণ", unit = "kg", buyPrice = 30.0, sellPrice = 35.0, currentStock = 80.0, lowStockThreshold = 10.0, categoryId = categoryId),
                ProductEntity(name = "Flour", nameBangla = "আটা", unit = "kg", buyPrice = 40.0, sellPrice = 45.0, currentStock = 60.0, lowStockThreshold = 10.0, categoryId = categoryId)
            )
            4L -> listOf(
                ProductEntity(name = "Cotton Saree", nameBangla = "কটন শাড়ি", unit = "piece", buyPrice = 400.0, sellPrice = 550.0, currentStock = 30.0, lowStockThreshold = 5.0, categoryId = categoryId),
                ProductEntity(name = "Lungi", nameBangla = "লুঙ্গি", unit = "piece", buyPrice = 250.0, sellPrice = 350.0, currentStock = 40.0, lowStockThreshold = 5.0, categoryId = categoryId),
                ProductEntity(name = "Panjabi", nameBangla = "পাঞ্জাবি", unit = "piece", buyPrice = 800.0, sellPrice = 1200.0, currentStock = 20.0, lowStockThreshold = 3.0, categoryId = categoryId)
            )
            5L -> listOf(
                ProductEntity(name = "Cement", nameBangla = "সিমেন্ট", unit = "sack", buyPrice = 420.0, sellPrice = 460.0, currentStock = 100.0, lowStockThreshold = 15.0, categoryId = categoryId),
                ProductEntity(name = "MS Rod", nameBangla = "এমএস রড", unit = "kg", buyPrice = 90.0, sellPrice = 105.0, currentStock = 500.0, lowStockThreshold = 50.0, categoryId = categoryId),
                ProductEntity(name = "Paint", nameBangla = "পেইন্ট", unit = "litre", buyPrice = 300.0, sellPrice = 380.0, currentStock = 25.0, lowStockThreshold = 5.0, categoryId = categoryId)
            )
            6L -> listOf(
                ProductEntity(name = "Paracetamol", nameBangla = "প্যারাসিটামল", unit = "piece", buyPrice = 1.0, sellPrice = 2.0, currentStock = 500.0, lowStockThreshold = 50.0, categoryId = categoryId),
                ProductEntity(name = "Antacid", nameBangla = "অ্যান্টাসিড", unit = "piece", buyPrice = 3.0, sellPrice = 5.0, currentStock = 200.0, lowStockThreshold = 20.0, categoryId = categoryId),
                ProductEntity(name = "Band Aid", nameBangla = "ব্যান্ড এইড", unit = "piece", buyPrice = 2.0, sellPrice = 5.0, currentStock = 100.0, lowStockThreshold = 10.0, categoryId = categoryId)
            )
            7L -> listOf(
                ProductEntity(name = "Notebook", nameBangla = "খাতা", unit = "piece", buyPrice = 20.0, sellPrice = 35.0, currentStock = 100.0, lowStockThreshold = 10.0, categoryId = categoryId),
                ProductEntity(name = "Pen", nameBangla = "কলম", unit = "piece", buyPrice = 5.0, sellPrice = 10.0, currentStock = 200.0, lowStockThreshold = 20.0, categoryId = categoryId),
                ProductEntity(name = "Pencil", nameBangla = "পেন্সিল", unit = "piece", buyPrice = 3.0, sellPrice = 5.0, currentStock = 150.0, lowStockThreshold = 20.0, categoryId = categoryId)
            )
            else -> listOf(
                ProductEntity(name = "Product 1", nameBangla = "পণ্য ১", unit = "piece", buyPrice = 50.0, sellPrice = 70.0, currentStock = 100.0, lowStockThreshold = 10.0, categoryId = categoryId),
                ProductEntity(name = "Product 2", nameBangla = "পণ্য ২", unit = "piece", buyPrice = 30.0, sellPrice = 45.0, currentStock = 80.0, lowStockThreshold = 10.0, categoryId = categoryId),
                ProductEntity(name = "Product 3", nameBangla = "পণ্য ৩", unit = "piece", buyPrice = 20.0, sellPrice = 30.0, currentStock = 60.0, lowStockThreshold = 10.0, categoryId = categoryId)
            )
        }
    }
}
