package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.OtpParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    assertEquals("SMS OTP Authenticator", appName)
  }

  @Test
  fun `parse real OTP message with code`() {
    val now = System.currentTimeMillis()
    val otp = OtpParser.parseOtp("Google", "G-492019 is your Google verification code.", now)
    assertNotNull(otp)
    assertEquals("G-492019", otp?.code)
    assertEquals("492019", otp?.code?.replace(Regex("[^0-9]"), ""))
    assertEquals("Google", otp?.serviceName)
  }

  @Test
  fun `parse standard numeric OTP message`() {
    val now = System.currentTimeMillis()
    val otp = OtpParser.parseOtp("Bank of America", "Your verification passcode is 839201. Valid for 5 minutes.", now)
    assertNotNull(otp)
    assertEquals("839201", otp?.code)
    assertEquals("Bank of America", otp?.serviceName)
  }

  @Test
  fun `parse non-OTP SMS returns null`() {
    val now = System.currentTimeMillis()
    val otp = OtpParser.parseOtp("Friend", "Hey, are we still meeting for lunch today?", now)
    assertNull(otp)
  }
}
