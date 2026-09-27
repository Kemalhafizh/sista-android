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

class SppWidgetProvider : AppWidgetProvider() {

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
            val content = WidgetTextMapper.spp(WidgetSnapshotStore.read(context), widgetNow())

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_spp)
                views.setTextViewText(R.id.widget_spp_status, content.status.resolve(context))
                views.setOptionalText(context, R.id.widget_spp_detail, content.detail)
                views.setOptionalText(context, R.id.widget_spp_updated, content.updated)

                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("sulaone://billing")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    3,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_spp_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
