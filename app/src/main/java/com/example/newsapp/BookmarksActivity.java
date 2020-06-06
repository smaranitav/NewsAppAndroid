package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.RecyclerAdapter.BookmarksAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarksActivity extends AppCompatActivity {

    //volley
    RequestQueue queue ;
    JsonObjectRequest getRequest;

    //AutoSuggest API
    ArrayAdapter<String> adapter_search;
    SearchView.SearchAutoComplete searchAutoComplete;
    String MyPREFERENCES = "ArticlePrefs" ;

    RecyclerView bookmarkRecyclerView;
    TextView noBookmarkText;
    BottomNavigationView bottomNavigationView;
    BookmarksAdapter bookmarksAdapter;
    List<Item> bookmarkItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        //recycler view and adapter
        bookmarkRecyclerView=(RecyclerView)findViewById(R.id.bookmarks_recycler);

        queue= Volley.newRequestQueue(getApplicationContext());
        populateCard();

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bookmarks);
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
                        startActivity(new Intent(getApplicationContext(),TrendingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.bookmarks:
                        return true;
                }
                return false;
            }
        });



    }
    public void populateCard(){
        //list for bookmark articles
        bookmarkItems=new ArrayList<>();

        //get from shared preferences
        SharedPreferences sharedpreferences= getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        //Map<>=sharedpreferences.getAll();
        Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
        if(allEntries.size()==0){
            //no bookmarked articles
            //display no bookmarked articles
            bookmarkRecyclerView.setVisibility(View.GONE);
            noBookmarkText=(TextView)findViewById(R.id.no_bookmark_text);
            noBookmarkText.setVisibility(View.VISIBLE);
        }
        else {
            bookmarkRecyclerView.setVisibility(View.VISIBLE);
            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                //Log.d("count","");
                try {
                    JSONObject bookmarkObject = new JSONObject(val);
                    String bookmarkTitle = bookmarkObject.getString("title");
                    String bookmarkImage = bookmarkObject.getString("image");
                    String bookmarkDate = bookmarkObject.getString("time");
                    String bookmarkSection = bookmarkObject.getString("section");
                    Log.d("title", bookmarkTitle);
                    Log.d("img", bookmarkImage);
                    Log.d("date", bookmarkDate);
                    Log.d("section", bookmarkSection);
                    Item item = new Item(bookmarkImage, bookmarkTitle, bookmarkDate, bookmarkSection, key, bookmarkDate);
                    bookmarkItems.add(item);

                    // Log.d("jsonobj",""+bookmarkObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("map values", key + ": " + val);
            }
            bookmarksAdapter = new BookmarksAdapter(this, bookmarkItems);
            bookmarkRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            bookmarkRecyclerView.setAdapter(bookmarksAdapter);
            bookmarksAdapter.notifyDataSetChanged();
        }

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
        bottomNavigationView.setSelectedItemId(R.id.bookmarks);
        Log.d("restart","bookmark");

    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.bookmarks);
        Log.d("resume","bookmark");
        //bookmarksAdapter.notifyDataSetChanged();
        populateCard();

    }

}