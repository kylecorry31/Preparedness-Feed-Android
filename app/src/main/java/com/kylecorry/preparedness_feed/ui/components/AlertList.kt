package com.kylecorry.preparedness_feed.ui.components

import android.graphics.Color
import com.kylecorry.andromeda.views.list.ListItem
import com.kylecorry.andromeda.views.list.ListItemTag
import com.kylecorry.andromeda.views.reactivity.AndromedaViews.AndromedaList
import com.kylecorry.andromeda.views.reactivity.AndromedaViews.Component
import com.kylecorry.andromeda.views.reactivity.ViewAttributes
import com.kylecorry.preparedness_feed.domain.Alert
import com.kylecorry.preparedness_feed.ui.FormatService

class AlertListAttributes : ViewAttributes() {
    var alerts: List<Alert> = emptyList()
    var onDelete: ((Alert) -> Unit)? = null
    var onOpen: ((Alert) -> Unit)? = null
}

fun AlertList(config: AlertListAttributes.() -> Unit) = Component(config) { attrs ->
    val context = useAndroidContext()
    val formatter = useMemo(context) {
        FormatService.getInstance(context)
    }

    val listItems = useMemo(attrs.alerts, attrs.onDelete, attrs.onOpen, formatter) {
        attrs.alerts.map {
            ListItem(
                it.id,
                it.title,
                formatter.formatDateTime(it.publishedDate) + "\n\n" + it.summary,
                tags = listOf(
                    ListItemTag(it.source, null, Color.GREEN),
                    ListItemTag(it.type, null, Color.BLUE),
                ),
                longClickAction = {
                    attrs.onDelete?.invoke(it)
                }
            ) {
                attrs.onOpen?.invoke(it)
            }
        }
    }

    AndromedaList {
        items = listItems
    }
}