package com.chessassistant.training;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    LinearLayout root;GridLayout boardView;TextView status;Button[] cells=new Button[64];
    @Override public void onCreate(Bundle b){super.onCreate(b);build();}
    TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(30,40,55));t.setGravity(Gravity.CENTER);t.setPadding(8,12,8,12);return t;}
    Button button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->action.run());return b;}
    void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(10,12,10,12);root.setBackgroundColor(Color.rgb(245,247,250));setContentView(root);
        root.addView(label("♟ Chess AI Assistant",24));
        root.addView(label("Antrenman tahtası • Taşı seç, hedef kareye dokun",14));
        status=label("",16);root.addView(status);
        boardView=new GridLayout(this);boardView.setColumnCount(8);boardView.setRowCount(8);root.addView(boardView);
        
int side = (int) (getResources().getDisplayMetrics().widthPixels - 20 * getResources().getDisplayMetrics().density);
int cell = side / 8;

        for(int i=0;i<64;i++){
            final int pos=i;Button b=new Button(this);cells[i]=b;b.setAllCaps(false);b.setTextSize(26);b.setPadding(0,0,0,0);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(GridLayout.spec(i/8),GridLayout.spec(i%8));lp.width=cell;lp.height=cell;lp.setMargins(0,0,0,0);boardView.addView(b,lp);b.setOnClickListener(v->tap(pos));
        }
        LinearLayout buttons=new LinearLayout(this);buttons.setOrientation(LinearLayout.HORIZONTAL);root.addView(buttons);
        Button suggest=button("Hamle öner",()->{status.setText("Hesaplanıyor...");new Thread(()->{String s=Chess.recommend();runOnUiThread(()->{status.setText("Öneri: "+s);OverlayService.refresh();});}).start();});buttons.addView(suggest,new LinearLayout.LayoutParams(0,-2,1));
        Button reset=button("Sıfırla",()->{Chess.reset();refresh();});buttons.addView(reset,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(button("Yüzen pencereyi aç",()->{
            if(!Settings.canDrawOverlays(this)){Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));startActivity(intent);Toast.makeText(this,"Diğer uygulamaların üzerinde göster iznini ver, sonra tekrar dokun",Toast.LENGTH_LONG).show();return;}
            startService(new Intent(this,OverlayService.class));Toast.makeText(this,"Yüzen pencere açıldı",Toast.LENGTH_SHORT).show();
        }));
        root.addView(label("Not: Bu sürümde motor basit yerleşik analiz kullanır; Stockfish değildir. Rok ve geçerken alma henüz desteklenmiyor. Yalnızca antrenman amaçlıdır.",12));
        refresh();
    }
    void tap(int p){if(Chess.selected<0){if(Chess.own(Chess.board[p],Chess.white)){Chess.selected=p;refresh();}return;}
        int from=Chess.selected;Chess.selected=-1;if(from==p){refresh();return;}
        if(!Chess.play(from,p)&&Chess.own(Chess.board[p],Chess.white))Chess.selected=p;refresh();OverlayService.refresh();
    }
    void refresh(){status.setText((Chess.white?"Beyaz":"Siyah")+" oynar • "+Chess.suggestion);
        String w="♔♕♖♗♘♙",bl="♚♛♜♝♞♟";
        for(int i=0;i<64;i++){char p=Chess.board[i];int index="KQRBNP".indexOf(Character.toUpperCase(p));cells[i].setText(p=='.'?"":String.valueOf((Chess.isWhite(p)?w:bl).charAt(index)));cells[i].setTypeface(null,Typeface.NORMAL);cells[i].setTextColor(Color.rgb(20,30,35));cells[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(i==Chess.selected?Color.rgb(255,209,77):((i/8+i%8)%2==0?Color.rgb(238,238,210):Color.rgb(118,150,86))));}
    }
}
