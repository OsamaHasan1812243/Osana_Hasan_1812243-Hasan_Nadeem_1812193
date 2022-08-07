package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.Adapters.AdapterChat;
import com.example.project.Adapters.AdapterUsers;
import com.example.project.Models.ModelChat;
import com.example.project.Models.ModelUser;
import com.example.project.Notifications.Data;
import com.example.project.Notifications.Sender;
import com.example.project.Notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv, blockIv;
    TextView nameTv;
    TextView userStatusTv;
    EditText messageEt;
    ImageButton sendBtn, attachBtn;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    DatabaseReference dbRef;
    DatabaseReference dRef;

    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String hisUid;
    String myUid;
    String hisImage;

    boolean isBlocked = false;
    private RequestQueue requestQueue;

    private boolean notify = false;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerview);
        profileIv = findViewById(R.id.profileIv);
        blockIv = findViewById(R.id.blockIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);

        cameraPermissions = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = firebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("User");

        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name =""+ ds.child("name").getValue();
                    hisImage =""+ ds.child("image").getValue();
                    String typingStatus =""+ ds.child("typingTo").getValue();

                    if(typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }else{
                        String onlineStatus =""+ ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }else{
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTv.setText("last seen at:"+ dateTime);

                        }
                    }


                    nameTv.setText(name);
                    try{

                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_baseline_face_24).into(profileIv);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.noprofile).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = messageEt.getText().toString().trim();

                if(TextUtils.isEmpty(message)){

                    Toast.makeText(ChatActivity.this, "Cannot send empty message", Toast.LENGTH_SHORT).show();

                }else{

                    sendMessage(message);
                }

                messageEt.setText("");
            }

        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(hisUid);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBlocked){
                    unBlockUser();
                }else{
                    blockUser();
                }
            }
        });
        readMessages();
        checkIsBlocked();
        seenMessage();

    }

    private void checkIsBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                blockIv.setImageResource(R.drawable.ic_baseline_block_24);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this, "Blocked Successfully...", Toast.LENGTH_SHORT).show();
                        blockIv.setImageResource(R.drawable.ic_baseline_block_24);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ChatActivity.this, "Unblocked Successfully...", Toast.LENGTH_SHORT).show();
                                                blockIv.setImageResource(R.drawable.ic_baseline_check_circle_24);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showImagePickDialog() {

        String [] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){

                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                }
                if(which == 1){

                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }

                }

            }
        });

        builder.create().show();
    }

    private void pickFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission(){


        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){


        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE );
    }


    private void seenMessage() {

        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds1: dataSnapshot.getChildren()){
                    ModelChat chat = ds1.getValue(ModelChat.class);
                    assert chat != null;
                    if (hisUid.equals(chat.getReciever()) && myUid.equals(chat.getSender())){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds1.getRef().updateChildren(hasSeenHashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages()  {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat != null) {
                        if (hisUid.equals(chat.getReciever()) && myUid.equals(chat.getSender())) {

                            chatList.add(chat);

                        } else if (myUid.equals(chat.getReciever()) && hisUid.equals(chat.getSender())) {

                            chatList.add(chat);

                        }
                    }

                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message)  {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap <String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("reciever", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);


        DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if(notify){
                 sendNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendImageMessage(Uri image_uri) throws IOException {
        notify = true;
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, boas);
        byte[] data = boas.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("reciever", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timeStamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);
                            databaseReference.child("Chats").push().setValue(hashMap);

                            final DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(myUid);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUser user = snapshot.getValue(ModelUser.class);
                                    if(notify){
                                        sendNotification(hisUid,user.getName(), "Sent you a photo...");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();

                    }
                });
    }


    private void sendNotification(String hisUid, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query =allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name+": "+message, "New Message", hisUid, R.drawable.ic_baseline_face_24);

                    Sender sender =  new Sender(data, token.getToken());
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String ,String > header =new HashMap<>();
                                header.put("Content-Type", "application/json");
                                header.put("Authorization", "key=AAAAREKyN84:APA91bGb9Suxvtx71Z6kK6czS_gq5I1k7BwfNigvSw02usDuLXv0yeAsCQ2behRBYDle7yWevdbNhtczBenkoPUayWnJrQjV28nJAHzjGoSbDidJ0aEznCMP-hE5mPxGRxx1Z1WfHQ-a");
                                return header;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){

            myUid = user.getUid();

        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus (String status){
        dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
        HashMap<String, Object> hashMap =  new HashMap<>();
        hashMap.put("onlineStatus", status);

        dbRef.updateChildren(hashMap);
    }
    private void checkTypingStatus (String typing){
        dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
        HashMap<String, Object> hashMap =  new HashMap<>();
        hashMap.put("typingTo", typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }


    @Override
    protected void onPause() {
        super.onPause();

        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camera & Storage both permissions are required", Toast.LENGTH_SHORT).show();
                    }

                }else{

                }
            }
            break;

            case STORAGE_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Storage both permissions required", Toast.LENGTH_SHORT).show();
                    }


                }else{

                }

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);

        menu.findItem(R.id.userSearch).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id=item.getItemId();
        if(id==R.id.logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}