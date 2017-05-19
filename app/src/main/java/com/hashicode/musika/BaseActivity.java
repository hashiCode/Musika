package com.hashicode.musika;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Base Activity that display a navigation drawer.
 *
 * Created by takahashi on 1/3/17.
 */

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private int itemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupDrawer(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                actionBarDrawerToggle.onDrawerSlide(drawerView, slideOffset);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                actionBarDrawerToggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                actionBarDrawerToggle.onDrawerClosed(drawerView);
                if(itemSelected!=0){
                    Bundle bundle = ActivityOptions.makeCustomAnimation(
                            BaseActivity.this, R.anim.fade_in, R.anim.fade_out).toBundle();
                    Class selectedActivity=null;
                    switch (itemSelected){
                        case R.id.nav_musics :
                            selectedActivity = MusicActivity.class;
                            break;
                        case R.id.nav_equalizer :
                            selectedActivity = EqualizerActivity.class;
                            break;
                        case R.id.nav_settings :
                            selectedActivity = PreferenceActivity.class;
                            break;
                    }
                    if(!BaseActivity.this.getClass().getName().equals(selectedActivity.getName())) {
                        Intent intent = new Intent(BaseActivity.this, selectedActivity);
                        startActivity(intent, bundle);
                    }

                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                actionBarDrawerToggle.onDrawerStateChanged(newState);
            }
        });
        setSupportActionBar(toolbar);
        updateActionBarDrawerToggle();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.itemSelected = 0;
        this.checkSelectedItem(navigationView);
    }

    private void checkSelectedItem(NavigationView navigationView) {

        if(this.getClass().getName().equals(MusicActivity.class.getName())){
            navigationView.setCheckedItem(R.id.nav_musics);
        } else  if(this.getClass().getName().equals(PreferenceActivity.class.getName())){
            navigationView.setCheckedItem(R.id.nav_settings);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //TODO selecionar a acicity
        itemSelected = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle != null && actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // If not handled by drawerToggle, home needs to be handled by returning to previous
        if (item != null && item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final FragmentManager.OnBackStackChangedListener mBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    updateActionBarDrawerToggle();
                }
            };

    private void updateActionBarDrawerToggle() {
        if (actionBarDrawerToggle == null) {
            return;
        }
        boolean isRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        actionBarDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            actionBarDrawerToggle.syncState();
        }
    }

    public Toolbar getToolbar(){
        return toolbar;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
