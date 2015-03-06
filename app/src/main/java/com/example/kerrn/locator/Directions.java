//Citation: Stackoverflow
//http://stackoverflow.com/questions/14495030/get-driving-directions-using-google-maps-api-v2/15053901#15053901

package com.example.kerrn.locator;


import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Directions {

    private static final String LOG_TAG = "Locator";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api";
    private static final String TYPE_DIRECTIONS = "/directions";
    private static final String OUT_XML = "/xml";

    private static final String API_KEY = "AIzaSyCGxRIaUDKyBaZhjkYRvTIEK-K34tZtrYU";


    public Document getDoc(String fromInput, String toInput) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        StringBuilder jsonResultsDir = new StringBuilder();
        Document doc;

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DIRECTIONS + OUT_XML);
            sb.append("?origin=" + URLEncoder.encode(fromInput, "UTF-8"));
            sb.append("&destination=" + URLEncoder.encode(toInput, "UTF-8"));
            sb.append("&key=" + API_KEY);
            String url = sb.toString();
            Log.d("url", url);

            try {
                //Do I even need this part?
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost, localContext);
                InputStream in = response.getEntity().getContent();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                //doc = builder.parse(in);
                doc = builder.parse(url);
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }

        return null;

    }

    public ArrayList<LatLng> getDir(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> geoPoints = new ArrayList<LatLng>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationNode = nl2
                        .item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                geoPoints.add(new LatLng(lat, lng));

                locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for (int j = 0; j < arr.size(); j++) {
                    geoPoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
                }

                locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                geoPoints.add(new LatLng(lat, lng));
            }
        }

        return geoPoints;
    }

    private int getNodeIndex(NodeList nl, String nodeName) {
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(nodeName))
                return i;
        }
        return -1;
    }



    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }





}
