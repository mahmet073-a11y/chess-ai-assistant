
package com.chessassistant.training;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private FrameLayout floatingWindow;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager)
            getSystemService(WINDOW_SERVICE);

        floatingWindow = new FrameLayout(this);

        WebView chessBoard = new WebView(this);
        chessBoard.getSettings().setJavaScriptEnabled(true);
        chessBoard.loadUrl(
            "file:///android_asset/chess.html"
        );

        floatingWindow.addView(chessBoard);

        TextView closeButton = new TextView(this);
        closeButton.setText("  X  ");
        closeButton.setTextSize(22);
        closeButton.setTextColor(0xFFFF0000);
        closeButton.setBackgroundColor(0xFFFFFFFF);

        FrameLayout.LayoutParams closeParams =
            new FrameLayout.LayoutParams(
                -2,
                -2,
                Gravity.TOP | Gravity.RIGHT
            );

        floatingWindow.addView(
            closeButton,
            closeParams
        );

        closeButton.setOnClickListener(
            v -> stopSelf()
        );

        WindowManager.LayoutParams params =
            new WindowManager.LayoutParams(
                330,
                470,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 20;
        params.y = 160;

        windowManager.addView(
            floatingWindow,
            params
        );
    }

    @Override
    public int onStartCommand(
        Intent intent, int flags, int startId
    ) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (floatingWindow != null) {
            windowManager.removeView(floatingWindow);
            floatingWindow = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
