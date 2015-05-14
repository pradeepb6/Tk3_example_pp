package org.umundo.samples.pingpong;

import org.umundo.core.Discovery;
import org.umundo.core.Discovery.DiscoveryType;
import org.umundo.core.Message;
import org.umundo.core.Node;
import org.umundo.core.Publisher;
import org.umundo.core.Receiver;
import org.umundo.core.Subscriber;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Getting it to run:
 * 
 * 1. Replace the umundo.jar in the libs folder by the one from the intaller and 
 * add it to the classpath. Make sure to take the umundo.jar for Android, as you 
 * are not allowed to have JNI code within and the desktop umundo.jar includes all 
 * supported JNI libraries.
 * 
 * 2. Replace the JNI library libumundoNativeJava.so (or the debug variant) into libs/armeabi/
 * 
 * 3. Make sure System.loadLibrary() loads the correct variant.
 * 
 * 4. Make sure you have set the correct permissions:
 * 	  <uses-permission android:name="android.permission.INTERNET"/>
 *		<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
 *   	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 * 
 * @author sradomski
 *
 */
public class UMundoPingPongActivity extends Activity {

	TextView tv;
	Thread testPublishing;
	Discovery disc;
	Node node;
	Publisher fooPub;
	Subscriber fooSub;

	ImageView imageView;

	public class TestPublishing implements Runnable {

		@Override
		public void run() {
			String message = "This is foo from android";
			while (testPublishing != null) {
				fooPub.send(message.getBytes());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				UMundoPingPongActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv.setText(tv.getText() + "o");
					}
				});
			}
		}
	}

	public class TestReceiver extends Receiver {
		public void receive(Message msg) {

			for (String key : msg.getMeta().keySet()) {
				Log.i("umundo", key + ": " + msg.getMeta(key));
			}
			UMundoPingPongActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
						tv.setText(tv.getText() + "i");
				}
			});
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tv = new TextView(this);
		tv.setText("");
		setContentView(tv);
		Random r = new Random();







		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			MulticastLock mcLock = wifi.createMulticastLock("mylock");
			mcLock.acquire();
			// mcLock.release();
		} else {
			Log.v("android-umundo", "Cannot get WifiManager");
		}

//		System.loadLibrary("umundoNativeJava");
		System.loadLibrary("umundoNativeJava_d");

		disc = new Discovery(DiscoveryType.MDNS);
    
		node = new Node();
		disc.add(node);

		fooPub = new Publisher("pingpong");
		node.addPublisher(fooPub);
    
		fooSub = new Subscriber("pingpong", new TestReceiver());
		node.addSubscriber(fooSub);
    
		testPublishing = new Thread(new TestPublishing());
		testPublishing.start();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_umundo_android, container, false);
		RelativeLayout layout= (RelativeLayout) view.findViewById(R.id.relative);
		ImageView picture = new ImageView(this);
		picture.setImageResource(R.drawable.insect);
		layout.addView(picture);
		return view;
	}
}
