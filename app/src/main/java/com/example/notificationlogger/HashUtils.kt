package com.example.notificationlogger

import java.security.MessageDigest

/** SHA-256 of the string, returned as lowercase hex. */
fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
