package com.kylecorry.bell.infrastructure.alerts.volcano

import android.content.Context
import com.kylecorry.bell.domain.Alert
import com.kylecorry.bell.domain.AlertLevel
import com.kylecorry.bell.domain.AlertType
import com.kylecorry.bell.infrastructure.alerts.AlertSpecification
import com.kylecorry.bell.infrastructure.alerts.BaseAlertSource
import com.kylecorry.bell.infrastructure.parsers.selectors.Selector

class USGSVolcanoAlertSource(context: Context) : BaseAlertSource(context) {

    override fun getSpecification(): AlertSpecification {
        return json(
            "USGS Volcanoes",
            "https://volcanoes.usgs.gov/vsc/api/volcanoApi/elevated",
            items = "$",
            title = Selector.text("vName"),
            link = Selector.text("noticeUrl"),
            uniqueId = Selector.text("vnum"),
            publishedDate = Selector.text("alertDate") { it?.replace(" ", "T") + "Z" },
            summary = Selector.text("noticeSynopsis"),
            additionalAttributes = mapOf(
                "alertLevel" to Selector.text("alertLevel")
            ),
            defaultAlertType = AlertType.Volcano,
        )
    }

    override fun process(alerts: List<Alert>): List<Alert> {
        return alerts.mapNotNull {
            val level = when (it.additionalAttributes["alertLevel"]?.lowercase()) {
                "advisory" -> AlertLevel.Advisory
                "watch" -> AlertLevel.Watch
                "warning" -> AlertLevel.Warning
                else -> AlertLevel.Other
            }
            if (level == AlertLevel.Other) {
                return@mapNotNull null
            }
            it.copy(
                title = "Volcano ${level.name} for ${it.title}",
                level = level,
                useLinkForSummary = false
            )
        }
    }
}