package org.umundo.samples.pingpong;

import android.app.Activity;
import android.graphics.Point;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Random;


public class MainActivity extends Activity {
    private static final String TAG = "MyActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Random r = new Random();
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x -250;
        final int height = size.y -600;

        Log.i(TAG+" Width",""+width);
        Log.i(TAG+" height", "" + height);

        final ImageView imageView;
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.insect);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(120, 120);
        layoutParams.leftMargin = r.nextInt(width);
        layoutParams.topMargin = r.nextInt(height);

        Log.i("leftMargin",""+layoutParams.leftMargin);
        Log.i("rightMargin",""+layoutParams.topMargin);
        imageView.setLayoutParams(layoutParams);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageView.getLayoutParams());
//                layoutParams.leftMargin = r.nextInt(width);
//                layoutParams.topMargin = r.nextInt(height);
                layoutParams.setMargins(r.nextInt(width),r.nextInt(height),0,0);
                imageView.setImageResource(R.drawable.insect);

                Log.i(TAG+" leftMargin",""+layoutParams.leftMargin);
                Log.i(TAG+" topMargin",""+layoutParams.topMargin);
                Log.i(TAG+" rightMargin",""+layoutParams.rightMargin);
                Log.i(TAG+" bottomMargin",""+layoutParams.bottomMargin);
                imageView.setLayoutParams(layoutParams);
            }
        });

    }


}
