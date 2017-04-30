package com.pipit.agc.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.pipit.agc.R;
import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;

/**
 * Created by Eric on 9/3/2016.
 */
public class NotificationUtil {

    private static final String TAG = "NotificationUtil";

    /**
     * @param context
     * @param header Title (The first thing that is seen)
     * @param body First line of msg body
     * @param reason Specifies if this is shown for missing/hitting a gym
     * @param reason_line Optional bottom left corner, offers explanation of message ("Tuesday, July 4th was a gym day")
     * @param attr_line Optional bottom right corner, offers credit to author of message
     * @param id ID of message, if applicable
     */
    public static void showNotification(Context context, String header, String body, String reason_line, String attr_line, int reason, long id){
        int expanded_layoutid = R.layout.expanded_notification;
        int notif_layoutid = R.layout.notification_layout;

        Intent notificationIntent = new Intent(context, AllinOneActivity.class);
        notificationIntent.putExtra(Constants.MESSAGE_ID, id);

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

        //Some customization depending on reason for notif
        if (reason == Message.MISSED_YESTERDAY){
            Log.d(TAG, "Showing missed yesterday notification");

            //The following commented lines would allow us to show a different color for Missed days.
            //Currently not being used as a design decision.
            //expanded_layoutid = R.layout.expanded_notification_red;
            //notif_layoutid = R.layout.notification_layout_red;

            /*
            builder.setSmallIcon(R.drawable.delete_icon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(ContextCompat.getColor(context, R.color.schemethree_darkerred));
            } */
        }else{
            Log.d(TAG, "Showing notification for reason number " + reason);
            /*
            builder.setSmallIcon(R.drawable.check_icon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(ContextCompat.getColor(context, R.color.schemethree_darkerteal));
            }*/
        }

        //RemoveView allows us to further customize notification with a layout
        RemoteViews rmv = getComplexNotificationView(context, notif_layoutid);
        //Set the ~two lines of text
        rmv.setTextViewText(R.id.title, "Abusive Gym Reminder");
        rmv.setTextViewText(R.id.lineone, header);
        builder.setContent(rmv);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification
        Notification notification = builder.build();

        //Set the expanded view (For some reason we can't do this in the builder
        RemoteViews bigrmv = getComplexNotificationView(context, expanded_layoutid);
        bigrmv.setTextViewText(R.id.title, "Abusive Gym Reminder");
        bigrmv.setTextViewText(R.id.lineone, header);
        bigrmv.setTextViewText(R.id.linetwo, body);
        bigrmv.setTextViewText(R.id.reason_line, reason_line);
        bigrmv.setTextViewText(R.id.attribution, attr_line);
        notification.bigContentView = bigrmv;

        //Vibrate
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        // Issue the notification
        mNotificationManager.notify(0, notification);
    }

    /**
     * Dedicated method for showing a "visiting gym" notification
     * @param context
     * @param header
     * @param body
     */
    public static void showGymVisitingNotification(Context context, String header, String body, boolean vibrate){
        //Since this notification is "onGoing", it is important that the user is actually at the gym
        //when it is shown. Otherwise, we may be unable to remove the notification.
        DayRecord today = StatsContent.getInstance().getToday(true);
        if (!today.isCurrentlyVisiting()){
            Log.d(TAG, "Not showing notification because gym visit is not detected");
            return;
        }

        Intent notificationIntent = new Intent(context, AllinOneActivity.class);
        notificationIntent.putExtra(Constants.SHOW_STATS_FLAG, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AllinOneActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_icon))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(header)
                .setOngoing(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setContentText(body)
                .setContentIntent(notificationPendingIntent);
        if (vibrate){
            builder.setVibrate(new long[]{1000, 1000});
        }
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification
        Notification notification = builder.build();
        if (vibrate){
            //Vibrate
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        // Issue the notification
        mNotificationManager.notify(0, notification);
        Log.d("Yuncun", "Notification should be shown");
    }

    public static void showGymVisitingNotification(Context context, String header, String body){
        showGymVisitingNotification(context, header, body, true);
    }

    public static void endNotifications(Context context){
        Log.d(TAG, "Ending all AbusiveGymReminder notifications");
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    /**
     * A helper funciton for retrieving the remoteviews from the notification layout
     * @param context
     * @param layout
     * @return
     */
    private static RemoteViews getComplexNotificationView(Context context, int layout) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews notificationView = new RemoteViews(
                context.getPackageName(),
                layout
        );

        // Locate and set the Image into customnotificationtext.xml ImageViews
        notificationView.setImageViewResource(
                R.id.imagenotileft,
                R.drawable.app_icon);

        return notificationView;
    }
}
