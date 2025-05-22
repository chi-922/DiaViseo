package com.example.diaviseo.ui.register.diet

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diaviseo.mapper.toFoodDetailResponse
import com.example.diaviseo.network.food.dto.res.FoodDetailResponse
import com.example.diaviseo.ui.components.BottomButtonSection
import com.example.diaviseo.ui.components.CommonTopBar
import com.example.diaviseo.ui.register.diet.components.SelectedFoodList
import com.example.diaviseo.ui.register.components.CommonSearchTopBar
import com.example.diaviseo.ui.register.diet.components.FoodDetailBottomSheet
import com.example.diaviseo.ui.register.diet.components.SearchSuggestionList
import com.example.diaviseo.ui.theme.DiaViseoColors
import com.example.diaviseo.ui.theme.regular14
import com.example.diaviseo.viewmodel.DietSearchViewModel

@Composable
fun FoodSetRegisterScreen(
    navController: NavController,
    viewModel: DietSearchViewModel
) {
    var setName by remember { mutableStateOf("") }
    val selectedItems = viewModel.selectedItems
    val context = LocalContext.current
    val selectedFoodDetail = remember { mutableStateOf<FoodDetailResponse?>(null) }
    val selectedQuantity = remember { mutableStateOf(1.0f) }
    val showFoodDetailSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "세트 등록",
                onLeftActionClick = { navController.popBackStack() }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 22.dp, end = 22.dp, bottom = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(42.dp))

            // 음식 검색창
            CommonSearchTopBar(
                placeholder = "세트에 추가할 음식을 검색해보세요.",
                navController = navController,
                keyword = viewModel.keyword,
                onKeywordChange = { viewModel.onKeywordChange(it) },
                onCancelClick = { viewModel.cancelSearch() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.isSearching) {
                // 검색 중일 때는 검색 결과만 보여줌
                SearchSuggestionList(
                    results = viewModel.searchResults,
                    selectedItems = selectedItems.map { it.foodId },
                    onFoodClick = {},
                    onToggleSelect = { viewModel.onToggleSelect(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // 세트 이름 입력
                Text(text = "세트 이름", style = regular14)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = setName,
                    onValueChange = { setName = it },
                    placeholder = { Text("예: 다이어트 도시락") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiaViseoColors.Main1,
                        focusedLabelColor = DiaViseoColors.Main1
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 세트 음식 목록
                if (selectedItems.isNotEmpty()) {
                    Text(text = "세트 음식 목록", style = regular14)
                    Spacer(modifier = Modifier.height(6.dp))

                    SelectedFoodList(
                        selectedItems = selectedItems,
                        onRemoveItem = { viewModel.removeSelectedFood(it) },
                        onItemClick = { foodItem ->
                            selectedFoodDetail.value = foodItem.toFoodDetailResponse()
                            selectedQuantity.value = foodItem.quantity
                            showFoodDetailSheet.value = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // 등록 버튼
                BottomButtonSection(
                    text = "세트 등록하기",
                    onClick = {
                        if (setName.isBlank()) {
                            Toast.makeText(context, "세트 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@BottomButtonSection
                        }

                        viewModel.registerFoodSet(
                            name = setName,
                            onSuccess = {
                                Toast.makeText(context, "세트가 등록되었어요!", Toast.LENGTH_SHORT).show()
                                viewModel.clearDietState() // 상태 초기화
                                navController.popBackStack()
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
                if (showFoodDetailSheet.value && selectedFoodDetail.value != null) {
                    FoodDetailBottomSheet(
                        food = selectedFoodDetail.value!!,
                        initialQuantity = selectedQuantity.value,
                        onToggleFavorite = {},
                        onAddClick = { newQuantity ->
                            viewModel.updateSelectedFoodQuantity(
                                foodId = selectedFoodDetail.value!!.foodId,
                                quantity = newQuantity
                            )
                            showFoodDetailSheet.value = false
                        },
                        onDismiss = {
                            showFoodDetailSheet.value = false
                        }
                    )
                }

            }
        }
    }
}
