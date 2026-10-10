package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiProvider
import com.example.data.ArabicDialect
import com.example.data.db.AetherDatabase
import com.example.data.db.VisionRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AETHER AI", appName)
  }

  @Test
  fun `verify arabic dialects available`() {
    val dialects = ArabicDialect.values()
    assertEquals(5, dialects.size)
    assertNotNull(ArabicDialect.MSA)
    assertEquals("ar", ArabicDialect.MSA.localeTag)
  }

  @Test
  fun `verify default ai providers`() {
    val providers = AiProvider.values()
    assertEquals(3, providers.size)
    assertEquals("gemini", AiProvider.GEMINI.id)
  }

  @Test
  fun `verify room database insert and query`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AetherDatabase::class.java).build()
    val dao = db.visionRecordDao()

    val record = VisionRecord(
      identifiedTitle = "حاسوب محمول",
      fullArabicResponse = "أرى أمامك حاسوباً محمولاً حديثاً بلون فضي.",
      userPrompt = "ما هذا؟",
      providerName = "Gemini Flash",
      latencyMs = 320
    )

    val id = dao.insertRecord(record)
    assert(id > 0)

    val records = dao.getAllRecords().first()
    assertEquals(1, records.size)
    assertEquals("حاسوب محمول", records[0].identifiedTitle)

    dao.deleteRecordById(id)
    val emptyRecords = dao.getAllRecords().first()
    assertEquals(0, emptyRecords.size)

    db.close()
  }
}
