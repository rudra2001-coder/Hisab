package com.rudra.hisab.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.GreenProfitContainer

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state.isComplete) {
        onComplete()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "হিসাব",
            style = MaterialTheme.typography.displayLarge,
            color = GreenProfit,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "আপনার দোকানের হিসাব রাখুন সহজে",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "step"
        ) { step ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    1 -> StepShopName(
                        shopName = state.shopName,
                        onShopNameChange = viewModel::setShopName
                    )
                    2 -> StepCategoryPicker(
                        categories = viewModel.presetCategories,
                        selectedId = state.selectedCategoryId,
                        onSelect = viewModel::selectCategory
                    )
                    3 -> StepReady(
                        isLoading = state.isLoading,
                        onFinish = viewModel::completeOnboarding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.currentStep == 1) {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.shopName.isNotBlank()
            ) {
                Text("পরবর্তী", style = MaterialTheme.typography.titleLarge)
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        if (state.currentStep == 2) {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.selectedCategoryId != null
            ) {
                Text("পরবর্তী", style = MaterialTheme.typography.titleLarge)
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.previousStep() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                Text("পিছনে", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (state.currentStep == 3) {
            if (!state.isLoading) {
                OutlinedButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    Text("পিছনে", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun StepShopName(
    shopName: String,
    onShopNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "আপনার দোকানের নাম লিখুন",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = shopName,
            onValueChange = onShopNameChange,
            label = { Text("দোকানের নাম") },
            placeholder = { Text("যেমন: রহিম স্টোর") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            textStyle = MaterialTheme.typography.titleLarge
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepCategoryPicker(
    categories: List<PresetCategory>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "আপনার দোকানের ধরন নির্বাচন করুন",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "আমরা আপনার জন্য কিছু পণ্য তৈরি করে দেব",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedId == category.id
                Card(
                    modifier = Modifier
                        .size(width = 150.dp, height = 80.dp)
                        .clickable { onSelect(category.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenProfitContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) BorderStroke(2.dp, GreenProfit) else null
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.nameBangla,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepReady(
    isLoading: Boolean,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "সবকিছু প্রস্তুত!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "আপনার দোকানের জন্য স্টার্টার পণ্য তৈরি করা হবে। আপনি পরে সেগুলো পরিবর্তন করতে পারবেন।",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = GreenProfit)
        } else {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("শুরু করুন", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
