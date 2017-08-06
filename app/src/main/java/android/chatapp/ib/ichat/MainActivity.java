package android.chatapp.ib.ichat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

        String type = "";
        String userid = "";
        String username= "";


        if(getIntent().getExtras()!=null){

            for(String key:getIntent().getExtras().keySet()){

                switch (key){
                    case "type": type = getIntent().getExtras().getString(key);
                        break;
                    case "from_user_id": userid = getIntent().getExtras().getString(key);
                        break;
                    case "from_username": username = getIntent().getExtras().getString(key);
                }

            }

            if(type.equals("message")){
                Intent chatIntent = new Intent(MainActivity.this,ChatActivity.class);
                chatIntent.putExtra("userid",userid);
                chatIntent.putExtra("username",username);
                startActivity(chatIntent);
            }else if(type.equals("request")){

                Intent profIntent = new Intent(MainActivity.this,ProfileActivity.class);
                profIntent.putExtra("userid",userid);
                startActivity(profIntent);
            }


        }

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null)
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

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        mUserref.child("online").setValue(ServerValue.TIMESTAMP);

                        mAuth.signOut();
                        sendToStart();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

                break;
            case R.id.profile_but:
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
