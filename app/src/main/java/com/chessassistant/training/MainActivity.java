package com.chessassistant.training;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("Chess AI Assistant\nUygulama basariyla acildi!");
        text.setTextSize(22);
        text.setGravity(Gravity.CENTER);

        setContentView(text);
    }
}
