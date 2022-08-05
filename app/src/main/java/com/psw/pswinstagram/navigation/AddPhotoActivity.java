package com.psw.pswinstagram.navigation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.psw.pswinstagram.R;
import com.psw.pswinstagram.navigation.model.ContentDTO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhotoActivity extends AppCompatActivity {
    public static final int PICK_IMAGE_FROM_ALBUM = 1;
    private FirebaseStorage storage;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore firestore;

    private Uri photoUri;
    private EditText addphoto_edit;
    private Button addphoto_btn;
    private ImageView addphoto_img;
    private SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private String imageFileName ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        //파이어베이스 인증
        mFirebaseAuth = FirebaseAuth.getInstance();
        //파이어베이스 파이어스토어(Cloud Firestore)
        firestore = FirebaseFirestore.getInstance();

        addphoto_edit = findViewById(R.id.addphoto_edit);
        addphoto_btn = findViewById(R.id.addphoto_btn);
        addphoto_img = findViewById(R.id.addphoto_img);


        //AddPhotoActivity가 실행되면 앨범이 intent된다.
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_FROM_ALBUM);



        addphoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentUpload();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //선택한 이미지의 경로를 저장한다.
                photoUri = data.getData();
                addphoto_img.setImageURI(photoUri);

            }else{
                //취소를 눌렀을때
                finish();
            }
        }
    }

    private void contentUpload() {
        Date date = new Date();
        String time = timestamp.format(date);
        imageFileName = "IMAGE_"+time+"_.png";

        //파이어베이스 스토리지를 사용하겠다.
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference pathReference = storageReference.child("images").child(imageFileName);

        UploadTask uploadTask = pathReference.putFile(photoUri);


        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //이미지 업로드 실패 공간
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //이미지 업로드 공간///////////////////////////
                //올라가는 중이라는 로딩 표시가 있으면 더 좋을 듯 하다.


                ////////////////////////////////////////////


                //바로 위에서 올린 이미지 uri를 가져와는 곳↓
                pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String img_uri = uri.toString();
                        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                        ContentDTO dto = new ContentDTO();
                        dto.setExplain(addphoto_edit.getText().toString());
                        dto.setUid(currentUser.getUid());
                        dto.setUserId(currentUser.getEmail());
                        Long now =  System.currentTimeMillis();
                        Date mDate = new Date(now);
                        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String getTime = simpleDate.format(mDate);
                        dto.setTimestamp(getTime);
                        dto.setImageUri(img_uri);



                        //이미지 uri를 가져왔으면 사용자 정보를 dto에 담고 업로드 하는 곳↓
                        firestore.collection("images").document()
                                .set(dto).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddPhotoActivity.this, "업로드 완료", Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_OK);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddPhotoActivity.this,"다시 시도해주세요",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }
}