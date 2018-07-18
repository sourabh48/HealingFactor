package com.example.soura.healingfactor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by soura on 22-12-2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter
{

    public SectionPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "CHATS";
            case 1:
                return "FRIENDS";
            case 2:
                return "REQUESTS";
            default:
                return null;
        }
    }
}
