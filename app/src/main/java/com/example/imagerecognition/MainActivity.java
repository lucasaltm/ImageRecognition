package com.example.imagerecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudmersive.client.FaceApi;
import com.cloudmersive.client.invoker.ApiClient;
import com.cloudmersive.client.invoker.ApiException;
import com.cloudmersive.client.invoker.Configuration;
import com.cloudmersive.client.invoker.auth.*;
import com.cloudmersive.client.RecognizeApi;
import com.cloudmersive.client.model.AgeDetectionResult;
import com.cloudmersive.client.model.DetectedObject;
import com.cloudmersive.client.model.GenderDetectionResult;
import com.cloudmersive.client.model.ImageDescriptionResponse;
import com.cloudmersive.client.model.ObjectDetectionResult;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import android.os.Bundle;

import com.cloudmersive.client.FaceApi;
import com.cloudmersive.client.model.PersonWithAge;
import com.cloudmersive.client.model.PersonWithGender;
import com.cloudmersive.client.model.RecognitionOutcome;

import static android.app.PendingIntent.getActivity;


public class MainActivity extends AppCompatActivity {

    private final int PEGAR_IMAGEM = 1;
    private final int PERMISSION_READEXTERNAL = 2;

    private boolean imgSelecionada = false;
    private boolean success = false;

    String path = null;
    Uri uri = null;

    ImageView imagem, imagemobj, imagemface;
    TextView description, objcount, objects, facecount, faces;

    ObjectDetectionResult result;
    AgeDetectionResult ageResult;
    GenderDetectionResult genderResult;
    ImageDescriptionResponse descriptionResult;
    RecognitionOutcome bestOutcomeDecription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagem = findViewById(R.id.imagem);
        description = findViewById(R.id.description);
        imagemobj = findViewById(R.id.imagemobj);
        objcount = findViewById(R.id.objcount);
        objects = findViewById(R.id.objects);
        imagemface = findViewById(R.id.imagemface);
        facecount = findViewById(R.id.facecount);
        faces = findViewById(R.id.faces);

        //Pedindo permissão de acesso a internet
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 123);


        //Para forçar a api a rodar na mainActivity
        StrictMode.ThreadPolicy policy = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        //Autenticação
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth Apikey = (ApiKeyAuth) defaultClient.getAuthentication("Apikey");
        //Apikey.setApiKey("17c12a0e-d7ba-4926-82c3-ea54da22f604");
        Apikey.setApiKey("1203e849-2d23-496f-abae-1a7ee030b161");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.mymenu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        Log.d("Log", "onOptionsItemSelected: " + item.getItemId());


        if (item.getItemId() == R.id.upload) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                //Verificando se o aplicativo está autorizado a acessar a galeria do celular
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //Se acesso for negado, faz a requisição ao usuário
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_READEXTERNAL);
                } else {
                    pegarImagem();
                }
            } else {
                pegarImagem();
            }
            return (true);
        }

        if (item.getItemId() == R.id.hp) {
            back(null);
        }

        return false;
    }

    private void pegarImagem() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PEGAR_IMAGEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PEGAR_IMAGEM) {

                uri = data.getData();
                path = uri.getPath();

                imagem.setImageURI(uri);
                imgSelecionada = true;
            }
        }
    }

    public String getPath(Uri uri) { //Pega o caminho do Uri
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public void detectFace(View view) {

        description.setText("Loading...");
        System.out.println("Detectando Idade e Gênero");

        if (imgSelecionada != true) {
            Toast.makeText(this, "Primeiro selecione a imagem no menu acima!", Toast.LENGTH_LONG).show();
            return;
        }


        String caminho = getPath(uri);
        File imageFile = new File(caminho);

        FaceApi apiInstance = new FaceApi();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ageResult = apiInstance.faceDetectAge(imageFile);
                    genderResult = apiInstance.faceDetectGender(imageFile);
                    System.out.println(ageResult + "\n" + genderResult);


                } catch (ApiException e) {
                    atualizaView(null, null, 3);//Toast de erro na MainActivity, fora da thread.
                    System.err.println("Exception when calling DetectFace APIs");
                    e.printStackTrace();
                }

                if (ageResult != null && genderResult != null) {
                    String resultados = "";

                    List<PersonWithAge> listaidades = ageResult.getPeopleWithAge();
                    List<PersonWithGender> listageneros = genderResult.getPersonWithGender();


                    resultados = "Age detection:\n";
                    for (int i = 0; i < ageResult.getPeopleIdentified(); i++) {
                        resultados = resultados +
                                "\nPerson " + i + ":" +
                                "\nClassification Confidence:" + String.format("%4.2f", listaidades.get(i).getAgeClassificationConfidence()) +
                                "\nAge Class: " + listaidades.get(i).getAgeClass() +
                                "\nAge: " + String.format("%4.1f", listaidades.get(i).getAge()) + "\n";
                    }

                    resultados = resultados + "\n\nGender detection:";
                    for (int i = 0; i < genderResult.getPeopleIdentified(); i++) {
                        resultados = resultados +
                                "\nPerson " + i + ":" +
                                "\nClassification Confidence:" + String.format("%4.2f", listageneros.get(i).getGenderClassificationConfidence()) +
                                "\nGender: " + listageneros.get(i).getGenderClass() + "\n";
                    }
                    String count = "FACES DETECTED: " + ageResult.getPeopleIdentified();
                    atualizaView(count, resultados, 1);

                }
            }
        });
        t.start();
    }

    public void describeImage(View view) {

        description.setText("Loading...");
        System.out.println("Analisando imagem!");

        if (imgSelecionada != true) {
            Toast.makeText(this, "Primeiro selecione a imagem no menu acima!", Toast.LENGTH_LONG).show();
            return;
        }

        String caminho = getPath(uri);
        File imageFile = new File(caminho);
        RecognizeApi apiInstance = new RecognizeApi();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    descriptionResult = apiInstance.recognizeDescribe(imageFile);
                    System.out.println("Resultado do describe:\n" + descriptionResult);

                } catch (ApiException e) {
                    atualizaView(null, null, 3);//Toast de erro na MainActivity, fora da thread.
                    System.err.println("Exception when calling RecognizeApi#recognizeDescribe");
                    e.printStackTrace();
                }

                if (descriptionResult != null) {
                    bestOutcomeDecription = descriptionResult.getBestOutcome();
                    atualizaView(null, bestOutcomeDecription.getDescription(), 2);
                }
            }
        });
        t.start();


    }

    public void detectObjects(View view) {

        description.setText("Loading...");
        System.out.println("Detectando objetos!");

        if (imgSelecionada != true) {
            Toast.makeText(this, "Primeiro selecione a imagem no menu acima!", Toast.LENGTH_LONG).show();
            return;
        }


        String caminho = getPath(uri);
        File imageFile = new File(caminho);

        RecognizeApi apiInstance = new RecognizeApi();
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    result = apiInstance.recognizeDetectObjects(imageFile);
                } catch (ApiException e) {
                    atualizaView(null, null, 3);//Toast de erro na MainActivity, fora da thread.
                    System.err.println("Exception when calling RecognizeApi#recognizeDetectObjects");
                    e.printStackTrace();
                }

                if (result != null) {
                    String objetos_detectados = "";
                    List<DetectedObject> lista = result.getObjects();
                    ;

                    for (int i = 0; i < result.getObjectCount(); i++) {

                        if (!objetos_detectados.isEmpty()) {
                            objetos_detectados = objetos_detectados + "\n\n";
                        }

                        objetos_detectados = objetos_detectados + "Object " + i +
                                "\nObject name: " + lista.get(i).getObjectClassName() +
                                "\nHeight: " + lista.get(i).getHeight() +
                                "\nWidth: " + lista.get(i).getWidth() +
                                "\nScore: " + String.format("%4.2f", lista.get(i).getScore()) +
                                "\nX: " + lista.get(i).getX() +
                                "\nY: " + lista.get(i).getY();
                    }

                    String objects_count = "OBJECTS DETECTED: " + result.getObjectCount();
                    System.out.println(result);
                    atualizaView(objects_count, objetos_detectados, 0);
                }
            }
        });
        t.start();
    }

    public void atualizaView(String count, String str, int i) {     //atualiza a view depois do retorno da thread

        //PARA A THREAD CONSEGUIR ACESSAR AS VIEWS DA MAIN ACTIVITY
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (i == 0) {//SIGNIFICA QUE A CHAMADA É DE DA FUNÇÃO DETECT OBJECTS
                    setContentView(R.layout.activity_obj);
                    objcount = findViewById(R.id.objcount);
                    objects = findViewById(R.id.objects);
                    imagemobj = findViewById(R.id.imagemobj);

                    objcount.setText(count);
                    objects.setText(str);
                    imagemobj.setImageURI(uri);
                }

                if (i == 1) {//SIGNIFICA QUE A CHAMADA É DE DA FUNÇÃO DETECT FACES

                    setContentView(R.layout.activity_face);

                    imagemface = findViewById(R.id.imagemface);
                    facecount = findViewById(R.id.facecount);
                    faces = findViewById(R.id.faces);

                    facecount.setText(count);
                    faces.setText(str);
                    imagemface.setImageURI(uri);


                }

                if (i == 2) {//SIGNIFICA QUE A CHAMADA É DE DA FUNÇÃO DESCRIBE
                    description.setText(str);
                }

                if (i == 3) {// TOAST DE ERRO
                    System.out.println("Toast de erro.");
                    description.setText("");
                    Toast.makeText(MainActivity.this, "Ocorreu um erro ao tentar identificar a imagem, tente novamente!", Toast.LENGTH_LONG).show();
                }

            }


        });
    }

    public void back(View view) {
        System.out.println("Voltando para tela inicial!");
        setContentView(R.layout.activity_main);

        imagem = findViewById(R.id.imagem);
        description = findViewById(R.id.description);

        imagem.setImageURI(uri);
    }
}