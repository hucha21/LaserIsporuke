package com.example.emina.laserisporuke;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.emina.laserisporuke.LoginActivity.MyPREFERENCES;
import static com.example.emina.laserisporuke.LoginActivity.Name;

public class Utovareno extends AppCompatActivity {

    private final List<String> ListaImena = new ArrayList<>();
    private final List<String> ListaPaleta = new ArrayList<>();
    private ListView listView;
    int prvi_put=0;
    private int Id;
    private URL url;
    private String rezultat="";
    private HttpURLConnection urlConnection;
    private int responseCode;
    int L;
    private String[] out;
    private String vozac;
    private final ArrayList<String> ListaId = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utovareno);
        this.setTitle("Utovareno");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.mobile_list);
        listView.setFastScrollEnabled(false);
        Id= getIntent().getIntExtra("ID",0);
        SharedPreferences sharedpreferences=getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        vozac=sharedpreferences.getString(Name,"null");
        new DajAdresu().execute(String.valueOf(Id));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new getUtovarenePalete().execute(ListaId.get(position));
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Utovareno.this, MainActivity.class);
        startActivity(intent);
        Utovareno.this.finish();
        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_utovareno);

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
    private class DajAdresu extends AsyncTask<String, Void, Void> {
        int rez;
        ProgressDialog dialog;
        String dd="";


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Utovareno.this, "", "Sacekajte...", true);
            rezultat="";
        }

        protected Void doInBackground(String... strings) {
            dd=strings[0];
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/DajAdresuzaIstovar/" + strings[0];
             Log.v("Proces",Url);
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
            new Utovareno.getIstovare().execute(dd);
        }
    }
    private class getIstovare extends AsyncTask<String, Void, Void> {
        int rez;
        String adr = "";
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Utovareno.this, "", "Sacekajte...", true);
            ListaImena.clear();
            ListaId.clear();
        }
        protected Void doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/dajUtovareno/" + strings[0];
            Log.v("Proces",Url);
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
                }
                try {
                    rezultat = new JSONObject(text).getString("DajUtovarenoResult");

                    JSONArray jArray = new JSONArray(null != rezultat ? rezultat.trim() : null);
                    int j=0;
                    if (jArray.length() != 0) {
                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject c;
                            try {
                                c = jArray.getJSONObject(i);
                                // String adress=new DajAdresu(c.getString("Redoslijed"))
                                final JSONObject finalC = c;
                                ListaImena.add(c.getString("Redoslijed") + " Id:" +c.getString("Id")+"\n"+out[j]);
                                ListaId.add(c.getString("Id"));
                                j++;
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
            dialog.dismiss();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ArrayAdapter adapter = new ArrayAdapter<String>(Utovareno.this,
                    R.layout.row_layout, R.id.label, ListaImena) {
                @Override
                public View getView(int position,
                                    View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text2 = (TextView) view.findViewById(R.id.label);
                    text2.setText(ListaImena.get(position));
                    return view;
                }
            };

            if(!adapter.isEmpty()) {
                listView.setAdapter(adapter);
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MaterialDialog.Builder mat = new MaterialDialog.Builder(Utovareno.this)
                                .title("Info! ")
                                .content("Nema utovarenih istovara!")
                                .cancelable(false)
                                .negativeText("Zatvori")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Utovareno.this, MainActivity.class);
                                        startActivity(intent);
                                        Utovareno.this.finish();
                                        overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                    }
                                });
                        mat.show();
                    }
                });
            }
        }
    }

    private class getUtovarenePalete extends AsyncTask<String, Void, Integer> {
        String rez;
        String adr = "";
        ProgressDialog dialog;


        protected Integer doInBackground(String... strings) {
            String Url = "http://80.65.91.194:60001/Service1.svc/rest/dajUtovarenePalete/" + strings[0];
            Log.v("Proces", Url);
            rez=strings[0];
            ListaPaleta.clear();
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
                }
                try {
                    rezultat = new JSONObject(text).getString("DajUtovarenePaleteResult");

                    JSONArray jArray = new JSONArray(null != rezultat ? rezultat.trim() : null);
                    int j = 0;
                    if (jArray.length() != 0) {
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject c;
                            try {
                                c = jArray.getJSONObject(i);
                                // String adress=new DajAdresu(c.getString("Redoslijed"))
                                final JSONObject finalC = c;
                                ListaPaleta.add(c.getString("IdPalete"));
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

            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(ListaPaleta.size()!=0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MaterialDialog.Builder mat = new MaterialDialog.Builder(Utovareno.this)
                                .title("Isporuka: " + rez)
                                .items(ListaPaleta)
                                .cancelable(false)
                                .negativeText("Zatvori")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                });
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mat.icon(getDrawable(R.drawable.ic_done_black_24dp));
                        }
                        mat.show();
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MaterialDialog.Builder mat = new MaterialDialog.Builder(Utovareno.this)
                                .title("Greška! ")
                                .content("Nema utovarenih paleta!")
                                .cancelable(false)
                                .negativeText("Zatvori")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                });
                        mat.show();
                    }
                });
            }

        }
    }
}
