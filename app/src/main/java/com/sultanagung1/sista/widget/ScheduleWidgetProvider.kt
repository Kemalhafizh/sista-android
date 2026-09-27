package com.sultanagung1.sista.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.sultanagung1.sista.MainActivity
import com.sultanagung1.sista.R
import com.sultanagung1.sista.core.widget.WidgetSnapshotStore

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        render(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private val LINE_IDS = intArrayOf(
            R.id.widget_schedule_item1,
            R.id.widget_schedule_item2,
            R.id.widget_schedule_item3
        )

        /**
         * Renders from the cached [WidgetSnapshotStore] snapshot; no network
         * call here. The periodic onUpdate re-filters the cached week to the
         * current day, so the widget follows the date without a new fetch.
         */
        fun render(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val content = WidgetTextMapper.schedule(WidgetSnapshotStore.read(context), widgetNow())

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_schedule)
                views.setTextViewText(R.id.widget_schedule_count, content.count.resolve(context))
                LINE_IDS.forEachIndexed { index, viewId ->
                    views.setOptionalText(context, viewId, content.lines.getOrNull(index))
                }

                // Intent on click -> Open schedule deep link
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("sulaone://schedule")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_schedule_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
