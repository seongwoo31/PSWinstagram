package com.psw.pswinstagram;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.psw.pswinstagram.navigation.AddPhotoActivity;
import com.psw.pswinstagram.navigation.Frag1;
import com.psw.pswinstagram.navigation.Frag2;
import com.psw.pswinstagram.navigation.Frag3;
import com.psw.pswinstagram.navigation.Frag4;
import com.psw.pswinstagram.navigation.Frag5;
import com.psw.pswinstagram.navigation.model.ProfileImage;
import com.psw.pswinstagram.navigation.util.FcmPush;

import java.util.HashMap;
import java.util.Map;



//https://lcw126.tistory.com/330
//바텁네비게이션뷰 이미지 색상 변경

public class MainActivity extends AppCompatActivity{
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore firestore;
    //하단 네비게이션바
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Frag1 frag1;
    private Frag2 frag2;
    private Frag3 frag3;
    private Frag4 frag4;
    private Frag5 frag5;

    public TextView toolbar_username;
    public ImageView toolbar_btn_back;
    public ImageView toolbar_title_image;

    private Uri imageUri;

    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    private String CHANNEL_ID = "channel1";
    private String CHANEL_NAME = "Channel1";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        toolbar_username = findViewById(R.id.toolbar_username);
        toolbar_btn_back = findViewById(R.id.toolbar_btn_back);
        toolbar_title_image = findViewById(R.id.toolbar_title_image);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(MainActivity.this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_home:
                        setFrag(0);
                        break;
                    case R.id.action_search:
                        setFrag(1);
                        break;
                    case R.id.action_add_photo:
                        setFrag(2);
                        Intent intent = new Intent(MainActivity.this, AddPhotoActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_favorite_alarm:
                        setFrag(3);
                        break;
                    case R.id.action_account:
                        Bundle bundle = new Bundle();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        bundle.putString("destinationUid",uid);
                        frag5.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_content,frag5).commit();

                        setFrag(4);
                        break;

                }
                return true;
            }
        });
        frag1 = new Frag1();
        frag2 = new Frag2();
        frag3 = new Frag3();
        frag4 = new Frag4();
        frag5 = new Frag5();

        setFrag(0);


        bottomNavigationView.setSelectedItemId(R.id.action_home);





        setToolbarDefault();
        registerPushToken();
//        registerFollow();


        

        //    getHashKey();
    }




    /*
    //해시값 추출 하는 방법
    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }*/
    public void setFrag(int n){
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n){
            case 0:
                ft.replace(R.id.main_content,frag1);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_content,frag2);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_content,frag3);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_content,frag4);
                ft.commit();
                break;
            case 4:
                toolbar_username.setVisibility(View.GONE);
                toolbar_btn_back.setVisibility(View.GONE);
                toolbar_title_image.setVisibility(View.VISIBLE);
                ft.replace(R.id.main_content,frag5);
                ft.commit();
                break;
        }
    }
    private void setToolbarDefault() {
        toolbar_username.setVisibility(View.GONE);
        toolbar_btn_back.setVisibility(View.GONE);
        toolbar_title_image.setVisibility(View.VISIBLE);

    }
    public void hello(){
        toolbar_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar_username.setVisibility(View.GONE);
                toolbar_btn_back.setVisibility(View.GONE);
                toolbar_title_image.setVisibility(View.VISIBLE);
                bottomNavigationView.setSelectedItemId(R.id.action_home);
            }
        });
    }
    public void registerPushToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("TOKEN_NONE", "registerPushToken: "+task.getException());
                        return;
                    }
                    Map<String,String> push_token  = new HashMap<>();
                    String token = task.getResult();
                    push_token.put("pushToken",token);
                    Log.e("TOKEN", "registerPushToken: "+token);
                    FirebaseFirestore.getInstance().collection("pushTokens")
                            .document(mFirebaseAuth.getUid()).set(push_token);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Frag5.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            imageUri = data.getData();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("userProfileImages").child(uid);
            UploadTask uploadTask = storageRef.putFile(imageUri);
            Map<String, Object > map  = new HashMap<>();
            map.put(storageRef.getDownloadUrl().toString(),imageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String img_uri = uri.toString();
                            ProfileImage profileimage = new ProfileImage();
                            profileimage.setImageUri(img_uri);
                            Toast.makeText(getApplication(),"성공",Toast.LENGTH_SHORT).show();
                            firestore.collection("profileImage").document(uid)
                                    .set(profileimage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    setResult(Activity.RESULT_OK);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,"다시 시도해주세요",Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });
                }
            });
        }
    }

    public void pushAlarm(){
        builder = null;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(this,CHANNEL_ID);
            //하위 버전일 경우
        }else{
            builder = new NotificationCompat.Builder(this);
        }
        //알림창 제목
        builder.setContentTitle("알림");
        //알림창 메시지
        builder.setContentText("알림 메시지");
        //알림창 아이콘
        builder.setSmallIcon(R.drawable.ic_baseline_favorite_24);
        Notification notification = builder.build();
        //알림창 실행
        manager.notify(1,notification);
    }



    @Override
    protected void onStop() {
        super.onStop();
        FcmPush.fcmPush.sendMessage("W02TxxCZp0UNpzhLtkPLdzI7hGw2","hi","hello");
    //    pushAlarm();
    }
}