
package com.chessassistant.training;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 50, 35, 20);

        TextView title = new TextView(this);
        title.setText("Chess AI Assistant");
        title.setTextSize(25);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText(
            "Satranç antrenmanı için yüzen pencere."
        );
        description.setTextSize(16);
        layout.addView(description);

        Button start = new Button(this);
        start.setText("Yüzen Pencereyi Aç");
        layout.addView(start);

        Button stop = new Button(this);
        stop.setText("Yüzen Pencereyi Kapat");
        layout.addView(stop);

        setContentView(layout);

        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivity(intent);
            } else {
                startService(
                    new Intent(this, OverlayService.class)
                );
            }
        });

        stop.setOnClickListener(v -> {
            stopService(
                new Intent(this, OverlayService.class)
            );
        });
    }
                                 }
