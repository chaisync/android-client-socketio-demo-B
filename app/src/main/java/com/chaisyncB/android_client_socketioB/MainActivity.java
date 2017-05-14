package com.chaisyncB.android_client_socketioB;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.provider.Settings.Secure;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Socket sock;
    private TextView textViewFromServer;
    private TextView deviceID_display;
    private TextView dateTime_display;
    private TextView database_display;
    private Button sendButton;
    private EditText name_display;
    private EditText reminderTime;
    private String android_id;
    private String timeStamp;
    //SharedPreferences sharedPref = this.getSharedPreferences("com.Chaisync.sharedPref", Context.MODE_PRIVATE);
    SharedPreferences sharedPref;
    Context sync_context;

    public MainActivity(){
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       //input fields and info set up and populate
        try {
            sync_context = createPackageContext("com.chaisync.android_client_socketio", 0);
            sharedPref = sync_context.getSharedPreferences("com.Chaisync.sharedPref", Context.CONTEXT_IGNORE_SECURITY | Context.MODE_MULTI_PROCESS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("CSdebug", "NameNotFoundException");
            Toast.makeText(this, "database sync error!!", Toast.LENGTH_LONG).show();
        }

        deviceID_display = (TextView) findViewById(R.id.deviceId_textview);
        dateTime_display = (TextView) findViewById(R.id.dateText_textview);
        name_display = (EditText) findViewById(R.id.nameInputText);
        reminderTime = (EditText) findViewById(R.id.reminderTimeText);
        textViewFromServer = (TextView) findViewById(R.id.ServerResponseTV);
        database_display = (TextView) findViewById(R.id.databaseView);
        android_id = Secure.getString(this.getContentResolver(),
                Secure.ANDROID_ID);

        Long tsLong = System.currentTimeMillis();
        timeStamp = tsLong.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        Date resultdate = new Date(tsLong);
        //System.out.println(sdf.format(resultdate));

        deviceID_display.setText("Device ID: " + android_id);
        dateTime_display.setText("Date/Time: " + sdf.format(resultdate));
        name_display.setText("Username Here");
        reminderTime.setText("December 25th 8:00AM");
        //readFromLocalDb();

        name_display.setSelectAllOnFocus(true);
        reminderTime.setSelectAllOnFocus(true);
        name_display.requestFocus();

        // Listen for button click to fire message from client to server
        sendButton = (Button) findViewById(R.id.buttonSendToServer);
        sendButton.setOnClickListener(new View.OnClickListener(){
          public void onClick(View v){
              try {
                  attemptSend();
              } catch (JSONException e){
                  throw new RuntimeException();
              }

              // Loopback demo
              //textViewFromServer.setText(editTextFromClient.getText());
          }
        });

        //refresh button function
        Button refresher = (Button) findViewById(R.id.buttonRefresh);
        refresher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sync_context = createPackageContext("com.chaisync.android_client_socketio", 0);
                    sharedPref = sync_context.getSharedPreferences("com.Chaisync.sharedPref", Context.CONTEXT_IGNORE_SECURITY | Context.MODE_MULTI_PROCESS);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("CSdebug", "NameNotFoundException");
                    //Toast.makeText(this, "database sync error!!", Toast.LENGTH_LONG).show();
                }

                String my_deviceID = sharedPref.getString("deviceID", "");
                String my_user = sharedPref.getString("user", "");
                String my_reminder = sharedPref.getString("reminder", "");
                String my_timestamp = sharedPref.getString("timestamp", "");
                database_display.setText("Local Database: \n" + my_deviceID + " "
                        + my_user + " " + my_reminder + " " + my_timestamp);
            }
        });

        // Start socket.io
        ChaisyncApplication app = (ChaisyncApplication) getApplication();
        sock = app.getSocket();

        // Configure socket.io events
        sock.on(Socket.EVENT_CONNECT, onConnect);
        sock.on(Socket.EVENT_DISCONNECT, onDisconnect);
        sock.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        sock.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeout);
        sock.on("new message", onNewMessage);

        // Connect to server
        sock.connect();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    //String firstname;
                    //String lastname;
                    String deviceID;
                    String user;
                    String reminder;
                    String timestamp;

                    try {
                        //firstname = data.getString("firstname");
                        //lastname = data.getString("lastname");

        git                 deviceID = data.getString("deviceID");
                        user = data.getString("user");
                        reminder = data.getString("reminderTime");
                        timestamp = data.getString("timestamp");

                    } catch (JSONException e) {
                        Log.d("CSdebug", "JSON.getString error: " + data);
                        return;
                    }
                    textViewFromServer.setText(deviceID+" "+user+" "+reminder+" "+timestamp);
                    //saveToLocalDb(firstname+","+lastname);
                    saveDeviceIdToLocalDb(android_id);
                    saveUserToLocalDb(user);
                    saveReminderToLocalDb(reminder);
                    saveTimeToLocalDb(timestamp);
                }
            });
        }
    };

    private void readFromLocalDb(){
        String my_deviceID = sharedPref.getString("deviceID", "");
        String my_user = sharedPref.getString("user", "");
        String my_reminder = sharedPref.getString("reminder", "");
        String my_timestamp = sharedPref.getString("timestamp", "");

        String readTotal = my_deviceID + " " + my_user + " " + my_reminder + " " + my_timestamp;
        Log.d("CSdebug","readFromLocalDb() " + readTotal);
        Toast.makeText(this, "readFromLocalDb() " + readTotal, Toast.LENGTH_LONG).show();
        //deviceID_display.setText("Database stuff: " + readTotal);
    }

    /*
    private void saveToLocalDb(String data) {
        //SharedPreferences sharedPref = this.getSharedPreferences("com.Chaisync.sharedPref", Context.MODE_PRIVATE);
        //if (sharedPref.getString("my_data", "").length() == 0) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("my_data", data);
        editor.commit();
        //}
        Log.d("CSdebug","saveToLocalDb() " + data.toString());
        Toast.makeText(this, "saveToLocalDb() " + data, Toast.LENGTH_LONG).show();
    }
    */

    private void saveDeviceIdToLocalDb(String deviceID) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("deviceID", deviceID);
        editor.apply();
    }

    private void saveUserToLocalDb(String user) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user", user);
        editor.apply();
    }

    private void saveReminderToLocalDb(String reminder) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("reminder", reminder);
        editor.apply();
    }

    private void saveTimeToLocalDb(String timestamp) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("timestamp", timestamp);
        editor.apply();
    }

    private void attemptSend() throws JSONException{
        // Put the client message into a JSON object
        String username = name_display.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            name_display.requestFocus();
            return;
        }
        //name_display.setText("");

        JSONObject data = new JSONObject();
        //data.put("firstname", message);
        //data.put("lastname", "client");

        data.put("deviceID", android_id);
        data.put("user", username);
        String reminderTimeText = reminderTime.getText().toString().trim();
        data.put("reminderTime",reminderTimeText);
        data.put("timestamp",timeStamp);

        Log.d("CSdebug","attempt send: " + data.toString());
        Toast.makeText(this, "attemptLogin() " + data, Toast.LENGTH_LONG).show();
        // Attempt to send the message
        sock.emit("send message", data);
        //saveToLocalDb(android_id+","+username+","+reminderTimeText+","+timeStamp);
        saveDeviceIdToLocalDb(android_id);
        saveUserToLocalDb(username);
        saveReminderToLocalDb(reminderTimeText);
        saveTimeToLocalDb(timeStamp);

    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Event");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Disconnect Event");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Error Event");
        }
    };

    private Emitter.Listener onConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("CSdebug","Connection Timeout Event");
        }
    };
}