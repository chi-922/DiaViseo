package com.example.diaviseo.ui.mypageedit.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.diaviseo.R
import com.example.diaviseo.ui.components.CommonTopBar
import com.example.diaviseo.ui.mypageedit.component.Exercise
import com.example.diaviseo.ui.mypageedit.component.ExerciseCategory
import com.example.diaviseo.ui.mypageedit.component.SelectableExerciseItem
import com.example.diaviseo.ui.mypageedit.bottomsheet.ExerciseSearchBottomSheetContent
import com.example.diaviseo.ui.theme.DiaViseoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredExerciseScreen(navController: NavHostController) {
    val context = LocalContext.current

    val dummyExercises = listOf(
        Exercise(1, "걷기", R.drawable.charac_exercise, categoryId = 2),
        Exercise(2, "달리기", R.drawable.charac_main, categoryId = 2),
        Exercise(3, "자전거타기", R.drawable.charac_eat, categoryId = 5)
    )
    val dummyCategories = listOf(
        ExerciseCategory(2, "유산소"),
        ExerciseCategory(5, "아웃도어")
    )

    var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var pendingExercise by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            Column {
                CommonTopBar(
                    title = "선호하는 운동",
                    onLeftActionClick = { navController.popBackStack() }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        Toast.makeText(context, "운동 정보가 저장되었습니다", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }) {
                        Text("완료", color = DiaViseoColors.Main1)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            if (selectedExercises.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.charac_main),
                        contentDescription = "운동 없음"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("아직 추가한 운동이 없어요", color = DiaViseoColors.Basic)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showBottomSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DiaViseoColors.Main1)
                    ) {
                        Text("운동 추가하기", color = Color.White)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn {
                        items(selectedExercises) { exercise ->
                            SelectableExerciseItem(
                                exercise = exercise,
                                isSelected = true,
                                onClick = {
                                    pendingExercise = exercise
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DiaViseoColors.Main1)
                    ) {
                        Text("운동 추가하기", color = Color.White)
                    }
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    containerColor = Color.White
                ) {
                    ExerciseSearchBottomSheetContent(
                        allExercises = dummyExercises,
                        categories = dummyCategories,
                        selectedExercises = selectedExercises,
                        onSelectExercise = { selectedExercises = selectedExercises + it },
                        onRemoveExercise = { selectedExercises = selectedExercises - it },
                        onDone = { showBottomSheet = false }
                    )
                }
            }

            if (pendingExercise != null) {
                AlertDialog(
                    onDismissRequest = { pendingExercise = null },
                    title = { Text("운동 삭제") },
                    text = { Text("${'$'}{pendingExercise!!.name} 운동을 삭제하시겠습니까?") },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedExercises = selectedExercises - pendingExercise!!
                            Toast.makeText(context, "운동이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            pendingExercise = null
                        }) {
                            Text("예", color = DiaViseoColors.Main1)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingExercise = null }) {
                            Text("아니오", color = DiaViseoColors.Unimportant)
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreferredExerciseScreenPreview() {
    PreferredExerciseScreen(navController = rememberNavController())
}
