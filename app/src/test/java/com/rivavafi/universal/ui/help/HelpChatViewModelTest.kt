package com.rivavafi.universal.ui.help

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpChatViewModelTest {

    @Test
    fun testInitialMessagePresent() {
        val viewModel = HelpChatViewModel()
        val messages = viewModel.messages.value
        assertEquals(1, messages.size)
        assertTrue(messages[0].text.contains("How can I help you today?"))
        assertEquals(false, messages[0].isUser)
    }

    @Test
    fun testEmptyMessageIsIgnored() {
        val viewModel = HelpChatViewModel()

        viewModel.sendMessage("   ")

        val messages = viewModel.messages.value
        assertEquals(1, messages.size)
    }
}
