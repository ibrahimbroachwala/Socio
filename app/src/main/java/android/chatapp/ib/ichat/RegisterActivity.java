package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private TextInputLayout mname, memail, mpass;
    private Button sub;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        Toolbar myToolbar = (Toolbar) findViewById(R.id.reg_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Create new Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);

        mname = (TextInputLayout) findViewById(R.id.et_dis_name);
        memail = (TextInputLayout) findViewById(R.id.et_email);
        mpass = (TextInputLayout) findViewById(R.id.et_pass);
        sub = (Button) findViewById(R.id.sub_but);

        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mname.getEditText().getText().toString().trim();
                String email = memail.getEditText().getText().toString().trim();
                String pass = mpass.getEditText().getText().toString().trim();

                int count = pass.length();

                if(TextUtils.isEmpty(name)){
                    mname.setError("Empty Field");
                }else if(TextUtils.isEmpty(email)){
                    memail.setError("Empty Field");
                }else if(TextUtils.isEmpty(pass)){
                   mpass.setError("Empty Field");
                }else if(count<6){
                    mpass.setError("Minimum 6 characters");

                }else {
                    register_user(name, email, pass);
                }
            }
        });


    }

    private void register_user(final String name, String email, String pass) {

        pd.setMessage("Registering...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = null;
                            if(curr_user!=null)
                            uid = curr_user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<String, String>();
                            userMap.put("name",name);
                            userMap.put("status","On Socio");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        pd.dismiss();
                                        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(mainintent);
                                        finish();
                                    }


                                }
                            });




                        }else{
                            pd.hide();
                            Toast.makeText(RegisterActivity.this,"Error",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
