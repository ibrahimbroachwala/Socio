package android.chatapp.ib.ichat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private Button mStatusButton;
    private TextInputLayout mStatusText;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrUser;
    private ProgressDialog pd;


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();


        if(mUser != null)
        mDatabase.child("online").setValue(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        pd = new ProgressDialog(this);

        String statusString = getIntent().getStringExtra("statusString");

        mStatusText = (TextInputLayout) findViewById(R.id.status_input);
        mStatusButton = (Button) findViewById(R.id.status_change_but);

        mStatusText.getEditText().setText(statusString);

        mAuth = FirebaseAuth.getInstance();
        mCurrUser = mAuth.getCurrentUser();
        String uid = mCurrUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


        mStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Updating Status");
                pd.show();
                String status = mStatusText.getEditText().getText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            pd.dismiss();
                    }
                });
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.status_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
}
