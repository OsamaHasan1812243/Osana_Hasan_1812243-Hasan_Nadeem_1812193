package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Adapters.AdapterComments;
import com.example.project.Models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class PostDetailActivity extends AppCompatActivity {

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    String myUid, hisUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;

    ProgressDialog pd;

    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageView moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;

    RecyclerView recyclerView;

    private DatabaseReference commentRef;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

//add comments views

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postId=intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id. uPictureIv);
        pImageIv=findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id. uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        LoadPostInfo();
        CheckUserStatus();
        LoadUserInfo();
        setLikes();

        actionBar.setSubtitle("SignedIn as:"+myEmail);

        loadComments();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                if(bitmapDrawable == null){
                    shareTextOnly(pTitle,pDescription);
                }else{
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });
    }

    private void shareTextOnly(String pTitle, String pDescription) {

        String shareBody = pTitle +"\n"+pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sIntent,"Share Via"));
    }
    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle +"\n"+pDescription;

        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdir();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this,"com.example.project.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }



    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(), commentList,myUid,postId);
                    recyclerView.setAdapter(adapterComments);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);


        if(hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                if(id == 0){

                    beingDelete();

                }else if(id ==1){
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }

                return false;
            }
        });

        popupMenu.show();

    }

    private void beingDelete() {
        if(pImage.equals("noImage")){

            deleteWithoutImage();

        }else{

            deleteWithImage();

        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }

                                Toast.makeText(PostDetailActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");


        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(PostDetailActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(postId).hasChild(myUid)){
                   likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_liked,0,0,0);
                   likeBtn.setText("Liked");
                }else{

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24,0,0,0);
                    likeBtn.setText("Like");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void likePost() {

        mProcessLike = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    }else{
                        postsRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");

        String comment = commentEt.getText().toString().trim();

        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put ("cId", timeStamp);
        hashMap.put ("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment added...", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateCommentCount() {
        mProcessComment=true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments = ""+snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LoadUserInfo() {

        Query myRef = FirebaseDatabase.getInstance().getReference (  "User");
        myRef.orderByChild ("uid").equalTo (myUid).addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_baseline_face_24).into(cAvatarIv);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_baseline_face).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void LoadPostInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ( "Posts");
        Query query = ref.orderByChild ("pId").equalTo (postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String comments = "" + ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pTitleTv.setText (pTitle);
                    pDescriptionTv.setText (pDescr);
                    pLikesTv.setText (pLikes + "Likes");
                    pTimeTiv.setText (pTime);
                    uNameTv.setText(hisName);
                    pCommentsTv.setText(comments + " Comments");

                    if(pImage.equals("noImage")) {
                        pImageIv.setVisibility(View.GONE);
                    } else{
                        pImageIv.setVisibility(View.VISIBLE);

                        try{
                            Picasso.get().load(pImage).into(pImageIv);
                        }catch (Exception e){

                        }
                    }

                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_baseline_face_24).into(uPictureIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_baseline_face_24).into(uPictureIv);
                    }

                }
            }

            @Override

            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void CheckUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user!= null) {
            myEmail=user.getEmail();
            myUid=user.getUid();

        }

        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.userSearch).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id=item.getItemId();
        if(id==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            CheckUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}