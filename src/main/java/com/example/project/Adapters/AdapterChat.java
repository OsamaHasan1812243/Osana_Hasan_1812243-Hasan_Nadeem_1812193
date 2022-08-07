package com.example.project.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.animation.content.Content;
import com.example.project.Models.ModelChat;
import com.example.project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;


    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if(i==MSG_TYPE_RIGHT){

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);

        }else{


            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        String message = chatList.get(i).getMessage();
        String timestamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if (!type.equals("text")) {
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_baseline_image_24).into(myHolder.messageIv);
        } else {
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);

            myHolder.messageTv.setText(message);
        }

        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try {

            Picasso.get().load(imageUrl).into(myHolder.profileIv);

        }catch (Exception e){

        }
        myHolder.messageLAyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this Message?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(i);

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
        });

        if (i == chatList.size()-1) {
            if(chatList.get(i).isSeen()){

                myHolder.isSeenTv.setText("Seen");
            } else{

                myHolder.isSeenTv.setText("Delivered");
            }

        } else {

            myHolder.isSeenTv.setVisibility(View.GONE);

        }

    }

    private void deleteMessage(int position) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query =  dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUID)){

                       // ds.getRef().removeValue();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted...", Toast.LENGTH_SHORT).show();
                    }else{

                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {

        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser.getUid().equals(chatList.get(position).getSender())){

            return MSG_TYPE_RIGHT;

        }else{

            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
