package com.example.diaviseo.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.diaviseo.ui.components.BottomNavigationBar
import com.example.diaviseo.ui.detail.ExerciseDetailScreen
import com.example.diaviseo.ui.detail.MealDetailScreen
import com.example.diaviseo.ui.detail.HomeDetailScreen
import com.example.diaviseo.ui.main.components.FabOverlayMenu
import com.example.diaviseo.ui.main.components.chat.ChatHistoryScreen
import com.example.diaviseo.ui.main.components.my.HealthConnectManageScreen
import com.example.diaviseo.ui.mypageedit.screen.AllergyEditScreen
import com.example.diaviseo.ui.mypageedit.screen.DiseaseEditScreen
import com.example.diaviseo.ui.mypageedit.screen.FaqScreen
import com.example.diaviseo.ui.mypageedit.screen.PhysicalInfoEditScreen
import com.example.diaviseo.ui.mypageedit.screen.PreferredExerciseScreen
import com.example.diaviseo.ui.mypageedit.screen.UserProfileEditScreen
import com.example.diaviseo.ui.register.bodyregister.BodyDataRegisterScreen
import com.example.diaviseo.ui.register.diet.DietRegisterMainScreen
import com.example.diaviseo.ui.register.exercise.ExerciseRegisterMainScreen
import com.example.diaviseo.ui.register.diet.DietAiRegisterScreen
import com.example.diaviseo.viewmodel.ProfileViewModel
import com.example.diaviseo.ui.register.diet.dietGraph
import com.example.diaviseo.viewmodel.AuthViewModel
import com.example.diaviseo.viewmodel.SplashViewModel
import com.example.diaviseo.viewmodel.condition.AllergyViewModel
import com.example.diaviseo.viewmodel.condition.DiseaseViewModel
import java.time.LocalDate

@Composable
fun MainScreen(
    navControll: NavHostController
) {
    // 화면 뜨자마자 회원정보 불러오기
    val profileViewModel: ProfileViewModel = viewModel()

    val profile by profileViewModel.myProfile.collectAsState()

    // 화면 이동을 관리해주는 내비게이션 컨트롤러
    val navController = rememberNavController()
    val isFabMenuOpen = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        viewModelStoreOwner = context as ComponentActivity
    )
    val splashViewModel: SplashViewModel = viewModel()
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsState()

    // [2] 로그아웃 감지 → 로그인 화면으로 이동
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) {
            navControll.navigate("signupGraph") {
                popUpTo("main") { inclusive = true }
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hideBottomBarRoutes = listOf(
        "body_register",
        "diet_register",
        "exercise_register",
        "exercise_register/{date}",
        "diet_ai_register",
        "edit_allergy",
        "edit_disease",
        "edit_exercise",
        "faq",
        "edit_profile",
        "edit_physical_info",
        "exercise_detail",
        "meal_detail",
        "diet_confirm",
        "food_set_register",
        "home_detail",
        "chat",
        "chat_history",
        "my",
        "healthConnect/manage"
    )
    val isBottomBarVisible = currentRoute !in hideBottomBarRoutes

    // 메인 탭 화면들 (하단바에서 이동하는 화면들)
    val mainTabRoutes = listOf("home", "chat_history", "goal", "my")

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigationBar(
                    navController = navController,
                    isFabMenuOpen = isFabMenuOpen
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
            // 메인 탭 간 이동을 위한 자연스러운 슬라이드 애니메이션
            enterTransition = {
                when {
                    // 메인 탭 간 이동 시 슬라이드 애니메이션
                    initialState.destination.route in mainTabRoutes &&
                            targetState.destination.route in mainTabRoutes -> {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    }
                    // 다른 화면으로 이동 시 페이드 인
                    else -> fadeIn(animationSpec = tween(250))
                }
            },
            exitTransition = {
                when {
                    // 메인 탭 간 이동 시 슬라이드 애니메이션
                    initialState.destination.route in mainTabRoutes &&
                            targetState.destination.route in mainTabRoutes -> {
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                    // 다른 화면으로 이동 시 페이드 아웃
                    else -> fadeOut(animationSpec = tween(250))
                }
            },
            popEnterTransition = {
                when {
                    // 메인 탭 복귀 시 슬라이드 애니메이션
                    initialState.destination.route in mainTabRoutes &&
                            targetState.destination.route in mainTabRoutes -> {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    }
                    // 뒤로가기 시 페이드 인
                    else -> fadeIn(animationSpec = tween(250))
                }
            },
            popExitTransition = {
                when {
                    // 메인 탭 복귀 시 슬라이드 애니메이션
                    initialState.destination.route in mainTabRoutes &&
                            targetState.destination.route in mainTabRoutes -> {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                    // 뒤로가기 시 페이드 아웃
                    else -> fadeOut(animationSpec = tween(250))
                }
            }
        ) {
            composable("home") {
                // 기존 HomeScreen을 그대로 재사용
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreen(
                        navController = navController,
                        viewModel = profileViewModel
                    )
                }
            }

            composable("chat_history") {
                ChatHistoryScreen(navController)
            }

            composable("chat") {
                ChatScreen(navController)
            }

            composable("goal") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    GoalScreen(
                        navController = navController,
                        gender = profile?.gender)
                }
            }
            // 마이페이지
            composable("my") {
                MyScreen(navController)
            }
            // 회원 정보 수정
            composable("edit_profile") {
                UserProfileEditScreen(
                    navController = navController,
                    rootNavController = navControll
                )
            }

            composable("edit_physical_info") {
                PhysicalInfoEditScreen(
                    navController = navController,
                    viewModel = profileViewModel
                )
            }

            composable("edit_allergy") {
                val allergyViewModel: AllergyViewModel = viewModel()
                AllergyEditScreen(
                    navController = navController,
                    viewModel = allergyViewModel
                )
            }

            composable("edit_disease") {
                val diseaseViewModel: DiseaseViewModel = viewModel()
                DiseaseEditScreen(
                    navController = navController,
                    viewModel = diseaseViewModel
                )
            }

            composable("healthConnect/manage") {
                HealthConnectManageScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("edit_exercise") {
                PreferredExerciseScreen(navController)
            }
            composable("faq") {
                FaqScreen(navController)
            }

            composable("body_register") {
                BodyDataRegisterScreen(navController)
            }

            dietGraph(navController)

            // 기본 라우트 (오늘 날짜 사용)
            composable("exercise_register") {
                val today = LocalDate.now().toString()
                ExerciseRegisterMainScreen(date = today, navController = navController)
            }

            // 날짜 파라미터를 받는 라우트
            composable("exercise_register/{date}") { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                ExerciseRegisterMainScreen(date = date, navController = navController)
            }

            composable("diet_ai_register") {
                DietAiRegisterScreen(navController)
            }

            // 운동 상세화면
            composable("exercise_detail") { backStackEntry ->
                ExerciseDetailScreen(
                    navController = navController,
                    viewModel = profileViewModel
                )
            }

            // 홈 상세화면
            composable("home_detail") { backStackEntry ->
                HomeDetailScreen(
                    navController = navController,
                    viewModel = profileViewModel
                )
            }
        }
    }
    // 조건부 UI는 Scaffold 바깥에!
    // 하단바 + 버튼 토글 (컴포넌트로 분리)
    if (isFabMenuOpen.value) {
        FabOverlayMenu(
            onDismiss = { isFabMenuOpen.value = false },
            navController = navController
        )
    }
}