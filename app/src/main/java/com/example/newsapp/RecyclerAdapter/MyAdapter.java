package com.example.newsapp.RecyclerAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsapp.Item;
import com.example.newsapp.R;
import com.example.newsapp.recyclerinterface.LoadMoreItems;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;
    public LoadingViewHolder(@NonNull View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }
}

class ItemViewHolder extends RecyclerView.ViewHolder  {
    public TextView title, time, section, articleId;
    public ImageView articleImg;
    public ImageView unclickedBookmark, clickedBookmark;

    private MyAdapter.RecyclerViewClickListener mListener;
    Context context;

    public ItemViewHolder(@NonNull View itemView, MyAdapter.RecyclerViewClickListener listener) {
        super(itemView);
        Log.d("context", ""+itemView.getContext());
        context=itemView.getContext();
        mListener =listener;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CLICKED CARD ","SMALL CLICK");
                mListener.onClick(view, getAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onLongClick(view, getAdapterPosition());
                return true;
            }
        });

//        itemView.setOnClickListener(this);
//        itemView.setOnLongClickListener(this);
        articleImg=(ImageView) itemView.findViewById(R.id.articleImg);
        title=(TextView) itemView.findViewById(R.id.title);
        time=(TextView) itemView.findViewById(R.id.time);
        section=(TextView) itemView.findViewById(R.id.section);
        unclickedBookmark=(ImageView)itemView.findViewById(R.id.unclickedBookmark);
        clickedBookmark=(ImageView)itemView.findViewById(R.id.clickedBookmark);

        //articleId=(TextView) itemView.findViewById(R.id.articleId);

    }
}
class WeatherViewHolder extends RecyclerView.ViewHolder{
    public TextView cityText, stateText, temp, descr;
    public RelativeLayout weatherLayout;
    public WeatherViewHolder(@NonNull View itemView) {
        super(itemView);

        weatherLayout = (RelativeLayout) itemView.findViewById(R.id.weatherLayout);
        cityText=(TextView) itemView.findViewById(R.id.cityText);
        stateText=(TextView) itemView.findViewById(R.id.stateText);
        temp=(TextView) itemView.findViewById(R.id.temp);
        descr=(TextView)itemView.findViewById(R.id.condDescr);

    }

}
public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final int VIEW_TYPE_ITEM=0, VIEW_TYPE_LOADING=1, VIEW_TYPE_WEATHER=2;
    LoadMoreItems loadMore;
    boolean isLoading;
    Activity activity;
    List<Item> items;
    int visibleThreshold=5;
    int lastVisibleItem, totalItemCount;
    private MyAdapter.RecyclerViewClickListener mListener;

    public MyAdapter(RecyclerView recyclerView, Context activity, List<Item> items, MyAdapter.RecyclerViewClickListener listener){
        this.activity= (Activity) activity;
        this.items=items;
        mListener=listener;

        final LinearLayoutManager linearLayoutManager=(LinearLayoutManager)recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount=linearLayoutManager.getItemCount();
                lastVisibleItem=linearLayoutManager.findLastVisibleItemPosition();
                if(!isLoading && totalItemCount <=(lastVisibleItem+visibleThreshold)){
                    if(loadMore!=null){
                        loadMore.onLoadMore();
                    }
                }
                isLoading=true;
            }
        });


    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return VIEW_TYPE_WEATHER;
        }
        else {
            return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }
    }

    public void setLoadMore(LoadMoreItems loadMore){
        this.loadMore =loadMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_WEATHER){
            Log.d("came inside weather","hey");
            View view = LayoutInflater.from(activity).inflate(R.layout.weather_layout, parent, false);
            return  new WeatherViewHolder(view);

        }
        if(viewType == VIEW_TYPE_ITEM){
            Log.d("item","hey");
            View view = LayoutInflater.from(activity).inflate(R.layout.item_layout, parent, false);
            return  new ItemViewHolder(view,mListener);
        }
        else if(viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return  new LoadingViewHolder(view);

        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if ( holder instanceof ItemViewHolder){
            final String MyPREFERENCES = "ArticlePrefs" ;

            Item item = items.get(position);
            final ItemViewHolder viewHolder =(ItemViewHolder) holder;
            Log.d("CONTEXT", String.valueOf(viewHolder.context));
            Log.d("position", ""+position);
            Log.d("image ",String.valueOf(items.get(position).getImage()));
            try {
                Picasso.with(viewHolder.context).load(String.valueOf(items.get(position).getImage())).resize(130, 130).into(viewHolder.articleImg);
            }
            catch(Exception e){
                e.printStackTrace();
                Log.d("picasso","error");
            }
            //viewHolder.articleImg.setText(items.get(position).getImage());
            viewHolder.title.setText(String.valueOf(items.get(position).getTitle()));
            viewHolder.time.setText(items.get(position).getTimes());
            viewHolder.section.setText(String.valueOf(items.get(position).getSection()));

            final String articleID=items.get(position).getArticleId(); //articleID
            final String articleImage= items.get(position).getImage();
            final String articleTitle=items.get(position).getTitle();
            final String articleOriginalTime=items.get(position).getArticleTime();//original date. not 3m ago or time difference
            final String articleSectionName= items.get(position).getSection();

            //get from shared preferences
            SharedPreferences sharedpreferences= viewHolder.context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
            int articleInBookMarked=0;
            Log.d("twice","pp");
            //check if article is bookmarked or no
            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                if(entry.getKey().equals(articleID)){
                    Log.d("inside","bookmarked");
                    //already bookmarked before
                    viewHolder.clickedBookmark.setVisibility(View.VISIBLE);
                    viewHolder.unclickedBookmark.setVisibility(View.GONE);
                    articleInBookMarked=1;
                    break;
                }
                Log.d("map values", entry.getKey() + ": " + entry.getValue());
            }
            if(articleInBookMarked == 0){
                viewHolder.unclickedBookmark.setVisibility(View.VISIBLE);
                viewHolder.clickedBookmark.setVisibility(View.GONE);
            }

            viewHolder.unclickedBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.clickedBookmark.setVisibility(View.VISIBLE);
                    viewHolder.unclickedBookmark.setVisibility(View.GONE);

                    String bookmarkAdd="\""+ items.get(position).getTitle() +"\""+" was added to Bookmarks";
                    Toast.makeText(viewHolder.context, bookmarkAdd, Toast.LENGTH_LONG).show();

                    SharedPreferences saveSharedPrefs = viewHolder.context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


                    JSONObject jsonArticle = new JSONObject();
                    try {
                        jsonArticle.put("title",articleTitle);
                        jsonArticle.put("image", articleImage);
                        jsonArticle.put("time",articleOriginalTime);
                        jsonArticle.put("section",articleSectionName);
                        jsonArticle.put("articleID",articleID);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.d("obj name is",String.valueOf(jsonArticle));

                    SharedPreferences.Editor editor = saveSharedPrefs.edit();
                    editor.putString(articleID,String.valueOf(jsonArticle));
                    editor.commit();

                }
            });
            viewHolder.clickedBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.unclickedBookmark.setVisibility(View.VISIBLE);
                    viewHolder.clickedBookmark.setVisibility(View.GONE);

                    String bookmarkRemove="\""+ items.get(position).getTitle() +"\""+" was removed from Bookmarks";
                    Toast.makeText(viewHolder.context, bookmarkRemove, Toast.LENGTH_LONG).show();


                    //removing this article from shared preferences
                    SharedPreferences removeSharedPrefs = viewHolder.context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                    String articleID=items.get(position).getArticleId(); //articleID
                    SharedPreferences.Editor editor = removeSharedPrefs.edit();
                    editor.remove(articleID);
                    editor.commit();

                }
            });

        }
        else if(holder instanceof LoadingViewHolder){
            LoadingViewHolder loadingViewHolder =(LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

        }
        else if(holder instanceof WeatherViewHolder){
            Item item=items.get(position);
            WeatherViewHolder weatherViewHolder =(WeatherViewHolder) holder;
//            String cityyy=String.valueOf(items.get(position).getCity());
//            Log.d("cityname in setting",cityyy);
            weatherViewHolder.cityText.setText(String.valueOf(items.get(position).getCity()));
            weatherViewHolder.stateText.setText(String.valueOf(items.get(position).getState()));
            weatherViewHolder.temp.setText(String.valueOf(items.get(position).getTemp()));
            Log.d("weathertemp",String.valueOf(items.get(position).getTemp()));
            weatherViewHolder.descr.setText(String.valueOf(items.get(position).getDescr()));
            String weather_descr=String.valueOf(items.get(position).getDescr());
            Log.d("weather descr", weather_descr);

            if(weather_descr.contains("Clear")){
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.clear_weather);
            }
            else if(weather_descr.contains("Clouds")){
                Log.d("inside","cloudy");
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.cloudy_weather);
            }
            else if(weather_descr.contains("Snow")){
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.snowy_weather);
            }
            else if(weather_descr.contains("Rain") || weather_descr.contains("Drizzle")){
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.rainy_weather);
            }
            else if(weather_descr.contains("Thunderstorm")) {
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.thunder_weather);
            }
            else {
                Log.d("inside","sunny");
                weatherViewHolder.weatherLayout.setBackgroundResource(R.drawable.sunny_weather);
            }
            //Picasso.with(items.get(position).getCtx()).load(String.valueOf(items.get(position).getWeatherImage())).into(weatherViewHolder.weatherImg);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setLoaded(){
        isLoading = false;

    }
    public interface RecyclerViewClickListener {

        public void onClick(View view, int position);
        public boolean onLongClick(View view, int position);
    }
}

