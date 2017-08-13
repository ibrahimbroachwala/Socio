package android.chatapp.ib.ichat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {


    
    DatabaseReference usersRef;
    DatabaseReference rootRef;
    TextView commentOnTv;
    RecyclerView commentsRv;
    Button subCommentBut;
    TextView commentOnTime;
    LinearLayoutManager llm;
    EditText AddCommentEt;
    FirebaseAuth mAuth;
    String Uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.comments_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AddCommentEt = (EditText) findViewById(R.id.Add_comment_et);
        commentOnTime = (TextView) findViewById(R.id.comment_on_time);
        subCommentBut = (Button) findViewById(R.id.sub_comment_but);
        commentsRv = (RecyclerView) findViewById(R.id.comments_rv);
        commentOnTv = (TextView) findViewById(R.id.comment_on_tv);
        



        


        final String moment_id = getIntent().getStringExtra("moment_id");
        String by = getIntent().getStringExtra("name");
        String time = getIntent().getStringExtra("time");
        commentOnTime.setText("shared "+time);

        llm = new LinearLayoutManager(this);
        commentsRv.setHasFixedSize(true);
        commentsRv.setLayoutManager(llm);

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();

        if(Uid.equals(by)){
            commentOnTv.setText("On Your moment");
        }else {
            usersRef.child(by).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    commentOnTv.setText("On " + name + "'s moment");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        loadcomments(moment_id);

        subCommentBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(moment_id);
            }
        });


    }

    private void postComment(String moment_id) {
        String commentToPost = AddCommentEt.getText().toString().trim();
        AddCommentEt.setText("");

        if(!TextUtils.isEmpty(commentToPost)) {

            Map commentMap = new HashMap<>();
            commentMap.put("timestamp", ServerValue.TIMESTAMP);
            commentMap.put("by", Uid);
            commentMap.put("text", commentToPost);

            rootRef.child("AllPosts").child(moment_id).child("comments").push().setValue(commentMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(CommentsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();

                }
            });
        }else{
            Toast.makeText(this, "Nothing to comment..", Toast.LENGTH_SHORT).show();
        }
        
        
        
    }

    private void loadcomments(final String moment_id) {


        DatabaseReference commentsRef = rootRef.child("AllPosts").child(moment_id).child("comments");

        commentsRef.keepSynced(true);
        Query commentQuery = commentsRef.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                Comments.class,
                R.layout.comment_item,
                CommentsViewHolder.class,
                commentQuery
        ) {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, Comments model, final int position) {

                String By = model.getBy();
                long timestamp = model.getTimestamp();
                String text = model.getText();

                GetTimeAgo gta = new GetTimeAgo();
                final String commentposttime = gta.getTimeAgo(timestamp);
                viewHolder.setTime(commentposttime);
                viewHolder.setText(text);
                
                
                if(By.equals(Uid)){
                    viewHolder.setDeleteView(true);
                    
                    viewHolder.delete_comment_but.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String comment_id = getRef(position).getKey();
                            rootRef.child("AllPosts").child(moment_id).child("comments")
                                    .child(comment_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(CommentsActivity.this, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    
                }else{
                    viewHolder.setDeleteView(false);
                }


                usersRef.child(By).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String username;
                        username = dataSnapshot.child("name").getValue().toString();
                        String thumbimage = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setDp(thumbimage, CommentsActivity.this);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        commentsRv.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {


        View mView;
        ImageView delete_comment_but;
        

        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        
        public void setDeleteView(boolean delete){
            
            delete_comment_but = (ImageView) mView.findViewById(R.id.comment_item_delete); 
            LinearLayout delete_comment_view = (LinearLayout) mView.findViewById(R.id.delete_comment_view);
            if(delete){
                delete_comment_view.setVisibility(View.VISIBLE);
            }else{
                delete_comment_view.setVisibility(View.GONE);
            }
            
        }

        public void setName(String name) {
            TextView friendtextview = (TextView) mView.findViewById(R.id.comment_item_name);
            friendtextview.setText(name);
        }

        public void setDp(String thumb_image, Context ctx) {
            CircleImageView userdp = (CircleImageView) mView.findViewById(R.id.comment_item_dp);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.ic_person_black_24dp).into(userdp);
        }

        public void setText(String text) {
            TextView commentText = (TextView) mView.findViewById(R.id.comment_item_text);
            commentText.setText(text);
        }

        public void setTime(String time) {
            TextView commentTime = (TextView) mView.findViewById(R.id.comment_item_time);
            commentTime.setText(time);
        }

    }

}
