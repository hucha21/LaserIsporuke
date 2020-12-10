package com.example.emina.laserisporuke;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Sequent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.LOCATION;
import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;
import static com.example.emina.laserisporuke.R.id.img;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String TAG = MainActivity.class.getSimpleName();
    String Url;

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
    private static final String[] PERMS_ALL = {
            CAMERA,
            LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INSTALL_PACKAGES,
            WRITE_EXTERNAL_STORAGE
    };
    private final LocationRequest defaultLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
private String vozac="";
    private SimpleLocation location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        vozac=sharedpreferences.getString(Name,"null");
        TextView txt=(TextView)findViewById(R.id.korisnik);
        txt.setText("Korisnik: "+vozac);
        //Log.v("Proces",Vozac);
        final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (checkPlayServices()) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            //createLocationRequest();
        }
        Button SwitchUser=(Button) findViewById(R.id.switchUser);
        SwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                MainActivity.this.finish();
            }
        });
        Button Istovari = (Button) findViewById(R.id.isporuci);
        Button Utovari = (Button) findViewById(R.id.utovari);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel);
        Sequent.origin(layout).anim(this, Animation.FADE_IN_LEFT).start();
            location=new SimpleLocation(this);


        Button utovareno=(Button)findViewById(R.id.prikazi_utovarene);
        utovareno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Test_connection().execute("2");
            }
        });
        Istovari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNetwork == null) {
                    goToSettings(2);
                }
                else if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                goToSettings(1);
                }
                else {

                    new Test_connection().execute("1");

                }
            }
        });
        Utovari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNetwork == null) {
                    goToSettings(2);
                }
                else if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    goToSettings(1);
                }
                else {

                    new Test_connection().execute("0");

                }
            }
        });
        final ImageView Img=(ImageView)findViewById(img);
        Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.view.animation.Animation rotate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_picture);
                Img.startAnimation(rotate);
            }
        });
        // Set up View: Map & Action Bar
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //createLocationRequest();
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    /*mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    Log.v("Proces",mLastLocation.getLatitude()+" "+mLastLocation.getLongitude());
                    gps = new GPSTracker(MainActivity.this);
                    if (gps.canGetLocation()) {

                        mLastLocation.setLatitude(gps.getLatitude());
                        mLastLocation.setLongitude(gps.getLongitude());
                        */
                    mLastLocation.setLatitude(location.getLatitude());
                    mLastLocation.setLongitude(location.getLongitude());
                    if (mLastLocation != null) {
                        displayLocation();
                        updatePolyline();
                        updateCamera();
                        updateMarker();
                    }
                }else {
                    CheckPerm();
                }
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        // make the device update its location
        location.beginUpdates();

        // ...
    }

    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        location.endUpdates();

        // ...

        super.onPause();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //takePicture();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode == 0) {
            return true;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();
        } else {
            Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        return false;
    }

    protected void onStart() {
        super.onStart();
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.connect();
        }
    }

    protected void onStop() {
        super.onStop();
        this.mGoogleApiClient.disconnect();
    }

    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    public void onConnectionSuspended(int i) {
        this.mGoogleApiClient.connect();
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed:ConnectionResult.getErrorCode()= " + connectionResult.getErrorCode());
    }

    /*private void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }*/

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled("gps")) {
            this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
            double mLat = mLastLocation.getLatitude();
            double mLng = mLastLocation.getLongitude();
            mLatLng = new LatLng(mLat, mLng);
        }
    }

    private void CheckPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMS_ALL, 200);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            alertDialogBuilder.setTitle("Dopuštenja");
            alertDialogBuilder
                    .setMessage("Nakon prihvatanja dopuštenja pritsnite dugme Nastavi")
                    .setCancelable(false)
                    .setNeutralButton("Nastavi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.recreate();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
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
            dialog = ProgressDialog.show(MainActivity.this, "", "Provjera Konekcije..", true);
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
                                MainActivity.this);
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
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    CheckPerm();
                } else {
                    switch (result) {
                        case "1":
                            utovar = false;
                            new getUtovare().execute();
                            ListaUtovara.clear();
                            ListaUtovaraId.clear();
                            break;
                        case "2":
                            utovar = true;
                            new getSveUtovare().execute();
                            ListaUtovara.clear();
                            ListaUtovaraId.clear();
                            break;
                        default:
                            utovar = true;
                            new getUtovare().execute();
                            ListaUtovara.clear();
                            ListaUtovaraId.clear();
                            break;
                    }
                }
                rezultat = "";
            }
        }
    }
    private void goToSettings(int a) {
        if (a == 1) {
            final Intent callGPSSettingIntent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            alertDialogBuilder.setTitle("Lokacija iskljucena");
            alertDialogBuilder
                    .setMessage("Želite li uključiti lokaciju?")
                    .setCancelable(false)
                    .setPositiveButton("Uključi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.startActivity(callGPSSettingIntent);
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
        else {
            //Toast.makeText(getApplicationContext(), "Niste povezani na internet! Molim povežite se ili uključite mobilne podatke", Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            alertDialogBuilder.setTitle("Nema Internet konekcije.");
            alertDialogBuilder
                    .setMessage("Molim uključite Wifi ili mobilne podatke")
                    .setCancelable(false)
                    .setPositiveButton("Uključi Wifi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            wifiManager.setWifiEnabled(true);
                        }
                    })
                    .setNeutralButton("Uključi mobilne", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                            MainActivity.this.startActivity(intent);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        initializeMap();
    }

    private void initializeMap() {
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
    }

    private void updatePolyline() {
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions.add(mLatLng));
    }

    @Override
    protected void onDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
        FileUtils.deleteQuietly(getApplicationContext().getExternalCacheDir());
        //deleteCache(getApplicationContext());
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
       /* new MaterialDialog.Builder(this)
                .title("Potvrdi")
                .content("Želite li napustiti aplikaciju?")
                .positiveText("Da")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MainActivity.this.finish();
                        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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
                .show();*/
        Intent intent = new Intent(MainActivity.this, Intro.class);
        startActivity(intent);
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        MainActivity.this.finish();
    }
    /*@Override
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
                        Toasty.info(MainActivity.this, "Aplikacija v." + getVersionName(MainActivity.this)+"\n"+"Copyright © 2017 Laser Swiss Kitchen", Toast.LENGTH_LONG, true).show();
                    }});
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }*/

    private void updateMarker() {
        mGoogleMap.addMarker(new MarkerOptions().position(mLatLng));
    }
    private void updateCamera() {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }

    private String getTime() {
        //return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()).replace(' ','-');
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.ENGLISH);
        return sdf.format(new Date());


    }
    private class getIstovare extends AsyncTask<String, Void, Void> {
        int rez;
        String adr = "";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "", "Sacekajte...", true);
            ListaIstovara.clear();
            ListaId.clear();
        }

        protected Void doInBackground(String... strings) {

            String Url = "http://80.65.91.194:60001/Service1.svc/rest/getIstovare/" + strings[0];
            if(utovar)
                Url+="B";
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
                                    MainActivity.this);
                            alertDialogBuilder.setTitle("Greška! " + responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: " + urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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
                       /* runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        MainActivity.this);
                                alertDialogBuilder.setTitle("Greška!");
                                alertDialogBuilder
                                        .setMessage("Nema odgovarajucih Istovara u bazi za isporuku!")
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(MainActivity.this, Intro.class);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });*/

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
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            final String formattedDate = df.format(c.getTime());
            runOnUiThread(new Runnable() {
                public void run() {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title("Istovari za " + UtovarId + "\n" + formattedDate)
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
                                    if (!utovar)
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                new MaterialDialog.Builder(MainActivity.this)
                                                        .title("Potvrda")
                                                        .content("Jeste li sigurni da zelite isporuciti isporuku br " + "\n" + ListaIstovara.get(which))
                                                        .positiveText("Da")
                                                        .negativeText("Ne")
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                potvrda="false";
                                                                new KreniSaIsporukom().execute();
                                                                ListaId.clear();
                                                                ListaIstovara.clear();
                                                            }
                                                        })
                                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                dialog.dismiss();
                                                                ListaId.clear();
                                                                ListaIstovara.clear();
                                                            }
                                                        }).show();
                                            }
                                        });
                                    else {
                                        Intent intent = new Intent(MainActivity.this, Utovari.class);
                                        intent.putExtra("ID", IsporukaId);
                                        intent.putExtra("LAT", mLastLocation.getLatitude());
                                        intent.putExtra("LON", mLastLocation.getLongitude());
                                        intent.putExtra("IMEI", getDeviceName());
                                        intent.putExtra("TIME", getTime());
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                                        ListaId.clear();
                                        ListaIstovara.clear();
                                        MainActivity.this.finish();
                                    }
                                }
                            })
                            .show();
                }
            });
        }
    }

    private class DajAdresu extends AsyncTask<String, Void, Void> {
        int rez;
        ProgressDialog dialog;
            String dd="";

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "", "Sacekajte...", true);
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

    private class getUtovare extends AsyncTask<Void, Void, Void> {
        int rez;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "", "Sacekajte...", true);
            ListaUtovara.clear();
            ListaUtovaraId.clear();
            out=null;
        }
        protected Void doInBackground(Void... strings) {
            String encoded= null;
            try {
                encoded = URLEncoder.encode(vozac, "UTF-8");
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
                                    MainActivity.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                ListaUtovaraId.clear();
                                                ListaUtovara.clear();
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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
                   // Log.v("Proces3",rezultat);
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
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                final String formattedDate = df.format(c.getTime());
                runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Utovari za " + formattedDate)
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
                                MainActivity.this);
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
                                    Intent i= new Intent(MainActivity.this,LoginActivity.class);
                                    startActivity(i);
                                    MainActivity.this.finish();}
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            }
        }
    }
    private class getSveUtovare extends AsyncTask<Void, Void, Void> {
        int rez;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "", "Sacekajte...", true);
            ListaUtovara.clear();
            ListaUtovaraId.clear();
            out=null;
        }
        protected Void doInBackground(Void... strings) {
            String encoded= null;
            try {
                encoded = URLEncoder.encode(vozac, "UTF-8");
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
                                    MainActivity.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                ListaUtovaraId.clear();
                                                ListaUtovara.clear();
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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
                    // Log.v("Proces3",rezultat);
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
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                final String formattedDate = df.format(c.getTime());
                runOnUiThread(new Runnable() {
                    public void run() {
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Utovari za " + formattedDate)
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
                                        Log.v("Return", String.valueOf(UtovarId));
                                       Intent i=new Intent(MainActivity.this,Utovareno.class);
                                        i.putExtra("ID",UtovarId);
                                        //String.valueOf(UtovarId)
                                        startActivity(i);
                                        ListaUtovara.clear();
                                        ListaUtovaraId.clear();
                                        if(dialog.isShowing())
                                            dialog.cancel();
                                        MainActivity.this.finish();
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
                                MainActivity.this);
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
                                        Intent i= new Intent(MainActivity.this,LoginActivity.class);
                                        startActivity(i);
                                        MainActivity.this.finish();}
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            }
        }
    }
    private class KreniSaIsporukom extends AsyncTask<String, Void, Void> {
            int rez;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/krenisaisporukom/";
            String st="";
            try {
                //mLastLocation.setLatitude(46.5391632);mLastLocation.setLongitude(6.5937025);
                String encoded = URLEncoder.encode(new JSONStringer().object().key("id").value(IsporukaId).key("lat").value(mLastLocation.getLatitude()).key("lon").value(mLastLocation.getLongitude()).key("Potvrda").value(potvrda).key("deviceId").value(getDeviceName()).key("time").value(getTime()).endObject().toString(), "UTF-8");
                url = new URL(Url + encoded);
                //Log.v("Proces",encoded);
                urlConnection = (HttpURLConnection) MainActivity.this.url.openConnection();
                responseCode = urlConnection.getResponseCode();
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
                                    MainActivity.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                            }
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }});}
                try {
                    rezultat = new JSONObject(text).getString("KreniSaIsporukomResult");
                   // Log.v("Proces",rezultat);
                    dialog.dismiss();
                        afterRest(rezultat);
                    urlConnection.disconnect();

                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }
    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
    private void afterRest(final String rezultat) {
        //rez="";
        if (Objects.equals(rezultat,"0")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.success(getApplicationContext(),"Uspješno!",Toast.LENGTH_SHORT,true).show();
                    new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run () {
                                    Intent intent = new Intent(MainActivity.this, Istovari.class);
                                    intent.putExtra("ID", IsporukaId);
                                    intent.putExtra("LAT", mLastLocation.getLatitude());
                                    intent.putExtra("LON", mLastLocation.getLongitude());
                                    //Log.v("Proces",mLastLocation.getLatitude()+" "+mLastLocation.getLongitude());
                                    intent.putExtra("IMEI", getDeviceName());
                                    intent.putExtra("TIME", getTime());
                                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                }
                            },Toast.LENGTH_SHORT);
                }
            });

        } else if (Objects.equals(rezultat, "1")) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toasty.warning(MainActivity.this,"Da li ste na pravoj lokaciji?",Toast.LENGTH_SHORT,true).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            MainActivity.this);
                    alertDialogBuilder.setTitle("Upozorenje");
                    alertDialogBuilder
                            .setMessage("Udaljeni ste vise od 2 km od adrese u bazi "+"\n"+"Zelite li nastaviti sa isporukom?")
                            .setCancelable(false)
                            .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    potvrda="null";
                                    new KreniSaIsporukom().execute();
                                }
                            })
                            .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            MainActivity.this);
                                    alertDialogBuilder.setTitle("Upozorenje");
                                    alertDialogBuilder
                                            .setMessage("Provjerite da li ste na pravoj adresi!")
                                            .setCancelable(false)
                                            .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                    Toasty.error(getApplicationContext(), "Nije isporučeno.Pogrešna lokacija!", Toast.LENGTH_SHORT,true).show();
                                                    //Toast.makeText(getApplicationContext(), "Nije isporučeno.Pogrešna lokacija!", Toast.LENGTH_SHORT).show();
                                                    rez=null;
                                                    IsporukaId=0;
                                                    //MainActivity.this.recreate();
                                                }
                                            });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toasty.error(MainActivity.this, "Greška", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            MainActivity.this);
                    alertDialogBuilder.setTitle("Greška");
                    alertDialogBuilder
                            .setMessage("Nepoznata greska! Pokusajte ponovo.")
                            .setCancelable(false)
                            .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    MainActivity.this.recreate();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        }
    }
}