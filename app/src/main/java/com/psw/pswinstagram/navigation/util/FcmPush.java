package com.psw.pswinstagram.navigation.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.psw.pswinstagram.R;
import com.psw.pswinstagram.navigation.model.PushDTO;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmPush extends Service {
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String url = "https://fcm.googleapis.com/fcm/send";
    private String serverKey = "AIzaSyBzf2ainfRVjomSeGAKAiMZplLrJKbrZNk";
    private Gson gson;
    private OkHttpClient okHttpClient;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private String CHANNEL_ID = "channel1";
    private String CHANEL_NAME = "Channel1";

    public static final FcmPush fcmPush = new FcmPush();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //FCM 푸시는 아직 완성 아님
        //좋아요 또는 댓글을 작성하면 Log.e로 성공이라 나오지만 이것을 푸시 알람으로 하는 방법을 모르겠음
        /*
        public void onResponse(Call call, Response response) throws IOException {
            Log.e("메세지TAG", "onResponse: 성공");
        }
        */
        Toast.makeText(getApplication(), "Fcm푸시", Toast.LENGTH_SHORT).show();
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
        builder.setContentTitle("알림123");
        //알림창 메시지
        builder.setContentText("알림 메시지123");
        //알림창 아이콘
        builder.setSmallIcon(R.drawable.ic_baseline_favorite_24);
        Notification notification = builder.build();
        //알림창 실행
        manager.notify(1,notification);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public FcmPush(){
        gson = new Gson();
        okHttpClient = new OkHttpClient();
    }

    public void sendMessage(String destinationUid,String title,String message){
        FirebaseFirestore.getInstance()
                .collection("pushTokens")
                .document(destinationUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String token = task.getResult().get("pushToken").toString();
                    Log.e("토큰값",token);
                    PushDTO pushDTO = new PushDTO();
                    pushDTO.setTo(token);
                    pushDTO.setTitle(title);
                    pushDTO.setBady(message);

                    RequestBody body = RequestBody.create(JSON,gson.toJson(pushDTO));
                    Request request = new Request.Builder()
                            .addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key="+serverKey)
                            .url(url)
                            .post(body)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("메세지TAG", "onResponse: 실패");

                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.e("메세지TAG", "onResponse: 성공");
                        }
                    });
                }
            }
        });
    }
}
