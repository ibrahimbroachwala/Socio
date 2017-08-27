package myapp.chatapp.ib.socio;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by ibrah on 7/30/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    FirebaseAuth mAuth;
    String Uid;


    private List<Messages> mMessageList;

    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item,parent,false);

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {


        Messages c = mMessageList.get(position);
        GetTimeAgo gta = new GetTimeAgo();
        long lastime = c.getTime();
        String lastseentime = gta.getTimeAgo(lastime);

        if(c.getFrom().equals(Uid)) {
            holder.messageView.setGravity(Gravity.END);
            holder.messageLayout.setBackgroundResource(R.drawable.message_bg_light);
            holder.messageText.setTextColor(Color.WHITE);
        }

        holder.messageText.setText(c.getMessage());
        holder.messageTime.setText(lastseentime);




    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView messageTime;
        public LinearLayout messageLayout;
        public LinearLayout messageView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageLayout = (LinearLayout) itemView.findViewById(R.id.message_item_layout);
            messageText = (TextView) itemView.findViewById(R.id.message_item_text);
            messageTime = (TextView) itemView.findViewById(R.id.message_item_time);
            messageView = (LinearLayout) itemView.findViewById(R.id.message_item_view);



        }
    }
}
