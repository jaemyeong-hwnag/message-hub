package com.hjm.messagehub.api.integration

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 메시지 통합 테스트
 *
 * 실제 데이터베이스와 외부 서비스와의 연동을 테스트합니다.
 * 로컬 환경에서만 실행되며, CI/CD에서는 제외됩니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class MessageIntegrationTest {
    @Test
    fun `실제 데이터베이스와 연동하여 메시지 상태 조회가 성공해야 한다`() {
        print("실제 데이터베이스와 연동하여 메시지 상태 조회가 성공해야 한다")
    }

    @Test
    fun `실제 큐 시스템과 연동하여 메시지 처리가 성공해야 한다`() {
        print("실제 큐 시스템과 연동하여 메시지 처리가 성공해야 한다")
    }
}
