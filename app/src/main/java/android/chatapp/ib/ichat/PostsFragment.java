package android.chatapp.ib.ichat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ibrah on 8/9/2017.
 */

public class PostsFragment extends Fragment{


    DatabaseReference postRef;
    DatabaseReference userRef;
    DatabaseReference allPostsRef;

    LinearLayoutManager linearLayoutManager;


    FirebaseAuth mAuth;
    String Uid;
    RecyclerView posts_rv;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posts,container,false);

        mAuth = FirebaseAuth.getInstance();
        postRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");

        Uid = mAuth.getCurrentUser().getUid();

        posts_rv = (RecyclerView) v.findViewById(R.id.posts_rv);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setReverseLayout(true);


        posts_rv.setHasFixedSize(true);

        allPostsRef.keepSynced(true);
        postRef.child(Uid).keepSynced(true);
        userRef.keepSynced(true);


        posts_rv.setLayoutManager(linearLayoutManager);




        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab_add_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(getContext(), AddPostActivity.class);
                startActivity(intent);
            }
        });
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Moment,MomentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Moment, MomentsViewHolder>(
                Moment.class,
                R.layout.moment_item,
                MomentsViewHolder.class,
                postRef.child(Uid)
        ) {
            @Override
            protected void populateViewHolder(final MomentsViewHolder viewHolder, Moment model, int position) {

                final String By = model.getBy();
                long timestamp = model.getTimestamp();
                final String liked = model.getLiked();

                viewHolder.initialize();

                GetTimeAgo gta = new GetTimeAgo();
                final String momentposttime = gta.getTimeAgo(timestamp);
                viewHolder.setTime(momentposttime);

                final String moment_id = getRef(position).getKey();


                if(liked.equals("true"))
                    viewHolder.likes_but.setBackgroundResource(R.drawable.liked);
                else
                    viewHolder.likes_but.setBackgroundResource(R.drawable.unliked);

                allPostsRef.child(moment_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final int likes = Integer.valueOf(dataSnapshot.child("likes").getValue().toString());
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
                                                    Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
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
                                                    Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                }
                            }
                        });

                        if(dataSnapshot.child("type").getValue().toString().equals("text")){
                            viewHolder.setText(dataSnapshot.child("text").getValue().toString());
                            viewHolder.imageView.setVisibility(View.GONE);
                        }else {
                            viewHolder.imageView.setVisibility(View.VISIBLE);
                            viewHolder.setText(dataSnapshot.child("text").getValue().toString());
                            viewHolder.setImage(dataSnapshot.child("image").getValue().toString(),getContext());
                        }
                        if(dataSnapshot.child("text").getValue().toString().equals("")){
                            viewHolder.textView.setVisibility(View.GONE);
                        }else{
                            viewHolder.textView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.comments_but.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(getActivity(),CommentsActivity.class);
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
                        viewHolder.setDp(thumbimage,getContext());


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        posts_rv.setAdapter(firebaseRecyclerAdapter);
        linearLayoutManager.smoothScrollToPosition(posts_rv,null,0);
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                posts_rv.smoothScrollToPosition(positionStart+1);
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

        public MomentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void initialize(){
            likes_but = (ImageView) mView.findViewById(R.id.mom_likes_image);
            imageView = (LinearLayout) mView.findViewById(R.id.mom_image_view);
            textView = (LinearLayout) mView.findViewById(R.id.mom_text_view);
            comments_but = (ImageView) mView.findViewById(R.id.mom_comment_but);
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
