package com.bluetooth.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity{

    private BluetoothAdapter bTAdapter;
    BroadcastReceiver mReceiver;
    private BluetoothSocket socket = null;
    private ArrayList<String> deviceNames = new ArrayList<>();
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Intent mainIntent;

    private ListView listView;
    private ArrayAdapter adapter;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainIntent = getIntent();

        adapter = new ArrayAdapter<String>(this, R.layout.adapter_list_view, R.id.adapter_text_view, deviceNames);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        /*if (!Settings.System.canWrite(getApplicationContext())){
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivityForResult(intent, 2);
        }else{
            adjustLight();
        }*/

        bTAdapter = BluetoothAdapter.getDefaultAdapter();


        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 1);
        Log.d("Bluetooth enabled!", "Bluetooth enabled!");

        Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // A Bluetooth device was found
                    // Getting device information from the intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    int signal = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    //adjustLight();

                    if (device != null) {
                        Log.d("Bluetooth Device ", deviceName + "___" + device.getAddress() + "___" + signal);
                        if (!deviceNames.contains(deviceName + "___" + device.getAddress())) {
                            //deviceNames.add(deviceName + "___" + device.getAddress());
                            String last = device.getAddress().substring(9, device.getAddress().length()).replace(":", "");
                            deviceNames.add(last);
                            adapter.notifyDataSetChanged();
                        }

                        if(deviceName != null && deviceName.contains("Socket")){
                            //adjustLight();
                            try {
                                socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
                                bTAdapter.cancelDiscovery();
                                socket.connect();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);



        final Button btnRefresh = (Button)findViewById(R.id.button_id);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deviceNames.clear();

                devices.clear();

                bTAdapter.cancelDiscovery();

                bTAdapter.startDiscovery();

            }
        });


    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onPause(){
        super.onPause();
        //adjustLight();
    }

    @Override
    public void onResume(){
        super.onResume();

        //adjustLight();

        /*deviceNames.clear();

        bTAdapter.cancelDiscovery();

        bTAdapter.startDiscovery();*/

        /*if (socket != null && socket.isConnected())
            try {
                socket.close();
            } catch (IOException e) {

            }*/

    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2){
            Log.d("TAG", "CODE_WRITE_SETTINGS_PERMISSION success");
            finish();
            startActivity(mainIntent);
        }

    }

    private void adjustLight() {
        if (socket != null && socket.getRemoteDevice().getName().contains("Socket")){
            if (Settings.System.canWrite(getApplicationContext())) {
                Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 127);
            }
        }
        else{
            Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
            }

    }

}
