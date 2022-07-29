package rajy.oussama.qcm;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//import com.chaquo.python.PyObject;
//import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;
import com.example.chyface.R;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.entity.UrlEncodedFormEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Activity_Ajouter extends AppCompatActivity {
    Bitmap selectedimage;
    Button sele_im, btn_enr;
    ImageView img;
    Bitmap bitmap;
    String imageString = "";

    EditText txt_nom, txt_answers;

    String ImageString_fromFlask_list = "";

   // public Python py;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter);

        sele_im = (Button) findViewById(R.id.btn_select);
        btn_enr = (Button) findViewById(R.id.btn_enregistrer);
        img = (ImageView) findViewById(R.id.imageView);

        txt_nom = (EditText) findViewById(R.id.editText_nom);
        txt_answers = (EditText) findViewById(R.id.editText_answers);
//        if(!Python.isStarted())
//            Python.start(new AndroidPlatform(this));
//        py= Python.getInstance();
        btn_enr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CorrTask().execute();
            }
        });

    }

    public void selectImage(View view){


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }else{
            Intent intenttogaleri = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          //0  intenttogaleri.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(intenttogaleri,2);

        }

    }
//
    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);//burdan hata çıkabilir
        return encodedImage;

    }

    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();

            try {

                if(Build.VERSION.SDK_INT >= 28){
                    int count = 0;
                    try {
                        count = data.getClipData().getItemCount();
                    }
                    catch(Exception ex){

                    }

                        //do something with the image (save it to some directory or whatever you need to do with it here)

                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                        selectedimage = ImageDecoder.decodeBitmap(source);
                        img.setImageBitmap(selectedimage);




                }
                else{
                    selectedimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    img.setImageBitmap(selectedimage);


                }

            } catch (IOException e) {
                e.printStackTrace();

            }



        }

    }

    private class CorrTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... urls) {

            Bitmap bmp = null;
            try {



                    bitmap = selectedimage; //drawable.getBitmap();
                    imageString = getStringImage(bitmap);
                    //PyObject pyo = py.getModule("myscript");

                    //PyObject obj = pyo.callAttr("list_answers", imageString);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(server.Url_server+"list-answers");

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("image_list", imageString));




                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                } catch (UnsupportedEncodingException e) {

                    e.printStackTrace();
                }

                HttpResponse response;
                HttpResponse response2;
                int status = 0;
                try {
                    response = httpClient.execute(httpPost);
                    status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    ImageString_fromFlask_list = convertStreamToString(is);

                    //




                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String str = ImageString_fromFlask_list;

                    if (!str.equals("Error Image")) {


                        //txt_answers.setText(str);
                        ConfigDB conf = new ConfigDB(getApplicationContext());
                        Module mod = new Module();
                        mod.setDesignation(txt_nom.getText().toString());
                        mod.setAnswers(str);
                        conf.InsertModule(mod);
                        // im1.setImageBitmap(bmp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Les information du module a été Ajoutés avec succès"+str, Toast.LENGTH_SHORT).show();
                                txt_answers.getText().clear();
                                txt_nom.getText().clear();
                                img.setVisibility(View.INVISIBLE);
                            }
                        });
                        Random r = new Random();
                        //saveImage(bmp, "qcm"+r.nextInt()+".jpg");

                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error Image : Éviter les images qui contiennent des ombres", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }


            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }

        protected void onProgressUpdate(Void... progress) {


        }

        protected void onPostExecute(Void result) {

        }
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}