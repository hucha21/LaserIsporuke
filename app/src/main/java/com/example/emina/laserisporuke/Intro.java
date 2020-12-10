package com.example.emina.laserisporuke;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Sequent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.LOCATION;
import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;

public class Intro extends AppCompatActivity {
    private final DropboxItemAsyncTask dropboxItemAsyncTask = new Intro.DropboxItemAsyncTask();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String potvrda = "false";
    private final ArrayList<Integer> ListaId = new ArrayList<>();
    private final ArrayList<Integer> ListaUtovaraId = new ArrayList<>();
    private int responseCode;
    private int IsporukaId = 0;
    private int UtovarId = 0;
    private String rezultat="";
    private String rez = "";
    private boolean utovar = false;
    private String[] out;
    private URL url;
    private ProgressDialog dialog;
    private final ArrayList<String> ListaIstovara = new ArrayList<>();
    private final ArrayList<String> ListaUtovara = new ArrayList<>();
    private HttpURLConnection urlConnection;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;
    GPSTracker gps;

    private final LocationRequest defaultLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private String vozac="";
    private static final String[] PERMS_ALL = {
            CAMERA,
            LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INSTALL_PACKAGES,
            WRITE_EXTERNAL_STORAGE
    };
    String Skladistar,Vozac;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        this.setTitle("Meni");
        final SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        Skladistar=sharedpreferences.getString("Skladistar","null");
        Vozac=sharedpreferences.getString(Name,"null");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel_intro);
        Sequent.origin(layout).anim(this, Animation.BOUNCE_IN).start();
        final Button skladiste=(Button)findViewById(R.id.button2);
        final Button transport=(Button)findViewById(R.id.button3);
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //mobile
        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
        //wifi
        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();

        if (NetworkInfo.State.CONNECTED == mobile || NetworkInfo.State.CONNECTING == mobile || NetworkInfo.State.CONNECTED == wifi || NetworkInfo.State.CONNECTING == wifi) {
            dropboxItemAsyncTask.execute();
        }
        else
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            Intro.this);
                    alertDialogBuilder.setTitle("Greška!");
                    alertDialogBuilder
                            .setMessage("Nema internet veze!")
                            .setCancelable(false)
                            .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    d.dismiss();
                                    skladiste.setClickable(false);
                                    transport.setClickable(false);
                                    //Intro.this.finish();
                                    }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        }

        transport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Objects.equals(Vozac, "null")) {
                    Intent intent = new Intent(Intro.this, LoginActivity.class);
                    SharedPreferences.Editor prefEdit = sharedpreferences.edit();
                    prefEdit.putString("transport", "true");
                    prefEdit.commit();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    Intro.this.finish();
                }
                else
                {
                    Intent intent = new Intent(Intro.this, MainActivity.class);
                    SharedPreferences.Editor prefEdit = sharedpreferences.edit();
                    prefEdit.putString("transport", "true");
                    prefEdit.commit();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    Intro.this.finish();
                }
            }
        });

        skladiste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Objects.equals(Skladistar, "null")) {
                    Intent i = new Intent(Intro.this, SkladisteActivity.class);
                    SharedPreferences.Editor prefEdit = sharedpreferences.edit();
                    prefEdit.putString("transport", "false");
                    prefEdit.commit();
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    Intro.this.finish();
                }
                else
                {
                    Intent i = new Intent(Intro.this, Login_Skladiste.class);
                    SharedPreferences.Editor prefEdit = sharedpreferences.edit();
                    prefEdit.putString("transport", "false");
                    prefEdit.commit();
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    Intro.this.finish();
                }
            }
        });
    }
    private class DropboxItemAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URLConnection conn = new URL("http://extranet.laserkitchen.ch/Content/LaserClient/versioninfoIsporuke.txt?dl=1").openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "UTF-8"), 8);
                String line;
                while ((line = reader.readLine()) != null) {
                    rez = line;
                }
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            String a = getVersionName(Intro.this);

            //
            Log.v("Update",rez);
            Log.v("Update",a);
            if (!a.equals(rez) && rez != null && rez.contains(".")) {
                showNotification();
                rez = "";
            }
        }
    }
    private void showNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, NotifyMessage.class), 0);
        getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Ažuriranje")
                .setContentInfo("")
                .setSubText("Pritisni za ažuriranje" + " v." + rez)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
     private void CheckPerm() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             requestPermissions(PERMS_ALL, 200);
             AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                     Intro.this);
             alertDialogBuilder.setTitle("Dopuštenja");
             alertDialogBuilder
                     .setMessage("Nakon prihvatanja dopuštenja pritsnite dugme Nastavi")
                     .setCancelable(false)
                     .setNeutralButton("Nastavi", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
                             Intro.this.recreate();
                         }
                     });
             AlertDialog alertDialog = alertDialogBuilder.create();
             alertDialog.show();
         }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toasty.info(Intro.this, "Aplikacija v." + getVersionName(Intro.this)+"\n"+"Copyright © 2017 Laser Swiss Kitchen", Toast.LENGTH_LONG, true).show();
                    }});
                return true;
            case R.id.changelog:
                Intent intent = new Intent(Intro.this, CVhangelog.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    @Nullable
    private static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return (pi.versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }
    @Override
    public void onBackPressed() {


        new MaterialDialog.Builder(this)
                .title("Potvrdi")
                .content("Želite li napustiti aplikaciju?")
                .positiveText("Da")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intro.this.finish();
                                    overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                    // overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                    dialog.dismiss();
                                }
                            }
                )
                .negativeText("Ne")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }




}
