package android.chatapp.ib.ichat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mprofile_name,mprofile_status,mprofile_friendscount;
    private CircleImageView mprofile_image;
    private Button mprofile_frndreq_but,mprof_frnreq_dec_but;




    private ProgressDialog pd;

    private int curr_state;

    private DatabaseReference mDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendreqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference rootref;
    private DatabaseReference notiref;
    private String Uid,user_key;


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
            mUserDatabase.child("online").setValue("true");

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading user Profile..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        curr_state = 0;

        user_key = getIntent().getStringExtra("from_user_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("Friendreq");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        notiref = rootref.child("Notifications");
        mUserDatabase = rootref.child("Users").child(Uid);
        mprofile_friendscount = (TextView) findViewById(R.id.prof_frcount);
        mprofile_name =(TextView) findViewById(R.id.prof_name);
        mprofile_status = (TextView) findViewById(R.id.prof_status);
        mprofile_frndreq_but = (Button) findViewById(R.id.prof_frreq_but);
        mprofile_image = (CircleImageView) findViewById(R.id.prof_image);
        mprof_frnreq_dec_but = (Button) findViewById(R.id.prof_frreq_decline_but);


        mDatabase.keepSynced(true);


        if(Uid.equals(user_key))
            mprofile_frndreq_but.setVisibility(View.GONE);

        mprof_frnreq_dec_but.setVisibility(View.GONE);
        mprof_frnreq_dec_but.setEnabled(false);



        mFriendDatabase.child(user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mprofile_friendscount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String dname = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mprofile_status.setText(status);
                mprofile_name.setText(dname);
                if(!image.equals("default"))
                Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.image_load_anim)
                        .into(mprofile_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ProfileActivity.this).load(image)
                                        .placeholder(R.drawable.image_load_anim).into(mprofile_image);
                            }
                        });

                mFriendreqDatabase.child(Uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_key)){
                            String req_type = dataSnapshot.child(user_key).child("req_type").getValue().toString();
                            if(req_type.equals("received")){
                                mprofile_frndreq_but.setText("Accept Friend Request");
                                curr_state = 2;

                                mprof_frnreq_dec_but.setVisibility(View.VISIBLE);
                                mprof_frnreq_dec_but.setEnabled(true);
                            } else if(req_type.equals("sent")){
                                mprofile_frndreq_but.setText("Cancel Friend Request");
                                curr_state = 1;

                                mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
                                mprof_frnreq_dec_but.setEnabled(false);
                            }
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                mFriendDatabase.child(Uid).addValueEventListener(new ValueEventListener() {
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

        //Decline friend request

        mprof_frnreq_dec_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd = new ProgressDialog(ProfileActivity.this);
                pd.setMessage("Declining request..");
                pd.show();


                mFriendreqDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendreqDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mprofile_frndreq_but.setEnabled(true);
                                curr_state = 0;
                                mprofile_frndreq_but.setText("Send Friend Request");
                                mprof_frnreq_dec_but.setVisibility(View.GONE);
                                mprof_frnreq_dec_but.setEnabled(false);

                                pd.dismiss();
                            }
                        });
                    }
                });



            }
        });


        mprofile_frndreq_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprofile_frndreq_but.setEnabled(false);

                //Send Friend Request

                if(curr_state == 0){


                    Map reqmap = new HashMap();
                    reqmap.put("Friendreq/"+Uid+"/"+user_key+"/req_type","sent");
                    reqmap.put("Friendreq/"+user_key+"/"+Uid+"/req_type","received");

                    final HashMap<String,String> notimap = new HashMap<String, String>();
                    notimap.put("from",Uid);
                    notimap.put("type","request");



                    rootref.updateChildren(reqmap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    notiref.child(user_key).push().setValue(notimap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mprofile_frndreq_but.setEnabled(true);
                                            curr_state = 1;
                                            mprofile_frndreq_but.setText("Cancel Friend Request");
                                            Toast.makeText(ProfileActivity.this, "Request sent Succesfully", Toast.LENGTH_SHORT).show();
                                            mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
                                            mprof_frnreq_dec_but.setEnabled(false);
                                        }
                                    });
                                }
                            });




//                    mFriendreqDatabase.child(Uid).child(user_key).child("req_type")
//                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//
//                                mFriendreqDatabase.child(user_key).child(Uid).child("req_type").setValue("received")
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//
//                                                mprofile_frndreq_but.setEnabled(true);
//                                                curr_state = 1;
//                                                mprofile_frndreq_but.setText("Cancel Friend Request");
//                                                Toast.makeText(ProfileActivity.this, "Request sent Succesfully", Toast.LENGTH_SHORT).show();
//                                                mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
//                                                mprof_frnreq_dec_but.setEnabled(false);
//
//                                            }
//                                        });
//
//                            }else{
//                                Toast.makeText(ProfileActivity.this, "Friend req unsuccessful", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

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

                                    mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
                                    mprof_frnreq_dec_but.setEnabled(false);
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


                    Map accfrndmap = new HashMap();
                    accfrndmap.put("Friends/"+Uid+"/"+user_key+"/date",currDate);
                    accfrndmap.put("Friends/"+user_key+"/"+Uid+"/date",currDate);

                    accfrndmap.put("Friendreq/"+Uid+"/"+user_key,null);
                    accfrndmap.put("Friendreq/"+user_key+"/"+Uid,null);


                    rootref.updateChildren(accfrndmap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    pd.dismiss();
                                    mprofile_frndreq_but.setEnabled(true);
                                    curr_state = 3;
                                    mprofile_frndreq_but.setText("Unfriend");

                                    mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
                                    mprof_frnreq_dec_but.setEnabled(false);
                                }
                            });

//                    mFriendDatabase.child(Uid).child(user_key).setValue(currDate)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    mFriendDatabase.child(user_key).child(Uid).setValue(currDate)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    mFriendreqDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            mFriendreqDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void aVoid) {
//
//                                                                    pd.dismiss();
//                                                                    mprofile_frndreq_but.setEnabled(true);
//                                                                    curr_state = 3;
//                                                                    mprofile_frndreq_but.setText("Unfriend");
//
//                                                                    mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
//                                                                    mprof_frnreq_dec_but.setEnabled(false);
//                                                                }
//                                                            });
//                                                        }
//                                                    });
//
//                                                }
//                                            });
//                                }
//                            });
                }

                //Unfriend

                if(curr_state==3){

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                    alertDialogBuilder.setTitle("Unfriend?").setMessage("You wil have to send request to be friends again.Unfriend?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            mFriendDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mprofile_frndreq_but.setEnabled(true);
                                            curr_state =0;
                                            mprofile_frndreq_but.setText("Send Friend Request");
                                            mprof_frnreq_dec_but.setVisibility(View.INVISIBLE);
                                            mprof_frnreq_dec_but.setEnabled(false);
                                        }
                                    });
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

                }
            }
        });
    }
}
