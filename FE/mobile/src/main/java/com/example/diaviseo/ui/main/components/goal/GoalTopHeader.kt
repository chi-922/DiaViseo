package com.example.diaviseo.ui.main.components.goal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diaviseo.R
import com.example.diaviseo.ui.theme.DiaViseoColors
import com.example.diaviseo.ui.theme.*
import com.example.diaviseo.viewmodel.goal.GoalViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import androidx.compose.runtime.getValue

@Composable
fun GoalTopHeader(
    currentDate: LocalDate,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    viewModel: GoalViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val month = selectedDate.month.value
    val day = selectedDate.dayOfMonth.toString()
    val dayOfWeek = selectedDate.dayOfWeek
    val isToday = selectedDate == LocalDate.now()

    val dayColor = when (dayOfWeek) {
        DayOfWeek.SATURDAY -> DiaViseoColors.Main2
        DayOfWeek.SUNDAY -> Color(0xFFFFB8B8)
        else -> Color.White
    }

    val textColor = if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
        Color.White
    } else DiaViseoColors.Basic

    val dayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF5C9DFF))
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 빈 공간이지만 weight(1f)로 채우면서 중앙 정렬
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(IntrinsicSize.Min),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = day,
                        style = bold24,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(color = dayColor, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(text = dayName, color = textColor, style = medium14)
                    }
                }
            }

            // 오른쪽: 캘린더 아이콘
            Image(
                painter = painterResource(id = R.drawable.diary),
                contentDescription = "달력",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { viewModel.setShowDatePicker() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
            text = buildAnnotatedString {
                val prefix = if (isToday) "오늘의 " else "${month}월 ${day}일의 "
                append(prefix)
                withStyle(SpanStyle(color = Color.White)) {
                    append(selectedTab)
                }
                append(" 평가")
            },
            style = bold20,
            color = DiaViseoColors.Basic
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabSelector(selectedTab = selectedTab, onTabChange = onTabChange)
    }
}
