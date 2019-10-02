package com.example.vidhan.speedometerdemo_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.PointerSpeedometer;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private LocationManager lm;
    private LocationListener locationListener;
    private Integer data_points = 2; // how many data points to calculate for
    private Double[][] positions;
    private Long[] times;
    private Boolean mirror_pref, full_screen_pref; // Preference Booleans
    private Integer units; // Preference integers
    private Float text_size; // Preference Float
    private TextView satellite;
    private TextView accuracy;
    private TextView maxSpeed;
    private TextView averageSpeed;
    public TextView distance;
    public double TotalDistance = 0;
    protected PointerSpeedometer pointerSpeedometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        pointerSpeedometer = (PointerSpeedometer) findViewById(R.id.pointerSpeedometer);
        distance = (TextView) findViewById(R.id.distance);
        //  pointerSpeedometer.setWithTremble(false);
        // two arrays for position and time.
        positions = new Double[data_points][2];
        times = new Long[data_points];

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        pointerSpeedometer.setPointerColor(Color.RED);
        pointerSpeedometer.setSpeedometerColor(Color.WHITE);
        pointerSpeedometer.setTrembleDegree(0);
        //  pointerSpeedometer.speedTo(50);

        //  distance.setText((int) TotalDistance);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing in Progress", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        lm.removeUpdates(locationListener);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private class MyLocationListener implements LocationListener {
        Integer counter = 0;
        double temp = 100.23;


        @SuppressLint("ResourceType")
        @Override
        public void onLocationChanged(Location loc) {

            if (loc != null) {
                Double d1;
                Long t1;
                Double speed = 0.0;
                d1 = 0.0;
                t1 = 0l;

                positions[counter][0] = loc.getLatitude();
                positions[counter][1] = loc.getLongitude();
                times[counter] = loc.getTime();

                try {
                    // get the distance and time between the current position, and the previous position.
                    // using (counter - 1) % data_points doesn't wrap properly
                    d1 = distance(positions[counter][0], positions[counter][1], positions[(counter + (data_points - 1)) % data_points][0], positions[(counter + (data_points - 1)) % data_points][1]);
                    t1 = times[counter] - times[(counter + (data_points - 1)) % data_points];
                    TotalDistance = TotalDistance + d1;
                } catch (NullPointerException e) {
                    //all good, just not enough data yet.
                }

                if (loc.hasSpeed()) {
                    speed = loc.getSpeed() * 1.0; // need to * 1.0 to get into a double for some reason...
                } else {
                    speed = d1 / t1; // m/s
                }
                counter = (counter + 1) % data_points;

                // convert from m/s to kmh
//                switch (units) {
//                    case R.id.kmph:
                speed = speed * 3.6d;
//                        break;
//                    case R.id.mph:
//                        speed = speed * 2.23693629d;
//                        break;
//                    case R.id.knots:
//                        speed = speed * 1.94384449d;
//                        break;
//                }
//                displayText(speed.intValue());
                pointerSpeedometer.speedTo(speed.intValue());
                //    distance.setText((int) (TotalDistance*1000));


                // SpannableString s = new SpannableString(String.format("%.0f", TotalDistance) + distanceUnits);
                //s = new SpannableString(String.format("%.3f", TotalDistance) + distanceUnits);
                //s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 2, s.length(), 0);

            } else {
                pointerSpeedometer.speedTo(-1);
            }


        }

        // private functions


        private double distance(double lat1, double lon1, double lat2, double lon2) {
            // haversine great circle distance approximation, returns meters
            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60; // 60 nautical miles per degree of seperation
            dist = dist * 1852; // 1852 meters per nautical mile
            return (dist);
        }


        private double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        private double rad2deg(double rad) {
            return (rad * 180.0 / Math.PI);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(getResources().getString(R.string.app_name), "Speedo status changed : " + extras.toString());


        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(getResources().getString(R.string.app_name), "Speedo provider enabled : " + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(getResources().getString(R.string.app_name), "Speedo provider disabled : " + provider);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();

        if (id == R.id.nav_speedometer) {

            // Begin the transaction
            //  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
// Replace the contents of the container with the new fragment
            //ft.replace(R.id.speed_placeholder,new SpeedometerFragment());
// or ft.add(R.id.your_placeholder, new FooFragment());
// Complete the changes added above
            //ft.commit();


            //Intent intentspeed=new Intent(this,speedmeter.class);
            // startActivity(intentspeed);

            // Handle the speedometer  action
        } else if (id == R.id.nav_compass) {
            Intent intentcompass = new Intent(this, Compass.class);
            startActivity(intentcompass);

        } else if (id == R.id.nav_settings) {

            // Intent intentsetting=new Intent(this,Setting.class);
            // startActivity(intentsetting);


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
