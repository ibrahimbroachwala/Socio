package android.chatapp.ib.ichat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {


    private Button regbut,logbut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        regbut = (Button) findViewById(R.id.start_reg_but);
        logbut = (Button) findViewById(R.id.start_login_but);

        logbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log_intent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(log_intent);
            }
        });
        regbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent reg_intent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

    }
}
