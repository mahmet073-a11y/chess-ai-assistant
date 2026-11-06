package com.chessassistant.training;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;private LinearLayout box;private WindowManager.LayoutParams params;private static TextView result;
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId){if(box!=null)return START_STICKY;
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        params=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;params.x=20;params.y=160;
        box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(16,10,16,10);box.setBackgroundColor(0xEE202A36);
        TextView title=new TextView(this);title.setText("♟ Chess AI • sürükle");title.setTextColor(-1);title.setTextSize(15);box.addView(title);
        result=new TextView(this);result.setText(Chess.suggestion);result.setTextColor(-1);result.setTextSize(18);box.addView(result);
        LinearLayout actions=new LinearLayout(this);box.addView(actions);
        Button suggest=new Button(this);suggest.setText("Öneri");actions.addView(suggest);suggest.setOnClickListener(v->{result.setText("Hesaplanıyor...");new Thread(()->{Chess.recommend();new android.os.Handler(getMainLooper()).post(OverlayService::refresh);}).start();});
        Button open=new Button(this);open.setText("Tahta");actions.addView(open);open.setOnClickListener(v->{Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);});
        Button close=new Button(this);close.setText("×");actions.addView(close);close.setOnClickListener(v->stopSelf());
        title.setOnTouchListener(new android.view.View.OnTouchListener(){int x,y;float sx,sy;@Override public boolean onTouch(android.view.View v,MotionEvent e){switch(e.getAction()){case MotionEvent.ACTION_DOWN:x=params.x;y=params.y;sx=e.getRawX();sy=e.getRawY();return true;case MotionEvent.ACTION_MOVE:params.x=x+(int)(e.getRawX()-sx);params.y=y+(int)(e.getRawY()-sy);wm.updateViewLayout(box,params);return true;}return false;}});
        wm.addView(box,params);return START_STICKY;
    }
    static void refresh(){if(result!=null)result.post(()->result.setText(Chess.suggestion));}
    @Override public void onDestroy(){if(box!=null){wm.removeView(box);box=null;}result=null;super.onDestroy();}
}
