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
 * Player class
 *
 * */
class Player implements Serializable {

    private String playerName;   //player name
    private int Score;          // player score

    public Player() {
        setPlayerName(new String());
        setScore(0);
    }

//    Getter and setter methods
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

/**
 * This class helps positioning the image.
 * */
class ImageDimensions implements Serializable {
    int leftMargin;         //position of image w.r.t  left margin
    int topMargin;          //position of image w.r.t  top margin
    int divHeight;          // Device height
    int divWidth;           // Device Width

    //Getter and setter methods
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

    /**
     * This method will position the image randomly.
     * */
    public void randomiseImagePosition() {
        Random r = new Random();

        //Randomizing left margin
        int rLeft = r.nextInt(getDivWidth());

        //Randomizing top margin and ignore the titlebar space
        int rTop = r.nextInt(getDivHeight() - 220);

        // setting image size w.r.t screen pixels
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

/**
 * Wrapper class which has player list and image position.
 * */
class FliegeScore implements Serializable {

    private ArrayList<Player> players;
    private ImageDimensions newImagePosition;

    public FliegeScore() {

        players = new ArrayList<Player>(10);
        newImagePosition = new ImageDimensions();
    }

    //Getter and setter methods.
    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public ImageDimensions getNewImagePosition() {
        return newImagePosition;
    }

    public void setNewImagePosition(ImageDimensions newImagePosition) {
        this.newImagePosition = newImagePosition;
    }

    /**
     * Serialize method for serializing the object of FliegeScore class. This method creates a
     * serialized byte array of the object of class FliegeScore. This method is used before publishing
     * the data in uMundo.
     * @return serialized byte array which is used by the application for transforming this class
     * object into serialized byte array.
     * */
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
    /**
     * De-Serialize method for de-serializing the byte array to the object of FliegeScore class. This
     * method creates a serialized byte array of the object of class FliegeScore. This method is used
     * after receiving the data from uMundo.
     * @param bytes - byte array which is obtained from the receive class and is deserialized by
     *              this method and transformed into an object.
     * @return An object is returned after transforming a byte array into an object. This process is
     * called as de-serialization.
     * */
    public Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return o.readObject();
        } catch (Exception e) {
            return null;
        }
    }


}


public class fliegeRun extends Activity {

    Discovery disc;
    Node node;
    Publisher fooPub;
    Subscriber fooSub;

    //Initialize the variables.
    ImageView imageView;
    ImageDimensions imageDimensions;
    Player player;
    FliegeScore fliegeScore;
    private boolean doubleBackToExitPressedOnce;


    /**
     * This method will set the position of image in the view with the position received
     * from the publisher. This method will transform the coordinates revieved into device's
     * normalized coordinates.
     * */
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
 * This method is called to update the user score in the player list.
 * It is called whenever the touch event is occurred.
 * */
    public void updatePlayerScoreForUsername(String username) {
        for (Player P : fliegeScore.getPlayers()) {
            if (P.getPlayerName().compareTo(username) == 0) {
                P.setScore(P.getScore() + 1);
            }
        }
    }
    /**
     * This is the method called from the callback when a node leaves the network.
     * @param playerName This particular user will be removed from the player list and updated
     *                   to all the nodes in the network.
     *
     * */
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

        //set the player name
        player.setPlayerName(username);

        // add the new player to the player list
        fliegeScore.getPlayers().add(player);

        // set the title of the screen with username
        setTitle("FliegeRun_" + username);
        //This will keep the screen on when the game is running.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        imageDimensions = new ImageDimensions();
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.insect);
        //set the image size initially
        imageView.setLayoutParams(new LinearLayout.LayoutParams(40 * getApplicationContext().getResources().getDisplayMetrics().heightPixels / 640, 40 * getApplicationContext().getResources().getDisplayMetrics().heightPixels / 640));
        //adding view to layout
        linearLayout.addView(imageView);
        //make visible to program
        setContentView(linearLayout);

        // set the device height and width
        imageDimensions.setDivWidth(getApplicationContext().getResources().getDisplayMetrics().widthPixels);
        imageDimensions.setDivHeight(getApplicationContext().getResources().getDisplayMetrics().heightPixels);

        /**
         * Creating a wifi manager object for creating a multicast lock inorder to perform the
         * mDNS operations used by uMundo Library.
         * */
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock mcLock = wifi.createMulticastLock("mylock");
            mcLock.acquire();
            // mcLock.release();
        } else {
            Log.v("android-umundo", "Cannot get WifiManager");
        }

        /**
         * Loading the uMundoNative liibrary.
         * */
//		System.loadLibrary("umundoNativeJava");
        System.loadLibrary("umundoNativeJava_d");

        /**
         * This method will display the score of all the players when long clicked on the screen
         * */
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

        /**
         * This method will be called when the user click on the image.
         * */
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    imageDimensions.randomiseImagePosition();
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

        /** Creating a new node.*/
        node = new Node();
        /** Adding the created node into the discovery list.*/
        disc.add(node);

        /** Creating a publisher object with the channel name as fliege.*/
        fooPub = new Publisher("fliege");
        /** Adding the publisher to the node in the network which created few steps back.*/
        node.addPublisher(fooPub);

        /** Creating a Subscriber object with the channel name as fliege.*/
        fooSub = new Subscriber("fliege", new TestReceiver());
        /** Adding the subscriber to the node in the network which created few steps back.*/
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
        //Node will publish a message that it is leaving the network when user hits back button.
        fooPub.send(("LEAVE"+player.getPlayerName()).getBytes());
    }

    public void onDestroy() {
        super.onDestroy();
//        fooPub.send(("LEAVE" + player.getPlayerName()).getBytes());
    }


    //Check for double pressing back button before exiting the Game.
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
                String recvmessage = new String(msg.getData(), "UTF-8");
                //Check if User is Joined newly, then add to player list.
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
//                    fooPub.send(fliegeScore.serialize());
                } else {
                    newFliegeScore = (FliegeScore) fliegeScore.deserialize(msg.getData());
                    setRelativeImagePosition(newFliegeScore.getNewImagePosition());
                    fliegeRun.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setX(imageDimensions.getLeftMargin());
                            imageView.setY(imageDimensions.getTopMargin());
                            fliegeScore.setPlayers(newFliegeScore.getPlayers());
                        }
                    });
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
