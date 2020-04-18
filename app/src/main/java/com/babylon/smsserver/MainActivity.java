package com.babylon.smsserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MessageListener {



    private String TAG = "MainActivity";

    private JobScheduler jobScheduler;
    private ComponentName componentName;
    private JobInfo jobInfo;

    private Timer timer;

    public static boolean sendingSMSFlag = false;

    private LinearLayout panel1;
    private LinearLayout panel2;
    private LinearLayout panel3;

    private TextView statusTxt;

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           // TextView textview = (TextView) findViewById(R.id.textview);
            Bundle bundle = intent.getBundleExtra("msg");
           // textview.setText(bundle.getString("msgBody"));
           // SaveUserData(bundle.getString("msgNumber"),bundle.getString("msgBody"));

            Log.e(TAG,"------------- Received Server Command ----------------");





            panel2.setVisibility(View.VISIBLE);
            statusTxt.setText("");
            statusTxt.setText("Received Master Server command to send SMS...");

            sendSMS(bundle.getString("msgNumber"),bundle.getString("msgBody"));

            statusTxt.setText(statusTxt.getText()+"\n"+"Preparing for sending SMS to "+bundle.getString("msgNumber"));

            MediaPlayer mediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.alert);
            mediaPlayer.start();

           // ShowStatusOneByOne(1);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        panel1 = (LinearLayout)findViewById(R.id.titleBar);
        panel2 = (LinearLayout)findViewById(R.id.msgBar);
        panel3 = (LinearLayout)findViewById(R.id.laodingBar);

        statusTxt = (TextView)findViewById(R.id.textView3);

        panel2.setVisibility(View.GONE);

      //  PrintFCMId();





       // sendSMS();

        if (activityReceiver != null) {
            IntentFilter intentFilter = new  IntentFilter("ACTION_STRING_ACTIVITY");
            registerReceiver(activityReceiver, intentFilter);
        }



      MessageReceiver.bindListener(this);


    }

    public void sendSMS(String number, String msg){

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(msg);

        String phoneNo = "";
       if(number.length() == 11){

           phoneNo = "+88"+number;
       }else{
           phoneNo = number;
       }


        sms.sendMultipartTextMessage(phoneNo, null, parts, null, null);
        Log.e(TAG,"------------- SMS sent to  "+number +"------------------");

        statusTxt.setText(statusTxt.getText()+"\n"+"SMS sent successfully to "+number);

        statusTxt.setText(statusTxt.getText()+"\n"+"SMS server is entering idle mode...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                panel2.setVisibility(View.GONE);
            }
        }, 10000);
    }

    @Override
    public void messageReceived(String number, String message) {
        Toast.makeText(this, "New Message Received: " + message, Toast.LENGTH_LONG).show();
        Log.e("MainActivity","New Message Received: " + number);

        MediaPlayer mediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.alert);
        mediaPlayer.start();

        panel2.setVisibility(View.VISIBLE);
        statusTxt.setText("");
        statusTxt.setText("Received Response SMS from "+number);
        statusTxt.setText(statusTxt.getText()+"\n"+"Processing the Response...");

        if(message.equalsIgnoreCase("1")){

            statusTxt.setText(statusTxt.getText()+"\n"+"User confirmed his/her donation");
            statusTxt.setText(statusTxt.getText()+"\n"+"Passing user response to Master Server...");
            statusTxt.setText(statusTxt.getText()+"\n"+"Notifying the user about his response by SMS...");
            sendSMS(number,"আপনাকে অনেক ধন্যবাদ নিশ্চিত করার জন্য।");

        }else if(message.equalsIgnoreCase("0")){

            statusTxt.setText(statusTxt.getText()+"\n"+"User declined his/her donation");
            statusTxt.setText(statusTxt.getText()+"\n"+"Passing user response to Master Server...");
            statusTxt.setText(statusTxt.getText()+"\n"+"Notifying the user about his response by SMS...");
            sendSMS(number,"আমরা খুবই দুঃখিত যে আপনি ত্রাণ পাননি। আমরা বিষয়টি খতিয়ে দেখছি।");

        }else{

            statusTxt.setText(statusTxt.getText()+"\n"+"User response is invalid.");
            statusTxt.setText(statusTxt.getText()+"\n"+"Notifying the user about his response by SMS...");
            sendSMS(number,"আপনি ভুল রিপ্লাই করেছেন। ত্রাণ পেয়ে থাকলে '1' আর না পেয়ে থাকলে '0' লিখে রিপ্লাই দিন।");
            //statusTxt.setText(statusTxt.getText()+"\n"+"Passing user response to Master Server...");

        }

        statusTxt.setText(statusTxt.getText()+"\n"+"SMS server notified the user successfully!");

        statusTxt.setText(statusTxt.getText()+"\n"+"SMS server is entering idle mode...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                panel2.setVisibility(View.GONE);
            }
        }, 10000);



    }


    public void PrintFCMId(){

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {
                    @Override public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e(TAG, token);
                        //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }



    public void ShowStatusOneByOne(final int flagShow){

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {




                        if (flagShow == 1){

                            //-- SMS Receive works



                        }else{


                        }




                    }
                });
            }
        }, 100, 2000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();


        timer.cancel();


        finish();

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
