package myapp.chatapp.ib.socio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private ViewPager mviewpager;
    private MainPagerAdapter mPa;
    private TabLayout mTabLayout;
    private FloatingActionButton fab;

    private DatabaseReference mUserref;
    private DatabaseReference pubkeyref;
    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;

    public String privateKeyString;
    private String publicKeystring;

    public String MY_PREFS_NAME = "socio_prefs";


    public void genKeys() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{



        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        privateKeyString = prefs.getString("private_key"+mAuth.getCurrentUser().getUid(), null);
        publicKeystring = prefs.getString("public_key"+mAuth.getCurrentUser().getUid(), null);

        if(privateKeyString==null && publicKeystring==null) {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();

            if (privateKey != null) {
                privateKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
            }
            if (publicKey != null) {
                publicKeystring = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
            }

            pubkeyref.child(mAuth.getCurrentUser().getUid()).child("pub").setValue(publicKeystring).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("private_key"+mAuth.getCurrentUser().getUid(), privateKeyString);
                    editor.putString("public_key"+mAuth.getCurrentUser().getUid(), publicKeystring);
                    editor.apply();


                }
            });
        }




//        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
//        String priKey = sharedPref.getString("private_key",null);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.app_name);



        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        fab = (FloatingActionButton) findViewById(R.id.fab_add);
        mAuth = FirebaseAuth.getInstance();

        pubkeyref = FirebaseDatabase.getInstance().getReference().child("PubKey");

        if(mAuth.getCurrentUser() != null)
        mUserref = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());



        mviewpager = (ViewPager) findViewById(R.id.tab_pager);
        mPa = new MainPagerAdapter(getSupportFragmentManager());

        mviewpager.setAdapter(mPa);

        mTabLayout.setupWithViewPager(mviewpager);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(intent);
            }
        });
        fab.show();





        mviewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                                startActivity(intent);
                            }
                        });
                        fab.show();
                        break;
                    case 2:
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                                startActivity(intent);
                            }
                        });
                        fab.show();
                        break;

                    default:
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(mAuth.getCurrentUser()!=null) {
            try {
                genKeys();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }

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
