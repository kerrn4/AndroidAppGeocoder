package com.example.kerrn.locator;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapState {

    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    private static final String MAPTYPE = "maptype";

    private static final String ZOOM = "zoom";
    private static final String TILT = "tilt";
    private static final String BEARING = "bearing";

    private static final String MAP_STATE = "mapstate";

    private SharedPreferences mapPrefs;

    public MapState(Context cont){

        mapPrefs = cont.getSharedPreferences(MAP_STATE, Context.MODE_PRIVATE);
    }

    public void saveMapState(GoogleMap map){
        SharedPreferences.Editor edit = mapPrefs.edit();
        CameraPosition position = map.getCameraPosition();

        edit.putFloat(LONGITUDE, (float) position.target.longitude);
        edit.putFloat(LATITUDE, (float) position.target.latitude);
        edit.putFloat(ZOOM, position.zoom);
        edit.putFloat(TILT, position.tilt);
        edit.putFloat(BEARING, position.tilt);
        edit.putInt(MAPTYPE, map.getMapType());

        edit.commit();
    }

    public CameraPosition restoreCameraPosition(){
        double lat = mapPrefs.getFloat(LATITUDE,0);
        if(lat == 0)
        {
            return null;
        }
            double lng = mapPrefs.getFloat(LONGITUDE,0);
            LatLng coordinates = new LatLng(lat, lng);
            float zoom = mapPrefs.getFloat(ZOOM,0);
            float bearing = mapPrefs.getFloat(BEARING, 0);
            float tilt = mapPrefs.getFloat(TILT,0);

        CameraPosition position = new CameraPosition(coordinates, zoom, tilt, bearing);
        return position;
    }

}
