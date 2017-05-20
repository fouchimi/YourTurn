package com.social.yourturn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ConfirmAmountActivity extends AppCompatActivity {

    private static ConfirmAmountActivity instance = null;

    protected ConfirmAmountActivity() {}

    public static ConfirmAmountActivity getInstance() {
        if(instance == null) {
            instance = new ConfirmAmountActivity();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_amount);
    }
}
