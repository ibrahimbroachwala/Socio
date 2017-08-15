package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.bitmap;
import static android.R.attr.data;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference mDatabase;
    private FirebaseUser mCurrUser;
    private FirebaseAuth mAuth;

    private CircleImageView dp;
    private TextView mStatus,mDname;
    private ImageButton mChangeStatusbut;
    private ImageButton mChangeDpbut;

    private Button logout_but;

    private static final int GALLERY_PICK = 1;
    private StorageReference mStorageRef;

    private String Uid;

    private ProgressDialog pd;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
        mDatabase.child("online").setValue("true");

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
            mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();


        mStatus = (TextView) findViewById(R.id.settings_status);
        mDname = (TextView) findViewById(R.id.settings_dname);
        logout_but = (Button) findViewById(R.id.settings_logout_but);
        dp = (CircleImageView) findViewById(R.id.settings_dp);
        mChangeStatusbut = (ImageButton) findViewById(R.id.settings_edit_status_but);
        mChangeDpbut = (ImageButton) findViewById(R.id.settings_edit_image_but);

        mCurrUser = FirebaseAuth.getInstance().getCurrentUser();
        Uid = mCurrUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
        mDatabase.keepSynced(true);


        logout_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                alertDialogBuilder.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
                        mAuth.signOut();
                        sendToStart();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });




        mChangeDpbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),GALLERY_PICK);
//                CropImage.activity()
//                        .start(SettingsActivity.this);
            }
        });

        mChangeStatusbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("statusString",mStatus.getText().toString());
                startActivity(statusIntent);
            }
        });


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                //String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                if(!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.image_load_anim).into(dp, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image)
                                    .placeholder(R.drawable.image_load_anim).into(dp);
                        }
                    });
                }
                mDname.setText(name);
                mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendToStart(){
        Intent start_int = new Intent(SettingsActivity.this, StartActivity.class);
        startActivity(start_int);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                uploadImage(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1,1)
                        .start(this);
            }
    }

    private void uploadImage(Uri resultUri) {

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading Image..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        File file = new File(resultUri.getPath());


        Bitmap compressedImageBitmap_thumb = new Compressor(this)
                .setMaxHeight(200)
                .setMaxWidth(200)
                .setQuality(75)
                .compressToBitmap(file);

        Bitmap compressedImageBitmap_prof = new Compressor(this)
                .setMaxHeight(400)
                .setMaxWidth(400)
                .setQuality(60)
                .compressToBitmap(file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageBitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        compressedImageBitmap_prof.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        final byte[] thumb_data = baos.toByteArray();
        final byte[] bigdata = baos.toByteArray();

        StorageReference filepath = mStorageRef.child("profile_images").child(Uid+".jpg");
        final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(Uid+".jpg");

        UploadTask uploadTaskbig = filepath.putBytes(bigdata);
        uploadTaskbig.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    @SuppressWarnings("VisibleForTests") final String download_url = task.getResult().getDownloadUrl().toString();

                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_data);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                            @SuppressWarnings("VisibleForTests") String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                            if(thumb_task.isSuccessful()){

                                Map update_map = new HashMap();
                                update_map.put("image",download_url);
                                update_map.put("thumb_image",thumb_downloadUrl);

                                mDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pd.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }else{
                    pd.dismiss();
                    Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
