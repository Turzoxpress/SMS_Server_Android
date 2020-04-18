package com.babylon.smsserver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class CloudMessagingService extends FirebaseMessagingService {
    private String TAG = "CloudMessagingService";

    private MainActivity mainActivity;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

      //  mainActivity.PushNotificationCame();
        // log the getting message from firebase
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //  if remote message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            String jobType = data.get("type");
     /* Check the message contains data If needs to be processed by long running job
         so check if data needs to be processed by long running job */

     /*
            if (jobType.equalsIgnoreCase(JobType.LONG.name())) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleLongRunningJob();
            } else {
                // Handle message within 10 seconds
                handleNow(data);
            }
            */

           // handleNow(data);

            Bundle bundle = new Bundle();
            bundle.putString("msgNumber", remoteMessage.getNotification().getTitle());
            bundle.putString("msgBody", remoteMessage.getNotification().getBody());

            Intent new_intent = new Intent();
            new_intent.setAction("ACTION_STRING_ACTIVITY");
            new_intent.putExtra("msg", bundle);

            sendBroadcast(new_intent);



            //Toast.makeText(GlobalApplication.getAppContext(),data.get("message"),Toast.LENGTH_LONG).show();

          //  handleNow(data);

        }
        // if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }
    /**
     * Persist token on third-party servers using your Retrofit APIs client.
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // make a own server request here using your http client
    }
    private void handleNow(Map<String, String> data) {
        if (data.containsKey("title") && data.containsKey("message")) {


            SaveUserData("test1","test2");
            sendNotification(data.get("title"), data.get("message"));




        }
    }
    /**
     * Schedule async work using WorkManager mostly these are one type job.
     */

    /**
     * Schedule async work using WorkManager mostly these are one type job.
     */
    private void scheduleLongRunningJob() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
    }

    /**
     * Create and show notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody) {




        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_phone)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel human readable title
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Cloud Messaging Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());




    }

    private void SaveUserData(String number, String msg){


        SharedPreferences pref = getApplicationContext().getSharedPreferences("UserInfo", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();



        editor.putString("userNumber", number);
        editor.putString("userMessage", msg);

        editor.commit();
        Log.e(TAG,"--------- Response Saved in Device ----------------- Number : "+number+"   Message : "+msg);

    }
}
