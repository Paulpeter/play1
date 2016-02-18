package com.agu.instasave;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by kjhn on 1/13/2016.
 */
public class ClipBoardService extends Service implements ClipboardManager.OnPrimaryClipChangedListener,View.OnClickListener {
   private ClipboardManager clipboardManager;
    private View overlayView;
    private String address;
    private WindowManager windowManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater inf= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        windowManager= (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlayView=inf.inflate(R.layout.overlay_menu,null,false);
        overlayView.findViewById(R.id.download).setOnClickListener(this);
        overlayView.findViewById(R.id.stopdownload).setOnClickListener(this);
        clipboardManager= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onPrimaryClipChanged() {
        address=clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        if(address.startsWith("https://www.instagram.com/p/")) {
           WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
           layoutParams.gravity= Gravity.CENTER;
            windowManager.addView(overlayView, layoutParams);


        }
        else
            Toast.makeText(this,"Your link is not an instagram address page",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.download)
            new DownloadAsyncTask(this).execute(address);
        else
            Toast.makeText(this,"Download cancelled",Toast.LENGTH_LONG).show();
        Toast.makeText(this, ((TextView) v).getText().toString()+" clicked",Toast.LENGTH_LONG).show();
        windowManager.removeView(overlayView);
    }
}
