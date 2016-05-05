package com.cxj.imageselect.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.cxj.imageselect.R;

import xiaojinzi.base.android.log.L;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    public void clickView(View view) {
        Intent i = new Intent(this, SelectLocalImageAct.class);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String[] arr = data.getStringArrayExtra(SelectLocalImageAct.RETURN_DATA_FLAG);
            if (arr != null) {
                for(int i = 0; i < arr.length; i++) {
                    String path = arr[i];
                    System.out.println("path = " + path);
                }
            }
        }
    }
}
