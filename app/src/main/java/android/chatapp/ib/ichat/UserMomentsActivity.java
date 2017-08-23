package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserMomentsActivity extends AppCompatActivity {


    private String Uid;
    private FirebaseAuth mAuth;

    private DatabaseReference postRef;
    private DatabaseReference userRef;
    private DatabaseReference allPostsRef;
    private DatabaseReference friendsRef;
    private DatabaseReference usersPostRef;

    private RecyclerView moments_rv;
    private ArrayList<String> friends_IdList;

    private ProgressDialog pd;
    private LinearLayoutManager linearLayoutManager;


    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_moments);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.user_moments_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Moments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();


        linearLayoutManager = new LinearLayoutManager(UserMomentsActivity.this);
        linearLayoutManager.setReverseLayout(true);

        user_id = getIntent().getStringExtra("user_id");




        pd = new ProgressDialog(UserMomentsActivity.this);

        postRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        friends_IdList = new ArrayList<String>();
        moments_rv = (RecyclerView) findViewById(R.id.user_moments_rv);
        moments_rv.setHasFixedSize(true);
        moments_rv.setLayoutManager(linearLayoutManager);

        usersPostRef.keepSynced(true);
        allPostsRef.keepSynced(true);
        userRef.keepSynced(true);
        postRef.keepSynced(true);


        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    friends_IdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        friends_IdList.add(Uid);



        FirebaseRecyclerAdapter<Moment,MomentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Moment, MomentsViewHolder>(
                Moment.class,
                R.layout.moment_item,
                MomentsViewHolder.class,
                usersPostRef.child(user_id)
        ) {

            @Override
            protected void populateViewHolder(final MomentsViewHolder viewHolder, Moment model, int position) {

                final String By = model.getBy();
                long timestamp = model.getTimestamp();

                viewHolder.initialize();

                final String moment_id = getRef(position).getKey();

                GetTimeAgo gta = new GetTimeAgo();
                final String momentposttime = gta.getTimeAgo(timestamp);
                viewHolder.setTime(momentposttime);



                if(By.equals(Uid)){
                    viewHolder.mom_delete_view.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.mom_delete_view.setVisibility(View.GONE);
                }


                viewHolder.mom_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserMomentsActivity.this);
                        alertDialogBuilder.setTitle("Confirm").setMessage("Are you sure you want to delete your moment?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                pd.setMessage("Deleting moment");
                                pd.show();
                                for(final String id: friends_IdList){
                                    postRef.child(id).child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersPostRef.child(id).child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                        }
                                    });
                                }
                                allPostsRef.child(moment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UserMomentsActivity.this, "Moment Deleted", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                });

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

                    }
                });



                postRef.child(Uid).child(moment_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String lliked = "false";
                        if(dataSnapshot.hasChild("liked"))
                        lliked = dataSnapshot.child("liked").getValue().toString();

                        final String liked = lliked;
                        if(liked.equals("true"))
                            viewHolder.likes_but.setBackgroundResource(R.drawable.liked);
                        else
                            viewHolder.likes_but.setBackgroundResource(R.drawable.unliked);


                        allPostsRef.child(moment_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                int llikes;
                                if(dataSnapshot.hasChild("likes"))
                                    llikes = Integer.valueOf(dataSnapshot.child("likes").getValue().toString());
                                else
                                    llikes = 0;

                                final int likes = llikes;
                                viewHolder.setLikes(likes);

                                viewHolder.likes_but.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if(liked.equals("false")){
                                            postRef.child(Uid).child(moment_id).child("liked").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    allPostsRef.child(moment_id).child("likes").setValue(likes+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(UserMomentsActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }else{
                                            postRef.child(Uid).child(moment_id).child("liked").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    allPostsRef.child(moment_id).child("likes").setValue(likes-1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(UserMomentsActivity.this, "Unliked", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });






                allPostsRef.child(moment_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild("type")) {
                                if (dataSnapshot.child("type").getValue().toString().equals("text")) {
                                    viewHolder.setText(dataSnapshot.child("text").getValue().toString());
                                    viewHolder.imageView.setVisibility(View.GONE);
                                } else {
                                    viewHolder.imageView.setVisibility(View.VISIBLE);
                                    viewHolder.setText(dataSnapshot.child("text").getValue().toString());
                                    viewHolder.setImage(dataSnapshot.child("image").getValue().toString(), UserMomentsActivity.this);
                                }
                            }

                            if(dataSnapshot.hasChild("text")) {
                                if (dataSnapshot.child("text").getValue().toString().equals("")) {
                                    viewHolder.textView.setVisibility(View.GONE);
                                } else {
                                    viewHolder.textView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.comments_but.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(UserMomentsActivity.this,CommentsActivity.class);
                        commentIntent.putExtra("moment_id",moment_id);
                        commentIntent.putExtra("name",By);
                        commentIntent.putExtra("time",momentposttime);
                        startActivity(commentIntent);
                    }
                });






                allPostsRef.child(moment_id).child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setCommentsCount(((int) dataSnapshot.getChildrenCount()));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





                userRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String username;
                        username = dataSnapshot.child("name").getValue().toString();
                        String thumbimage = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setDp(thumbimage,UserMomentsActivity.this);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        moments_rv.setAdapter(firebaseRecyclerAdapter);
        linearLayoutManager.smoothScrollToPosition(moments_rv,null,0);
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                moments_rv.smoothScrollToPosition(positionStart+1);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
            }
        });

    }

    public static class MomentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView likes_but;
        LinearLayout imageView;
        LinearLayout textView;
        ImageView comments_but;
        ImageView mom_delete;
        LinearLayout mom_delete_view;

        public MomentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void initialize(){
            likes_but = (ImageView) mView.findViewById(R.id.mom_likes_image);
            imageView = (LinearLayout) mView.findViewById(R.id.mom_image_view);
            textView = (LinearLayout) mView.findViewById(R.id.mom_text_view);
            comments_but = (ImageView) mView.findViewById(R.id.mom_comment_but);
            mom_delete = (ImageView) mView.findViewById(R.id.mom_item_delete);
            mom_delete_view = (LinearLayout) mView.findViewById(R.id.delete_comment_view);
        }


        public void setCommentsCount(int count){
            TextView setcount_tv = (TextView) mView.findViewById(R.id.mom_comments_text);
            setcount_tv.setText(count+"");
        }
        public void setImage(String image, Context ctx){
            ImageView moment_image = (ImageView) mView.findViewById(R.id.mom_image_item);
            Picasso.with(ctx).load(image).placeholder(R.drawable.image_load_anim).into(moment_image);
        }
        public void setText(String text){
            TextView text_tv = (TextView) mView.findViewById(R.id.mom_text_item);
            text_tv.setText(text);
        }

        public void setLikes(final int likes){
            TextView setLikes_tv = (TextView) mView.findViewById(R.id.mom_likes_text);
            setLikes_tv.setText(likes+"");
        }


        public void setName(String name){
            TextView nameview = (TextView) mView.findViewById(R.id.mom_name);
            nameview.setText(name);
        }
        public void setDp(String thumb_image, Context ctx){
            CircleImageView userdp = (CircleImageView) mView.findViewById(R.id.mom_dp);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.ic_person_black_24dp).into(userdp);
        }

        public void setTime(String time){
            TextView timeview = (TextView) mView.findViewById(R.id.mom_time);
            timeview.setText(time);
        }
    }

}
