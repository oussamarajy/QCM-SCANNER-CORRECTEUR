package rajy.oussama.qcm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.chaquo.python.PyObject;
//import com.chaquo.python.Python;
//import com.chaquo.python.android.AndroidPlatform;
import com.example.chyface.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.entity.UrlEncodedFormEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;


public class MainActivity extends AppCompatActivity {
    public Integer answers[] = {};
    private  static  final int GALLERY_REQUEST_CODE = 123;
    Button Btn, btn2, btn_aff, btn_sui, btn_back, btn_save, btn_vider;
    ImageView im1, load;
    BitmapDrawable drawable ;
    Bitmap bitmap;
    String imageString = "";
    Bitmap selectedimage;
    ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
    ArrayList<Bitmap> bitmapArray_corr = new ArrayList<Bitmap>();
    ArrayList<String> bitmapArray_names = new ArrayList<String>();
    ProgressBar bar;
    Integer index = 0;
    ArrayList<Integer> index_of_image_error = new ArrayList<>();

    String ImageString_fromFlask = "";
    String ImageString_fromFlask_name = "";

   // public Python py;


    Bitmap bmp = null;
    Bitmap bmp2 = null;

    TextView txt_reco, txt_error;

    //
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    Uri image_uri;
    String for_list;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ConfigDB conf = new ConfigDB(getApplicationContext());
        Integer id_module = Integer.parseInt(getIntent().getExtras().getString("id_module"));
        Module mod = conf.getModule(id_module);
        for_list = mod.answers;
        Toast.makeText(getApplicationContext(), "la liste "+for_list, Toast.LENGTH_SHORT).show();

        im1 = (ImageView) findViewById(R.id.image_view);
        Btn = (Button)findViewById(R.id.btn);
        btn2 = (Button)findViewById(R.id.button);
        btn_sui = (Button)findViewById(R.id.button3);
        btn_sui.setVisibility(View.INVISIBLE);
        btn_back = (Button)findViewById(R.id.button4);
        btn_back.setVisibility(View.INVISIBLE);

        btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setVisibility(View.INVISIBLE);
        btn_vider = (Button)findViewById(R.id.btn_vider);
        btn_vider.setVisibility(View.INVISIBLE);
        load = (ImageView)findViewById(R.id.img_load);
        load.setImageResource(R.drawable.load);
        load.setVisibility(View.INVISIBLE);

        txt_reco = (TextView)findViewById(R.id.txt_reco);
        txt_error = (TextView)findViewById(R.id.txt_error);
        txt_error.setVisibility(View.INVISIBLE);


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Bitmap bmp: bitmapArray_corr){
                    Random r = new Random();
                    try {
                        int ii = bitmapArray_corr.indexOf(bmp);
                        if(!index_of_image_error.contains(ii)){
                            saveImage(bmp, "qcm"+r.nextInt()+".jpg");
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(), "Les images ont été enregistrés avec succès", Toast.LENGTH_SHORT).show();
            }
        });

        btn_vider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                im1.setImageBitmap(bitmapArray_corr.get(0));
                txt_reco.setText(bitmapArray_names.get(0));
                btn_sui.setVisibility(View.VISIBLE);
                btn_back.setVisibility(View.VISIBLE);
            }
        });

        btn_sui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                index++;
                im1.setImageBitmap(bitmapArray_corr.get(index));
                txt_reco.setText(bitmapArray_names.get(index));
                if(index_of_image_error.contains(index)){
                    txt_error.setVisibility(View.VISIBLE);
                }
                else{
                    txt_error.setVisibility(View.INVISIBLE);
                }
            }
                catch(Exception ex){
                ex.printStackTrace();
                    index--;
            }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    index--;
                    im1.setImageBitmap(bitmapArray_corr.get(index));
                    txt_reco.setText(bitmapArray_names.get(index));
                    if(index_of_image_error.contains(index)){
                        txt_error.setVisibility(View.VISIBLE);
                    }
                    else{
                        txt_error.setVisibility(View.INVISIBLE);
                    }

                }
                catch(Exception ex){
                    ex.printStackTrace();
                    index++;
                }
            }
        });
       btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.CAMERA},1);

                }
                else
                {
                    openCamera();
                }
            }
        });
//        if(!Python.isStarted())
//            Python.start(new AndroidPlatform(this));
//        py= Python.getInstance();

        Btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {


                //Integer prog = 100/bitmapArray.size();
                //bar.setVisibility(View.VISIBLE);
                load.setVisibility(View.VISIBLE);
                Btn.setEnabled(false);
                btn2.setEnabled(false);
                im1.setEnabled(false);
                new CorrTask().execute();





            }
        });





       /* if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py= Python.getInstance();
        final PyObject pyobj = py.getModule("script");


        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            PyObject obj = pyobj.callAttr("main");

            Tv.setText(obj.toString());

            }
        });*/



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();

            try {
                bitmapArray.clear();
                if(Build.VERSION.SDK_INT >= 28){
                    int count = 0;
                    try {
                        count = data.getClipData().getItemCount();
                    }
                    catch(Exception ex){

                    }
                    Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();
                    for(int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        //do something with the image (save it to some directory or whatever you need to do with it here)

                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageUri);
                         selectedimage = ImageDecoder.decodeBitmap(source);

                        bitmapArray.add(selectedimage);
                    }


                   // im1.setImageBitmap(bitmapArray.get(0));
                    Toast.makeText(getApplicationContext(), "vous avez sélectionné "+String.valueOf(bitmapArray.size()), Toast.LENGTH_LONG).show();
                }
                else{
                    selectedimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    bitmapArray.add(selectedimage);
                    im1.setImageBitmap(bitmapArray.get(0));

                }

            } catch (IOException e) {
               e.printStackTrace();

            }



        }
        else  if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {

            im1.setImageURI(image_uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
                bitmapArray.add(bitmap);
                im1.setImageBitmap(bitmapArray.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);//burdan hata çıkabilir
        return encodedImage;

    }



    public void selectImage(View view){


        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }else{
            Intent intenttogaleri = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intenttogaleri.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(intenttogaleri,2);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intenttogaleri = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intenttogaleri,2);
            }
        }
        else if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "Camera");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "Camera";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    // test on background
    private class CorrTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... urls) {


            try {
//                ConfigDB conf = new ConfigDB(getApplicationContext());
//                Integer id_module = Integer.parseInt(getIntent().getExtras().getString("id_module"));
//                Module mod = conf.getModule(id_module);
//                String for_list = mod.answers;
//                Toast.makeText(getApplicationContext(), "la liste "+for_list, Toast.LENGTH_SHORT).show();
//                String pre = for_list.replace("[", "").replace("]", "").replace(" ", "");
//
//                String[] s = pre.split(",");
//
//                Integer[] answers = new Integer[s.length];
//                // List<Integer> answers_l = new ArrayList<Integer>();
//
//                for(int index = 0 ; index<s.length ; index++){
//
//                    //answers_l.add(Integer.parseInt(s[index]));
//                    answers[index] = Integer.parseInt(s[index]);
//                }
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "le nombre de réponse "+String.valueOf(answers.length), Toast.LENGTH_SHORT).show();
//                    }
//                });
                for(int i=0; i<bitmapArray.size(); i++) {

                    //bar.setProgress(prog);
                    //drawable = (BitmapDrawable) im1.getDrawable();+
                    bitmap = bitmapArray.get(i); //drawable.getBitmap();
                    imageString = getStringImage(bitmap);
                   // PyObject pyo = py.getModule("myscript");

                    //Integer answers[] = {0, 3, 1, 2, 1, 3, 0, 2, 2, 0, 1, 0, 1, 4, 4, 2, 3, 3, 0, 2};
                    //PyObject obj = pyo.callAttr("main", imageString, answers);
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(server.Url_server);

                    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                    nameValuePair.add(new BasicNameValuePair("image", imageString));
                    nameValuePair.add(new BasicNameValuePair("answers", for_list));

                    HttpClient httpClient2 = new DefaultHttpClient();
                    HttpPost httpPost2 = new HttpPost(server.Url_server+"image-name");

                    List<NameValuePair> nameValuePair2 = new ArrayList<NameValuePair>(2);
                    nameValuePair2.add(new BasicNameValuePair("image_name", imageString));


                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                        httpPost2.setEntity(new UrlEncodedFormEntity(nameValuePair2));
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
                        ImageString_fromFlask = convertStreamToString(is);

                        //

                        response2 = httpClient2.execute(httpPost2);
                        HttpEntity entity2 = response2.getEntity();
                        InputStream is2 = entity2.getContent();
                        ImageString_fromFlask_name = convertStreamToString(is2);


                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }







                    String str = ImageString_fromFlask; //obj.toString();
                    //PyObject obj2 = pyo.callAttr("image_name", imageString);
                    String str2 = ImageString_fromFlask_name;
                    if (str.length() > 50) {

                        // data resultat image
                        byte data[] = android.util.Base64.decode(str, Base64.DEFAULT);

                         bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        // data croped image for detect name
                        byte data2[] = android.util.Base64.decode(str2, Base64.DEFAULT);

                        bmp2 = BitmapFactory.decodeByteArray(data2, 0, data2.length);

                       // text recognisation
                       // try {

                            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                            Frame frameImage = new Frame.Builder().setBitmap(bmp2).build();
                            SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frameImage);
                            String stringImageText = "";
                            for (int j = 0; j<textBlockSparseArray.size();j++){
                                TextBlock textBlock = textBlockSparseArray.get(textBlockSparseArray.keyAt(j));
                                stringImageText = stringImageText + " " + textBlock.getValue();
                            }
                           // txt_reco.setText(stringImageText);


//                        }
//                        catch (Exception e){
//                            txt_reco.setText("Failed");
//                        }

                        //






                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Le papier a été corrigé avec succès", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // ajouter image apres la correction dans une liste
                        bitmapArray_corr.add(bmp);
                        // ajouter name apres la correction dans une liste
                        bitmapArray_names.add(stringImageText);
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error Image : Éviter les images qui contiennent des ombres", Toast.LENGTH_SHORT).show();

                            }
                        });
                        bitmapArray_corr.add(bitmapArray.get(i));
                        index_of_image_error.add(i);


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "error : "+e.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }

            return null;
        }

        protected void onProgressUpdate(Void... progress) {


        }

        protected void onPostExecute(Void result) {
            load.setVisibility(View.INVISIBLE);
            Btn.setEnabled(true);
            btn2.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Les papiers ont été corrigés avec succès", Toast.LENGTH_LONG).show();
            if(bitmapArray_names.size() > 0 && bitmapArray_corr.size() > 0) {
                im1.setImageBitmap(bitmapArray_corr.get(0));
                txt_reco.setText(bitmapArray_names.get(0));
            }
            if(index_of_image_error.contains(0)){
                txt_error.setVisibility(View.VISIBLE);
            }
            else{
                txt_error.setVisibility(View.INVISIBLE);
            }

            btn_sui.setVisibility(View.VISIBLE);
            btn_back.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.VISIBLE);
            btn_vider.setVisibility(View.VISIBLE);
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