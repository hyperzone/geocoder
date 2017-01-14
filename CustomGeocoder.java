package it.simonevitale.takeover;

import android.location.Address;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ENGLISH
 * I have created a class to use Google Maps service. Unfortunately the GeoCoder class present
 * in Google API doesn't work correctly on any device. With this class you can use the Google
 * Maps service directly without utilize the Google Android API
 *
 * ITALIAN
 * Per effettuare la tralaslazione delle coordinate in un indirizzo, ho creato una classe personalizzata
 * per usare il servizio offerto da Google Maps. Purtroppo la libreria Geocoder offerta nelle API di android
 * da Google non è disponibile su tutti i dispositivi per via dello stop del servizio di Network:
 * questo rende inutilizzabile il servizio e quindi ho dovuto provare un'altra strada.
 * Created by Simone Vitale on 10/10/2016.
 */

public class CustomGeocoder
{
    public static List<Address> getFromLocation(double lat, double lng, int maxResult) {

        String data = "";
        URL url;
        HttpURLConnection urlConnection = null;



        List<Address> retList = null;

        try {

            String address = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=false&language=" + Locale.getDefault().getCountry(), lat, lng);
            url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                data = readStream(urlConnection.getInputStream());
            }


            JSONObject jsonObject = new JSONObject(data);

            retList = new ArrayList<Address>();

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                if (results.length() > 0) {
                    for (int i = 0; i < results.length() && i < maxResult; i++) {
                        JSONObject result = results.getJSONObject(i);
                        Address addr = new Address(Locale.getDefault());
                        JSONArray components = result.getJSONArray("address_components");
                        String streetNumber = "";
                        String route = "";
                        for (int a = 0; a < components.length(); a++) {
                            JSONObject component = components.getJSONObject(a);
                            JSONArray types = component.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if (type.equals("locality")) {
                                    addr.setLocality(component.getString("long_name"));
                                } else if (type.equals("street_number")) {
                                    streetNumber = component.getString("long_name");
                                } else if (type.equals("route")) {
                                    route = component.getString("long_name");
                                }
                                else if(type.equals("administrative_area_level_3"))
                                {
                                    String tmp = component.getString("long_name");
                                    if(!tmp.isEmpty())
                                    {
                                        addr.setAdminArea(tmp);
                                    }
                                    else
                                    {
                                        addr.setAdminArea("");
                                    }
                                }
                                else if(type.equals("administrative_area_level_2"))
                                {
                                    String tmp = component.getString("short_name");
                                    if(!tmp.isEmpty())
                                    {
                                        addr.setCountryName(tmp);
                                    }
                                    else
                                    {
                                        addr.setCountryName("");
                                    }
                                }
                            }
                        }
                        addr.setAddressLine(0, route + " " + streetNumber);

                        addr.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                        addr.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                        retList.add(addr);
                    }
                }
            }


        } catch (JSONException e) {
            Log.e(CustomGeocoder.class.getName(), "Error parsing Google geocode webservice response.", e);
        } catch (Exception e) {
            Log.e(CustomGeocoder.class.getName(), "Error calling Google geocode webservice.", e);
        }


        return retList;
    }

    // Converting InputStream to String
    //StackOverflow question: http://stackoverflow.com/questions/8654876/http-get-using-android-httpurlconnection
    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
