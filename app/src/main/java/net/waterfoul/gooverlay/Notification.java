package net.waterfoul.gooverlay;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

public class Notification {
    public static final int NOTIFICATION_ID = 1;
    private static android.app.Notification.Builder builder;
    private static NotificationManager notificationManager;
    private static android.app.Notification n;

    public static void makeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder = new android.app.Notification.Builder(context)
                .setContentTitle("Go Overlay")
                .setContentText("")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            n = builder.build();
        } else {
            n = builder.getNotification();
        }

        n.flags |= android.app.Notification.FLAG_NO_CLEAR | android.app.Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, n);
    }

    public static void removeNotification(Context context) {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static void setMessage(String message) {
        builder.setContentText(message);
        notificationManager.notify(
            NOTIFICATION_ID,
            builder.build()
        );
    }
}
