//Citation: lyndia.com?
//https://www.youtube.com/watch?v=O5pxlyyyvbw

package com.example.kerrn.locator;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.location.Geocoder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.location.Address;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener{


    private static final int GPSERROR = 9001;
    GoogleMap mMap;
    private static final float DEFAULTZOOM = 15;
    Marker mapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                Toast.makeText(this, "LOCATOR", Toast.LENGTH_SHORT).show();
                mMap.setMyLocationEnabled(true);

            }
            else {
                Toast.makeText(this, "Error: Map not available", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setContentView(R.layout.activity_main);
        }


        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        autoCompView.setOnItemClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPSERROR);
            dialog.show();
        }
        else {
            Toast.makeText(this, "Error: Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            mMap = mapFrag.getMap();
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    hideSoftKeyboard();
                }
            });

        }
        return (mMap != null);
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    public void locationGeocoder(View v) throws IOException{

        hideSoftKeyboard();

        EditText et = (EditText) findViewById(R.id.autocomplete);
        String location = et.getText().toString();

        if(location.length() == 0)
        {
            Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocationName(location, 1);
            Address add = list.get(0);
            String locality = add.getLocality();

            Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();

            gotoLocation(lat, lng, DEFAULTZOOM);

            if(mapMarker != null){
                mapMarker.remove();
            }
            LatLng coordinates = new LatLng(lat,lng);
            MarkerOptions mark = new MarkerOptions().title(locality).position(coordinates).draggable(true);
            mapMarker = mMap.addMarker(mark);

            Toast.makeText(this, "Lat: "+ lat +"\nLng: "+ lng, Toast.LENGTH_LONG).show();

        }
        catch (IOException io)
        {
            Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
        }


    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

          case R.id.mapTypeNormal:
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            Toast.makeText(this, "Normal View", Toast.LENGTH_SHORT).show();
            break;
          case R.id.mapTypeSatellite:
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            Toast.makeText(this, "Satellite View", Toast.LENGTH_SHORT).show();
            break;
          case R.id.mapTypeHybrid:
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Toast.makeText(this, "Hybrid View", Toast.LENGTH_SHORT).show();
            break;
          case R.id.mapTypeTerrain:
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            Toast.makeText(this, "Terrain View", Toast.LENGTH_SHORT).show();
            break;
          case R.id.showCoordinates:
            if(mapMarker != null) {
                Toast.makeText(this, mapMarker.getPosition().toString(), Toast.LENGTH_SHORT).show();
                break;
            }
              Toast.makeText(this, "Search Location", Toast.LENGTH_SHORT).show();
            break;
          case R.id.directions:
            Intent intent = new Intent(this, DirectionsActivity.class);
            this.startActivity(intent);
            return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        MapState state = new MapState(this);
        state.saveMapState(mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MapState state = new MapState(this);
        CameraPosition position = state.restoreCameraPosition();
        if(position != null)
        {
            CameraUpdate cUpdate = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(cUpdate);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

}

