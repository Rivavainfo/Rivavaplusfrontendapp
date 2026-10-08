package com.rivavafi.universal.ui.help

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class HelpChatViewModelTest {

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testInitialMessagePresent() {
        val viewModel = HelpChatViewModel()
        val messages = viewModel.messages.value
        assertEquals(1, messages.size)
        assertEquals("Hello! I'm your Rivava+ AI Financial Assistant. How can I help you today?", messages[0].text)
        assertEquals(false, messages[0].isUser)

        // Send a message
        viewModel.sendMessage("I need help with my account")

        // Wait for coroutine delay (800ms in ViewModel)
        Thread.sleep(1200)

        // Now there should be 3 messages: welcome, user message, bot reply
        val updatedMessages = viewModel.messages.value
        assertEquals(3, updatedMessages.size)

        // Check user message
        assertEquals("I need help with my account", updatedMessages[1].text)
        assertEquals(true, updatedMessages[1].isUser)

        // Check bot reply
        assertTrue(updatedMessages[2].text.contains("8881176909"))
        assertEquals(false, updatedMessages[2].isUser)
    }

    @Test
    fun testEmptyMessageIsIgnored() {
        val viewModel = HelpChatViewModel()

        viewModel.sendMessage("   ")

        val messages = viewModel.messages.value
        assertEquals(1, messages.size)
    }
}
