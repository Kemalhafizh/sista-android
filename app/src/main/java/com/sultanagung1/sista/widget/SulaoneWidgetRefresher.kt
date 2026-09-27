package com.sultanagung1.sista.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.core.widget.WidgetRefresher
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Re-renders the data-backed widgets after [com.sultanagung1.sista.core.widget.WidgetSnapshotStore]
 * changes. The prayer widget has no data source yet, so it is not refreshed here.
 */
class SulaoneWidgetRefresher(context: Context) : WidgetRefresher {

    private val appContext = context.applicationContext

    override fun refresh() {
        val manager = AppWidgetManager.getInstance(appContext) ?: return
        ScheduleWidgetProvider.render(appContext, manager, idsOf(manager, ScheduleWidgetProvider::class.java))
        AttendanceWidgetProvider.render(appContext, manager, idsOf(manager, AttendanceWidgetProvider::class.java))
        SppWidgetProvider.render(appContext, manager, idsOf(manager, SppWidgetProvider::class.java))
    }

    private fun idsOf(manager: AppWidgetManager, provider: Class<*>): IntArray =
        manager.getAppWidgetIds(ComponentName(appContext, provider))
}

/** Same clock as the in-app screens (server-corrected when synced). */
internal fun widgetNow(): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(DateUtils.nowMillis()), ZoneId.systemDefault())

/** Sets [text] on [viewId], or hides the view when there is nothing true to say. */
internal fun RemoteViews.setOptionalText(context: Context, viewId: Int, text: WidgetText?) {
    if (text == null) {
        setViewVisibility(viewId, View.GONE)
    } else {
        setTextViewText(viewId, text.resolve(context))
        setViewVisibility(viewId, View.VISIBLE)
    }
}
