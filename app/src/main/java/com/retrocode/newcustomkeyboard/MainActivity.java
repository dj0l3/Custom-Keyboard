package com.retrocode.newcustomkeyboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private CustomKeyboardView customKeyboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        customKeyboardView = (CustomKeyboardView) findViewById(R.id.custom_keyboard_view);

        final EditText secondEditText = (EditText) findViewById(R.id.edittext1);


        customKeyboardView.setCallback(new CustomKeyboardView.CustomKeyboardCallback() {
            @Override
            public void okPressed() {
                secondEditText.setText("Ok pressed");
            }
        });

        customKeyboardView.registerEditText(secondEditText);

        onButtonClick();

        findViewById(R.id.crash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();

            }
        });
    }

    private void onButtonClick() {
        throw new RuntimeException("This is a crash");
    }

    @Override
    public void onBackPressed() {
        if (customKeyboardView.isCustomKeyboardVisible()) {
            customKeyboardView.hideCustomKeyboard();
        } else {
            finish();
        }
    }

}