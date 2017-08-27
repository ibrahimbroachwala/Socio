package myapp.chatapp.ib.socio;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by ibrah on 6/25/2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {


    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {


        switch (position){
            case 1: ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2: FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 0: PostsFragment postsFragment = new PostsFragment();
                return postsFragment;
            default: return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

            switch (position){
                case 1: return "Chats";
                case 2: return "Friends";
                case 0: return "Moments";
                default: return null;
            }

    }
}
