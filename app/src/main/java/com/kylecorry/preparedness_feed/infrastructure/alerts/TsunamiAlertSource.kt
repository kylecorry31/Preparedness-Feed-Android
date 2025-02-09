package com.kylecorry.preparedness_feed.infrastructure.alerts

import android.content.Context
import com.kylecorry.preparedness_feed.domain.Alert
import com.kylecorry.preparedness_feed.domain.AlertLevel
import com.kylecorry.preparedness_feed.domain.AlertType

abstract class TsunamiAlertSource(context: Context) :
    AtomAlertSource(context, linkSelector = "link[title=Bulletin][href]") {

    private val header = "TSUNAMI WARNING CENTER"

    private val advisory = "TSUNAMI ADVISORY"
    private val watch = "TSUNAMI WATCH"
    private val warning = "TSUNAMI WARNING"
    private val threat = "TSUNAMI THREAT MESSAGE"

    private val locationMap = mapOf(
        // Only using the non segmented alerts: https://tsunami.gov/?page=product_list
        "WEAK51" to "Alaska, British Colombia, U.S. West Coast",
        "WEHW40" to "Hawaii",
        "WEZS40" to "American Samoa",
        "WEGM40" to "Guam, CNMI",
        "WEXX30" to "U.S. Atlantic, Gulf of Mexico, Canada",
        "WECA40" to "Puerto Rico, Virgin Islands",
    )

    private val cancellationMessages = listOf(
        "IS CANCELLED",
        "IS NOW CANCELLED",
        "TSUNAMI THREAT HAS NOW LARGELY PASSED",
        "NO LONGER A TSUNAMI THREAT",
        "FINAL TSUNAMI THREAT MESSAGE"
    )

    override fun getSystemName(): String {
        return "NOAA Tsunami"
    }

    override fun isActiveOnly(): Boolean {
        return true
    }

    override fun postProcessAlerts(alerts: List<Alert>): List<Alert> {
        return alerts.map {
            it.copy(
                title = "Tsunami ${it.title}",
                type = AlertType.Water,
                level = AlertLevel.Other
            )
        }.filter { alert ->
            locationMap.any { alert.link.contains(it.key) }
        }
    }

    override fun updateFromFullText(alert: Alert, fullText: String): Alert {
        val headerIndex = fullText.uppercase().indexOf(header) + header.length
        val body = fullText.substring(headerIndex).trim()

        val level = when {
            cancellationMessages.any { body.contains(it) } -> null
            body.contains(threat) -> AlertLevel.Warning
            body.contains(warning) -> AlertLevel.Warning
            body.contains(watch) -> AlertLevel.Watch
            body.contains(advisory) -> AlertLevel.Advisory
            else -> null
        }

        val location = locationMap.entries.find { alert.link.contains(it.key) }?.value ?: ""

        if (level == null) {
            return alert.copy(expirationDate = alert.publishedDate)
        }

        return alert.copy(level = level, title = "Tsunami $level for $location")
    }
}