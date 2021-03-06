package com.example.soura.healingfactor;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class RequestFragment extends Fragment {
    private RecyclerView mFriendsList;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mRequestDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_User_ID;
    private View mMainView;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_User_ID = mAuth.getCurrentUser().getUid();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_requests").child(mCurrent_User_ID);
        mRequestDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mRequestDatabase.orderByChild("time");

        FirebaseRecyclerOptions<Requests> options = new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(query, Requests.class)
                .setLifecycleOwner(this)
                .build();


        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(options) {

            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_list, parent, false);

                return new RequestViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(final RequestViewHolder requestviewHolder, int position, Requests model) {

                requestviewHolder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        requestviewHolder.setName(userName);
                        requestviewHolder.setUser_Thumbs(userThumb, getApplicationContext());

                        final String profile_uid =getRef(requestviewHolder.getAdapterPosition()).getKey();

                        requestviewHolder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                profileIntent.putExtra("user_id",profile_uid);
                                startActivity(profileIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(adapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.name_text);
            userNameView.setText(name);

        }

        public void setDate(String date)
        {
            TextView userNameView=(TextView)mView.findViewById(R.id.status_text);
            userNameView.setText(date);
        }

        public void setUser_Thumbs(final String thumb_image, final Context ctx)
        {
            final CircleImageView userImageView =(CircleImageView)mView.findViewById(R.id.profile_image);
            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(userImageView, new Callback() {
                @Override
                public void onSuccess()
                {}

                @Override
                public void onError(Exception e)
                {
                    Picasso.get().load(thumb_image).placeholder(R.drawable.profile).into(userImageView);
                }
            });
        }
    }
}
