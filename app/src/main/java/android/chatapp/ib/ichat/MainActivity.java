package android.chatapp.ib.ichat;

import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private ViewPager mviewpager;
    private MainPagerAdapter mPa;
    private TabLayout mTabLayout;

    private DatabaseReference mUserref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Ichat home");

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        mAuth = FirebaseAuth.getInstance();
        mUserref = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());

        mviewpager = (ViewPager) findViewById(R.id.tab_pager);
        mPa = new MainPagerAdapter(getSupportFragmentManager());

        mviewpager.setAdapter(mPa);

        mTabLayout.setupWithViewPager(mviewpager);

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser curr_user = mAuth.getCurrentUser();

        if (curr_user != null) {
            mUserref.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser curr_user = mAuth.getCurrentUser();

        if(curr_user == null){
            sendToStart();
        }else{

            mUserref.child("online").setValue("true");
        }
    }


    public void sendToStart(){
        Intent start_int = new Intent(MainActivity.this, StartActivity.class);
        startActivity(start_int);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        switch (id)
        {

            case R.id.logout_but :
                mAuth.signOut();
                sendToStart();
                break;
            case R.id.settings_but:
                Intent set_intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(set_intent);
                break;
            case  R.id.allusers_but:

                Intent allusersIntent = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(allusersIntent);

        }
        return super.onOptionsItemSelected(item);
    }
}
