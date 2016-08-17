package com.natech.roja;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.natech.roja.Utilities.AppSharedPreferences;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
@SuppressWarnings("SameParameterValue")
public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer,mUserLearnedFAB;

    private final String TUTORIAL = "LearnedMainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        mUserLearnedFAB = getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                Context.MODE_PRIVATE).getBoolean(TUTORIAL,false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        final List<NavigationItem> navigationItems = getMenu();
        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(navigationItems);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(adapter);
        //selectItem(mCurrentSelectedPosition);
        return view;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

// --Commented out by Inspection START (2015/09/15 06:09 PM):
//    public ActionBarDrawerToggle getActionBarDrawerToggle() {
//        return mActionBarDrawerToggle;
//    }
// --Commented out by Inspection STOP (2015/09/15 06:09 PM)

// --Commented out by Inspection START (2015/09/15 06:09 PM):
//    public DrawerLayout getDrawerLayout() {
//        return mDrawerLayout;
//    }
// --Commented out by Inspection STOP (2015/09/15 06:09 PM)

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectItem(position);
    }

    @SuppressWarnings("deprecation")
    List<NavigationItem> getMenu() {
        List<NavigationItem> items = new ArrayList<>();

        items.add(new NavigationItem("Trending Restaurants", getResources().getDrawable(R.drawable.ic_trends_white)));
        items.add(new NavigationItem("Around Me", getResources().getDrawable(R.drawable.ic_around_me)));
        items.add(new NavigationItem("My Favourites",getResources().getDrawable(R.drawable.ic_favourite_white)));
        items.add(new NavigationItem("Scan Table", getResources().getDrawable(R.drawable.ic_qr)));
        items.add(new NavigationItem("History", getResources().getDrawable(R.drawable.ic_history2)));
        items.add(new NavigationItem("Loyalty Points", getResources().getDrawable(R.drawable.ic_points)));
        items.add(new NavigationItem("Edit Profile", getResources().getDrawable(R.drawable.ic_user)));
        items.add(new NavigationItem("Tell A Buddy", getResources().getDrawable(R.drawable.ic_mega_phone)));
        items.add(new NavigationItem("Information", getResources().getDrawable(R.drawable.ic_info)));
        items.add(new NavigationItem("Log Out", getResources().getDrawable(R.drawable.ic_log_out)));


        return items;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param toolbar      The Toolbar of the activity.
     */
    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = (View) getActivity().findViewById(fragmentId).getParent();
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }
                if(!mUserLearnedFAB){
                    mUserLearnedFAB = true;
                    MainActivity.mainActivity.FAB.post(new Runnable() {
                        @Override
                        public void run() {
                            final ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity()).
                                    setTarget(new ViewTarget(R.id.scanFAB, getActivity())).
                                    setContentTitle("Scan Table").setStyle(R.style.CustomShowcaseTheme3)
                                    .setContentText("Touch this button to scan the QR code on your table").
                                            hideOnTouchOutside().build();
                            showcaseView.setButtonText("Got It!");
                            RelativeLayout.LayoutParams layoutParams
                                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                            layoutParams.setMargins(15, 15, 15, 15);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            showcaseView.setButtonPosition(layoutParams);
                            showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {

                                }

                                @Override
                                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                    getActivity().getSharedPreferences(AppSharedPreferences.getSettings(),
                                            Context.MODE_PRIVATE).edit().
                                            putBoolean(TUTORIAL,true).apply();
                                }

                                @Override
                                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
        ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }

// --Commented out by Inspection START (2015/09/15 06:10 PM):
//    public void openDrawer() {
//        mDrawerLayout.openDrawer(mFragmentContainerView);
//    }
// --Commented out by Inspection STOP (2015/09/15 06:10 PM)

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setUserData(String user, String email, String picLink) {
        ImageView avatarContainer = (ImageView) mFragmentContainerView.findViewById(R.id.profilePic);
        ((TextView) mFragmentContainerView.findViewById(R.id.txtUserEmail)).setText(email);
        ((TextView) mFragmentContainerView.findViewById(R.id.txtUsername)).setText(user);

        if(!picLink.equalsIgnoreCase("none"))
            Picasso.with(getActivity().getApplicationContext()).load(picLink).into(avatarContainer);
        else
            avatarContainer.setImageResource(R.drawable.user);
        avatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.mainActivity.openEditProfileActivity();
            }
        });


    }

// --Commented out by Inspection START (2015/09/15 06:10 PM):
//    public View getGoogleDrawer() {
//        return mFragmentContainerView.findViewById(R.id.googleDrawer);
//    }
// --Commented out by Inspection STOP (2015/09/15 06:10 PM)

}
