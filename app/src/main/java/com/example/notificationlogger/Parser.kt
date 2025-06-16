package com.example.notificationlogger

import android.app.Notification
import android.service.notification.StatusBarNotification
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Parser {

    private const val WALLET_PACKAGE = "com.google.android.apps.walletnfcrel"
    private const val CHASE_PACKAGE  = "com.chase.mobile.android"  // adjust to your actual Chase package

    // Expanded‐style: optional prefix "CARD NAME: " then "You made a $... transaction with MERCHANT on DATE at TIME ET"
    private val fullTextRegex = Regex(
        """^(?:(.+?):\s*)?You made a \$(\d[\d,\.]*) transaction with (.+?) on ([A-Za-z]{3} \d{1,2}, \d{4}) at (\d{1,2}:\d{2} [AP]M) ET""",
        RegexOption.IGNORE_CASE
    )

    // Wallet summary‐style
    private val summaryRegex = Regex(
        """\$(\d[\d,\.]*) with (Visa|Mastercard|Amex) •+ (\d{4})""",
        RegexOption.IGNORE_CASE
    )

    fun parse(sbn: StatusBarNotification): Pair<WalletTxn?, OtherNotif?> {
        val extras = sbn.notification.extras
        val title  = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            ?.trim()
            .orEmpty()
        val text   = extras.getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            ?.trim()
            .orEmpty()
        val hash   = text.lowercase().sha256()

        // 1) Expanded full‐text (Wallet or Chase)
        fullTextRegex.find(text)?.destructured?.let { (maybeCard, amt, merchant, rawDate, rawTime) ->
            // if the regex saw a prefix, that’s the card; otherwise fallback to notification title
            val cardName = maybeCard?.takeIf { it.isNotBlank() } ?: title

            val date = LocalDate.parse(
                rawDate,
                DateTimeFormatter.ofPattern("MMM d, yyyy")
            )
            val time = LocalTime.parse(
                rawTime,
                DateTimeFormatter.ofPattern("h:mm a")
            ).truncatedTo(ChronoUnit.SECONDS)

            return WalletTxn(
                amount = "\$$amt",
                card   = cardName,
                date   = date.toString(),
                time   = time.toString() + " ET",
                where  = merchant,
                raw    = text,
                hash   = hash
            ) to null
        }

        // 2) Condensed Wallet summary (only for Wallet package)
        if (sbn.packageName == WALLET_PACKAGE) {
            summaryRegex.find(text)?.destructured?.let { (amt, cardType, last4) ->
                val zdt = Instant.ofEpochMilli(sbn.postTime)
                    .atZone(ZoneId.systemDefault())
                return WalletTxn(
                    amount = "\$$amt",
                    card   = "$cardType •••• $last4",
                    date   = zdt.toLocalDate().toString(),
                    time   = zdt.toLocalTime()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toString(),
                    where  = title,  // merchant name is the notification title
                    raw    = text,
                    hash   = hash
                ) to null
            }
        }

        // 3) fallback
        return null to OtherNotif(raw = text, hash = hash)
    }
}
