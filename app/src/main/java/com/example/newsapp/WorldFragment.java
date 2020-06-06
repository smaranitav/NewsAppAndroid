package com.example.newsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.RecyclerAdapter.FragAdapter;
import com.example.newsapp.RecyclerAdapter.MyAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class WorldFragment extends Fragment {
    FragAdapter adapter;
    RecyclerView recyclerView;

    RequestQueue queue ;
    JsonObjectRequest getRequest;

    //AutoSuggest API
    ArrayAdapter<String> adapter_search;
    SearchView.SearchAutoComplete searchAutoComplete;

    //data for latest news
    JSONObject jObj;
    JSONArray jArray;

    Context context;
    ProgressBar progressBar;
    TextView progressText;

    //refreshlayout
    SwipeRefreshLayout mSwipeRefreshLayout;
    Handler handler;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        //recyclerView = (RecyclerView) viewGroup.findViewById(R.id.world_recycler);
        view= inflater.inflate(R.layout.worldlayout, viewGroup, false);

        progressBar=(ProgressBar)view.findViewById(R.id.worldProgress);
        progressBar.setVisibility(View.VISIBLE);

        progressText=(TextView)view.findViewById(R.id.worldProgressText);
        progressText.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_world);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to make your refresh action
                // CallYourRefreshingMethod();
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                callLatestGuardian(view);
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
            }
        });

        context=view.getContext();
        queue= Volley.newRequestQueue(context);
        callLatestGuardian(view);
        return view;
    }
    public void callLatestGuardian(final View entireView){

        final List<Item> allCardItems=new ArrayList<>();

        final String url = "http://smaranitreact.us-east-1.elasticbeanstalk.com/guardianSection?sectionName=world";
        //final String url="http://10.0.2.2:9000/guardianSection?sectionName=world";
        final ArrayList<String> images=new ArrayList<String>();
        final ArrayList<String> titles=new ArrayList<String>();
        final ArrayList<String> times=new ArrayList<String>();
        final ArrayList<String> article_ids=new ArrayList<String >();

        // prepare the Request
        getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                         display response
                        Log.d("Response", response.toString());
                        final String MyPREFERENCES = "ArticlePrefs" ;
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);

                        try {
                            jArray = response.getJSONArray("guardianArticles");
                            Log.d("Results Search", String.valueOf(jArray));
                            Log.d("Array length", String.valueOf(jArray.length()));

                            for (int i = 0; i < jArray.length(); i++) {
                                //Log.d("Yes", String.valueOf(i));
                                JSONObject res = jArray.getJSONObject(i);
                                String title = res.optString("webTitle");
                                String article_time = res.optString("webPublicationDate");
                                String section_name = res.optString("sectionId");
                                String article_id = res.optString("id");

                                String thumbnail = res.optString("imgSrc");
                                Log.d("Titles in fragment",title);

                                if (title != null && article_time != null && section_name != null && article_id != null) {

                                    titles.add(title);
                                    times.add(article_time);
                                    //sections.add("World");
                                    article_ids.add(article_id);

                                    if (thumbnail != null) {
                                        if(thumbnail.length()!=0) {
                                            images.add(thumbnail);
                                        }
                                        else {
                                            //if thumbnail is null, add default image for Guardian
                                            thumbnail = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                                            images.add(thumbnail);
                                        }

                                    } else {
                                        //if thumbnail is null, add default image for Guardian
                                        thumbnail = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                                        images.add(thumbnail);
                                    }
                                    String newsDeliveredTime= findTimeDuration(article_time);
                                    Log.d("minutes ago", newsDeliveredTime);


                                    Item item = new Item(thumbnail, title, newsDeliveredTime, "World news", article_id, article_time);
                                    allCardItems.add(item);
                                    //recycler view for displaying latest news
                                    recyclerView=entireView.findViewById(R.id.world_recycler);

                                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                    // adapter = new MyAdapter(recyclerView, MainActivity.this, items);
                                    //recyclerView.setAdapter(adapter);

                                    FragAdapter.RecyclerViewClickListener listener = new FragAdapter.RecyclerViewClickListener() {
                                        @Override
                                        public void onClick(View view, int position) {
                                            String detailedArticleId=String.valueOf(article_ids.get(position));

                                            //Toast.makeText(context, "articleID " + detailedArticleId , Toast.LENGTH_SHORT).show();
                                            Intent intent=new Intent(context, DetailedArticleActivity.class);
                                            intent.putExtra("articleID",detailedArticleId);
                                            intent.putExtra("image",images.get(position));
                                            Log.d("Sending article ID",detailedArticleId);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public boolean onLongClick(View view, final int position) {
                                            //Toast.makeText(context, "Long click " + "yes", Toast.LENGTH_SHORT).show();
                                            // Create dialog object
                                            final Dialog dialog = new Dialog(context);
                                            // Include share_dialog.xml file
                                            dialog.setContentView(R.layout.share_dialog);

                                            ImageView dialog_image = (ImageView) dialog.findViewById(R.id.imageDialog);
                                            try {
                                                Picasso.with(context).load(images.get(position)).resize(dialog_image.getWidth(), 160).into(dialog_image);
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                                Log.d("picasso","error");
                                            }

                                            // set values for custom dialog components - text, image and button
                                            TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                                            text.setText(titles.get(position));

                                            final ImageButton savedBookmarkButton =(ImageButton) dialog.findViewById(R.id.bookmark_save); //clicked
                                            final ImageButton unsavedBookmarkButton =(ImageButton) dialog.findViewById(R.id.bookmark_unsave); //unclicked


                                            final SharedPreferences sharedpreferences= context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                                            Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
                                            String article_id_to_check=String.valueOf(article_ids.get(position));
                                            int articleInBookMarked=0;
                                            Log.d("checking if ","bookmarked");
                                            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                                                if(entry.getKey().equals(article_id_to_check)){
                                                    Log.d("inside","bookmarked");
                                                    //already bookmarked before
                                                    savedBookmarkButton.setVisibility(View.VISIBLE);
                                                    unsavedBookmarkButton.setVisibility(View.GONE);
                                                    articleInBookMarked=1;
                                                    break;
                                                }
                                                Log.d("map values", entry.getKey() + ": " + entry.getValue());
                                            }
                                            if(articleInBookMarked == 0){
                                                unsavedBookmarkButton.setVisibility(View.VISIBLE);
                                                savedBookmarkButton.setVisibility(View.GONE);
                                            }

                                            dialog.show();

                                            ImageButton twitterShareButton = (ImageButton) dialog.findViewById(R.id.twitter_share);
                                            twitterShareButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String twitterText="text=Check out this Link:";
                                                    String twitterURL="url=https://www.theguardian.com/"+article_ids.get(position);
                                                    String twitterHashtag ="hashtags=CSCI571NewsSearch";
                                                    String url = "https://twitter.com/intent/tweet?"+twitterText+"&"+twitterURL+"&"+twitterHashtag;
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

                                                    String bookmarkAdd="\""+ titles.get(position) +"\""+" was added to Bookmarks";
                                                    Toast.makeText(context, bookmarkAdd, Toast.LENGTH_LONG).show();

                                                    SharedPreferences saveSharedPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                                                    JSONObject jsonArticle = new JSONObject();
                                                    try {
                                                        jsonArticle.put("title",titles.get(position));
                                                        jsonArticle.put("image",images.get(position));
                                                        jsonArticle.put("time",times.get(position));
                                                        jsonArticle.put("section","World news");
                                                        jsonArticle.put("articleID",article_ids.get(position));
                                                    } catch (JSONException e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                    }
                                                    Log.d("obj name is",String.valueOf(jsonArticle));

                                                    SharedPreferences.Editor editor = saveSharedPrefs.edit();
                                                    editor.putString(article_ids.get(position),String.valueOf(jsonArticle));
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

                                                    String bookmarkRemove="\""+ titles.get(position) +"\""+" was removed from Bookmarks";
                                                    Toast.makeText(context, bookmarkRemove, Toast.LENGTH_LONG).show();

                                                    //removing this article from shared preferences
                                                    SharedPreferences removeSharedPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                                                    String article_id_to_delete=article_ids.get(position); //articleID
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
                                    adapter = new FragAdapter(recyclerView, context, allCardItems, listener);
                                    recyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                            }

//                            Log.d("Titles", String.valueOf(titles));
                            Log.d("title size", ""+titles.size());
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
        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add the volley request for latest news to the RequestQueue
        queue.add(getRequest);

    }
    public String findTimeDuration(String webPubDate){
        //works only with api 26 and above.
        if (android.os.Build.VERSION.SDK_INT >=26) {

            //current time in Los Angeles
            ZonedDateTime laCurTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
            LocalDateTime ldt = laCurTime.toLocalDateTime();
            //Log.d("Present time in LA",String.valueOf(ldt));

            int hr_now=ldt.getHour();
            int min_now=ldt.getMinute();
            int sec_now=ldt.getSecond();
            int day_now=ldt.getDayOfYear();
            int year_now=ldt.getYear();

            //web publication date
            //Log.d("web publication date as received from backend",webPubDate);
            int year=Integer.parseInt(webPubDate.substring(0,4));
            int month=Integer.parseInt(webPubDate.substring(5,7));
            int dayOfMonth=Integer.parseInt(webPubDate.substring(8,10));
            int hour=Integer.parseInt(webPubDate.substring(11,13));
            int minute=Integer.parseInt(webPubDate.substring(14,16));
            int second=Integer.parseInt(webPubDate.substring(17,19));

            //this is for getting the web publication date in LA time
            //web date as received from the news api
            LocalDateTime webDate = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
            //web date is in GMT Format. Converting from LocalDateTime GMT to ZonedDateTime GMT(basically just adds zone)
            ZonedDateTime webDateGMTZone = ZonedDateTime.of(webDate, ZoneOffset.UTC );
            //GMT Time converted to LA time
            ZonedDateTime webDateLA= webDateGMTZone.withZoneSameInstant( ZoneId.of( "America/Los_Angeles" ) );
//            Log.d("GMT TIME OF WEB DATE ",String.valueOf(webDateGMTZone));
//            Log.d("LA EQUIVALENT TIME OF WEB PUB DATE", String.valueOf(webDateLA));

            int hr_before =webDateLA.getHour();
            int min_before=webDateLA.getMinute();
            int sec_before=webDateLA.getSecond();
            int day_before=webDateLA.getDayOfYear();
            int year_before=webDateLA.getYear();

            if(year_now-year_before>0)
                day_now=day_now+365;
            //compare the dates
            //dif in days more than 1
            if(day_now-day_before>=1){
                int dif_in_days=day_now-day_before;
                return ""+dif_in_days+"d ago";
            }
            //dif in hrs more than 1
            else if(hr_now-hr_before>=1){
                int dif_in_hrs=hr_now-hr_before;
                return ""+dif_in_hrs+"h ago";
            }
            //dif in min more than 1
            else if(min_now-min_before>=1){
                int dif_in_mins=min_now-min_before;
                return ""+dif_in_mins+"m ago";
            }
            //dif in secs
            else{
                int dif_in_secs=sec_now-sec_before;
                if(dif_in_secs<=0){
                    dif_in_secs=1;
                }
                return ""+dif_in_secs+"s ago";
            }

        }
        return "";
    }
    public void onResume() {
        super.onResume();
        callLatestGuardian(view);
    }



}
