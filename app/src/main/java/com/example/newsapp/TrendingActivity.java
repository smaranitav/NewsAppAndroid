package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrendingActivity extends AppCompatActivity {


    JsonObjectRequest getRequest_trends;
    //RequestQueue queue_trends;
    JSONObject default_object;
    JSONArray timelineData;
    LineChart mpLineChart;
    ArrayList<ILineDataSet> dataSets=new ArrayList<>();
    LineDataSet lineDataSet;
    LineData data;
    Legend legend;
    EditText searchText;
    String keyword;
    ArrayList<Entry> dataValues1=new ArrayList<Entry>();
    ArrayList<Integer> values=new ArrayList<>();

    //volley
    RequestQueue queue ;
    JsonObjectRequest getRequest;

    //AutoSuggest API
    ArrayAdapter<String> adapter_search;
    SearchView.SearchAutoComplete searchAutoComplete;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        queue= Volley.newRequestQueue(getApplicationContext());

        searchText=findViewById(R.id.searchText);
        keyword="Coronavirus";
        mpLineChart=findViewById(R.id.chart);

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.trending);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId()){
                    case R.id.headlines:
                        startActivity(new Intent(getApplicationContext(),HeadlinesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.trending:
                        return true;
                    case R.id.bookmarks:
                        startActivity(new Intent(getApplicationContext(),BookmarksActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        // queue_trends= Volley.newRequestQueue(this);
        createGraph(keyword);


        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==EditorInfo.IME_ACTION_SEND){
                    Log.d("Enter","yes");
                    keyword=searchText.getText().toString();
                    if(keyword.length()==0)
                        keyword="CoronaVirus";
                    createGraph(keyword);
                    return true;
                }
                return false;
            }
        });

    }


    public void createGraph(String searchWord){

        values.clear();
        dataValues1.clear();

        if(lineDataSet!=null)
            lineDataSet.clear();
        if(dataSets!=null)
            dataSets.clear();
        if(data!=null)
            data.clearValues();

        mpLineChart.invalidate();
        mpLineChart.clear();

        final String url = "http://smaranitreact.us-east-1.elasticbeanstalk.com/showTrends?searchKeyword="+keyword;
        //final String url = "http://10.0.2.2:9000/showTrends?searchKeyword="+keyword;

        getRequest_trends=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("response", response.toString());

                try {
                    default_object=response.getJSONObject("default");
                    timelineData=default_object.getJSONArray("timelineData");

                    for(int i=0;i<timelineData.length();i++){
                        JSONObject jObj=timelineData.getJSONObject(i);
                        JSONArray jArray=jObj.getJSONArray("value");
                        int value=jArray.getInt(0);

                        values.add(value);
                        Log.d("Value " + String.valueOf(i), String.valueOf(value));

                    }

                    Log.d("Values", String.valueOf(values));


                    for(int i=0;i<values.size();i++){
                        dataValues1.add(new Entry(i,values.get(i)));
                    }

                    mpLineChart.clear();

                    lineDataSet=new LineDataSet(dataValues1,"Trending Chart for "+keyword);
                    lineDataSet.setColor(R.color.colorAccent);
                    lineDataSet.setCircleColor(R.color.colorAccent);
                    dataSets.add(lineDataSet);
                    data=new LineData(dataSets);
                    mpLineChart.setData(data);

                    mpLineChart.getXAxis().setDrawGridLines(false);
                    mpLineChart.getAxisLeft().setDrawGridLines(false);
                    mpLineChart.getAxisRight().setDrawGridLines(false);
                    mpLineChart.getAxisLeft().setDrawAxisLine(false);

                    legend=mpLineChart.getLegend();
                    legend.setTextColor(Color.BLACK);
                    legend.setTextSize(15);
                    legend.setFormSize(12f);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        });

        queue.add(getRequest_trends);

    }

    public void callLatestSuggestion(String query){
        String host= "https://api.cognitive.microsoft.com";
        String path= "/bing/v7.0/suggestions";
        String mkt= "en-US";
        String params = "?mkt=" + mkt + "&q=" + query;

        final String url = host+path+params;



        getRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Auto", String.valueOf(response));
                try {
                    JSONObject suggestion=response.getJSONArray("suggestionGroups").getJSONObject(0);
                    JSONArray searchSuggestions=suggestion.getJSONArray("searchSuggestions");
                    adapter_search.clear();

                    for(int i=0;i<5;i++){
                        String word=searchSuggestions.getJSONObject(i).getString("displayText");
                        adapter_search.add(word);
                    }

                    Log.d("Suggested", String.valueOf(adapter_search.getCount()));
                    adapter_search.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Auto","Error");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params=new HashMap<String,String>();
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
        super.onRestart();
        bottomNavigationView.setSelectedItemId(R.id.trending);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.trending);
    }


}
