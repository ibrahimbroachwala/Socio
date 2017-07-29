package android.chatapp.ib.ichat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {


    DatabaseReference friendDatabase;
    DatabaseReference mUsersdatabase;
    String Uid;

    RecyclerView friends_rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_friends,container,false);
        friends_rv = (RecyclerView) v.findViewById(R.id.friends_rv);


        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(Uid);
        mUsersdatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        friendDatabase.keepSynced(true);
        mUsersdatabase.keepSynced(true);
        friends_rv.setHasFixedSize(true);
        friends_rv.setLayoutManager(new LinearLayoutManager(getActivity()));


        return v;
    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_item,
                FriendsViewHolder.class,
               friendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String friend_user_id = getRef(position).getKey();


                mUsersdatabase.child(friend_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("name").getValue().toString();
                        String thumbimage = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            Boolean useronline = (boolean) dataSnapshot.child("online").getValue();
                            viewHolder.setUserOnline(useronline);
                        }
                        viewHolder.setName(username);
                        viewHolder.setDp(thumbimage,getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};


                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Choose");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                            if(which==0){
                                                Intent profIntent = new Intent(getContext(),ProfileActivity.class);
                                                profIntent.putExtra("userid",friend_user_id);
                                                startActivity(profIntent);
                                            }
                                            if(which==1){

                                            }
                                    }
                                });

                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friends_rv.setAdapter(firebaseRecyclerAdapter);

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{


        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setDate(String date){

            TextView user_name_tv = (TextView) mView.findViewById(R.id.user_item_status);
            user_name_tv.setText(date);
        }


        public void setName(String name){
            TextView friendtextview = (TextView) mView.findViewById(R.id.user_item_name);
            friendtextview.setText(name);
        }
        public void setDp(String thumb_image, Context ctx){
            CircleImageView userdp = (CircleImageView) mView.findViewById(R.id.user_item_dp);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.ic_person_black_24dp).into(userdp);
        }

        public void setUserOnline(boolean online){
            ImageView onlineView = (ImageView)  mView.findViewById(R.id.user_item_online);

            if(online){
                onlineView.setVisibility(View.VISIBLE);
            }else{
                onlineView.setVisibility(View.INVISIBLE);
            }
        }


    }

}
