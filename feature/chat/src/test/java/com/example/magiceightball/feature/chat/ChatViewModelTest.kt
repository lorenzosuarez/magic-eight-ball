package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.common.Result
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import com.example.magiceightball.core.domain.model.QueryConfig
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendMessageUseCase: SendMessageUseCase = mockk()
    private val observeShakeUseCase: ObserveShakeUseCase = mockk()

    private val queryConfig = QueryConfig(
        processingDelayMs = 1L,
        completionHoldTimeMs = 1L,
        cooldownMs = 1L
    )

    @Test
    fun `initial state is correct`() = runTest {
        every { observeShakeUseCase() } returns flowOf()
        val viewModel = ChatViewModel(sendMessageUseCase, observeShakeUseCase, queryConfig)
        
        assertEquals(false, viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.machineState is ChatStateMachine.Idle)
        assertEquals(R.string.title_main, viewModel.state.value.titleRes)
    }

    @Test
    fun `shake starts transitions to Running`() = runTest {
        coEvery { sendMessageUseCase(any()) } coAnswers {
            delay(1000) 
            Result.Success("Answer")
        }
        
        every { observeShakeUseCase() } returns flow {
            emit(ShakeEvent.Started)
            delay(Long.MAX_VALUE)
        }
        
        val viewModel = ChatViewModel(sendMessageUseCase, observeShakeUseCase, queryConfig)
        
        testScheduler.advanceTimeBy(10)
        
        assertEquals(ChatStateMachine.Running, viewModel.state.value.machineState)
    }

    @Test
    fun `full flow transitions to Completed and then Idle`() = runTest {
        var subscriptionCount = 0
        every { observeShakeUseCase() } answers {
            subscriptionCount++
            if (subscriptionCount == 1) flowOf(ShakeEvent.Started) else flowOf()
        }
        coEvery { sendMessageUseCase(any()) } returns Result.Success("Answer")

        val viewModel = ChatViewModel(sendMessageUseCase, observeShakeUseCase, queryConfig)
        
        testScheduler.advanceUntilIdle()
        
        assertTrue(viewModel.state.value.machineState is ChatStateMachine.Idle)
    }
}
