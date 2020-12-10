package com.example.emina.laserisporuke;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;

public class USkladisti extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;
    private String rez="";
    private String rezultat="";
    private int responseCode;
    private URL url;
    private HttpURLConnection urlConnection;
    //int proba=7064;
    private String Id;
    private Double lon,lat;
    private String imei,time;
    private final List<String> ListaPaleta = new ArrayList<>(),Isporuceno=new ArrayList<>();
        private int Flash=0;
     private FloatingActionButton fabFlash;
    String skladistar="";
    private Location mLastLocation;
    private SimpleLocation location;
    private GoogleApiClient mGoogleApiClient;
    List<String> ListaPalete=new ArrayList<>(),ListaKolete=new ArrayList<>();
    int k=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_skladisti);
        setContentView(R.layout.activity_uskladisti);// Set the scanner view as the content view
        mScannerView = (ZBarScannerView) findViewById(R.id.scannerSkladiste);
        // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true);
        this.setTitle("Skladište");
        SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        skladistar=getIntent().getStringExtra("skladistar");
        Id= getIntent().getStringExtra("ID");
        imei=getIntent().getStringExtra("IMEI");
        time=getIntent().getStringExtra("TIME");
        new sendPostData().execute();
      fabFlash=(FloatingActionButton)findViewById(R.id.fab2);
        fabFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flash++;
                if(Flash%2!=0) {
                    mScannerView.setFlash(true);
                    fabFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
                }
                else
                {
                    mScannerView.setFlash(false);
                    fabFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
                }
            }
        });
    }

    private class sendPostData extends AsyncTask<String, Void, Void> {
        int rez;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(USkladisti.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/DajPaleteZaSkaldiste/"+Id.replace('"',' ').replace(" ","");
            Log.v("Update",Url);
            String st="";
            try {

                url = new URL(Url);
                urlConnection = (HttpURLConnection) USkladisti.this.url.openConnection();
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
                                    USkladisti.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                                startActivity(intent);
                                                USkladisti.this.finish();
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
                    rezultat = new JSONObject(text).getString("DajPaleteZaSkladisteResult");
                    JSONArray jArray=new JSONArray(null != rezultat ? rezultat.trim() : null);

                    if(jArray.length()!=0) {
                        for (int i = 0; i < jArray.length(); i++) {
                            HashMap<String, String> contact = new HashMap<>();
                            JSONObject c = null;
                            try {
                                c = jArray.getJSONObject(i);
                                if(c.getBoolean("DaLiJePaleta"))
                                ListaPalete.add(c.getString("Id"));
                                else
                                    ListaKolete.add(c.getString("Id"));
                                Isporuceno.add("0");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        dialog.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                {
                                    onPause();
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            USkladisti.this);
                                    alertDialogBuilder.setTitle("Info o utovaru!");
                                    alertDialogBuilder
                                            .setMessage("Ref: "+Id+"\n"+ "Paleta: "+ListaPalete.size()+":"+ListaPalete.toString()+"\n"+ "Kolete: "+ListaKolete.size()+":"+ListaKolete.toString())
                                            .setCancelable(false)
                                            .setNegativeButton("Zatvori", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    USkladisti.this.finish();
                                                    overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                                }
                                            })
                                            .setPositiveButton("Nastavi", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface d, int which) {
                                                    d.dismiss();
                                                    Toasty.info(USkladisti.this,"Molim skenirajte palete ref "+Id,Toast.LENGTH_SHORT,true).show();
                                                    onResume();
                                                    //mScannerView.resumeCameraPreview(Istovari.this);
                                                }
                                            });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }}});
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        USkladisti.this);
                                alertDialogBuilder.setTitle("Greška!");
                                alertDialogBuilder
                                        .setMessage("Nema odgovarajucih paleta u bazi za ref!")
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                                startActivity(intent);
                                                USkladisti.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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
            return null;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        /*if(Flash%2!=0) {
            mScannerView.setFlash(true);
            fabFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
        else
        {*/
            //mScannerView.setFlash(false);
            // Register ourselves as a handler for scan results.
                 // Start camera on resume
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
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
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
    private class KreniSaIsporukom extends AsyncTask<Void, Void, Void> {
        int rez;
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(USkladisti.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(Void... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/krenisaisporukom/";
            String st="";
            try {
                String encoded = URLEncoder.encode(new JSONStringer().object().key("id").value(Id).key("lat").value(44.795009).key("lon").value(15.919987).key("Potvrda").value("skladiste").key("deviceId").value(imei).key("time").value(time).endObject().toString(), "UTF-8");
                url = new URL(Url + encoded);
                Log.v("Update", String.valueOf(url));
                urlConnection = (HttpURLConnection) USkladisti.this.url.openConnection();
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
                                    USkladisti.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(USkladisti.this, MainActivity.class);
                                                startActivity(intent);
                                                USkladisti.this.finish();
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
                    rezultat = new JSONObject(text).getString("JSONDataResult");
                    Log.v("Update",rezultat);
                    urlConnection.disconnect();
                    ListaPaleta.clear();
                    Isporuceno.clear();
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
        protected void onPostExecute(Void aVoid) {
            if(responseCode==200)
            {
                Log.v("Update","Dialog#");
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                USkladisti.this);
                        alertDialogBuilder.setTitle("Info!");
                        alertDialogBuilder
                                .setMessage("Sve palete prebačene u skladiste!")
                                .setCancelable(false)
                                .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        d.dismiss();
                                        Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                        startActivity(intent);
                                        USkladisti.this.finish();
                                        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }});
            }
        }
    }
    @Override
    public void handleResult(me.dm7.barcodescanner.zbar.Result result) {
        // Do something with the result here
        //Log.v("kkkk", result.getContents()); // Prints scan results
        //
        //Log.v("uuuu", result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
        rez="";
        String id= result.getContents();
        //Log.v("Proces",id);

        if (id != null) {
            for(int i=id.length()-1;i>=0;i--)
            {
                if(id.charAt(i)=='/')
                {
                    break;
                }
                else
                    rez+=id.charAt(i);
            }
            //  scanResults.setText(new StringBuilder(rez).reverse().toString());
            rez=new StringBuilder(rez).reverse().toString();
            //scanResults.setText(barcode.displayValue);
            //Log.v("Scan",rez);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    USkladisti.this);
            alertDialogBuilder.setTitle("Jeste Li Sigurni?");
            alertDialogBuilder
                    .setMessage("Id:"+rez+"\n")
                    .setCancelable(false)
                    .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (OznaciUtovarPaletu(String.valueOf(rez)) == 1) {
                                new PrebaciPaletuUSkladiste().execute(String.valueOf(rez));
                                //proba++;
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toasty.error(USkladisti.this, "Ne pripada ref!", Toast.LENGTH_SHORT, true).show();
                                        mScannerView.setFlash(false);
                                        onResume();
                                        fabFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
                                    }
                                });
                            }
                        }
                            //6020 -example
                    })
                    .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //disable wifi
                            mScannerView.resumeCameraPreview(USkladisti.this);
                            rez="";
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            onResume();
            //Log.d("as", "No barcode captured, intent data is null");
        }

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
    private class PrebaciPaletuUSkladiste extends AsyncTask<String, Void, Void> {

        ProgressDialog dialog;
        String i;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(USkladisti.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(final String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/prebaciPaletuUSkladiste/"+strings[0];
            rez="";
            i=strings[0];
            Log.v("Proces",Url);
            String st="";
            try {

                url = new URL(Url);
                urlConnection = (HttpURLConnection) USkladisti.this.url.openConnection();
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
                                    USkladisti.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                                startActivity(intent);
                                                USkladisti.this.finish();
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
                    rezultat = new JSONObject(text).getString("PrebaciPaletuUSkladisteResult");
                    Log.v("Proces", String.valueOf(responseCode));
                    dialog.dismiss();
                    if(rezultat.length()!=0) {


                            if (PreostaloPaleta() == 0) {
                                Log.v("Update", "Sve");
                                onPause();
                                 //Toasty.success(USkladisti.this, "Sve palete prebačene u skladiste!", Toast.LENGTH_SHORT, true).show();

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.v("Update", "Krecem sa isporukom");
                                        new KreniSaIsporukom().execute();
                                    }
                                });
                            } else {
                                if (rezultat.equals("0")) {
                                    OznaciUtovarPaletu("");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toasty.success(USkladisti.this, "Prebaceno u skladiste!" + "\n" + "Id: " + strings[0], Toast.LENGTH_SHORT, true).show();
                                            Toasty.info(USkladisti.this, "Preostalo: " + PreostaloPaleta(), Toast.LENGTH_SHORT, true).show();
                                            onResume();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toasty.error(USkladisti.this, "Nema u bazi!", Toast.LENGTH_SHORT, true).show();
                                            mScannerView.setFlash(false);
                                            onResume();
                                            fabFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
                                        }
                                    });
                                }
                            }
                        }

                        else
                        {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            USkladisti.this);
                                    alertDialogBuilder.setTitle("Greška! " + responseCode);
                                    try {
                                        alertDialogBuilder
                                                .setMessage("Greška: " + urlConnection.getResponseMessage())
                                                .setCancelable(false)
                                                .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface d, int which) {
                                                        d.dismiss();
                                                        Intent intent = new Intent(USkladisti.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        USkladisti.this.finish();
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
    private int OznaciUtovarPaletu(String rez) {
        for(int i=0;i<ListaPalete.size();i++)
        {
            if(ListaPalete.get(i).equals(rez))
            {
                Isporuceno.set(k,"1");
                k++;
                return 1;
            }
        }
        for(int i=0;i<ListaKolete.size();i++)
        {
            if(ListaKolete.get(i).equals(rez))
            {
                Isporuceno.set(k,"1");
                k++;
                return 1;
            }
        }
        return 0;
    }
    private int PreostaloPaleta() {
        int preostalo=0;
        for(int i=0;i<Isporuceno.size();i++) {
            if(Isporuceno.get(i).equals("0"))
                preostalo++;
        }
        return preostalo;
    }
    @Override
    public void onBackPressed() {
        onPause();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                USkladisti.this);
        alertDialogBuilder.setTitle("Izađi?");
        alertDialogBuilder
                .setCancelable(false)
                .setMessage("Jeste li sigurni?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(USkladisti.this, LoginActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                        USkladisti.this.finish();
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onResume();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }
}