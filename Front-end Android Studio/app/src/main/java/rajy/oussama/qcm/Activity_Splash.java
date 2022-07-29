package rajy.oussama.qcm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.example.chyface.R;

public class Activity_Splash extends AppCompatActivity {

    ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.quiz);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    connected = true;
                    Intent prepare = new Intent(Activity_Splash.this, ChoixActivity.class);
                    startActivity(prepare);
                }
                else {
                    connected = false;

                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Splash.this);
                    builder.setCancelable(false).setMessage("Vérifier votre connexion").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    AlertDialog alertDialog =  builder.create();
                    alertDialog.show();
                }


            }
        },3500);
    }
}