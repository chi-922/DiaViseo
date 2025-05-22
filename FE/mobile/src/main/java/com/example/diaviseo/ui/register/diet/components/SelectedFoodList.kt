package com.example.diaviseo.ui.register.diet.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.diaviseo.model.diet.FoodWithQuantity
import com.example.diaviseo.ui.components.AddRemoveIconButton

@Composable
fun SelectedFoodList(
    selectedItems: List<FoodWithQuantity>,
    onRemoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (FoodWithQuantity) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        if (selectedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "선택한 음식이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                selectedItems.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable{ onItemClick(item) }
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.foodName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (item.quantity % 1f == 0f)
                                        "${item.quantity.toInt()}인분"
                                    else
                                        "${String.format("%.1f", item.quantity)} 인분",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )

                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${item.calorie}kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                AddRemoveIconButton(
                                    isSelected = true,
                                    onClick = { onRemoveItem(item.foodId) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // 마지막 항목이 아닌 경우에만 Divider 표시
                        if (index != selectedItems.lastIndex) {
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF0F0F0),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
