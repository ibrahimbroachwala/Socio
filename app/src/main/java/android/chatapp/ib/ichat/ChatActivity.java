package android.chatapp.ib.ichat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatuser,mChatusername;
    private Toolbar mtoolbar;
    private DatabaseReference mRootref;

    private FirebaseAuth mAuth;

    private TextView username_tv,lastseen_tv;
    private CircleImageView muserdp;
    
    
    private ImageButton addbut,sendbut;
    private EditText messagetext;


    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser curr_user = mAuth.getCurrentUser();

        if (curr_user != null) {
            mRootref.child("Users").child(curr_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser curr_user = mAuth.getCurrentUser();

        if (curr_user != null) {
            mRootref.child("Users").child(curr_user.getUid()).child("online").setValue("true");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mtoolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mtoolbar);

        mAuth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatuser = getIntent().getStringExtra("userid");
        mChatusername = getIntent().getStringExtra("username");
        //getSupportActionBar().setTitle(mChatusername);

        mRootref = FirebaseDatabase.getInstance().getReference();
        mRootref.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(actionbar_view);


        lastseen_tv = (TextView) findViewById(R.id.chat_user_last_seen);
        username_tv = (TextView) findViewById(R.id.chat_user_name);
        muserdp = (CircleImageView) findViewById(R.id.chat_user_dp);
        
        addbut = (ImageButton) findViewById(R.id.chat_add_but);
        sendbut = (ImageButton) findViewById(R.id.chat_send_but);
        messagetext = (EditText) findViewById(R.id.chat_message_text); 
        

        username_tv.setText(mChatusername);

        lastseen_tv.setVisibility(View.INVISIBLE);

        mRootref.child("Users").child(mChatuser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online.equals("true")){
                    lastseen_tv.setText("online");
                    lastseen_tv.setVisibility(View.VISIBLE);

                }else {
                    GetTimeAgo gta = new GetTimeAgo();
                    long lastime = Long.parseLong(online);
                    String lastseentime = gta.getTimeAgo(lastime,getApplicationContext());
                    lastseen_tv.setText(lastseentime);
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



        mRootref.child("Chat").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatuser)){

                    Map chataddmap = new HashMap();
                    chataddmap.put("seen",false);
                    chataddmap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatusermap = new HashMap();
                    chatusermap.put("Chat/"+mAuth.getCurrentUser().getUid()+"/"
                    +mChatuser,chataddmap);
                    chatusermap.put("Chat/"+mChatuser+"/"
                            +mAuth.getCurrentUser().getUid(),chataddmap);

                    mRootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){

                                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        
        
        sendbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                sendmessage();
            }
            
        });
        
    }

    private void sendmessage() {

        String message = messagetext.getText().toString();
        String Uid = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(message)){

            String curruserref = "messages/"+Uid+"/"+mChatuser;
            String chatuserref = "messages/"+mChatuser+"/"+Uid;

            DatabaseReference usermessage_push = mRootref.child("messages")
                    .child(Uid).child(mChatuser).push();

            String push_id = usermessage_push.getKey();

            Map messagemap = new HashMap();
            messagemap.put("message",message);
            messagemap.put("seen",false);
            messagemap.put("type","text");
            messagemap.put("time",ServerValue.TIMESTAMP);


            Map messageUsermap = new HashMap();
            messageUsermap.put(curruserref+"/"+push_id,messagemap);
            messageUsermap.put(chatuserref+"/"+push_id,messagemap);


            mRootref.updateChildren(messageUsermap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }

    }
}
