package com.example.emina.laserisporuke;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Sequent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.LOCATION;

public class Login_Skladiste extends AppCompatActivity {
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
    String[] names={"Frances Grimmer",
            "Alvin Orlando",
            "Freddy Gaudet",
            "Cornell Crosswhite",
            "Sterling Grassi"};
    private static final String[] PERMS_ALL = {
            CAMERA,
            LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INSTALL_PACKAGES,
            WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle("Prijava");
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, names);
            Spin.setAdapter(adapter); // this will set list of values to spinner
            if (sharedpreferences.contains("Skladistar")) {
                //Log.v("UserShr",sharedpreferences.getString("Name"," "));
                runOnUiThread(new Runnable() {
                    public void run() {
                        Spin.setSelection(0);
                        //Log.v("UserIIDD", String.valueOf(sharedpreferences.getInt("Id",0)));
                    }
                });
            }
            //new LoginActivity.getVozace().execute();
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    Login_Skladiste.this);
            alertDialogBuilder.setTitle("Nema Internet konekcije.");
            alertDialogBuilder
                    .setMessage("Molim uključite Wifi ili mobilne podatke")
                    .setCancelable(false)
                    .setPositiveButton("Uključi Wifi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            wifiManager.setWifiEnabled(true);
                            Login_Skladiste.this.recreate();
                        }
                    })
                    .setNeutralButton("Uključi mobilne", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                            Login_Skladiste.this.startActivity(intent);
                            Login_Skladiste.this.recreate();
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
        int permissionCheck = ContextCompat.checkSelfPermission(Login_Skladiste.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            CheckPerm();
        }
        Button Kreni=(Button)findViewById(R.id.kreni);

        Kreni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.info(Login_Skladiste.this,"Odabran Korisnik: "+Spin.getSelectedItem().toString(), Toast.LENGTH_SHORT,true).show();
                vozac=Spin.getSelectedItem().toString();
                Intent intent = new Intent(Login_Skladiste.this, SkladisteActivity.class);
                SharedPreferences.Editor prefEdit = sharedpreferences.edit();
                prefEdit.putString("Skladistar", Spin.getSelectedItem().toString());
                prefEdit.putInt("SkladistarId", (int) Spin.getSelectedItemId());
                prefEdit.apply();
                startActivity(intent);
                Login_Skladiste.this.finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });
    }
    private void CheckPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMS_ALL, 200);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    Login_Skladiste.this);
            alertDialogBuilder.setTitle("Dopuštenja");
            alertDialogBuilder
                    .setMessage("Nakon prihvatanja dopuštenja pritsnite dugme Nastavi")
                    .setCancelable(false)
                    .setNeutralButton("Nastavi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Login_Skladiste.this.recreate();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent i = new Intent(Login_Skladiste.this, Intro.class);
        startActivity(i);
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
        Login_Skladiste.this.finish();
    }
}
