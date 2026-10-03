package com.sultanagung1.sista.ui.announcements

import com.sultanagung1.sista.data.model.AnnouncementItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AnnouncementsUiTest {

    private val today = LocalDate.of(2026, 9, 30)
    private fun item(published: String?, date: String = "3 hari yang lalu") = AnnouncementItem(id = "1", title = "T", date = date, publishedAt = published)

    @Test
    fun `published time reads relative to today`() {
        assertEquals("Hari ini 16.20", publishedLabel(item("2026-09-30T16:20:00+07:00"), today))
        assertEquals("Kemarin 08.05", publishedLabel(item("2026-09-29T08:05:00+07:00"), today))
        assertEquals("21 Sep", publishedLabel(item("2026-09-21T10:00:00+07:00"), today))
        assertEquals("3 Mar 2025", publishedLabel(item("2025-03-03T10:00:00+07:00"), today))
        // An older server sends only its own text.
        assertEquals("3 hari yang lalu", publishedLabel(item(null), today))
    }

    @Test
    fun `storage paths become full urls`() {
        assertNull(absoluteUrl(null))
        assertEquals("https://sekolah.sch.id/storage/a.pdf", absoluteUrl("https://sekolah.sch.id/storage/a.pdf"))
        assertEquals("https://sekolah.sch.id/storage/a.pdf", absoluteUrl("/storage/a.pdf") { "https://sekolah.sch.id/" })
    }

    @Test
    fun `categories follow the web form, anything else is general`() {
        assertEquals(AnnouncementCategory.Finance, AnnouncementCategory.of("keuangan"))
        assertEquals(AnnouncementCategory.Islamic, AnnouncementCategory.of("Keislaman"))
        assertEquals(AnnouncementCategory.General, AnnouncementCategory.of("Darurat"))
    }

    @Test
    fun `unread and confirmation come only from the server`() {
        assertFalse(isUnread(AnnouncementItem(id = "1", title = "T")))
        assertTrue(isUnread(AnnouncementItem(id = "1", title = "T", isRead = false)))
        assertTrue(needsAcknowledgement(AnnouncementItem(id = "1", title = "T", requireAcknowledgement = true)))
        assertFalse(needsAcknowledgement(AnnouncementItem(id = "1", title = "T", requireAcknowledgement = true, acknowledgedAt = "2026-09-30T08:00:00+07:00")))
    }
}
