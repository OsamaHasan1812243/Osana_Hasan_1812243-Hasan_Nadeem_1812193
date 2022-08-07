package com.example.project.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Adapters.AdapterPosts;
import com.example.project.AddPostActivity;
import com.example.project.HomeActivity;
import com.example.project.Models.ModelPost;
import com.example.project.R;
import com.example.project.home;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagePath1 = "User_Profile_Image/";

    ImageView avatarTv, coverTv;
    TextView nameTv,phoneTv,emailTv,cnicTv;
    FloatingActionButton fab;
    RecyclerView postRecyclerView;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;
    private static final int IMAGE_PICK_GALLERY_CODE =300;
    private static final int IMAGE_PICK_CAMERA_CODE =400;

    String cameraPermissions [];
    String storagePermissions [];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    Uri image_uri;

    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("User");
        storageReference = getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

        avatarTv=view.findViewById(R.id.avatarTv);
        coverTv=view.findViewById(R.id.coverTv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        cnicTv=view.findViewById(R.id.cnicTv);
        fab=view.findViewById(R.id.fab);
        postRecyclerView=view.findViewById(R.id.recyclerview_posts);

        pd=new ProgressDialog(getActivity());
        Query query = databaseReference.orderByChild("uid").equalTo(mUser.getUid());
          query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()){
                String name = ""+ds.child("name").getValue();
                String email = ""+ds.child("email").getValue();
                String phone = ""+ds.child("phone").getValue();
                String cnic = ""+ds.child("cnic").getValue();
                String image = ""+ds.child("image").getValue();
                String cover = ""+ds.child("cover").getValue();
                nameTv.setText(name);
                emailTv.setText(email);
                phoneTv.setText(phone);
                cnicTv.setText(cnic);

                try{
                    Picasso.get().load(image).into(avatarTv);
                }catch (Exception e){
                    Picasso.get().load(String.valueOf(avatarTv)).into(avatarTv);
                }
                try{
                    Picasso.get().load(cover).into(coverTv);
                }catch (Exception e){
                    Picasso.get().load(R.drawable.ic_baseline_add_a_photo_24).into(coverTv);
                }
            }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
          });

       fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               showEditProfileDialog();
           }
       });

       postList = new ArrayList<>();
       checkUserStatus();
       LoadMyPosts();

        return view;
    }

    private void LoadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){

                        postList.add(myPosts);
                    }

                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions( storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        requestPermissions( cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        String options[]= {"Add Profile Picture", "Add CNIC Picture","Edit Name","Edit Phone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0){
                    pd.setMessage("Uploading Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();

                }else if (which==1){
                    pd.setMessage("Uploading CNIC Picture");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();


                }else if(which==2){
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");

                }else if(which==3){
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");

                }
            }
        });
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+ key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter"+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               final String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put (key, value);

                    databaseReference.child(mUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                    if(key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child =ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                for(DataSnapshot ds: snapshot.getChildren()){
                                                    String child =ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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
                }else{
                    Toast.makeText(getActivity(), "Please enter"+key, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.create().show();
    }


    private void showImagePicDialog() {


        String options[]= {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }else if (which==1){
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else{
                        Toast.makeText(getActivity(), "Please Enable Camera & Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if( writeStorageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(getActivity(), "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();



        String filePathAndName =storagePath1+""+ profileOrCoverPhoto+""+mUser.getUid();
        StorageReference storageReference1nd = storageReference.child(filePathAndName);
        storageReference1nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        if(uriTask.isSuccessful()){
                            HashMap<String,Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());


                            databaseReference.child(mUser.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Uploaded....", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Uploading Image....", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                            if(profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            String child =ds.getKey();
                                            if(snapshot.child(child).hasChild("Comments")){
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        for(DataSnapshot ds: snapshot.getChildren()){
                                                            String child =ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

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
                        }else{
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                   pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE );

    }

    private void pickFromGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    private void checkUserStatus(){
        if(mUser != null){

            uid = mUser.getUid();

        }/*else{
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }*/
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.userSearch);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    LoadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    LoadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_add_post){
            Intent intent= new Intent(getActivity(), AddPostActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
