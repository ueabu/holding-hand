package me.cchiang.holdinghand;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View.OnTouchListener;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    CountDownTimer cTimer = null;
    private boolean isTouch = false;
    private TextView txt1;

    private ArrayList<Contact> conList;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Uri uriContact;
    private String contactID;     // contacts unique ID



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Gets contacts
        txt1 = (TextView)findViewById(R.id.txt1);
        conList = new ArrayList<>();
        requestPerms();
    

        findViewById(R.id.screen).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    Toast.makeText(MainActivity.this, "Aware mode on", Toast.LENGTH_SHORT).show();
                    isTouch = true;
                    cancelTimer();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //Toast.makeText(this, "RELEASED "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                    startTimer();
                    showDialog();

                }
                return true;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int X = (int) event.getX();
        int Y = (int) event.getY();
        int eventaction = event.getAction();


        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
                Toast.makeText(this, "Aware mode on", Toast.LENGTH_SHORT).show();
                isTouch = true;
                cancelTimer();
                break;

            case MotionEvent.ACTION_UP:
                //Toast.makeText(this, "RELEASED "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                startTimer();
                showDialog();
                break;
        }
        return true;
    }

    public void showDialog(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.searchprompt, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.user_input);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setTitle("Enter Your 4 Number Pin:")
                .setNegativeButton("Enter",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                String user_text = (userInput.getText()).toString();

                                /** CHECK FOR USER'S INPUT **/
                                if (user_text.equals("1234"))
                                {
                                    //Log.d(user_text, "HELLO THIS IS THE MESSAGE CAUGHT :)");
                                    Toast.makeText(getApplicationContext(), "Emergency canceled", Toast.LENGTH_SHORT).show();
                                    //Search_Tips(user_text);
                                    cancelTimer();

                                }
                                else{
                                    //Log.d(user_text,"string is empty");
                                    Toast.makeText(getApplicationContext(), "Empty String", Toast.LENGTH_SHORT).show();
                                    String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
                                    //startTimer();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Error");
                                    builder.setMessage(message);
                                    builder.setPositiveButton("Cancel", null);
                                    builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            showDialog();

                                        }
                                    });
                                    builder.create().show();

                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                                //startTimer();
                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }


    //start timer function
    void startTimer() {
        cTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished;
                final Toast cntdwn_msg = Toast.makeText(getApplicationContext(), "Touch released, starting countdown: "+seconds/1000, Toast.LENGTH_SHORT);
                cntdwn_msg.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cntdwn_msg.cancel();
                    }
                }, 1000);
            }
            public void onFinish() {
                sendSMS();
                Toast.makeText(getApplicationContext(), "Emergency!", Toast.LENGTH_SHORT).show();

            }
        };
        cTimer.start();
    }


    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // NAVIGATIONS CHANGE ACTIVITIES????

        if (id == R.id.nav_contacts) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
        } else if (id == R.id.nav_settings) {

        }else if (id == R.id.log_out){

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * SMS STUFFF
     */

    /**
     * Sends the SMS to the assign numbers using the SmsManager
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            String name = retrieveContactName();
            String num = retrieveContactNumber();
            conList.add(new Contact(name,num));

            setContacts();

        }
    }

    protected void sendSMS(){
        SmsManager manager = SmsManager.getDefault();
        for(int i = 0; i < conList.size(); i++){
            String name = conList.get(i).name;
            String num = conList.get(i).phone;
            manager.sendTextMessage(num, null, "Hello " + name, null, null);
            Log.w("SENT TO: ", name + " " + num);
        }
    }

    /**
     * Set Contacts on TextView (txt1)
     */
    public void setContacts(){
        String name, num, combo;
        String total = "";
        for(int i = 0; i < conList.size(); i++){
            name = conList.get(i).name;
            num = conList.get(i).phone;
            combo = name + " " + num + "\n";
            total += combo;
        }
        txt1.setText(total);

    }

    /**
     * Retrieve Contact Number
     */
    private String retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        return contactNumber;
//        Toast.makeText(getApplicationContext(), contactNumber, Toast.LENGTH_SHORT).show();
    }

    /**
     * Retrieve Contact Name
     */
    private String retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        return contactName;

    }


    /**
     * Request Permissions for Sending SMS, Reading Contacts, Accessing Locations
     */
    protected void requestPerms(){
        requestPermissions(new String[]{SEND_SMS, READ_CONTACTS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, INTERNET}, 1);

    }
}
