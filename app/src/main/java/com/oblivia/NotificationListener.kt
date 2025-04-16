package com.oblivia

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val text = extras.getString("android.text") ?: return
        // Handle storing/analyzing this notification if needed
    }
}
