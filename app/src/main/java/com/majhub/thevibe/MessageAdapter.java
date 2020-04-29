package com.majhub.thevibe;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;



public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }





    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }else if (fromMessageType.equals("image")){
           if (fromUserID.equals(messageSenderId)){
               messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
               Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

           }else{
               messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
               messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
               Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
           }
        }else
        if (fromMessageType.equals("pdf") ||fromMessageType.equals("docx")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/thevibe-f4ef9.appspot.com/o/Image%20Files%2Fzip_files.jpg?alt=media&token=2192edc3-9bfc-4b9a-afa3-b3905d2d5f34").into(messageViewHolder.messageSenderPicture);

            }else{
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/thevibe-f4ef9.appspot.com/o/Image%20Files%2Fzip_files.jpg?alt=media&token=2192edc3-9bfc-4b9a-afa3-b3905d2d5f34").into(messageViewHolder.messageReceiverPicture);



            }


        }




        if(fromUserID.equals(messageSenderId)){
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options [] = new CharSequence[]{
                                "Download and View This Document",

                                "Delete For Me",
                                "Delete For Everyone",

                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else
                                if (which == 1){
                                    deleteSentMessage(position, messageViewHolder);
                                    onBindViewHolder(messageViewHolder,  position);

                                }else
                                if (which == 2){
                                    deleteMessageForEveryone(position, messageViewHolder);


                                }

                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options [] = new CharSequence[]{

                                "Delete For Me",
                                "Cancel",
                                "Delete For Everyone"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);


                                }else

                                if (which == 2){
                                    deleteMessageForEveryone(position, messageViewHolder);


                                }
                            }
                        });
                        builder.show();
                    }else
                    if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options [] = new CharSequence[]{
                                " View This Image",

                                "Delete For Me",
                                "Delete For Everyone",

                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("Url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }else
                                if (which == 1){
                                    deleteSentMessage(position, messageViewHolder);


                                }else
                                if (which == 2){
                                    deleteMessageForEveryone(position, messageViewHolder);


                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        else{
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options [] = new CharSequence[]{
                                "Download and View This Document",

                                "Delete For Me",


                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }else
                                if (which == 1){
                                    deleteRecieveMessage(position, messageViewHolder);


                                }

                            }
                        });
                        builder.show();
                    }else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options [] = new CharSequence[]{

                                "Delete For Me",
                                "Cancel",


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteRecieveMessage(position, messageViewHolder);


                                }


                            }
                        });
                        builder.show();
                    }else
                    if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options [] = new CharSequence[]{
                                " View This Image",

                                "Delete For Me",


                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Action");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("Url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }else
                                if (which == 1){
                                    deleteRecieveMessage(position, messageViewHolder);

                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
            }

    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void deleteRecieveMessage(final int position,final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position,final MessageViewHolder holder){
       final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
