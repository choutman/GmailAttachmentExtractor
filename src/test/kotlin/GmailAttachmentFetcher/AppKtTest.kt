package GmailAttachmentFetcher

import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class AppKtTest {
    @Test
    fun name() {
        val date = LocalDate.ofEpochDay(4344753210)
        assertEquals(null, date)
    }
}