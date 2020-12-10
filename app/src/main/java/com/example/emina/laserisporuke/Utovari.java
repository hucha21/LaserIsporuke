package com.example.emina.laserisporuke;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

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
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;

public class Utovari extends AppCompatActivity implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;
    private String rez="";
    private String rezultat="";
    private int responseCode;
    private URL url;
    private HttpURLConnection urlConnection;
    int proba=7204;
    private int Id;
    private Double lon,lat;
    private String imei,time,vozac;
    private final List<String> ListaPaleta = new ArrayList<>(),Isporuceno=new ArrayList<>();
    private int Flash=0;
    int k=0;
    private FloatingActionButton fabFlash;
    List<String> ListaPalete=new ArrayList<>(),ListaKolete=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_utovari);
        setContentView(R.layout.activity_uskladisti);// Set the scanner view as the content view
        mScannerView = new ZBarScannerView(this);
        mScannerView = (ZBarScannerView) findViewById(R.id.scannerSkladiste);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true);

        Id= getIntent().getIntExtra("ID",0);
        lat=getIntent().getDoubleExtra("LAT",0);
        lon=getIntent().getDoubleExtra("LON",0);
        imei=getIntent().getStringExtra("IMEI");
        time=getIntent().getStringExtra("TIME");
        SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        vozac=sharedpreferences.getString(Name,"null");
        //getActionBar().setDisplayHomeAsUpEnabled(true);
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

   /* @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }*/
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
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
                    Utovari.this);
            alertDialogBuilder.setTitle("Jeste Li Sigurni?");
            alertDialogBuilder
                    .setMessage("Id:"+rez+"\n"+"Utovari?")
                    .setCancelable(false)
                    .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //rez
                            new UtovariPaletu().execute(String.valueOf(proba));
                            OznaciUtovarPaletu(String.valueOf(proba));
                            proba++;
                            rez="";
                        }
                    })
                    .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //disable wifi
                            mScannerView.resumeCameraPreview(Utovari.this);
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
    private class UtovariPaletu extends AsyncTask<String, Void, Void> {

        ProgressDialog dialog;
        int i=-1;


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Utovari.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(final String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/utovariPaletu/"+strings[0];
            rez="";
            String st="";
            try {

                url = new URL(Url);
                urlConnection = (HttpURLConnection) Utovari.this.url.openConnection();
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
                                    Utovari.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                startActivity(intent);
                                                Utovari.this.finish();
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
                    rezultat = new JSONObject(text).getString("UtovariPaletuResult");
                    dialog.dismiss();
                    if(rezultat.length()!=0) {
                       i=Integer.parseInt(rezultat);

                        if(i==0) {
                            if(PreostaloPaleta()==0)
                            {

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        {
                                            onPause();
                                            new KreniSaIsporukom().execute();
                                        }
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        {
                                            onPause();
                                            Toasty.success(Utovari.this,"Utovareno!"+"\n"+"Id: " + strings[0],Toast.LENGTH_SHORT,true).show();
                                            Toasty.info(Utovari.this,"Preostalo: "+PreostaloPaleta(),Toast.LENGTH_SHORT,true).show();
                                            onResume();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        { if(responseCode!=200) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            Utovari.this);
                                    alertDialogBuilder.setTitle("Greška! " + responseCode);
                                    try {
                                        alertDialogBuilder
                                                .setMessage("Greška: " + urlConnection.getResponseMessage())
                                                .setCancelable(false)
                                                .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface d, int which) {
                                                        d.dismiss();
                                                        Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                        startActivity(intent);
                                                        Utovari.this.finish();
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
                        else
                        {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toasty.error(Utovari.this,"Paleta/koleta ne pripada naznačenom istovaru!",Toast.LENGTH_SHORT,true).show();
                                    onResume();
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
                                        Utovari.this);
                                alertDialogBuilder.setTitle("Greška!");
                                alertDialogBuilder
                                        .setMessage("Servis Greška.")
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                startActivity(intent);
                                                Utovari.this.finish();
                                                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                            }
                                        });
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
    private class sendPostData extends AsyncTask<String, Void, Void> {
        int rez;
        int i=0;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Utovari.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/dajPaletezaUtovar/"+Id;
            String st="";
            Log.v("Update0",Url);
            try {

                url = new URL(Url);
                urlConnection = (HttpURLConnection) Utovari.this.url.openConnection();
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
                                    Utovari.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                startActivity(intent);
                                                Utovari.this.finish();
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
                    rezultat = new JSONObject(text).getString("DajPaleteZaUtovarResult").replace("[","").replace("]","");
                    Log.v("Update",rezultat);
                    //JSONArray jArray=new JSONArray(null != rezultat ? rezultat.trim() : null);
                    String[] a=rezultat.split(Pattern.quote(","));
                    Log.v("Update2",a.toString());
                    if(a.length>0) {
                    while(!a[i].equals("-1"))
                    {
                        ListaPaleta.add(a[i]);
                        Isporuceno.add("0");
                        i++;
                    }
                    i++;
                    int j=0;
                        Log.v("Update3", String.valueOf(i));
                    while(i<a.length)
                    {
                        if(a[i].equals("1"))
                        {
                            ListaPalete.add(a[j]);
                        }
                        else
                        {
                            ListaKolete.add(a[j]);
                        }
                        i++;
                        j++;
                    }
                        /*for (int i = 0; i < jArray.length(); i++) {
                            HashMap<String, String> contact = new HashMap<>();
                            JSONObject c = null;
                            try {
                                c = jArray.getJSONObject(i);
                                ListaPaleta.add(c.getString("IdPalete"));
                                Isporuceno.add("0");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }*/
                        dialog.dismiss();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                {
                                    onPause();
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            Utovari.this);
                                    alertDialogBuilder.setTitle("Info o utovaru "+Id);
                                    alertDialogBuilder
                                            .setMessage("Palete: "+ListaPalete.size()+" "+ListaPalete.toString()+"\n"+ "Kolete: "+ListaKolete.size()+" "+ListaKolete.toString())
                                            .setCancelable(false)
                                            .setNegativeButton("Zatvori", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Utovari.this.finish();
                                                    overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                                }
                                            })
                                            .setPositiveButton("Nastavi", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface d, int which) {
                                                    d.dismiss();
                                                    Toasty.info(Utovari.this,"Molim utovarite palete isporuke "+Id,Toast.LENGTH_SHORT,true).show();
                                                   /* Toast toast=Toast.makeText(Utovari.this,"Molim utovarite palete isporuke "+Id,Toast.LENGTH_SHORT);
                                                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                                    v.setTextColor(Color.YELLOW);
                                                    toast.show();*/
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
                                        Utovari.this);
                                alertDialogBuilder.setTitle("Greška!");
                                alertDialogBuilder
                                        .setMessage("Nema odgovarajucih paleta u bazi za utovar!")
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                startActivity(intent);
                                                Utovari.this.finish();
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
        new MaterialDialog.Builder(this)
                .title("Izađi?")
                .content("Nisu utovarene sve palete. "+"\n"+"Želite li zavrsiti utovar?")
                .titleColor(Color.RED)
                .positiveText("Da")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent(Utovari.this, MainActivity.class);
                                    startActivity(intent);
                                    Utovari.this.finish();
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
                .show();

    }
    private class KreniSaIsporukom extends AsyncTask<Void, Void, Void> {
        int rez;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Utovari.this, "", "Sacekajte...", true);
        }
        protected Void doInBackground(Void... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/krenisaisporukom/";
            String st="";
            try {
                String encoded = URLEncoder.encode(new JSONStringer().object().key("id").value(Id).key("lat").value(lat).key("lon").value(lon).key("Potvrda").value("utovari").key("deviceId").value(imei).key("time").value(time).endObject().toString(), "UTF-8");
                url = new URL(Url + encoded);
                Log.v("Preces3", String.valueOf(url));
                urlConnection = (HttpURLConnection) Utovari.this.url.openConnection();
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
                                    Utovari.this);
                            alertDialogBuilder.setTitle("Greška! "+responseCode);
                            try {
                                alertDialogBuilder
                                        .setMessage("Greška: "+urlConnection.getResponseMessage())
                                        .setCancelable(false)
                                        .setNeutralButton("Zatvori", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                d.dismiss();
                                                Intent intent = new Intent(Utovari.this, MainActivity.class);
                                                startActivity(intent);
                                                Utovari.this.finish();
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        {
                            onPause();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    Utovari.this);
                            alertDialogBuilder.setTitle("Uspješno");
                            alertDialogBuilder
                                    .setMessage("Utovarene sve palete/kolete!")
                                    .setCancelable(false)
                                    .setPositiveButton("Završi", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            Intent intent = new Intent(Utovari.this, MainActivity.class);
                                            startActivity(intent);
                                            Utovari.this.finish();
                                            overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }
                });
            }
        }
    }
}