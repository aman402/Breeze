package com.example.android.breeze;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Create all textview and imageview objects here and then link them with their XML counterpart

    private RequestQueue mQueue; // Volley's request queue object, its initialised in executeAPI
    String lat = "", lon = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQueue = Volley.newRequestQueue(MainActivity.this);
        // request queue created, JSON requests to this are added in executeAPI

        // After starting the application, show details of Delhi, calling executeFunctions
        String api_key = "56a18259f14e7df8c73ba46e749557a9";
        executeAPI("New Delhi", api_key); // passing city name as Delhi and api_key

        // Calling executeAPI after getting data from editText field after search button is pressed
        ImageButton search_button = findViewById(R.id.search);
        search_button.setOnClickListener(view -> {
            EditText CityName = findViewById(R.id.editTextCityName);
            String city_name = CityName.getText().toString();
            String error_message = "City name field is empty, retry!";
            if (city_name.isEmpty()) {
                Toast.makeText(MainActivity.this, error_message, Toast.LENGTH_LONG).show();
            } else {
                executeAPI(city_name, api_key);
            }
            hideKeyboard(MainActivity.this);
        });
    }

    private void executeAPI(String city_name, String api_key) {
        String apiURL = "https://api.openweathermap.org/data/2.5/weather?q=" + city_name + "&appid=" + api_key + "&units=metric";

        // Requesting JSON object from apiURL
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, apiURL, null, response -> {
                    // Successful request response here

                    // extracting "main" object from response, "main" contains temp, pressure
                    //humidity, max_temp, min_temp
                    extractMain(response);

                    // extracting "wind" object from response, it contains wind speed
                    extractWind(response);

                    // extracting "sys" object from response, it contains sunset, sunrise,
                    // address and updated_at API data
                    extractSys(response);

                    // extracting "weather" array from response, it contains description
                    // and weather icon code
                    extractWeather(response);

                    // Extracting longitude and latitude and storing them in global string
                    // variables and then calling setAQI function after mQueue.add(request)
                    // here in this function
                    try {
                        lat = response.getJSONObject("coord").getString("lat");
                        lon = response.getJSONObject("coord").getString("lon");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Error response here
                    String responseErrorMessage = "Something went wrong in JsonObjectRequest, executeAPI()";
                    Toast.makeText(MainActivity.this, responseErrorMessage, Toast.LENGTH_LONG).show();
                });

        mQueue.add(request); // Json Object Request added to Volley Request Queue
        setAQI(lat, lon); // Get AQI data from airvisual.com
    }

    private void extractMain(JSONObject response) {
        // extracting "main" object from response
        try {
            JSONObject mainJsonObject = response.getJSONObject("main");

            //Setting up temperature from mainJsonObject
            TextView tempTextView = findViewById(R.id.temp);
            String temp = mainJsonObject.getString("temp");
            temp = temp + "°C";
            tempTextView.setText(temp);

            //Setting up temp_max from mainJsonObject
            TextView maxTempTextView = findViewById(R.id.temp_max);
            String temp_max = mainJsonObject.getString("temp_max");
            temp_max = "Max Temp : " + temp_max + "°C";
            maxTempTextView.setText(temp_max);

            //Setting up temp_min from mainJsonObject
            TextView minTempTextView = findViewById(R.id.temp_min);
            String temp_min = mainJsonObject.getString("temp_max");
            temp_min = "Min Temp : " + temp_min + "°C";
            minTempTextView.setText(temp_min);

            //Setting up humidity from mainJsonObject
            TextView humidityTextView = findViewById(R.id.humidity);
            String humidity = mainJsonObject.getString("humidity");
            humidityTextView.setText(humidity);

            //Setting up pressure from mainJsonObject
            TextView pressureTextView = findViewById(R.id.pressure);
            String pressure = mainJsonObject.getString("pressure") + "hPa";
            pressureTextView.setText(pressure);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractWind(JSONObject response) {
        //extracting "wind" from response
        try {
            JSONObject windJsonObject = response.getJSONObject("wind");

            //Setting up wind speed from windJsonObject
            TextView windTextView = findViewById(R.id.wind);
            String wind = windJsonObject.getString("speed");
            windTextView.setText(wind);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractSys(JSONObject response) {
        //extracting "sys" from response
        try {

            //extracting "sys"
            JSONObject sysJsonObject = response.getJSONObject("sys");

            //Time format for sunrise and sunset
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

            //Setting up sunset data
            TextView sunsetTextView = findViewById(R.id.sunset);
            long sunsetData = sysJsonObject.getLong("sunset");
            String sunset_text = simpleDateFormat.format(new Date(sunsetData * 1000));
            sunsetTextView.setText(sunset_text);

            //Setting up sunrise data
            TextView sunriseTextView = findViewById(R.id.sunrise);
            long sunriseData = sysJsonObject.getLong("sunrise");
            String sunrise_text = simpleDateFormat.format(new Date(sunriseData * 1000));
            sunriseTextView.setText(sunrise_text);

            //Setting up address (country name and location name)
            String location = response.getString("name");
            String country_name = sysJsonObject.getString("country");
            String address = location + ", " + country_name;
            TextView addressTextView = findViewById(R.id.address);
            addressTextView.setText(address.toUpperCase(Locale.ROOT));

            //Setting up updated_at time

            //Date and time format for updated_at
            SimpleDateFormat simpleDateFormat_UpdatedAt = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
            long updated_at = response.getLong("dt");
            String updated_at_text = "Updated at : " + simpleDateFormat_UpdatedAt.format(new Date(updated_at * 1000));
            TextView updatedAtTextView = findViewById(R.id.updated_at);
            updatedAtTextView.setText(updated_at_text);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractWeather(JSONObject response) {
        //extracting "weather" array from response
        try {

            //Setting up description from "weather" array
            TextView descriptionTextView = findViewById(R.id.description);
            String descriptionText = response.getJSONArray("weather").getJSONObject(0).getString("description");
            descriptionTextView.setText(descriptionText);

            // Extracting and setting up weather icon code (Code not working, image not loading)
            String iconCode = response.getJSONArray("weather").getJSONObject(0).getString("icon");

            // by doing this we get only day icons, removed potential "n"  from end and added "d"
            iconCode = iconCode.substring(0, 2) + "d";

            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            ImageView weatherIconView = findViewById(R.id.weatherIcon);
            Glide.with(this).load(iconUrl).into(weatherIconView);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAQI(String lat, String lon)
    {
        String aqi_api_key = "49e64a55-033c-42d3-bb37-cbe436c78682";
        String aqiURL = "https://api.airvisual.com/v2/nearest_city?lat=" + lat + "&lon=" + lon + "&key=" + aqi_api_key;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, aqiURL, null, response -> {

                    try{
                        String aqi_data = response.getJSONObject("data").getJSONObject("current").getJSONObject("pollution").getString("aqius");
                        TextView aqiTextView = findViewById(R.id.aqi);
                        aqiTextView.setText(aqi_data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {
                    String responseMessage = "Something went wrong in JsonObjectRequest, setAQI()";
                    Toast.makeText(MainActivity.this, responseMessage, Toast.LENGTH_LONG).show();
                });
        mQueue.add(request);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
