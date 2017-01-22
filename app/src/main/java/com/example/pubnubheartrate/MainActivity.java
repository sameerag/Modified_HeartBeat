package com.example.pubnubheartrate;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements OnClickListener {

    private ImageView mImgVwLaunchBeatMonitor;
    private int INDEX=0;
    private SharedPreferences mSharedPreferences;

    public static final int COLOR_RED   = 0;
    public static final int COLOR_BLUE  = 1;
    public static final int COLOR_GREEN = 2;

    private Pubnub mPubNub;
    public static final String PUBLISH_KEY = "pub-c-11ee32fc-9811-4de8-ba8f-594a79ecc709";
    public static final String SUBSCRIBE_KEY = "sub-c-d57009f0-dc46-11e6-a669-0619f8945a4f";
    public static final String CHANNEL = "phue";

    // set the default values
    public boolean blink = false;
    public String text = "Hello";
    public int duration = 20;
    public int rate = 70;

    private long lastUpdate = System.currentTimeMillis();
    private boolean pHueOn = false;
    private boolean sendPulse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences= MainActivity.this.getSharedPreferences("app_prefs", MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        initializeLayout();
        initPubNub();
    }

    public void onCheckboxClicked(View v) {
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();

        // Check which checkbox was clicked
        if(checked){
                blink = true;
        }
    }


    private void initializeLayout() {
        mImgVwLaunchBeatMonitor=(ImageView)findViewById(R.id.imgvwLaunchHeartRater);
        mImgVwLaunchBeatMonitor.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.imgvwLaunchHeartRater){
            INDEX=0;
            startActivity(new Intent(MainActivity.this,HeartRateMonitor.class));
        }

        // display params
        String sBlink = String.valueOf(blink);
        Log.d("BLINK"," : " + sBlink);

        String sRate = String.valueOf(rate);
        Log.d("RATE"," : " + sRate);
    }

    public void initPubNub(){
        this.mPubNub = new Pubnub(
                PUBLISH_KEY,
                SUBSCRIBE_KEY
        );
        this.mPubNub.setUUID("MoodColor");

        subscribe();
    }

    public void publish(int red, int green, int blue){
        JSONObject js = new JSONObject();
        try {

            // color params
            js.put("RED",   red);
            js.put("GREEN", green);
            js.put("BLUE",  blue);

            // display params
            String sBlink = String.valueOf(blink);
            Log.d("BLINK"," : " + sBlink);

            js.put("BLINK", blink);
            js.put("TEXT", text);

            String sRate = String.valueOf(rate);
            Log.d("RATE"," : " + sRate);
            js.put("RATE", this.rate);


            js.put("TIME", duration);

        } catch (JSONException e) { e.printStackTrace(); }

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.d("PUBNUB",response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                Log.d("PUBNUB",error.toString());
            }
        };
        this.mPubNub.publish(CHANNEL, js, callback);
    }

    public void subscribe(){
        try {
            this.mPubNub.subscribe(CHANNEL, new Callback() {
                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d("PUBNUB","SUBSCRIBE : CONNECT on channel:" + channel
                            + " : " + message.getClass() + " : "
                            + message.toString());
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.d("PUBNUB","SUBSCRIBE : DISCONNECT on channel:" + channel
                            + " : " + message.getClass() + " : "
                            + message.toString());
                }

                public void reconnectCallback(String channel, Object message) {
                    Log.d("PUBNUB","SUBSCRIBE : RECONNECT on channel:" + channel
                            + " : " + message.getClass() + " : "
                            + message.toString());
                }

                @Override
                public void successCallback(String channel, Object message) {
                    Log.d("PUBNUB","SUBSCRIBE : " + channel + " : "
                            + message.getClass() + " : " + message.toString());
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("PUBNUB","SUBSCRIBE : ERROR on channel " + channel
                            + " : " + error.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void missOn(View view){
        publish(0, 100, 0);
        EditText mBlinkRate  = (EditText)findViewById(R.id.blinkrate);
        this.rate = Integer.parseInt(mBlinkRate.getText().toString());

    }

    public void loveOn(View view){
        publish(100, 0, 0);
        EditText mBlinkRate  = (EditText)findViewById(R.id.blinkrate);
        this.rate = Integer.parseInt(mBlinkRate.getText().toString());
    }

    public void madOn(View view){
        publish(0, 0, 100);
        EditText mBlinkRate  = (EditText)findViewById(R.id.blinkrate);
        this.rate = Integer.parseInt(mBlinkRate.getText().toString());
    }

}
