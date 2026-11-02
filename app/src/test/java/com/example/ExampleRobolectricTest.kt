package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.firebase.FirebaseConfig
import com.example.data.model.SouthAfricanCampus
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
  fun `read app_name string resource from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Campus Plug SA", appName)
  }

  @Test
  fun `verify South African campuses resolution`() {
    val uj = SouthAfricanCampus.fromCode("uj")
    assertEquals(SouthAfricanCampus.UJ, uj)
    assertEquals("University of Johannesburg", uj.institutionName)

    val uct = SouthAfricanCampus.fromCode("uct")
    assertEquals("Western Cape", uct.province)
  }

  @Test
  fun `evaluate Firebase readiness without unhandled crashes`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val report = FirebaseConfig.evaluateReadiness(context)
    assertNotNull(report)
  }
}
