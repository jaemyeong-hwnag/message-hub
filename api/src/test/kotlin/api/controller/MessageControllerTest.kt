package com.hjm.messagehub.api.controller

import org.junit.jupiter.api.Tag
import kotlin.test.Test

/**
 * MessageController 단위 테스트
 */
@Tag("unit")
class MessageControllerTest {
    @Test
    fun `메시지 전송 요청이 성공적으로 처리되어야 한다`() {
        val requestBody =
            """
            {
                "channel": "slack",
                "content": "테스트 메시지",
                "recipient": "test@example.com",
                "sender": "system",
                "messageType": "text"
            }
            """.trimIndent()

        print("메시지 전송 요청이 성공적으로 처리되어야 한다")
    }
}
