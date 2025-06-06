package com.a401.spicoandroid.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a401.spicoandroid.common.ui.component.CommonList
import com.a401.spicoandroid.common.ui.theme.*
import com.a401.spicoandroid.common.ui.theme.dropShadow1
import com.a401.spicoandroid.domain.home.model.HomeReport
import com.a401.spicoandroid.domain.home.model.PracticeType

@Composable
fun RecentReportSection(
    modifier: Modifier = Modifier,
    reportList: List<HomeReport>,
    onReportClick: (HomeReport) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BrokenWhite)
            .padding(top = 28.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "최근 연습 리포트",
            style = Typography.displaySmall,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (reportList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "최근 연습 리포트가 없습니다.",
                    style = Typography.titleLarge,
                    color = TextTertiary
                )
            }
        } else {
            reportList.forEachIndexed { index, report ->
                val modeShortLabel = when (report.type) {
                    PracticeType.COACHING -> "코칭"
                    PracticeType.FINAL -> "파이널"
                }

                val title = "$modeShortLabel ${report.practiceName}"
                val description = report.projectName

                CommonList(
                    modifier = Modifier.dropShadow1(),
                    title = title,
                    description = description,
                    onClick = { onReportClick(report)}
                )
                if (index < reportList.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
