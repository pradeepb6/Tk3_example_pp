package com.groupx.fliege;

/*
* This will first show up when the application is laoded and it takes the player name and pass it to game.
* */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class PlayerName extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        Button enterButton = (Button) findViewById(R.id.button);
        final Intent nextIntent = new Intent(getApplicationContext(), fliegeRun.class);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editText.getText().toString();
                if (username.matches("") || username.matches("Username")) {
                    Toast.makeText(getApplicationContext(), "Enter a valid username", Toast.LENGTH_SHORT).show();
                    return;
                }
                nextIntent.putExtra("username", username);
                startActivity(nextIntent);

            }
        });

    }


}
