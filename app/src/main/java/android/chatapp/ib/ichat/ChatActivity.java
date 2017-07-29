package android.chatapp.ib.ichat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatuser,mChatusername;
    private Toolbar mtoolbar;
    private DatabaseReference mRootref;

    private TextView username_tv,lastseen_tv;
    private CircleImageView muserdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mtoolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mtoolbar);


        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatuser = getIntent().getStringExtra("userid");
        mChatusername = getIntent().getStringExtra("username");
        //getSupportActionBar().setTitle(mChatusername);

        mRootref = FirebaseDatabase.getInstance().getReference();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(actionbar_view);


        lastseen_tv = (TextView) findViewById(R.id.chat_user_last_seen);
        username_tv = (TextView) findViewById(R.id.chat_user_name);
        muserdp = (CircleImageView) findViewById(R.id.chat_user_dp);

        username_tv.setText(mChatusername);

        lastseen_tv.setVisibility(View.INVISIBLE);

        mRootref.child("Users").child(mChatuser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean online =(boolean) dataSnapshot.child("online").getValue();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online){
                    lastseen_tv.setText("online");
                    lastseen_tv.setVisibility(View.VISIBLE);
                }else {
                    lastseen_tv.setText("offline");
                    lastseen_tv.setVisibility(View.VISIBLE);
                }

                if(image!=null){
                    Picasso.with(ChatActivity.this).load(image)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .into(muserdp);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
