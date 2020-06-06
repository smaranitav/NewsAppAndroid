package com.example.newsapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class TabsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    public TabsAdapter(FragmentManager fm, int NoofTabs){
        super(fm);
        this.mNumOfTabs = NoofTabs;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                WorldFragment world = new WorldFragment();
                return world;
            case 1:
                BusinessFragment business= new BusinessFragment();
                return business;
            case 2:
                PoliticsFragment politics= new PoliticsFragment();
                return politics;
            case 3:
                SportsFragment sports= new SportsFragment();
                return sports;
            case 4:
                TechFragment tech= new TechFragment();
                return tech;
            case 5:
               ScienceFragment science= new ScienceFragment();
                return science;
            default:
                return null;
        }
    }
}