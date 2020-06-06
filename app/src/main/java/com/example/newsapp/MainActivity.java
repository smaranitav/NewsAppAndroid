package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.RecyclerAdapter.MyAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //items for recycler view
    //List<Item> dummy_items= new ArrayList<>();

    //adapter for recycler view
    MyAdapter adapter;
    RecyclerView recyclerView;
    //location and openweatherapi
    int PERMISSION_ID = 44;
    static int restart = 0;
    FusedLocationProviderClient mFusedLocationClient;
    String cityName, stateName, temp_celsius, weather_descr;
    static ArrayList<String> jsonArrayList = new ArrayList<>();
    // TextView cityText, condDescr,temp,stateText;
    String weatherData, weatherImage;

    //volley
    RequestQueue queue;
    JsonObjectRequest getRequest;

    //AutoSuggest API
    ArrayAdapter<String> adapter_search;
    SearchView.SearchAutoComplete searchAutoComplete;

    //data for latest news
    JSONObject jObj;
    JSONArray jArray;

    Context ctx;
    //progressbar
    ProgressBar progressBar;
    TextView progressText;

    //refreshlayout
    SwipeRefreshLayout mSwipeRefreshLayout;
    Handler handler;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.loadingProgress);

        progressText = (TextView) findViewById(R.id.progressText);
        // progressText.setVisibility(View.VISIBLE);

        ctx = getApplicationContext();
        queue = Volley.newRequestQueue(getApplicationContext());
        //items.clear();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.headlines:
                        startActivity(new Intent(getApplicationContext(), HeadlinesActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.trending:
                        startActivity(new Intent(getApplicationContext(), TrendingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.bookmarks:
                        startActivity(new Intent(getApplicationContext(), BookmarksActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh_items);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to make your refresh action
                // CallYourRefreshingMethod();
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                getLastLocation();
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {

                                    requestNewLocationData();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(final LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            try {

                List<Address> addresses = null;
                while(addresses==null) {
                    Log.d("coming","once");
                    addresses=geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                    cityName = addresses.get(0).getLocality();
                    stateName = addresses.get(0).getAdminArea();
                }
                callLatestWeather(cityName);
            } catch (IOException e) {

                //handling it because of grpc failed error
                Item dummyItem = new Item("","","","","");
                callLatestGuardian(dummyItem);
                e.printStackTrace();
//                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
//                List<Address> addresses = null;
//                try {
//                    addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
//                    cityName = addresses.get(0).getLocality();
//                    stateName = addresses.get(0).getAdminArea();
//                    callLatestWeather(cityName);
//                } catch (IOException ex) {

//                    ex.printStackTrace();
//                }
//
//                e.printStackTrace();
            }

        }
    };

    public void callLatestWeather(final String cityName) {
        //call the openweathermap api
        Log.d("call weather", "call");
        final String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName +
                "&units=metric&appid=39ad4b22164f4c571c1050340a5070c5";
        //final String city = cityName;
        Log.d("called weather", "weatherfunc");
        final List<Item> weather_items = new ArrayList<>();
        Log.d("items are inside weather", String.valueOf(weather_items));
        weather_items.clear();
        //Log.d("items after clearing inside weather", String.valueOf(items));

        // prepare the Request
        getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    Weather weather = new Weather();

                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        //Log.d("Response", response.toString());
                        weatherData = response.toString();
                        //Log.d("weatherData", weatherData);
                        try {
                            weather = JSONWeatherParser.getWeather(weatherData);
                            temp_celsius = "" + Math.round(weather.temperature.getTemp()) + " \u2103";
                            weather_descr = weather.currentCondition.getCondition();
                            if (weather_descr == "Clear") {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/clear_weather.jpg";
                            } else if (weather_descr == "Clouds") {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/cloudy_weather.jpg";
                            } else if (weather_descr == "Snow") {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/snowy_weather.jpeg";
                            } else if (weather_descr == "Rain" || weather_descr == "Drizzle") {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/rainy_weather.jpg";
                            } else if (weather_descr == "Thunderstorm") {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/thunder_weather.jpg";
                            } else {
                                weatherImage = "https://csci571.com/hw/hw9/images/android/sunny_weather.jpg";
                            }

                            //WeatherItems weather_item=new WeatherItems(cityName, stateName, temp_celsius,weather_descr);
                            Item item = new Item(cityName, stateName, temp_celsius, weather_descr, weatherImage);
                            weather_items.add(item);

                            //recycler view for displaying weather condition
                            recyclerView = (RecyclerView) findViewById(R.id.recycler);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            MyAdapter.RecyclerViewClickListener listener = new MyAdapter.RecyclerViewClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    Toast.makeText(getApplicationContext(), "Position " + position, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public boolean onLongClick(View view, int position) {
                                    return true;
                                }


                            };


                            adapter = new MyAdapter(recyclerView, MainActivity.this, weather_items, listener);
                            recyclerView.setAdapter(adapter);
//                            adapter.notifyDataSetChanged(); KEEP THIS COMMENTED

                            callLatestGuardian(item);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public void callLatestGuardian(final Item weatherItem) {
        Log.d("called guardian", "call");
        Log.d("items are inside guardian", String.valueOf(weatherItem));
        final List<Item> allCardItems = new ArrayList<>();
        final String url = "http://smaranitreact.us-east-1.elasticbeanstalk.com/showLatestGuardian";
        //final String url = "http://10.0.2.2:9000/showLatestGuardian";
        final ArrayList<String> images = new ArrayList<String>();
        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> times = new ArrayList<String>();
        final ArrayList<String> sections = new ArrayList<String>();
        final ArrayList<String> article_ids = new ArrayList<String>();

        // prepare the Request
        getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //see if an article is bookmarked or not
                        final String MyPREFERENCES = "ArticlePrefs";
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);
                        allCardItems.add(weatherItem);
                        recyclerView = (RecyclerView) findViewById(R.id.recycler);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                        // display response
//                        Log.d("Response", response.toString());
//                        Log.d("second","data");
                        try {
                            jObj = response.getJSONObject("response");
                            jArray = jObj.getJSONArray("results");
                            //Log.d("Results", String.valueOf(jArray));

                            for (int i = 0; i < jArray.length(); i++) {
                                //Log.d("Yes", String.valueOf(i));
                                JSONObject res = jArray.getJSONObject(i);
                                String title = res.optString("webTitle");
                                String article_time = res.optString("webPublicationDate");
                                String section_name = res.optString("sectionName");
                                final String article_id = res.optString("id");
                                JSONObject fields = res.getJSONObject("fields");
                                //Log.d("Fields", String.valueOf(fields));
                                String thumbnail = fields.optString("thumbnail");

                                if (title != null && article_time != null && section_name != null && article_id != null) {

                                    titles.add(title);
                                    times.add(article_time);
                                    sections.add(section_name);
                                    article_ids.add(article_id);

                                    if (thumbnail != null) {
                                        if (thumbnail.length() != 0) {
                                            images.add(thumbnail);
                                        } else {
                                            //if thumbnail is null, add default image for Guardian
                                            thumbnail = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                                            images.add(thumbnail);
                                        }

                                    } else {
                                        //if thumbnail is null, add default image for Guardian
                                        thumbnail = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                                        images.add(thumbnail);
                                    }
                                    String newsDeliveredTime = findTimeDuration(article_time);
                                    //Log.d("minutes ago", newsDeliveredTime);
                                    Item item = new Item(thumbnail, title, newsDeliveredTime, section_name, article_id, article_time);
                                    allCardItems.add(item);
                                    //recycler view for displaying latest news

                                    // adapter = new MyAdapter(recyclerView, MainActivity.this, items);
                                    //recyclerView.setAdapter(adapter);

                                    MyAdapter.RecyclerViewClickListener listener = new MyAdapter.RecyclerViewClickListener() {
                                        @Override
                                        public void onClick(View view, int position) {
                                            String detailedArticleId = String.valueOf(article_ids.get(position - 1));

                                            //Toast.makeText(getApplicationContext(), "articleID " + detailedArticleId, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(MainActivity.this, DetailedArticleActivity.class);
                                            intent.putExtra("articleID", detailedArticleId);
                                            intent.putExtra("image", images.get(position - 1));
                                            Log.d("Sending article ID", detailedArticleId);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public boolean onLongClick(final View view, final int position) {
                                            //Toast.makeText(getApplicationContext(), "Long click " + "yes", Toast.LENGTH_SHORT).show();
                                            // Create dialog object
                                            final Dialog dialog = new Dialog(MainActivity.this);
                                            // Include share_dialog.xml file
                                            dialog.setContentView(R.layout.share_dialog);

                                            ImageView dialog_image = (ImageView) dialog.findViewById(R.id.imageDialog);
                                            try {
                                                Picasso.with(MainActivity.this).load(images.get(position - 1)).resize(dialog_image.getWidth(),160).into(dialog_image);
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                                Log.d("picasso","error in dialog image");
                                            }

                                            // set values for custom dialog components - text, image and button
                                            TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                                            text.setText(titles.get(position - 1));

                                            final ImageButton savedBookmarkButton = (ImageButton) dialog.findViewById(R.id.bookmark_save); //clicked
                                            final ImageButton unsavedBookmarkButton = (ImageButton) dialog.findViewById(R.id.bookmark_unsave); //unclicked


                                            final SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                                            Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
                                            String article_id_to_check = String.valueOf(article_ids.get(position - 1));
                                            int articleInBookMarked = 0;
                                            Log.d("checking if ", "bookmarked");
                                            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                                                if (entry.getKey().equals(article_id_to_check)) {
                                                    Log.d("inside", "bookmarked");
                                                    //already bookmarked before
                                                    savedBookmarkButton.setVisibility(View.VISIBLE);
                                                    unsavedBookmarkButton.setVisibility(View.GONE);
                                                    articleInBookMarked = 1;
                                                    break;
                                                }
                                                Log.d("map values", entry.getKey() + ": " + entry.getValue());
                                            }
                                            if (articleInBookMarked == 0) {
                                                unsavedBookmarkButton.setVisibility(View.VISIBLE);
                                                savedBookmarkButton.setVisibility(View.GONE);
                                            }

                                            dialog.show();

                                            ImageButton twitterShareButton = (ImageButton) dialog.findViewById(R.id.twitter_share);
                                            twitterShareButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String twitterText = "text=Check out this Link:";
                                                    String twitterURL = "url=https://www.theguardian.com/" + article_ids.get(position - 1);
                                                    String twitterHashtag = "hashtags=CSCI571NewsSearch";
                                                    String url = "https://twitter.com/intent/tweet?" + twitterText + "&" + twitterURL + "&" + twitterHashtag;
                                                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
                                                    twitterIntent.setData(Uri.parse(url));
                                                    startActivity(twitterIntent);
                                                    dialog.dismiss();
                                                }
                                            });
                                            //Saving the article which is not saved
                                            unsavedBookmarkButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    savedBookmarkButton.setVisibility(View.VISIBLE);
                                                    unsavedBookmarkButton.setVisibility(View.GONE);

                                                    String bookmarkAdd = "\"" + titles.get(position - 1) + "\"" + " was added to Bookmarks";
                                                    Toast.makeText(MainActivity.this, bookmarkAdd, Toast.LENGTH_LONG).show();

                                                    SharedPreferences saveSharedPrefs = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                                                    JSONObject jsonArticle = new JSONObject();
                                                    try {
                                                        jsonArticle.put("title", titles.get(position - 1));
                                                        jsonArticle.put("image", images.get(position - 1));
                                                        jsonArticle.put("time", times.get(position - 1));
                                                        jsonArticle.put("section", sections.get(position - 1));
                                                        jsonArticle.put("articleID", article_ids.get(position - 1));
                                                    } catch (JSONException e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                    }
                                                    Log.d("obj name is", String.valueOf(jsonArticle));

                                                    SharedPreferences.Editor editor = saveSharedPrefs.edit();
                                                    editor.putString(article_ids.get(position - 1), String.valueOf(jsonArticle));
                                                    editor.commit();
                                                    dialog.dismiss();
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });

                                            //remove an article which was saved
                                            savedBookmarkButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    unsavedBookmarkButton.setVisibility(View.VISIBLE);
                                                    savedBookmarkButton.setVisibility(View.GONE);

                                                    String bookmarkRemove = "\"" + titles.get(position - 1) + "\"" + " was removed from Bookmarks";
                                                    Toast.makeText(MainActivity.this, bookmarkRemove, Toast.LENGTH_LONG).show();

                                                    //removing this article from shared preferences
                                                    SharedPreferences removeSharedPrefs = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                                                    String article_id_to_delete = article_ids.get(position - 1); //articleID
                                                    SharedPreferences.Editor editor = removeSharedPrefs.edit();
                                                    editor.remove(article_id_to_delete);
                                                    editor.commit();
                                                    dialog.dismiss();
                                                    adapter.notifyDataSetChanged();

                                                }
                                            });
                                            return true;
                                        }

                                    };
                                    adapter = new MyAdapter(recyclerView, MainActivity.this, allCardItems, listener);
                                    recyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }

//                            Log.d("Titles", String.valueOf(titles));
                            Log.d("title size", "" + titles.size());
//                            Log.d("Times", String.valueOf(times));
//                            Log.d("Images", String.valueOf(images.size()));
//                            Log.d("Sections", String.valueOf(sections));
//                            Log.d("Article_ids", String.valueOf(article_ids));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // add the volley request for latest news to the RequestQueue
        queue.add(getRequest);

    }


    public String findTimeDuration(String webPubDate) {
        //works only with api 26 and above.
        if (android.os.Build.VERSION.SDK_INT >= 26) {

            //current time in Los Angeles
            ZonedDateTime laCurTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
            LocalDateTime ldt = laCurTime.toLocalDateTime();
            //Log.d("Present time in LA",String.valueOf(ldt));

            int hr_now = ldt.getHour();
            int min_now = ldt.getMinute();
            int sec_now = ldt.getSecond();
            int day_now = ldt.getDayOfYear();
            int year_now = ldt.getYear();

            //web publication date
            //Log.d("web publication date as received from backend",webPubDate);
            int year = Integer.parseInt(webPubDate.substring(0, 4));
            int month = Integer.parseInt(webPubDate.substring(5, 7));
            int dayOfMonth = Integer.parseInt(webPubDate.substring(8, 10));
            int hour = Integer.parseInt(webPubDate.substring(11, 13));
            int minute = Integer.parseInt(webPubDate.substring(14, 16));
            int second = Integer.parseInt(webPubDate.substring(17, 19));

            //this is for getting the web publication date in LA time
            //web date as received from the news api
            LocalDateTime webDate = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
            //web date is in GMT Format. Converting from LocalDateTime GMT to ZonedDateTime GMT(basically just adds zone)
            ZonedDateTime webDateGMTZone = ZonedDateTime.of(webDate, ZoneOffset.UTC);
            //GMT Time converted to LA time
            ZonedDateTime webDateLA = webDateGMTZone.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
//            Log.d("GMT TIME OF WEB DATE ",String.valueOf(webDateGMTZone));
//            Log.d("LA EQUIVALENT TIME OF WEB PUB DATE", String.valueOf(webDateLA));

            int hr_before = webDateLA.getHour();
            int min_before = webDateLA.getMinute();
            int sec_before = webDateLA.getSecond();
            int day_before = webDateLA.getDayOfYear();
            int year_before = webDateLA.getYear();

            if (year_now - year_before > 0)
                day_now = day_now + 365;
            //compare the dates
            //dif in days more than 1
            if (day_now - day_before >= 1) {
                int dif_in_days = day_now - day_before;
                return "" + dif_in_days + "d ago";
            }
            //dif in hrs more than 1
            else if (hr_now - hr_before >= 1) {
                int dif_in_hrs = hr_now - hr_before;
                return "" + dif_in_hrs + "h ago";
            }
            //dif in min more than 1
            else if (min_now - min_before >= 1) {
                int dif_in_mins = min_now - min_before;
                return "" + dif_in_mins + "m ago";
            }
            //dif in secs
            else {
                int dif_in_secs = sec_now - sec_before;
                if(dif_in_secs<=0){
                    dif_in_secs=1;
                }
                return "" + dif_in_secs + "s ago";
            }

        }
        return "";
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


    public void callLatestSuggestion(String query) {
        String host = "https://api.cognitive.microsoft.com";
        String path = "/bing/v7.0/suggestions";
        String mkt = "en-US";
        String params = "?mkt=" + mkt + "&q=" + query;

        final String url = host + path + params;

        getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Auto", String.valueOf(response));
                try {
                    JSONObject suggestion = response.getJSONArray("suggestionGroups").getJSONObject(0);
                    JSONArray searchSuggestions = suggestion.getJSONArray("searchSuggestions");
                    adapter_search.clear();

                    for (int i = 0; i < 5; i++) {
                        String word = searchSuggestions.getJSONObject(i).getString("displayText");
                        adapter_search.add(word);
                    }

                    Log.d("Suggested", String.valueOf(adapter_search.getCount()));
                    adapter_search.notifyDataSetChanged();
                    //searchAutoComplete.setAdapter(adapter_search);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Auto", "Error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Ocp-Apim-Subscription-Key", "72f79c91d9f24f4b9f6ca16e19e97290");
                return params;
            }
        };

        queue.add(getRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.search_view,menu);

        SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView=(SearchView) menu.findItem(R.id.search).getActionView();
        //final SearchView.SearchAutoComplete
        final AutoCompleteTextView searchAutoComplete=searchView.findViewById(R.id.search_src_text);

        //searchView.setIconifiedByDefault(true);
        searchAutoComplete.setThreshold(3);
        SpannableString spannableString = new SpannableString("@");
        Drawable d = getResources().getDrawable(R.drawable.ic_search_grey);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
        spannableString.setSpan(span, spannableString.toString().indexOf("@"),  spannableString.toString().indexOf("@")+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        searchView.setQueryHint(spannableString);
        //adapter_search=new ArrayAdapter<>(getApplicationContext(),R.layout.suggestion,suggestedWords);

        adapter_search=new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line);

        searchAutoComplete.setAdapter(adapter_search);


        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this,SearchResultsActivity.class)));
        //searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.length()>2)
                    callLatestSuggestion(newText);
                //Log.d("Query", String.valueOf(suggestedWords));
                //Log.d("Query",callLatestSuggestion(newText));
                return true;
            }
        });



        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.d("Suggestion",suggestedWords.get(i));
                String query_string=(String) adapterView.getItemAtPosition(i);
                searchAutoComplete.setText(query_string);
            }
        });


        final InputMethodManager imm=(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        final View view=getCurrentFocus();

        menu.findItem(R.id.search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                //Log.d("MenuOpen","Yes");
                menuItem.getActionView().requestFocus();
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                Log.d("MenuCLose","Yes");
                if(view!=null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                return true;
            }
        });

        return true;
    }
    @Override
    protected void onRestart() {
        //items.clear();
        //restart=1;
        super.onRestart();
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.home);
        if (checkPermissions()) {
            //items.clear();
            Log.d("resume","resuming");
            //Log.d("items are",String.valueOf(items));
            getLastLocation();
//            Item item=new Item(cityName, stateName, temp_celsius, weather_descr, weatherImage);
//            callLatestGuardian(item);
        }

    }


    }
