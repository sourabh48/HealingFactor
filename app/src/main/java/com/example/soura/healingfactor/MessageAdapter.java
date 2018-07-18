package com.example.soura.healingfactor;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    String date;

    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout ,parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName,messageTime;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            messageTime = (TextView) view.findViewById(R.id.time_text_layout);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();
        long timestamplong = c.getTime();


        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(timestamplong);
        date = DateFormat.format("hh:mm a", cal.getTimeInMillis()).toString();

        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        formatter.setTimeZone(TimeZone.getDefault());
        Date value = null;
        try
        {
            value = formatter.parse(date);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        date = dateFormatter.format(value);

        viewHolder.messageTime.setText(date);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);

                Picasso.get().load(image).placeholder(R.drawable.profile).into(viewHolder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text"))
        {
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }
        else
        {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.profile).into(viewHolder.messageImage);
        }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }
}