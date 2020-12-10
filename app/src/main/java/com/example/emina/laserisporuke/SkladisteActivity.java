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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Sequent;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.CAMERA;
import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;

public class SkladisteActivity extends AppCompatActivity {

    private static final String[] PERMS_ALL = {
            CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INSTALL_PACKAGES,
    };
    String rez="";
    ArrayList<Integer> ListaId = new ArrayList<>(), ListaUtovaraId = new ArrayList<>();
    private final ArrayList<String> ListaIstovara = new ArrayList<>();
    private final ArrayList<String> ListaUtovara = new ArrayList<>();
    private int responseCode;
    private HttpURLConnection urlConnection;
    private String rezultat="";
    String Skladistar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skladiste);
        this.setTitle("Skladište");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel);
        Sequent.origin(layout).anim(this, Animation.FADE_IN_UP).start();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        Skladistar=sharedpreferences.getString("Skladistar","null");
        TextView txt=(TextView)findViewById(R.id.korisnik);
        txt.setText("Korisnik: "+Skladistar);
        //dropboxItemAsyncTask.execute();
        final Button btn=(Button)findViewById(R.id.button);
        final ImageView img=(ImageView)findViewById(R.id.imageView);
        final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Button SwitchUser=(Button) findViewById(R.id.switchUser);
SwitchUser.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent i = new Intent(SkladisteActivity.this, Login_Skladiste.class);
        startActivity(i);
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        SkladisteActivity.this.finish();
    }
});
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNetwork == null) {
                    goToSettings();
                } else if (ContextCompat.checkSelfPermission(SkladisteActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    CheckPerm();
                else {
                    new DajZaSkladiste().execute();
                }
            }
        });
    }
    @Nullable
    private static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return (pi.versionName);
        } catch (PackageManager.NameNotFoundException ex) {
        }
        return null;
    }
    private void goToSettings() {
        //Toast.makeText(getApplicationContext(), "Niste povezani na internet! Molim povežite se ili uključite mobilne podatke", Toast.LENGTH_LONG).show();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                SkladisteActivity.this);
        alertDialogBuilder.setTitle("Nema Internet konekcije.");
        alertDialogBuilder
                .setMessage("Molim uključite Wifi ili mobilne podatke")
                .setCancelable(false)
                .setPositiveButton("Uključi Wifi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);
                        SkladisteActivity.this.recreate();
                    }
                })
                .setNeutralButton("Uključi mobilne", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                        SkladisteActivity.this.startActivity(intent);
                        SkladisteActivity.this.recreate();
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
    private void showNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, NotifyMessage.class), 0);
        getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Ažuriranje")
                .setContentInfo("")
                .setSubText("Pritisni za ažuriranje" + " v." + rez)
                .setSmallIcon(R.mipmap.ic_launcher_round)
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
                    SkladisteActivity.this);
            alertDialogBuilder.setTitle("Dopuštenja");
            alertDialogBuilder
                    .setMessage("Nakon prihvatanja dopuštenja pritsnite dugme Nastavi")
                    .setCancelable(false)
                    .setNeutralButton("Nastavi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SkladisteActivity.this.recreate();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
   private String readStream(InputStream in) throws Throwable {
       IOException e;
       Throwable th;
       BufferedReader bufferedReader = null;
       StringBuilder response = new StringBuilder();
       try {
           BufferedReader reader = new BufferedReader(new InputStreamReader(in));
           while (true) {
               try {
                   String line = reader.readLine();
                   if (line == null) {
                       break;
                   }
                   response.append(line);
               } catch (IOException e2) {
                   e = e2;
                   bufferedReader = reader;
               } catch (Throwable th2) {
                   th = th2;
                   bufferedReader = reader;
               }
           }
           try {
               reader.close();
               bufferedReader = reader;
           } catch (IOException e3) {
               e3.printStackTrace();
               bufferedReader = reader;
           }
       } catch (Throwable th3) {
           th = th3;
           if (bufferedReader != null) {
               try {
                   bufferedReader.close();
               } catch (IOException e322) {
                   e322.printStackTrace();
               }
           }
           throw th;
       }

       return response.toString();
   }
    private class DajZaSkladiste extends AsyncTask<Void, Void, Void> {
        int rez;
        String adr = "";
        ProgressDialog dialog;


        String[] re;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(SkladisteActivity.this, "", "Sacekajte...", true);
            ListaIstovara.clear();
            ListaId.clear();
        }

        protected Void doInBackground(Void... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/DajZaSkladiste";
            Log.v("Proces",Url);
            String st = "";
            try {
                URL url = new URL(Url);
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
                                    SkladisteActivity.this);
                            alertDialogBuilder.setTitle("Greška! " + responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: " + urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                SkladisteActivity.this.finish();
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
                    rezultat = new JSONObject(text).getString("DajZaSkladisteResult").replace('[',' ').replace(']',' ').replace('"',' ');
                    Log.v("Proces",rezultat);
                    re=rezultat.split(",");
                    Log.v("Proces",re[0]);
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
            runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(SkladisteActivity.this)
                            .title("Reference za skladiste")
                            .items( re)
                            .dividerColor(getResources().getColor(R.color.colorAccent))
                            .itemsColor(Color.BLACK)
                            .cancelable(false)
                            .negativeText("Zatvori")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ListaUtovara.clear();
                                    ListaUtovaraId.clear();
                                    ListaIstovara.clear();
                                    ListaId.clear();
                                    re=null;
                                }
                            })
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                    Intent intent = new Intent(SkladisteActivity.this, USkladisti2.class);
                                    intent.putExtra("ID", re[which]);
                                    intent.putExtra("IMEI", getDeviceName());
                                    intent.putExtra("TIME", getTime());
                                    //intent.putExtra("skladistar",skladistar);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                    ListaId.clear();
                                    ListaIstovara.clear();
                                    SkladisteActivity.this.finish();
                                }
                            })
                            .show();
                }
            });
        }
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
    @Override
    protected void onDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        // *** IMPORTANT ***
        // Stop Sensey and release the context held by it
        FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getExternalCacheDir());
        super.onDestroy();
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent i = new Intent(SkladisteActivity.this, Intro.class);
        startActivity(i);
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        SkladisteActivity.this.finish();
    }
}