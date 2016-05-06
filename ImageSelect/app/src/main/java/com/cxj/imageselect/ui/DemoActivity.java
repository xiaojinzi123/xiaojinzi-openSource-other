package com.cxj.imageselect.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


import com.cxj.imageselect.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import xiaojinzi.base.android.image.ImageUtil;
import xiaojinzi.base.android.log.L;
import xiaojinzi.base.android.store.FileUtil;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    public void clickView(View view) {
        Intent i = new Intent(this, SelectLocalImageAct.class);
        startActivityForResult(i, 0);
//        String path = "/storage/emulated/0/imageLoadCache/104/111/97/103/19/8a11369862f193daaafe29332a8a1b70.jpg";
//        File f = new File(path);
//
//        try {
//            Bitmap bitmap = ImageUtil.decodeLocalImage(path, 100, 100);
//            L.s("b = " + bitmap);
//        } catch (Exception e) {
//            L.s("挂了");
//            e.printStackTrace();
//        }

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
