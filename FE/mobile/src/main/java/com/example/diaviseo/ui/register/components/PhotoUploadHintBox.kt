package com.example.diaviseo.ui.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.diaviseo.R
import com.example.diaviseo.ui.theme.semibold18

@Composable
fun PhotoUploadHintBox(
    isAiMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val mainText = if (isAiMode) "사진을 올리면" else "식단 사진을 기록해보세요"
    val subText = if (isAiMode) "AI가 자동으로 인식해요" else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(color = Color(0xFFF1F6FF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "사진 업로드",
                tint = Color.Unspecified,
                modifier = Modifier.size(120.dp)
            )
            Text(text = mainText, style = semibold18, color = Color.Black)
            if (subText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subText, style = semibold18, color = Color.Black)
            }

        }
    }
}
