package rajy.oussama.qcm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.example.chyface.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChoixActivity extends AppCompatActivity {
VideoView vd;
Button btn_tele, btn_ajouter, btn_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_choix);
        vd = (VideoView) findViewById(R.id.videoView);

        //
        btn_tele = (Button)findViewById(R.id.btn_tele);
        btn_ajouter = (Button)findViewById(R.id.btn_ajouter);
        btn_list = (Button) findViewById(R.id.btn_list);
        // button telecharger
        btn_tele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://drive.google.com/file/d/10EQdyZRyTGjeGtCITywCVoXQQ-ksKduf/"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);


            }
        });
        // button ajouter
        btn_ajouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent acAjou = new Intent(ChoixActivity.this, Activity_Ajouter.class);
                startActivity(acAjou);
            }
        });
        // button liste
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
Intent list_module = new Intent(ChoixActivity.this, Activity_List.class);
startActivity(list_module);
            }
        });
        //
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.background_video);
        vd.setVideoURI(video);

        vd.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vd.start();
                mp.setLooping(true);
            }
        });
    }

    private File exportFile(File src, File dst) throws IOException {

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File expFile = new File(dst.getPath() + File.separator + "MODEL_" + timeStamp + ".pdf");
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        return expFile;
    }
}