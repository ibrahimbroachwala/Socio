package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView mprofile_name,mprofile_status,mprofile_friendscount;
    private ImageView mprofile_image;
    private Button mprofile_frndreq_but;

    private ProgressDialog pd;

    private int curr_state;

    private DatabaseReference mDatabase;
    private DatabaseReference mFriendreqDatabase;
    private DatabaseReference mFriendDatabase;
    private String Uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading user Profile..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        curr_state = 0;

        final String user_key = getIntent().getStringExtra("userid");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("Friendreq");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mprofile_friendscount = (TextView) findViewById(R.id.prof_frcount);
        mprofile_name =(TextView) findViewById(R.id.prof_name);
        mprofile_status = (TextView) findViewById(R.id.prof_status);
        mprofile_frndreq_but = (Button) findViewById(R.id.prof_frreq_but);
        mprofile_image = (ImageView) findViewById(R.id.prof_image);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String dname = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mprofile_status.setText(status);
                mprofile_name.setText(dname);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.ic_person_black_24dp).into(mprofile_image);

                mFriendreqDatabase.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_key)){
                            String req_type = dataSnapshot.child(user_key).child("req_type").getValue().toString();
                            if(req_type.equals("received")){
                                mprofile_frndreq_but.setText("Accept Friend Request");
                                curr_state = 2;
                            } else if(req_type.equals("sent")){
                                mprofile_frndreq_but.setText("Cancel Friend Request");
                                curr_state = 1;
                            }
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                mFriendDatabase.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_key)){
                            curr_state = 3 ;
                            mprofile_frndreq_but.setText("Unfriend");
                        }
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


        mprofile_frndreq_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprofile_frndreq_but.setEnabled(false);

                //Send Friend Request

                if(curr_state == 0){
                    mFriendreqDatabase.child(Uid).child(user_key).child("req_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendreqDatabase.child(user_key).child(Uid).child("req_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mprofile_frndreq_but.setEnabled(true);
                                                curr_state = 1;
                                                mprofile_frndreq_but.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this, "Request sent Succesfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else{
                                Toast.makeText(ProfileActivity.this, "Friend req unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                //Cancel friend Request

                if(curr_state == 1) {
                    mprofile_frndreq_but.setEnabled(false);
                    mFriendreqDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendreqDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mprofile_frndreq_but.setEnabled(true);
                                    curr_state = 0;
                                    mprofile_frndreq_but.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                //Accept Friend request

                if (curr_state == 2){

                    final String currDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    pd = new ProgressDialog(ProfileActivity.this);
                    pd.setMessage("Accepting Friend Request..");
                    pd.show();

                    mFriendDatabase.child(Uid).child(user_key).setValue(currDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_key).child(Uid).setValue(currDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendreqDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendreqDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    pd.dismiss();
                                                                    mprofile_frndreq_but.setEnabled(true);
                                                                    curr_state = 3;
                                                                    mprofile_frndreq_but.setText("Unfriend");
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                            });
                                }
                            });
                }

                //Unfriend

                if(curr_state==3){
                    mFriendDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mprofile_frndreq_but.setEnabled(true);
                                    curr_state =0;
                                    mprofile_frndreq_but.setText("Send Friend Request");
                                }
                            });
                        }
                    });

                }
            }
        });
    }
}
