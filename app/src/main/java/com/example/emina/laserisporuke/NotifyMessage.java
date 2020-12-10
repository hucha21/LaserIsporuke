package com.example.emina.laserisporuke;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class NotifyMessage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
       Uri uri=Uri.parse("http://extranet.laserkitchen.ch/Content/LaserClient/LaserIsporuke.apk");
        DownloadManager.Request req=new DownloadManager.Request(uri);
        //Log.v("Update","Skidam");
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        final File rootDirectory = new File(getApplicationContext().getExternalFilesDir(null).getAbsoluteFile().toString());
        if(!rootDirectory.exists()){
            rootDirectory.mkdirs();
        }
        File n=new File(rootDirectory+"/", "LaserIsporuke.apk");
        if(n.exists())
        {
            try{
                n.delete();}
            catch (RuntimeException e)
            {
                Log.e("App", "Exception while deleting file " + e.getMessage());
            }
        }
        req.setDestinationInExternalFilesDir(getApplicationContext(),null ,"/" + "LaserIsporuke.apk");
        final Long reference= dm.enqueue(req);
        //Log.v("Update","Skidam");
        this.finish();
    }
}
