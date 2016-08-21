package com.pipit.agc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.pipit.agc.model.Message;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.ReminderOracle;
import com.pipit.agc.util.SharedPrefUtil;

/**
 * This receiver catches the "android.intent.action.USER_PRESENT" intent, which is triggered when
 * the phone starts being used.
 *
 * We use this to show the user the missed gym notification. When ReminderOracle decides to leave a
 * message, it will set an alarmmanager. Previous implementations showed a notification when the
 * alarm manager woke. Now we can set a flag instead and check it here so that the user receives the
 * message while opening the phone.
 *
 * Created by Eric on 8/20/2016.
 */
public class UserPresentReceiver extends BroadcastReceiver{
    private static final String TAG = "UserPresentReceiver";
    @Override
    public void onReceive(final Context context, Intent intent) {
        int flag = SharedPrefUtil.getInt(context, Constants.FLAG_WAKEUP_SHOW_NOTIF, 0);
        if (flag==1){
            Log.d(TAG, "FLAG_WAKEUP_SHOW_NOTIF is true - showing notification");
            String mjson = SharedPrefUtil.getString(context, Constants.CONTENT_WAKEUP_SHOW_NOTIF, null);
            final Message m;
            if (mjson!=null && !mjson.isEmpty()){
                try{
                    m = Message.fromJson(mjson);
                } catch (Exception e){
                    Log.e(TAG, "Error while attempting to show wakeup message " + e.toString());
                    SharedPrefUtil.putInt(context, Constants.FLAG_WAKEUP_SHOW_NOTIF, 0);
                    return;
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        ReminderOracle.showNotificationFromMessage(context, m);
                        SharedPrefUtil.putInt(context, Constants.FLAG_WAKEUP_SHOW_NOTIF, 0);
                        SharedPrefUtil.putString(context, Constants.CONTENT_WAKEUP_SHOW_NOTIF, "");
                    }
                }, 2000);
            }
        }else{
            Log.d(TAG, "Wakeup detected, no notifications queued");
        }
    }
}
