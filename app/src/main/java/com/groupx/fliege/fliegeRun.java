package com.groupx.fliege;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Getting it to run:
 * <p>
 * 1. Replace the umundo.jar in the libs folder by the one from the intaller and
 * add it to the classpath. Make sure to take the umundo.jar for Android, as you
 * are not allowed to have JNI code within and the desktop umundo.jar includes all
 * supported JNI libraries.
 * <p>
 * 2. Replace the JNI library libumundoNativeJava.so (or the debug variant) into libs/armeabi/
 * <p>
 * 3. Make sure System.loadLibrary() loads the correct variant.
 * <p>
 * 4. Make sure you have set the correct permissions:
 * <uses-permission android:name="android.permission.INTERNET"/>
 * <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 *
 */

class Player implements Serializable {

    private String playerName;
    private int Score;

    public Player() {
        setPlayerName(new String());
        setScore(0);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }
}

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
}

class FliegeScore implements Serializable {

    private ArrayList<Player> players;
    private ImageDimensions newImagePosition;

    public FliegeScore() {

        players = new ArrayList<Player>(10);
        newImagePosition = new ImageDimensions();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }


    public byte[] serialize() {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(this);
            return b.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }

    public Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return o.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public ImageDimensions getNewImagePosition() {
        return newImagePosition;
    }

    public void setNewImagePosition(ImageDimensions newImagePosition) {
        this.newImagePosition = newImagePosition;
    }
}


public class fliegeRun extends Activity {

    Discovery disc;
    Node node;
    Publisher fooPub;
    Subscriber fooSub;

    ImageView imageView;
    ImageDimensions imageDimensions;

    Player player;
    FliegeScore fliegeScore;


    public void setRelativeImagePosition(ImageDimensions newImageDims) {


        float devHeight = imageDimensions.getDivHeight();
        float devWidth = imageDimensions.getDivWidth();

        float scaleRatioHeight, scaleRatioWidth;
        scaleRatioHeight = devHeight / newImageDims.getDivHeight();
        scaleRatioWidth = devWidth / newImageDims.getDivWidth();

        imageDimensions.setLeftMargin((int) (newImageDims.getLeftMargin() * scaleRatioWidth));
        imageDimensions.setTopMargin((int) (newImageDims.getTopMargin() * scaleRatioHeight));
    }

    public void updatePlayerScoreForUsername(String username) {
        for (Player P : fliegeScore.getPlayers()) {
            if (P.getPlayerName().compareTo(username) == 0) {
                P.setScore(P.getScore() + 1);
            }
        }
    }

    public void removePlayerFromPlayerListForPlayerName(String playerName) {

        for (Player P : fliegeScore.getPlayers()) {
            if (P.getPlayerName().compareTo(playerName) == 0) {
                fliegeScore.getPlayers().remove(P);
            }
        }

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        final String username = intent.getStringExtra("username");
        player = new Player();
        fliegeScore = new FliegeScore();

        player.setPlayerName(username);
        fliegeScore.getPlayers().add(player);

        System.out.println("username = " + username);
        setTitle("FliegeRun_" + username);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String scorecard = "ScoreCard\n";
                for (Player P : fliegeScore.getPlayers()) {
                    scorecard += P.getPlayerName();
                    scorecard += " - ";
                    scorecard += String.valueOf(P.getScore());
                    scorecard += "\n";
                }


                Toast.makeText(getApplicationContext(), scorecard,
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    imageDimensions.randomiseImagePosition();
//                    imageView.setLayoutParams(new LinearLayout.LayoutParams(imageDimensions.getLeftMargin(),imageDimensions.getTopMargin()));
                    imageView.setX(imageDimensions.getLeftMargin());
                    imageView.setY(imageDimensions.getTopMargin());

                    updatePlayerScoreForUsername(username);
                    fliegeScore.setNewImagePosition(imageDimensions);
                    fooPub.send(fliegeScore.serialize());
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                fooPub.waitForSubscribers(1);
                fooPub.send(("JOINED"+username).getBytes());
            }
        }).start();
    }

    public void onStop() {

        super.onStop();
        fooPub.send(("LEAVE"+player.getPlayerName()).getBytes());
    }

    public void onDestroy() {
        super.onDestroy();
//        fooPub.send(("LEAVE" + player.getPlayerName()).getBytes());
    }

    private boolean doubleBackToExitPressedOnce;

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            this.finish();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit the game", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public class TestReceiver extends Receiver {
        public void receive(Message msg) {
            ImageDimensions newImageDims;
            final FliegeScore newFliegeScore;
            try {
                //Log.d("JOINED","NODE");
                String recvmessage = new String(msg.getData(), "UTF-8");
                if (recvmessage.contains("JOINED")) {
                    String username = recvmessage.replace("JOINED", "");
                    Log.d("JOINED", username);
                    Player newPlayer = new Player();
                    newPlayer.setPlayerName(username);
                    fliegeScore.getPlayers().add(newPlayer);
                    fooPub.send(fliegeScore.serialize());
                } else if (recvmessage.contains("LEAVE")) {
                    String username = recvmessage.replace("LEAVE", "");
                    Log.d("LEAVE", username);
                    removePlayerFromPlayerListForPlayerName(username);
                    fooPub.send(fliegeScore.serialize());
                } else {

                    newFliegeScore = (FliegeScore) fliegeScore.deserialize(msg.getData());
                    //newImageDims = (ImageDimensions) imageDimensions.deserialize(msg.getData());
                    setRelativeImagePosition(newFliegeScore.getNewImagePosition());
                    fliegeRun.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setX(imageDimensions.getLeftMargin());
                            imageView.setY(imageDimensions.getTopMargin());
                            fliegeScore.setPlayers(newFliegeScore.getPlayers());
                        }
                    });
                    for (Player P : fliegeScore.getPlayers()) {
                        Log.d("PlayerScore&Score", P.getPlayerName() + ":" + P.getScore());
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
