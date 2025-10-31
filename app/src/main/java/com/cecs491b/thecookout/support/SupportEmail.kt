package com.cecs491b.thecookout.support

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.cecs491b.thecookout.BuildConfig

/**
 * Opens the user's email app with a prefilled support email.
 * Safe to call from any screen (Activity or Composable via LocalContext).
 */
fun openSupportEmail(
    context: Context,
    subjectExtra: String = "",
    bodyExtra: String = ""
) {
    val subject = buildString {
        append(SupportConfig.SUPPORT_SUBJECT_PREFIX)
        if (subjectExtra.isNotBlank()) append(" ").append(subjectExtra)
    }

    val deviceInfo = """
        ---
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
        Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
        App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        UID: (if logged in, include UID in body)
        ---
    """.trimIndent()

    val body = if (bodyExtra.isNotBlank()) "$bodyExtra\n\n$deviceInfo" else deviceInfo

    // Prefer ACTION_SENDTO with mailto: so only email apps show up
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:") // ensures only email apps handle this
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportConfig.SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found on this device.", Toast.LENGTH_LONG).show()
    }
}