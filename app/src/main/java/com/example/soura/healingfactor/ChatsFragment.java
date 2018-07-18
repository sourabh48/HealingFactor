package com.example.soura.healingfactor;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;

    public ChatsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);

        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);

        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override

    public void onStart()
    {
        super.onStart();

        Query conversationQuery  = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options = new FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(conversationQuery , Conv.class)
                .setLifecycleOwner(this)
                .build();

        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options)
        {
            @Override
            public ConvViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_list, parent, false);

                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ConvViewHolder holder, int position, final Conv model)
            {
                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data, model.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}

                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(userName);
                        holder.setUserImage(userThumb, getContext());
                        holder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        };

        mConvList.setAdapter(adapter);
    }


    public static class ConvViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public ConvViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }


        public void setMessage(String message, boolean isSeen)
        {
            TextView userStatusView = (TextView) mView.findViewById(R.id.status_text);
            userStatusView.setText(message);


            if(!isSeen)
            {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else{
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.name_text);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image,Context ctx)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.profile_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile).into(userImageView);
        }
    }
}