package com.lorenzosuarez.magiceightball.phone.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives the developer's messages while the app is open. With the app closed or in the
 * background, the system shows a message with a title and a text on its own.
 *
 * A message may carry its words as a notification or, for full control, as the data fields
 * `title` and `body`.
 */
class PushService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]
        Notifications.show(this, title, body)
    }
}
