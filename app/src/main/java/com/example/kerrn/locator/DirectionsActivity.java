

package com.example.kerrn.locator;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DirectionsActivity extends FragmentActivity implements AdapterView.OnItemClickListener {

    private static final int GPSERROR = 9001;
    GoogleMap mMapDir;
    Marker mapMarkerFrom;
    Marker mapMarkerTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_directions);

            if (initMap()) {
                Toast.makeText(this, "DIRECTIONS", Toast.LENGTH_SHORT).show();
                mMapDir.setMyLocationEnabled(true);

            }
            else {
                Toast.makeText(this, "Error: Map not available", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setContentView(R.layout.activity_main);
        }
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.toAutocompleteDir);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        autoCompView.setOnItemClickListener(this);
        AutoCompleteTextView autoCompView2 = (AutoCompleteTextView) findViewById(R.id.fromAutocompleteDir);
        autoCompView2.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        autoCompView2.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_directions, menu);
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
        if (mMapDir == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDir);

            mMapDir = mapFrag.getMap();
            mMapDir.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    hideSoftKeyboard();
                }
            });

        }
        return (mMapDir != null);
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMapDir.moveCamera(update);
    }

    public void directions(View v) throws IOException {

        hideSoftKeyboard();

        mMapDir.clear();

        EditText et = (EditText) findViewById(R.id.fromAutocompleteDir);
        String fromLocation = et.getText().toString();
        EditText et2 = (EditText) findViewById(R.id.toAutocompleteDir);
        String toLocation = et2.getText().toString();

        if(fromLocation.length() == 0)
        {
            Toast.makeText(this, "Enter starting location", Toast.LENGTH_SHORT).show();
            return;
        }
        if(toLocation.length() == 0)
        {
            Toast.makeText(this, "Enter end location", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            Geocoder gcFrom = new Geocoder(this);
            List<Address> listFrom = gcFrom.getFromLocationName(fromLocation, 1);
            Address addFrom = listFrom.get(0);
            String localityFrom = addFrom.getLocality();

            if(localityFrom == null){
                localityFrom = addFrom.getSubLocality();
            }

            Geocoder gcTo = new Geocoder(this);
            List<Address> listTo = gcTo.getFromLocationName(toLocation, 1);
            Address addTo = listTo.get(0);
            String localityTo = addTo.getLocality();

            if(localityTo == null){
                localityTo = addFrom.getSubLocality();
            }

            Toast.makeText(this, localityFrom +" to "+localityTo, Toast.LENGTH_LONG).show();

            double latFrom = addFrom.getLatitude();
            double lngFrom = addFrom.getLongitude();

            double latTo = addTo.getLatitude();
            double lngTo = addTo.getLongitude();

            route(localityFrom, localityTo);


            if(mapMarkerFrom != null){
                mapMarkerFrom.remove();
            }
            if(mapMarkerTo != null){
                mapMarkerTo.remove();
            }
            LatLng coordinatesFrom = new LatLng(latFrom,lngFrom);
            LatLng coordinatesTo = new LatLng(latTo,lngTo);

            MarkerOptions markFrom = new MarkerOptions().title(localityFrom).position(coordinatesFrom).draggable(true);
            MarkerOptions markTo = new MarkerOptions().title(localityTo).position(coordinatesTo).draggable(true);


            mapMarkerTo = mMapDir.addMarker(markTo);
            mapMarkerFrom = mMapDir.addMarker(markFrom);

            LatLngBounds.Builder build = new LatLngBounds.Builder();
            build.include(mapMarkerFrom.getPosition());
            build.include(mapMarkerTo.getPosition());
            LatLngBounds bounds = build.build();
            CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            mMapDir.animateCamera(camera);

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
                mMapDir.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Toast.makeText(this, "Normal View", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mapTypeSatellite:
                mMapDir.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                Toast.makeText(this, "Satellite View", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mapTypeHybrid:
                mMapDir.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                Toast.makeText(this, "Hybrid View", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mapTypeTerrain:
                mMapDir.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                Toast.makeText(this, "Terrain View", Toast.LENGTH_SHORT).show();
                break;
            case R.id.locator:
                Intent intent = new Intent(DirectionsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
        state.saveMapState(mMapDir);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MapState state = new MapState(this);
        CameraPosition position = state.restoreCameraPosition();
        if(position != null)
        {
            CameraUpdate cUpdate = CameraUpdateFactory.newCameraPosition(position);
            mMapDir.moveCamera(cUpdate);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void route(String inputFrom, String inputTo){
        Directions dir = new Directions();
        Document doc = dir.getDoc(inputFrom, inputTo);

        if(doc == null){
            Toast.makeText(this, "No Route Found", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<LatLng> points = dir.getDir(doc);
        PolylineOptions routes = new PolylineOptions().width(3).color(Color.BLUE);

        for (int i = 0; i < points.size(); i++) {
            routes.add(points.get(i));
        }
        mMapDir.addPolyline(routes);

    }
}
