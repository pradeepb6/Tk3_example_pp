package com.groupx.fliege;

/**
 * Created by prade on 5/16/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.umundo.core.Discovery;
import org.umundo.core.Message;
import org.umundo.core.Node;
import org.umundo.core.Publisher;
import org.umundo.core.Receiver;
import org.umundo.core.Subscriber;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * Getting it to run:
 * <p/>
 * 1. Replace the umundo.jar in the libs folder by the one from the intaller and
 * add it to the classpath. Make sure to take the umundo.jar for Android, as you
 * are not allowed to have JNI code within and the desktop umundo.jar includes all
 * supported JNI libraries.
 * <p/>
 * 2. Replace the JNI library libumundoNativeJava.so (or the debug variant) into libs/armeabi/
 * <p/>
 * 3. Make sure System.loadLibrary() loads the correct variant.
 * <p/>
 * 4. Make sure you have set the correct permissions:
 * <uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 *
 * @author sradomski
 */

class ImageDimensions implements Serializable {
    int leftMargin;
    int topMargin;

    int divHeight;
    int divWidth;

    public int getDivWidth() {
        return divWidth;
    }

    public void setDivWidth(int divWidth) {
        this.divWidth = divWidth;
    }

    public int getDivHeight() {
        return divHeight;
    }

    public void setDivHeight(int divHeight) {
        this.divHeight = divHeight;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public void randomiseImagePosition() {
        Random r = new Random();
        int rLeft = r.nextInt(getDivWidth());
        int rTop = r.nextInt(getDivHeight() - 220);

        final int imageSize = 40 * getDivHeight() / 640;
        Log.i("Height:", String.valueOf(getDivHeight()));

        if (rLeft >= (getDivWidth() - imageSize)) {
            rLeft -= imageSize;
        } else if (rLeft <= imageSize) {
            rLeft += imageSize;
        }
        if (rTop > getDivHeight() - imageSize) {
            rTop += imageSize;
        } else if (rTop < imageSize) {
            rTop = imageSize;
        }
        this.setLeftMargin(rLeft);
        this.setTopMargin(rTop);
    }


    public byte[] serialize() {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(this);
            return b.toByteArray();
        } catch (IOException ioe) {
            //Handle logging exception
            return null;
        }
    }

    public Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return o.readObject();
        } catch (Exception e) {
            //Handle logging exception
            return null;
        }
    }


}

public class fliegeRun extends Activity {

    Discovery disc;
    Node node;
    Publisher fooPub;
    Subscriber fooSub;

    ImageView imageView;
    ImageDimensions imageDimensions;


    public void setRelativeImagePosition(ImageDimensions newImageDims) {


        float devHeight = imageDimensions.getDivHeight();
        float devWidth = imageDimensions.getDivWidth();

        float scaleRatioHeight, scaleRatioWidth;
        scaleRatioHeight = devHeight / newImageDims.getDivHeight();
        scaleRatioWidth = devWidth / newImageDims.getDivWidth();

        imageDimensions.setLeftMargin((int) (newImageDims.getLeftMargin() * scaleRatioWidth));
        imageDimensions.setTopMargin((int) (newImageDims.getTopMargin() * scaleRatioHeight));
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String username = intent.getStringExtra("username");

        System.out.println("username = " + username);
        setTitle("FliegeRun_" + username);

        imageDimensions = new ImageDimensions();
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.insect);

        imageView.setLayoutParams(new LinearLayout.LayoutParams(40 * getApplicationContext().getResources().getDisplayMetrics().heightPixels / 640, 40 * getApplicationContext().getResources().getDisplayMetrics().heightPixels / 640));
        Log.i("get x intitially", "" + imageView.getX());
        Log.i("get t intitially", "" + imageView.getY());
        //adding view to layout
        linearLayout.addView(imageView);
        //make visible to program
        setContentView(linearLayout);
//        setContentView(imageView);
        imageDimensions.setDivWidth(getApplicationContext().getResources().getDisplayMetrics().widthPixels);
        imageDimensions.setDivHeight(getApplicationContext().getResources().getDisplayMetrics().heightPixels);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock mcLock = wifi.createMulticastLock("mylock");
            mcLock.acquire();
            // mcLock.release();
        } else {
            Log.v("android-umundo", "Cannot get WifiManager");
        }

//		System.loadLibrary("umundoNativeJava");
        System.loadLibrary("umundoNativeJava_d");


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getX() == imageView.getX())
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Pradeep X= " + motionEvent.getX());
                    System.out.println("Pradeep Y= " + motionEvent.getY());
                    imageDimensions.randomiseImagePosition();
//                    imageView.setLayoutParams(new LinearLayout.LayoutParams(imageDimensions.getLeftMargin(),imageDimensions.getTopMargin()));
                    imageView.setX(imageDimensions.getLeftMargin());
                    imageView.setY(imageDimensions.getTopMargin());

                    Log.i("get x random", "" + imageView.getX());
                    Log.i("get t random", "" + imageView.getY());

                    fooPub.send(imageDimensions.serialize());
                }
                return true;
            }
        });

        disc = new Discovery(Discovery.DiscoveryType.MDNS);

        node = new Node();
        disc.add(node);

        fooPub = new Publisher("pingpong");
        node.addPublisher(fooPub);

        fooSub = new Subscriber("pingpong", new TestReceiver());
        node.addSubscriber(fooSub);

//        testPublishing = new Thread(new TestPublishing());
//        testPublishing.start();
    }

    public class TestReceiver extends Receiver {
        public void receive(Message msg) {
            ImageDimensions newImageDims;
            newImageDims = (ImageDimensions) imageDimensions.deserialize(msg.getData());
            setRelativeImagePosition(newImageDims);
            fliegeRun.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setX(imageDimensions.getLeftMargin());
                    imageView.setY(imageDimensions.getTopMargin());
                }
            });
        }
    }
}
