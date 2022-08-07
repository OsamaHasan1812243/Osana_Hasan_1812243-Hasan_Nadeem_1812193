package com.example.project.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.project.Adapters.AdapterChatlist;
import com.example.project.LoginActivity;
import com.example.project.Models.ModelChat;
import com.example.project.Models.ModelChatlist;
import com.example.project.Models.ModelUser;
import com.example.project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;


    public ChatListFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for(ModelChatlist chatlist: chatlistList){
                        if(user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    recyclerView.setAdapter(adapterChatlist);
                    for(int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String reciever = chat.getReciever();
                    if(sender==null || reciever==null){
                        continue;
                    }
                    if(chat.getReciever().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                    chat.getReciever().equals(userId) &&
                    chat.getSender().equals(currentUser.getUid())){
                        if("image".equals(chat.getType())) {
                            theLastMessage = "Sent a photo";
                        }else{
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){

        }else{
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        int id=item.getItemId();
        if(id==R.id.logout){
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}