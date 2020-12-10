package com.example.emina.laserisporuke;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Sequent;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.LOCATION;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    ArrayList<String> ListaVozaca=new ArrayList<>();
    private URL url;
    private ProgressDialog dialog;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String TAG = MainActivity.class.getSimpleName();
    String Url;
    private GoogleApiClient mGoogleApiClient;
    private String potvrda = "false";
    ArrayList<Integer> ListaId = new ArrayList<>(), ListaUtovaraId = new ArrayList<>();
    private int responseCode;
    List<String> ListaReferenci;
    int IsporukaId = 0;
    int UtovarId = 0;
    private String rezultat="";
    private String rez = "";
    String vozac="";
    boolean utovar = false;
    ArrayList<String> options= new ArrayList<>();
    private String[] out;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;
    GPSTracker gps;
    private Spinner Spin;
    private float scale = 1f;
    private ScaleGestureDetector SGD;
    ImageView iv;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Name = "Name";
    public static final String Id = "Id";
    String skladistar="";
    String transport;
    private final ArrayList<String> ListaIstovara = new ArrayList<>();
    private final ArrayList<String> ListaUtovara = new ArrayList<>();
    private HttpURLConnection urlConnection;
    private static final String[] PERMS_ALL = {
            CAMERA,
            LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INSTALL_PACKAGES,
            WRITE_EXTERNAL_STORAGE
    };

    String[]ListRef;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relLogin);
        Sequent.origin(layout).anim(this, Animation.FADE_IN_LEFT).start();
         Spin=(Spinner)findViewById(R.id.spinner);
        //transport=sharedpreferences.getString("transport","null");
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //mobile
        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
        //wifi
        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();


        if (NetworkInfo.State.CONNECTED == mobile || NetworkInfo.State.CONNECTING == mobile || NetworkInfo.State.CONNECTED == wifi || NetworkInfo.State.CONNECTING == wifi) {
            new getVozace().execute();
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    LoginActivity.this);
            alertDialogBuilder.setTitle("Nema Internet konekcije.");
            alertDialogBuilder
                    .setMessage("Molim uključite Wifi ili mobilne podatke")
                    .setCancelable(false)
                    .setPositiveButton("Uključi Wifi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            wifiManager.setWifiEnabled(true);
                            LoginActivity.this.recreate();
                        }
                    })
                    .setNeutralButton("Uključi mobilne", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                            LoginActivity.this.startActivity(intent);
                            LoginActivity.this.recreate();
                        }
                    })
                    .setNegativeButton("Izađi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //disable wifi
                            finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        int permissionCheck = ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            CheckPerm();
        }
        Button Kreni=(Button)findViewById(R.id.kreni);


Kreni.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        Toasty.info(LoginActivity.this,"Odabran Korisnik: "+Spin.getSelectedItem().toString(), Toast.LENGTH_SHORT,true).show();
        vozac=Spin.getSelectedItem().toString();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            SharedPreferences.Editor prefEdit = sharedpreferences.edit();
            prefEdit.putString(Name, Spin.getSelectedItem().toString());
            prefEdit.putInt(Id, (int) Spin.getSelectedItemId());
            prefEdit.apply();
            //Log.v("User: ",sharedpreferences.getString("Name","-"));
            //Log.v("UserId:", String.valueOf(sharedpreferences.getInt("Id",0)));
            //intent.putExtra("Vozac", Spin.getSelectedItem().toString());
            //Log.v("Proces", String.valueOf(Spin.getSelectedItemId()));
            startActivity(intent);
            LoginActivity.this.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
});
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(LoginActivity.this, Intro.class);
        startActivity(i);
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        LoginActivity.this.finish();
    }
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while (null != (line = reader.readLine())) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
    private class Test_connection extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            rezultat="";
            dialog = ProgressDialog.show(LoginActivity.this, "", "Provjera Konekcije..", true);
            //createLocationRequest();
        }

        @Override
        protected final String doInBackground(String... params) {
            String text = null;
            String url1 = "http://80.65.91.194:60001/Service1.svc/rest";
            try {
                URL url = new URL(url1 + "/hallo");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(2500);
                int responseCode = urlConnection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) {
                    try {
                        text = readStream(urlConnection.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Log.v("Connection:", text);
                } else {
                    urlConnection.disconnect();
                }
                try {
                    rezultat = new JSONObject(text).getString("HalloResult");
                    urlConnection.disconnect();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            // Log.v("Update",rezultat.toString());
            if (rezultat.isEmpty()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                LoginActivity.this);
                        alertDialogBuilder.setTitle("Problem sa vezom");
                        alertDialogBuilder
                                .setMessage("Problem sa vezom.Molimo pokušajte kasnije.")
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_signal_wifi_off_black_24dp)
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        rezultat="";
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            } else {
                int permissionCheck = ContextCompat.checkSelfPermission(LoginActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    CheckPerm();
                } else {
                    utovar = true;
                    new getUtovare().execute();
                    ListaUtovara.clear();
                    ListaUtovaraId.clear();

                }
                rezultat = "";
            }
        }
    }

    private void CheckPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMS_ALL, 200);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    LoginActivity.this);
            alertDialogBuilder.setTitle("Dopuštenja");
            alertDialogBuilder
                    .setMessage("Nakon prihvatanja dopuštenja pritsnite dugme Nastavi")
                    .setCancelable(false)
                    .setNeutralButton("Nastavi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            LoginActivity.this.recreate();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    private class getUtovare extends AsyncTask<Void, Void, Void> {
        int rez;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Sacekajte...", true);
            ListaUtovara.clear();
            ListaUtovaraId.clear();
            out=null;
        }
        protected Void doInBackground(Void... strings) {
            String encoded= null;
            try {
                encoded = URLEncoder.encode("12 2", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/dajUtovare/"+ (encoded != null ? encoded.replace("+", "%20") : null);
            //Log.v("Proces3",Url);
            String st="";
            try {
                url = new URL(Url);
                urlConnection = (HttpURLConnection)url.openConnection();
                responseCode = urlConnection.getResponseCode();
                //
                // Log.v("Proces3 ", String.valueOf(responseCode));
                String text = null;
                if (responseCode == 200) {
                    try {
                        text = readStream(urlConnection.getInputStream());

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                else
                {dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LoginActivity.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                LoginActivity.this.finish();
                                                ListaUtovaraId.clear();
                                                ListaUtovara.clear();
                                            }
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }});}
                try {
                    rezultat = new JSONObject(text).getString("DajUtovareResult");
                     Log.v("Update",rezultat);
                    JSONArray jArray=new JSONArray(null != rezultat ? rezultat.trim() : null);

                    if(jArray.length()!=0) {
                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject c = null;
                            try {
                                c = jArray.getJSONObject(i);
                                ListaUtovara.add("ID: "+c.getString("Id"));
                                ListaUtovaraId.add(c.getInt("Id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    urlConnection.disconnect();
                    dialog.dismiss();
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            if (ListaUtovara.size()!=0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title("Utovari ")
                                .items(ListaUtovara)
                                .dividerColor(getResources().getColor(R.color.colorAccent))
                                .itemsColor(getResources().getColor(R.color.colorAccent))
                                .cancelable(false)
                                .negativeText("Zatvori")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ListaUtovara.clear();
                                        ListaUtovaraId.clear();
                                        ListaIstovara.clear();
                                        ListaId.clear();
                                        out = null;
                                    }
                                })
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                        UtovarId = ListaUtovaraId.get(which);
                                        //Log.v("Return", String.valueOf(ListaUtovaraId.get(which)));
                                        new DajAdresu().execute(String.valueOf(ListaUtovaraId.get(which)));
                                        ListaUtovara.clear();
                                        ListaUtovaraId.clear();
                                    }
                                })
                                .show();
                    }
                });
            }
            else
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                LoginActivity.this);
                        alertDialogBuilder.setTitle("Greška!");
                        alertDialogBuilder
                                .setMessage("Nema odgovarajucih Utovara u bazi za isporuku!")
                                .setCancelable(false)
                                .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        d.dismiss();
                                        ListaUtovara.clear();
                                        ListaUtovaraId.clear();
                                        LoginActivity.this.finish();}
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
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
    private class DajAdresu extends AsyncTask<String, Void, Void> {
        int rez;
        ProgressDialog dialog;
        String dd="";

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Sacekajte...", true);
            rezultat="";
        }

        protected Void doInBackground(String... strings) {
            dd=strings[0];
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/DajAdresuzaIstovar/" + strings[0];
            //Log.v("Proces",Url);
            String text = null;
            try {
                url = new URL(Url);
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
                //
                //Log.v("Proces3 ", String.valueOf(responseCode));

                if (responseCode == 200) {
                    text = readStream(urlConnection.getInputStream());
                    //Log.v("Proces",text);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            try {
                rezultat = new JSONObject(text).getString("DajAdresuzaIstovarResult").replace('[' , ' ').replace(']' , ' ').replace('"',' ');
                //Log.v("Proces",rezultat);
                if (!rezultat.isEmpty())
                    out = rezultat.split(",");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            new getIstovare().execute(dd);
        }
    }

    private class getIstovare extends AsyncTask<String, Void, Void> {
        int rez;
        String adr = "";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Sacekajte...", true);
            ListaIstovara.clear();
            ListaId.clear();
        }

        protected Void doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/getIstovare/" + strings[0]+"A";
            // Log.v("Proces",Url);
            String st = "";
            try {
                url = new URL(Url);
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
                //Log.v("Proces3 ", String.valueOf(responseCode));
                String text = null;
                if (responseCode == 200) {
                    try {
                        text = readStream(urlConnection.getInputStream());

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LoginActivity.this);
                            alertDialogBuilder.setTitle("Greška! " + responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: " + urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                LoginActivity.this.finish();
                                            }
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    });
                }
                try {
                    rezultat = new JSONObject(text).getString("DajIstovareResult");

                    JSONArray jArray = new JSONArray(null != rezultat ? rezultat.trim() : null);
                    int j=0;
                    if (jArray.length() != 0) {
                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject c;
                            try {
                                c = jArray.getJSONObject(i);
                                // String adress=new DajAdresu(c.getString("Redoslijed"))
                                final JSONObject finalC = c;
                                ListaIstovara.add(c.getString("Redoslijed") + " Id:" +c.getString("Id") + "\n"+out[j]);
                                ListaId.add(c.getInt("Id"));
                                j++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        LoginActivity.this);
                                alertDialogBuilder.setTitle("Greška!");
                                alertDialogBuilder
                                        .setMessage("Nema odgovarajucih Istovara u bazi za isporuku!")
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                LoginActivity.this.finish();
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });

                    }
                    urlConnection.disconnect();
                    dialog.dismiss();
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            out=null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(LoginActivity.this)
                            .title("Istovari za " + UtovarId + "\n")
                            .items(ListaIstovara)
                            .dividerColor(getResources().getColor(R.color.colorAccent))
                            .itemsColor(getResources().getColor(R.color.colorAccent))
                            .cancelable(false)
                            .negativeText("Zatvori")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ListaUtovara.clear();
                                    ListaUtovaraId.clear();
                                    ListaIstovara.clear();
                                    ListaId.clear();
                                    out=null;
                                }
                            })
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                    IsporukaId = ListaId.get(which);
                                    Intent intent = new Intent(LoginActivity.this, USkladisti.class);
                                    intent.putExtra("ID", IsporukaId);
                                    intent.putExtra("IMEI", getDeviceName());
                                    intent.putExtra("TIME", getTime());
                                    intent.putExtra("skladistar",skladistar);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                    ListaId.clear();
                                    ListaIstovara.clear();
                                    LoginActivity.this.finish();
                                }
                            })
                            .show();
                }
            });
        }
    }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_info:
                Toasty.info(getApplicationContext(),"Laser Isporuke v."+getVersionName(getApplicationContext())+"\n"+"Copyright © 2017 Laser Swiss Kitchen",Toast.LENGTH_LONG).show();
                break;
        }return true;}
    @Nullable*/
    private static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
           // Log.v("Update2",pi.versionName);
            return (pi.versionName);
        } catch (PackageManager.NameNotFoundException ex) {
        }
        return null;
    }
    private class getVozace extends AsyncTask<Void, Void, Void> {
        int rez;
        String adr = "";


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "", "Sacekajte...", true);
            out=null;
            rezultat="";
        }

        protected Void doInBackground(Void... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/dajVozace";
            // Log.v("Proces",Url);
            String st = "";
            try {
                url = new URL(Url);
                urlConnection = (HttpURLConnection) url.openConnection();
                responseCode = urlConnection.getResponseCode();
                //Log.v("Proces3 ", String.valueOf(responseCode));
                String text = null;
                if (responseCode == 200) {
                    try {
                        text = readStream(urlConnection.getInputStream());

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    LoginActivity.this);
                            alertDialogBuilder.setTitle("Greška! " + responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: " + urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                LoginActivity.this.finish();
                                            }
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    });
                }
                try {
                    rezultat = new JSONObject(text).getString("DajVozaceResult");
                    out=rezultat.split(",");
                    urlConnection.disconnect();
                    //dialog.dismiss();
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, out);
            Spin.setAdapter(adapter); // this will set list of values to spinner
            if (sharedpreferences.contains("Name")) {
                //Log.v("UserShr",sharedpreferences.getString("Name"," "));
                runOnUiThread(new Runnable() {
                    public void run() {
                        Spin.setSelection(sharedpreferences.getInt("Id", 0));
                        //Log.v("UserIIDD", String.valueOf(sharedpreferences.getInt("Id",0)));
                    }
                });
            }
        }
    }
    protected void onDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        /*FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getExternalCacheDir());*/
        //deleteCache(getApplicationContext());
        super.onDestroy();
    }
    private String getTime() {
        //return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()).replace(' ','-');
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.ENGLISH);
        return sdf.format(new Date());
    }
    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
}