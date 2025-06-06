package com.a401.spicoandroid.presentation.finalmode.screen

import android.net.Uri
import androidx.compose.runtime.*
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.a401.spicoandroid.R
import com.a401.spicoandroid.common.ui.component.LoadingInProgressView
import com.a401.spicoandroid.presentation.finalmode.viewmodel.FinalModeViewModel
import com.a401.spicoandroid.presentation.navigation.NavRoutes
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.a401.spicoandroid.data.finalmode.dto.AnswerDto
import com.a401.spicoandroid.data.finalmode.dto.FinalModeResultRequestDto
import com.a401.spicoandroid.data.finalmode.dto.PauseRecordDto
import com.a401.spicoandroid.data.finalmode.dto.SpeedRecordDto
import com.a401.spicoandroid.data.finalmode.dto.VolumeRecordDto
import com.a401.spicoandroid.domain.finalmode.model.toFinalModeResultRequestDto
import com.a401.spicoandroid.presentation.practice.viewmodel.PracticeViewModel

enum class FinalModeLoadingType {
    QUESTION,
    REPORT
}

@Composable
fun FinalModeLoadingScreen(
    navController: NavController,
    parentNavController: NavController,
    projectId: Int,
    practiceId: Int,
    viewModel: FinalModeViewModel = hiltViewModel(),
    type: FinalModeLoadingType
) {
    val context = LocalContext.current
    val result by viewModel.assessmentResult.collectAsState()
    val questionState by viewModel.finalQuestionState.collectAsState()
    val isAnswerCompleted by viewModel.isAnswerCompleted.collectAsState()

    // 뒤로 가기 막기
    BackHandler(enabled = true){}

// 질문 생성용 LaunchedEffect
    if (type == FinalModeLoadingType.QUESTION) {
        LaunchedEffect(result) {
            result?.let {
                Log.d("FinalFlow", "🚀 질문 생성 시작")
                viewModel.generateFinalQuestions(
                    projectId = projectId,
                    practiceId = practiceId,
                    speechContent = it.transcribedText
                )
                navController.navigate(NavRoutes.FinalModeQnA.withArgs(projectId, practiceId))
            }
        }
    }

    // 결과 전송용 LaunchedEffect
    if (type == FinalModeLoadingType.REPORT) {
        LaunchedEffect(isAnswerCompleted) {
            if (isAnswerCompleted) {
                Log.d("FinalFlow", "📤 결과 전송 시작")

                viewModel.setPracticeId(practiceId)

                val answers: List<AnswerDto> = questionState.questions.map { question ->
                    AnswerDto(
                        questionId = question.id,
                        answer = questionState.answers.find { it.questionId == question.id }?.text ?: ""
                    )
                }

                viewModel.submitFinalModeResult(
                    projectId = projectId,
                    request = result!!.toFinalModeResultRequestDto(answers = answers)
                )

                Log.d("FinalFlow", "📦 전송 request = ${result!!.toFinalModeResultRequestDto(answers)}")

                // 결과 전송이 완료되면 즉시 리포트 화면으로 이동
                parentNavController.navigate(
                    NavRoutes.FinalReport.createRoute(
                        projectId = projectId,
                        practiceId = practiceId
                    )
                )
            }
        }
    }




    val (imageRes, message) = when (type) {
        FinalModeLoadingType.QUESTION -> R.drawable.character_home_1 to
                "질문 생성중입니다.\n숨을 고르고 답변을 생각해주세요."
        FinalModeLoadingType.REPORT -> R.drawable.character_home_5 to
                "리포트를 정리 중이에요.\n잠시만 기다려주세요!"
    }

    LoadingInProgressView(
        imageRes = imageRes,
        message = message
    )
}





