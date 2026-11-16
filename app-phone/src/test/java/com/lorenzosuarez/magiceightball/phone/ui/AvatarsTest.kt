package com.lorenzosuarez.magiceightball.phone.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/** The picture of an account: asked for at the size it is shown, and never over a connection that can be read. */
class AvatarsTest {

    @Test
    fun `a picture of Google's is asked for at the size it is shown, and any other address is left as it is`() {
        assertEquals("https://lh3.googleusercontent.com/a/abc=s144-c", Avatars.sized("https://lh3.googleusercontent.com/a/abc=s96-c", 144))
        assertEquals("https://lh3.googleusercontent.com/a/abc=s144-c", Avatars.sized("https://lh3.googleusercontent.com/a/abc=s96", 144))
        assertEquals("https://example.com/me.png", Avatars.sized("https://example.com/me.png", 144))
    }

    @Test
    fun `a picture that is not served securely is not fetched`() {
        assertNull(Avatars.load(File(System.getProperty("java.io.tmpdir")), "http://example.com/me.png", 96))
    }
}
