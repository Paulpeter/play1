package com.agu.instasave;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by kjhn on 1/13/2016.
 */
public class DownloadAsyncTask extends AsyncTask<String,Boolean,String> {
private Context context;
    private  DownloadManager idm;
    private NetworkInfo netInfo;
    private File file;
    private String saveDir=null;
    private static final String downloadStr ="InstaSave Download",pending="Download pending...",netError="Network error, retry";
    private NotificationCompat.Builder notifBuilder;
    private NotificationManager notifManager;
public DownloadAsyncTask(Context context)
    {
        netInfo=((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
        builder.setContentTitle(downloadStr).setContentText(pending);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        notifBuilder =builder.setTicker(pending);

        notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        file=new File(context.getFilesDir().getAbsolutePath()+"/instahtml.xml");
        idm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
       saveDir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/InstaSave";
        File myDir=new File(saveDir);
        if(!myDir.exists()||!myDir.isDirectory())
            myDir.mkdir();
        this.context = context;
    }


    private void parsePageAndDownload()
    {
        XmlPullParser xmlPullParser=null;
        String contentAddress=null;
        String fileName=null;
        boolean hasException;
        do {
            try {
                if (xmlPullParser == null) {
                    XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    xmlPullParser = xmlPullParserFactory.newPullParser();
                    xmlPullParser.setInput(new FileInputStream(file), null);
                }
                hasException=false;

                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                    if (xmlPullParser.getEventType() == XmlPullParser.START_TAG && xmlPullParser.getName().equals("meta")) {
                        // Toast.makeText(context, "Meta is " + (xmlPullParser.getEventType() == XmlPullParser.START_TAG ? "start tag" : "not start tag"), Toast.LENGTH_SHORT).show();
                        String imageVideo=xmlPullParser.getAttributeValue(null, "property");

                        if (imageVideo!=null&&(imageVideo.equals("og:image")||imageVideo.equals("og:video")))
                        {

                            if(imageVideo.equals("og:image"))
                                fileName=System.currentTimeMillis()+".jpg";
                            else
                                fileName=System.currentTimeMillis()+".mp4";

                            contentAddress = xmlPullParser.getAttributeValue(null, "content");
                            //Toast.makeText(context,"File directory is "+context.getFilesDir(),Toast.LENGTH_LONG).show();

                        }
                    }
                }

                    File media=new File(saveDir+"/"+fileName);
                    media.createNewFile();
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(contentAddress));
                    req.setTitle(downloadStr);
                    req.setDescription(fileName);
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    req.setDestinationUri(Uri.fromFile(media));
                    req.allowScanningByMediaScanner();
                    idm.enqueue(req);
                    // Toast.makeText(context, "Download to " + contentAddress + " requested", Toast.LENGTH_LONG).show();

            } catch (XmlPullParserException e) {
                hasException=true;
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                hasException=true;
                e.printStackTrace();
            } catch (IOException e) {
                hasException=true;
                e.printStackTrace();
            }
        }while(hasException);
        // super.onPostExecute(s);

    }

    @Override
    protected String doInBackground(String... params) {
        if(netInfo!=null&&!netInfo.isConnected())
        {
            notifBuilder.setContentText(netError).setTicker(netError);
            publishProgress(true);
            return null;
        }

publishProgress(true);//show Pending task in notification
        for(String thisurl:params)
        {
            try {
               URL url=new URL(thisurl);
                URLConnection conn=url.openConnection();
                byte buffer[]=new byte[1024];

                if(!file.exists())
                file.createNewFile();
                DataInputStream din=new DataInputStream(conn.getInputStream());
                FileOutputStream fos=new FileOutputStream(file);
                //float bytesread=
                int x;
                while((x=din.read(buffer))!=-1)
                {
                    fos.write(buffer,0,x);
                    fos.flush();
                }
                fos.close();
                din.close();
              //  publishProgress("Instagram html file " + file.getAbsolutePath() + " downloaded successfully");
                parsePageAndDownload();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context,"MalformedURLException has occured "+e.getMessage(),Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context,"IOException has occured "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        publishProgress(false);
        return null;

    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
      //  Toast.makeText(context,values[0],Toast.LENGTH_LONG).show();

        if(values[0]==true)
            notifManager.notify(1, notifBuilder.build());

        else
            notifManager.cancel(1);
    }
}
