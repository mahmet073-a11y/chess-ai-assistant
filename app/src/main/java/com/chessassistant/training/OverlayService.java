package com.chessassistant.training;

import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private LinearLayout panel;
    private WindowManager.LayoutParams params;
    private TextView status;
    private final Button[] squares = new Button[64];
    private int cellDp = 36;
    private static OverlayService instance;

    @Override public IBinder onBind(Intent intent) { return null; }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (panel != null) { render(); return START_STICKY; }
        instance = this;
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(-2, -2,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(12); params.y = dp(110);
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(5), dp(5), dp(5), dp(7));
        panel.setBackgroundColor(0xF5222B37);

        TextView title = new TextView(this);
        title.setText("♟ Satranç tahtası  •  Sürükle");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(5), 0, dp(5));
        panel.addView(title);
        title.setOnTouchListener(new android.view.View.OnTouchListener() {
            int ox, oy; float sx, sy;
            @Override public boolean onTouch(android.view.View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ox=params.x; oy=params.y; sx=e.getRawX(); sy=e.getRawY(); return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x=ox+(int)(e.getRawX()-sx);
                        params.y=oy+(int)(e.getRawY()-sy);
                        wm.updateViewLayout(panel,params); return true;
                    case MotionEvent.ACTION_UP: return true;
                }
                return false;
            }
        });

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        panel.addView(status);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(8); grid.setRowCount(8);
        panel.addView(grid);
        int availableDp = (int)(getResources().getDisplayMetrics().widthPixels /
            getResources().getDisplayMetrics().density) - 36;
        cellDp = Math.min(38, Math.max(24, availableDp / 8));
        for (int i=0; i<64; i++) {
            final int pos=i;
            Button b=new Button(this);
            b.setAllCaps(false);
            b.setTextSize(cellDp < 30 ? 16 : 21);
            b.setPadding(0,0,0,0);
            b.setMinWidth(0); b.setMinimumWidth(0);
            b.setMinHeight(0); b.setMinimumHeight(0);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(
                GridLayout.spec(i/8),GridLayout.spec(i%8));
            lp.width=dp(cellDp); lp.height=dp(cellDp);
            lp.setMargins(0,0,0,0);
            grid.addView(b,lp);
            squares[i]=b;
            b.setOnClickListener(v -> onSquare(pos));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        panel.addView(actions);
        Button suggest=new Button(this); suggest.setText("Öneri"); actions.addView(suggest);
        suggest.setOnClickListener(v -> {
            status.setText("Hesaplanıyor...");
            new Thread(() -> {
                Chess.recommend();
                new Handler(getMainLooper()).post(this::render);
            }).start();
        });
        Button reset=new Button(this); reset.setText("Yeni"); actions.addView(reset);
        reset.setOnClickListener(v -> { Chess.reset(); render(); });
        Button close=new Button(this); close.setText("×"); actions.addView(close);
        close.setOnClickListener(v -> stopSelf());
        wm.addView(panel,params);
        render();
        return START_STICKY;
    }

    private void onSquare(int pos) {
        if (Chess.selected < 0) {
            if (Chess.own(Chess.board[pos], Chess.white)) Chess.selected=pos;
        } else {
            int from=Chess.selected;
            Chess.selected=-1;
            if (from!=pos && !Chess.play(from,pos) && Chess.own(Chess.board[pos],Chess.white))
                Chess.selected=pos;
        }
        render();
    }

    private void render() {
        if (panel == null) return;
        status.setText((Chess.white ? "Beyaz" : "Siyah") + " oynar • " + Chess.suggestion);
        String white="♔♕♖♗♘♙", black="♚♛♜♝♞♟";
        for (int i=0;i<64;i++) {
            char p=Chess.board[i];
            int idx="KQRBNP".indexOf(Character.toUpperCase(p));
            squares[i].setText(p=='.' ? "" : String.valueOf(
                (Chess.isWhite(p)?white:black).charAt(idx)));
            squares[i].setTextColor(Color.BLACK);
            int bg=i==Chess.selected ? 0xFFFFD34D :
                ((i/8+i%8)%2==0 ? 0xFFEEEED2 : 0xFF769656);
            squares[i].setBackgroundTintList(ColorStateList.valueOf(bg));
        }
    }

    public static void refresh() {
        OverlayService s=instance;
        if (s!=null && s.panel!=null) s.panel.post(s::render);
    }

    @Override public void onDestroy() {
        if (panel!=null) { wm.removeView(panel); panel=null; }
        instance=null;
        super.onDestroy();
    }
}
