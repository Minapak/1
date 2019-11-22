package com.example.volley;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private ImageView imageView;
    private ImageView imageView2;
    private Uri contentURI;
    private Bitmap bitmap;
    private final int GALLERY = 1;
    private String upload_URL = "http://49.247.132.18/uploadVolley.php";

//    StringRequest: 문자열을 결과로 받는 요청 정보
//    ImageRequest: 이미지를 결과로 받는 요청 정보
//    JsonObjectRequest: JSONObject를 결과로 받는 요청 정보
//    JsonArrayRequest: JSONArray를 결과로 받는 요청 정보


    JSONObject jsonObject;
//RequestQueue: 서버 요청자. 다른 Request 클래스들의 정보대로 서버에 요청을 보내는 역할
    RequestQueue rQueue;
//    값 불러오기
//    SharedPreferences pref1 = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
//
//    String img_str=pref1.getString("imagestrings", "");
//
//    Bitmap bitmap = decodeBase64(img_str);
//    public static Bitmap decodeBase64(String input) {
//        byte[] decodedByte = Base64.decode(input, 0);
//        return BitmapFactory
//                .decodeByteArray(decodedByte, 0, decodedByte.length);
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestMultiplePermissions();



        btn = findViewById(R.id.btn);
        imageView = (ImageView) findViewById(R.id.iv);




        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.e("쉐어드 값 불러오기  : "+preferences,"    ");

        String contentURI = preferences.getString("image", "");
        Log.e("쉐어드 키값 불러오기 : "+contentURI,"    ");

        if (contentURI != null) {
            imageView.setImageURI(Uri.parse(contentURI));
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        imageView.setImageURI(Uri.parse(contentURI));
        Log.e("쉐어드 URI  : "+(Uri.parse(contentURI)),"    ");


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, GALLERY);
            }
        });


    }
    //후속 액티비티에서 작업한 결과물을
    //호출한 액티비티에서 사용하고 싶은 경우
    //onActivityResult
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        //**선택된 사진 서버에 업로드 하기 위한 과정

        //디바이스의 갤러리에서 사진을 골라온게 맞다면
        if (requestCode == GALLERY) {
            if (data != null) {
                //받아온 데이터를 URI로 변환해 넣는다
                contentURI = data.getData();
                try {
                    //변환한 URI를 또 다시 비트맵으로 변환
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);


                    //배치해놓은 ImageView에 비트맵 넣어서 set
                    imageView.setImageBitmap(bitmap);

                    // Saves image URI as string to Default Shared Preferences
                    SharedPreferences preferences =
                            PreferenceManager.getDefaultSharedPreferences(this);
                    Log.e("쉐어드 변수 선언  : "+preferences,"    ");

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("image", String.valueOf(contentURI));
                    Log.e("쉐어드 키값   : "+editor.putString("image", String.valueOf(contentURI)),"    ");

                    editor.commit();
                    Log.e("쉐어드 저장   : "+ editor.commit(),"    ");




                    imageView.setImageURI(contentURI);
                    Log.e("쉐어드 저장   : "+ contentURI,"    ");
                    imageView.invalidate();

                    //업로드 이미지 메소드 실행
                    uploadImage(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadImage(Bitmap bitmap){


        //출력되는 모든 내용들이 내부 저장소에 쌓이는 메소드
        // byteArrayOutputStream 생성
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // compress 함수를 사용해 스트림에
        // byteArrayOutputStream 메소드에 있는 데이터를
        // 비트맵을 변환해 저장
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        SharedPreferences pref = (SharedPreferences) getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("imagestrings", encodedImage);
        editor.commit();

        try {
            jsonObject = new JSONObject();
            String imgname = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String imgname1 = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            jsonObject.put("name", imgname1);
            Log.e("Image name : ", imgname1);
            jsonObject.put("image", encodedImage);
            // jsonObject.put("aa", "aa");
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }


        //JsonObjectRequest: JSONObject를 결과로 받는 요청 정보
        //POST 방식 -> 어떤 데이터를 서버에 보낼 때 사용
        //getParams라는 override method가 추가
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("upload_URL + "+upload_URL," 업로드 URL");
                        Log.e("jsonObject + ", jsonObject.toString());

                        rQueue.getCache().clear();
                        Toast.makeText(getApplication(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("volleyError + ", volleyError.toString());

            }
        });

        rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(jsonObjectRequest);
        Log.e("어디 + "+jsonObjectRequest," jsonObjectRequest");
        Log.e("어디 + "+rQueue," rQueue");
    }









    //요청허가 함수
    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(

                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

}