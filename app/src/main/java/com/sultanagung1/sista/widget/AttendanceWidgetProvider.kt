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

class AttendanceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        render(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        /** Renders from the cached [WidgetSnapshotStore] snapshot; no network call here. */
        fun render(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val content = WidgetTextMapper.attendance(WidgetSnapshotStore.read(context), widgetNow())

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_attendance)
                views.setTextViewText(R.id.widget_attendance_status, content.status.resolve(context))
                views.setOptionalText(context, R.id.widget_attendance_detail, content.detail)
                views.setOptionalText(context, R.id.widget_attendance_updated, content.updated)

                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("sulaone://attendance")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    2,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_attendance_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
