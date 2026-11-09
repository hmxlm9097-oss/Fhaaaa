package com.example

import com.example.data.remote.NetworkClientFactory
import com.example.data.system.TotpAuthenticator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreSecurityAndNetworkUnitTest {

    @Test
    fun `totp generates 6 digit code and verifies within active window`() {
        val secret = TotpAuthenticator.generateSecretBase32()
        assertTrue(secret.length >= 16)
        val now = 1_700_000_000_000L
        val code = TotpAuthenticator.currentCode(secret, now)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
        assertTrue(TotpAuthenticator.verifyCode(secret, code, now))
        assertFalse(TotpAuthenticator.verifyCode(secret, "000000", now))
    }

    @Test
    fun `totp uri includes issuer and secret`() {
        val uri = TotpAuthenticator.buildOtpAuthUri("admin@h4xml.cloud", "JBSWY3DPEHPK3PXP")
        assertTrue(uri.startsWith("otpauth://totp/H4XML Cloud:admin@h4xml.cloud"))
        assertTrue(uri.contains("secret=JBSWY3DPEHPK3PXP"))
    }

    @Test
    fun `network url normalization enforces http or https and trailing slash`() {
        assertEquals("https://api.h4xml.cloud/", NetworkClientFactory.normalizeBaseUrl("https://api.h4xml.cloud"))
        assertEquals("http://10.0.2.2:4000/", NetworkClientFactory.normalizeBaseUrl(" http://10.0.2.2:4000/ "))
        assertNull(NetworkClientFactory.normalizeBaseUrl("ftp://invalid.host"))
        assertNull(NetworkClientFactory.normalizeBaseUrl(""))
    }
}
