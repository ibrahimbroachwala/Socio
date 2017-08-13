package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddPostActivity extends AppCompatActivity {


    private Button post_image_but;
    private Button submit_post_but;
    private EditText post_text_et;
    private ImageView post_imageView;
    private Uri resultUri;
    private byte[] image_data;

    private DatabaseReference allPostsRef;
    private DatabaseReference postsToShowRef;
    private DatabaseReference usersPostRef;
    private DatabaseReference friendsRef;

    private StorageReference mStorageref;

    private ProgressDialog pd;

    private ArrayList<String> friends_IdList;

    private FirebaseAuth mAuth;
    private String Uid;

    private Bitmap compressedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        pd = new ProgressDialog(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.new_post_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Share Moment");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();

        friends_IdList = new ArrayList<String>();


        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        postsToShowRef = FirebaseDatabase.getInstance().getReference().child("PostsToShow");
        usersPostRef = FirebaseDatabase.getInstance().getReference().child("UsersPost").child(Uid);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);

        mStorageref = FirebaseStorage.getInstance().getReference();

        allPostsRef.keepSynced(true);
        postsToShowRef.keepSynced(true);
        usersPostRef.keepSynced(true);
        friendsRef.keepSynced(true);


        post_image_but = (Button) findViewById(R.id.new_post_addphoto_but);
        submit_post_but = (Button) findViewById(R.id.sub_post_but);
        post_text_et = (EditText) findViewById(R.id.new_post_text);
        post_imageView = (ImageView) findViewById(R.id.new_post_image);

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

        post_image_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .start(AddPostActivity.this);

            }
        });


        submit_post_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                pd.setMessage("Sharing...");
                pd.show();

                final String text = post_text_et.getText().toString();

                DatabaseReference userpost_push = allPostsRef.push();
                final String push_id = userpost_push.getKey();

                StorageReference posts_image_ref = mStorageref.child("Posts").child(Uid).child(push_id+".jpg");

                if(compressedImageBitmap!=null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    image_data = baos.toByteArray();

                    UploadTask uploadTask = posts_image_ref.putBytes(image_data);

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            @SuppressWarnings("VisibleForTests") String download_url = task.getResult().getDownloadUrl().toString();
                        final Map imagemap = new HashMap();
                            imagemap.put("type","image");
                            imagemap.put("timestamp", ServerValue.TIMESTAMP);
                            imagemap.put("by",Uid);
                            imagemap.put("text",text);
                            imagemap.put("image",download_url);
                            imagemap.put("likes",0);

                            final Map timeby = new HashMap();
                            timeby.put("timestamp",ServerValue.TIMESTAMP);
                            timeby.put("by",Uid);
                            timeby.put("liked","false");

                            allPostsRef.child(push_id).setValue(imagemap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    for(String id: friends_IdList){
                                        postsToShowRef.child(id).child(push_id).setValue(timeby).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                usersPostRef.child(push_id).setValue(timeby).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(AddPostActivity.this, "Succesfully Posted", Toast.LENGTH_SHORT).show();
                                                        post_text_et.setText("");
                                                        pd.dismiss();
                                                        finish();

                                                    }
                                                });

                                            }
                                        });

                                    }

                                }
                            });
                        }
                    });
                }else {
                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(AddPostActivity.this, "Nothing to Share..", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                    else {

                        final Map textmap = new HashMap();
                        textmap.put("type", "text");
                        textmap.put("timestamp", ServerValue.TIMESTAMP);
                        textmap.put("by", Uid);
                        textmap.put("text", text);
                        textmap.put("image", "noimage");
                        textmap.put("likes", 0);

                        final Map timeby2 = new HashMap();
                        timeby2.put("timestamp", ServerValue.TIMESTAMP);
                        timeby2.put("by", Uid);
                        timeby2.put("liked", "false");

                        allPostsRef.child(push_id).setValue(textmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                for (String id : friends_IdList) {
                                    postsToShowRef.child(id).child(push_id).setValue(timeby2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            usersPostRef.child(push_id).setValue(timeby2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(AddPostActivity.this, "Succesfully Posted", Toast.LENGTH_SHORT).show();
                                                    post_text_et.setText("");
                                                    pd.dismiss();
                                                    finish();

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

               resultUri = result.getUri();

                File image_file = new File(resultUri.getPath());

                compressedImageBitmap = new Compressor(this)
                        .setMaxHeight(800)
                        .setMaxWidth(800)
                        .setQuality(2)
                        .compressToBitmap(image_file);

//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                image_data = baos.toByteArray();

                post_imageView.setImageBitmap(compressedImageBitmap);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
