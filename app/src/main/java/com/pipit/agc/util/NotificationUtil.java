package com.pipit.agc.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.pipit.agc.R;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.model.Message;

/**
 * Created by Eric on 9/3/2016.
 */
public class NotificationUtil {


    /**
     * @param context
     * @param header Title (The first thing that is seen)
     * @param body First line of msg body
     * @param messageID
     * @param reason Specifies if this is shown for missing/hitting a gym
     */
    public static void showNotification(Context context, String header, String body, long messageID, int reason){
        Intent notificationIntent = new Intent(context, AllinOneActivity.class);
        if (messageID>0){
            notificationIntent = new Intent(context, AllinOneActivity.class);
            notificationIntent.putExtra(Constants.MESSAGE_ID, messageID);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AllinOneActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.notification_icon)
        .setTicker("Ticker Message")
        .setVibrate(new long[]{1000, 1000})
        .setAutoCancel(true)
        .setContentIntent(notificationPendingIntent);

        //RemoveView allows us to further customize notification with a layout
        RemoteViews rmv = getComplexNotificationView(context, R.layout.notification_layout);
        //Set the ~two lines of text
        rmv.setTextViewText(R.id.title, "Abusive Gym Reminder");
        rmv.setTextViewText(R.id.lineone, header);
        //rmv.setTextViewText(R.id.linetwo, body);
        builder.setContent(rmv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // build a complex notification, with buttons and such
            if (reason == Message.MISSED_YESTERDAY) builder.setColor(Color.RED);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification
        Notification notification = builder.build();

        //Set the expanded view (For some reason we can't do this in the builder
        RemoteViews bigrmv = getComplexNotificationView(context, R.layout.expanded_notification);

        bigrmv.setTextViewText(R.id.title, "Abusive Gym Reminder");
        bigrmv.setTextViewText(R.id.lineone, header);
        bigrmv.setTextViewText(R.id.linetwo, body);
        notification.bigContentView = bigrmv;

        // Issue the notification
        mNotificationManager.notify(0, notification);
    }



    private static RemoteViews getComplexNotificationView(Context context, int layout) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews notificationView = new RemoteViews(
                context.getPackageName(),
                layout
        );

        // Locate and set the Image into customnotificationtext.xml ImageViews
        notificationView.setImageViewResource(
                R.id.imagenotileft,
                R.drawable.common_ic_googleplayservices);

        return notificationView;
    }

}
