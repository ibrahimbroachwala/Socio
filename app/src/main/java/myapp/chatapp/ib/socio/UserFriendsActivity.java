package myapp.chatapp.ib.socio;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFriendsActivity extends AppCompatActivity {


    private DatabaseReference userRef;
    private DatabaseReference mRootref;
    private DatabaseReference friendsRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private String Uid;
    private String user_id;
    private RecyclerView userfriends_rv;
    private String receivedText;
    public Uri receivedUri;

    private int MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friends);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.user_friends_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userfriends_rv = (RecyclerView) findViewById(R.id.user_friends_rv);
        userfriends_rv.setHasFixedSize(true);
        userfriends_rv.setLayoutManager(new LinearLayoutManager(this));

        getSupportActionBar().setTitle("Friends");

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();



        if(getIntent().getExtras().containsKey("user_id")) {
            user_id = getIntent().getStringExtra("user_id");
            MODE = 0;
            getSupportActionBar().setTitle("Friends");
        }
        else {
            receivedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            receivedUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            user_id = Uid;
            MODE = 1;
            getSupportActionBar().setTitle("Share to..");
        }


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user_id);
        mRootref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        friendsRef.keepSynced(true);
        userRef.keepSynced(true);


       final FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, UsersViewHolder>(
               Friends.class,
                R.layout.user_item,
                UsersViewHolder.class,
               friendsRef
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, Friends model, int position) {
                final String friend_user_id = getRef(position).getKey();


                userRef.child(friend_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        String thumbimage = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();


                        viewHolder.setName(username);
                        viewHolder.setDp(thumbimage,UserFriendsActivity.this);
                        viewHolder.setStatus(status);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(MODE==0) {
                                    Intent profIntent = new Intent(UserFriendsActivity.this, ProfileActivity.class);
                                    profIntent.putExtra("from_user_id", friend_user_id);
                                    startActivity(profIntent);
                                }else if(MODE==1){
                                    if(receivedText!=null)
                                    sendmessage(friend_user_id);
                                    if(receivedUri!=null)
                                    uploadImage(receivedUri,friend_user_id);
                                    Intent chatIntent = new Intent(UserFriendsActivity.this,ChatActivity.class);
                                    chatIntent.putExtra("from_user_id", friend_user_id);
                                    chatIntent.putExtra("from_username",username);
                                    startActivity(chatIntent);
                                    finish();
                                }
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        userfriends_rv.setAdapter(firebaseRecyclerAdapter);

    }




    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser muser = FirebaseAuth.getInstance().getCurrentUser();
        if(muser!=null)
           userRef.child(muser.getUid()).child("online").setValue("true");

    }


    private void uploadImage(Uri resultUri,final String mChatuser) {

        Random random = new Random();

        Map chatusermap = new HashMap();
        chatusermap.put("Chat/"+mChatuser+"/"
                +Uid+"/timestamp",ServerValue.TIMESTAMP);
        chatusermap.put("Chat/"+Uid+"/"+mChatuser+"/timestamp",ServerValue.TIMESTAMP);

        mRootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                }
            }
        });

        StorageReference filepath = mStorageRef.child("image_messages").child(Uid).child(mChatuser).child(random.nextInt(10000000)+".jpg");
//        File image_file = new File(getRealPathFromURI(resultUri));
//        Bitmap compressedImageBitmap = new Compressor(this)
//                .setMaxHeight(400)
//                .setMaxWidth(400)
//                .setQuality(2)
//                .compressToBitmap(image_file);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        final byte[] image_data = baos.toByteArray();
//

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(
                    resultUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bmp = BitmapFactory.decodeStream(imageStream);
        bmp = resize(bmp,800,800);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                @SuppressWarnings("VisibleForTests") String download_url = task.getResult().getDownloadUrl().toString();
                if(task.isSuccessful()){
                    String curruserref = "messages/"+Uid+"/"+mChatuser;
                    String chatuserref = "messages/"+mChatuser+"/"+Uid;

                    DatabaseReference usermessage_push = mRootref.child("messages")
                            .child(Uid).child(mChatuser).push();

                    String push_id = usermessage_push.getKey();

                    Map messagemap = new HashMap();
                    messagemap.put("message",download_url);
                    messagemap.put("seen",false);
                    messagemap.put("type","image");
                    messagemap.put("time",ServerValue.TIMESTAMP);
                    messagemap.put("from",Uid);


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
        });
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }



    private void sendmessage(final String mChatuser) {

        final String Uid = mAuth.getCurrentUser().getUid();


        if(!TextUtils.isEmpty(receivedText)){

        Map chatusermap = new HashMap();
        chatusermap.put("Chat/"+mChatuser+"/"
                +Uid+"/timestamp", ServerValue.TIMESTAMP);
        chatusermap.put("Chat/"+Uid+"/"+mChatuser+"/timestamp",ServerValue.TIMESTAMP);

        mRootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                }
            }
        });



            mRootref.child("Chat").child(mChatuser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(Uid)){
                        Map chataddmap = new HashMap();
                        chataddmap.put("seen","false");
                        chataddmap.put("timestamp",ServerValue.TIMESTAMP);

                        Map chatusermap = new HashMap();
                        chatusermap.put("Chat/"+mChatuser+"/"
                                +Uid,chataddmap);

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

            String curruserref = "messages/"+Uid+"/"+mChatuser;
            String chatuserref = "messages/"+mChatuser+"/"+Uid;

            DatabaseReference usermessage_push = mRootref.child("messages")
                    .child(Uid).child(mChatuser).push();

            String push_id = usermessage_push.getKey();

            Map messagemap = new HashMap();
            messagemap.put("message",receivedText);
            messagemap.put("seen",false);
            messagemap.put("type","text");
            messagemap.put("time",ServerValue.TIMESTAMP);
            messagemap.put("from",Uid);


            Map messageUsermap = new HashMap();
            messageUsermap.put(curruserref+"/"+push_id,messagemap);
            messageUsermap.put(chatuserref+"/"+push_id,messagemap);


            mRootref.updateChildren(messageUsermap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }else{


                        mRootref.child("Users").child(mChatuser).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String online = dataSnapshot.getValue().toString();

                                if(!online.equals("true")){

                                    Map notimap = new HashMap();
                                    notimap.put("from",Uid);
                                    notimap.put("type","message");
                                    notimap.put("message",receivedText);

                                    mRootref.child("MessageNoti").child(mChatuser).push().setValue(notimap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                }
            });
        }

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setStatus(String status){
            TextView user_status_tv = (TextView) mView.findViewById(R.id.user_item_status);
            user_status_tv.setText(status);
        }
        public void setName(String name){

            TextView user_name_tv = (TextView) mView.findViewById(R.id.user_item_name);
            user_name_tv.setText(name);
        }

        public void setDp(String thumb_image, Context ctx){
            CircleImageView userdp = (CircleImageView) mView.findViewById(R.id.user_item_dp);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.ic_person_black_24dp).into(userdp);
        }
    }
}
