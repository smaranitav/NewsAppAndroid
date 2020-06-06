package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

public class DetailedArticleActivity extends AppCompatActivity {
    String articleId;
    String img;
    //volley
    RequestQueue queue ;
    JsonObjectRequest getRequest;
    String date;
    String section;
    String description;
    String articleURL;
    String image;
    String webTitle;
    ImageView imageView;
    CardView cardView;
    TextView title_textView;
    TextView section_textView;
    TextView date_textView;
    TextView description_textView;
    TextView viewFullArticle_textView;
    ProgressBar progressBar;
    TextView progressText;
    String MyPREFERENCES = "ArticlePrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_article);

        queue= Volley.newRequestQueue(getApplicationContext());

        cardView=findViewById(R.id.cardView_detailed);
        imageView=findViewById(R.id.image_detailed_article);
        title_textView=findViewById(R.id.title_detailed_article);
        section_textView=findViewById(R.id.section_detailed_article);
        date_textView=findViewById(R.id.date_detailed_article);
        description_textView=findViewById(R.id.description_detailed_article);
        viewFullArticle_textView=findViewById(R.id.viewFullArticle_detailed_article);

        //progress bar
        progressBar=findViewById(R.id.loadingProgress_detailed);
        progressText=findViewById(R.id.progressText_detailed);
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.GONE);

        Bundle obj=getIntent().getExtras();
        articleId=obj.getString("articleID");
        img=obj.getString("image");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        callDetailedArticle();
        Log.d("activity name","Detailed article");
        Log.d("article id received in a new activity",articleId);

        viewFullArticle_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(articleURL!=null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(articleURL));
                    startActivity(i);
                }
            }
        });
    }

    public void callDetailedArticle(){

        final String url = "http://smaranitreact.us-east-1.elasticbeanstalk.com/GuardianDetailedArticle?articleId="+articleId;
        //final String url = "http://10.0.2.2:9000/GuardianDetailedArticle?articleId="+articleId;


        getRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("DetailedArticle", String.valueOf(response));
                try {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    cardView.setVisibility(View.VISIBLE);
                    JSONObject responseObject=response.getJSONObject("response");
                    JSONObject contentObj= responseObject.getJSONObject("content");
                    Log.d("Object", String.valueOf(responseObject));

                    webTitle= contentObj.optString("webTitle");
                    getSupportActionBar().setTitle(webTitle);

                    date=contentObj.optString("webPublicationDate");
                    String date_formated=findTime(date);
                    section=contentObj.optString("sectionName");
                    JSONArray body=contentObj.getJSONObject("blocks").getJSONArray("body");

                    for(int i=0;i<body.length();i++){
                        String des=body.getJSONObject(i).optString("bodyHtml");
                        if(des!=null && des.length()!=0)
                            description=description+des;
                        Log.d("Des",des);
                    }
                    description=description.substring(4);

                    articleURL=contentObj.optString("webUrl");
                    //image=contentObj.getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).optString("file");

//                    if(image==null || image.length()==0)
//                        image="https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                    try {
                        ImageView image_detailed_article=findViewById(R.id.image_detailed_article);
                        Picasso.with(getApplicationContext()).load(img).resize(image_detailed_article.getWidth(),250).into(imageView);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Log.d("picasso", "error in displaying due to canvas too large");
                    }
                    title_textView.setText(webTitle);
                    section_textView.setText(section);
                    date_textView.setText(date_formated);
                    description_textView.setText(HtmlCompat.fromHtml(description,0));
                    String text="<u>View Full Article</u>";
                    viewFullArticle_textView.setText(HtmlCompat.fromHtml(text,0));

                    Log.d("Title",webTitle);
                    Log.d("Date",date_formated);
                    Log.d("Section",section);
                    Log.d("Description",description);
                    Log.d("ArticleURL",articleURL);
                    Log.d("Image",img);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DetailedArticle","Error");
                Log.d("Error", String.valueOf(error));
            }
        });

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(getRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.toolbar_view,menu);

        //get from shared preferences
        SharedPreferences sharedpreferences= getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
        int articleInBookMarked=0;
        Log.d("came inside","menuu");
        //check if article is bookmarked or no
        for (Map.Entry<String, String> entry : allEntries.entrySet()) {
            Log.d("article id inside menu",articleId);
            if(entry.getKey().equals(articleId)){
                Log.d("inside","bookmarked");

                menu.findItem(R.id.bookmark_toolbar).setIcon(R.drawable.big_clicked_bookmark);
                //already bookmarked before
                articleInBookMarked=1;
                break;
            }
            Log.d("map values", entry.getKey() + ": " + entry.getValue());
        }
        if(articleInBookMarked == 0){
            menu.findItem(R.id.bookmark_toolbar).setIcon(R.drawable.big_unclicked_bookmark);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.bookmark_toolbar:
                Log.d("Bookmarked","symbol");
                //when u click on bookmark
                //get from shared preferences
                SharedPreferences sharedpreferences= getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
                int articleAlreadyBookMarked=0;//0 if not bookmarked before
                Log.d("came inside","menuu");
                //check if article is bookmarked
                for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                    Log.d("article id inside menu",articleId);
                    if(entry.getKey().equals(articleId)){
                        //article is bookmarked, now remove from bookmark
                        Log.d("inside","bookmarked");

                        //change icon
                        item.setIcon(R.drawable.big_unclicked_bookmark);

                        //already bookmarked before, so unbookmark it
                        articleAlreadyBookMarked=1;
                        Log.d("webtitle",webTitle);
                        String bookmarkRemove="\""+ webTitle +"\""+" was removed from Bookmarks";
                        Toast.makeText(getApplicationContext(), bookmarkRemove, Toast.LENGTH_LONG).show();

                        //removing this article from shared preferences
                        SharedPreferences removeSharedPrefs = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                        //articleID
                        SharedPreferences.Editor editor = removeSharedPrefs.edit();
                        editor.remove(articleId);
                        editor.commit();

                        break;
                    }
                    Log.d("map values", entry.getKey() + ": " + entry.getValue());
                }
                if(articleAlreadyBookMarked == 0){
                    //bookmark it now
                   item.setIcon(R.drawable.big_clicked_bookmark);

                    String bookmarkAdd="\""+ webTitle +"\""+" was added to Bookmarks";
                    Toast.makeText(getApplicationContext(), bookmarkAdd, Toast.LENGTH_LONG).show();

                    SharedPreferences saveSharedPrefs = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


                    JSONObject jsonArticle = new JSONObject();
                    try {
                        jsonArticle.put("title",webTitle);
                        jsonArticle.put("image", img);
                        jsonArticle.put("time",date);
                        jsonArticle.put("section",section);
                        jsonArticle.put("articleID",articleId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.d("obj name is",String.valueOf(jsonArticle));

                    SharedPreferences.Editor editor = saveSharedPrefs.edit();
                    editor.putString(articleId,String.valueOf(jsonArticle));
                    editor.commit();
                }

                break;


            case R.id.twitter_toolbar:
                Log.d("Twitter","Yes");
                String twitterText="text=Check out this Link:";
                String twitterURL="url=https://www.theguardian.com/"+articleId;
                String twitterHashtag ="hashtags=CSCI571NewsSearch";
                String url = "https://twitter.com/intent/tweet?"+twitterText+"&"+twitterURL+"&"+twitterHashtag;
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
                twitterIntent.setData(Uri.parse(url));
                startActivity(twitterIntent);
                break;
            case android.R.id.home:
                Log.d("Home","Yes");
                finish();
                break;
            default:
                break;
        }

        return true;
    }
    // Override this method to do what you want when the menu is recreated
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       // String icon=String.valueOf(menu.findItem(R.id.bookmark_toolbar).getIcon());
        //Log.d("icon is",icon);
//        if(icon)
//
//        menu.findItem(R.id.bookmark_toolbar).setIcon(R.drawable.clicked_bookmark);
        return super.onPrepareOptionsMenu(menu);
    }

    public String findTime(String webPubDate) {
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

            int dd = webDateLA.getDayOfMonth();
            String ddDate;

            if(dd<10){
                ddDate= "0"+dd;
            }
            else{
                ddDate=String.valueOf(dd);
            }
            int yyyy = webDateLA.getYear();
            String mmm = webDateLA.getMonth().name();

            String final_date = ddDate + " " + mmm.charAt(0)+mmm.substring(1,3).toLowerCase() + " " + String.valueOf(yyyy);

            return final_date;
        }

        return "";
    }


}