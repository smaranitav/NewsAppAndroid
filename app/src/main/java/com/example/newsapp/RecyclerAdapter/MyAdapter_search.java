package com.example.newsapp.RecyclerAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Item;
import com.example.newsapp.R;
import com.example.newsapp.recyclerinterface.LoadMoreItems_search;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

class LoadingViewHolder_search extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;
    public LoadingViewHolder_search(@NonNull View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar_search);
    }
}

class ItemViewHolder_search extends RecyclerView.ViewHolder {
    public TextView title, time, section, articleId;
    public ImageView articleImg;
    public ImageView unclickedBookmark, clickedBookmark;
    private MyAdapter_search.RecyclerViewClickListener mListener;
    Context context;

    public ItemViewHolder_search(@NonNull View itemView, MyAdapter_search.RecyclerViewClickListener listener) {
        super(itemView);

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

        articleImg=(ImageView) itemView.findViewById(R.id.articleImg_search);
        title=(TextView) itemView.findViewById(R.id.title_search);
        time=(TextView) itemView.findViewById(R.id.time_search);
        section=(TextView) itemView.findViewById(R.id.section_search);
        unclickedBookmark=(ImageView)itemView.findViewById(R.id.unclickedBookmark_search);
        clickedBookmark=(ImageView)itemView.findViewById(R.id.clickedBookmark_search);
        //articleId=(TextView) itemView.findViewById(R.id.articleId);
    }
}


public class MyAdapter_search extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM=0, VIEW_TYPE_LOADING=1;
    LoadMoreItems_search loadMore;
    boolean isLoading;
    Activity activity;
    List<Item> items;
    int visibleThreshold=5;
    int lastVisibleItem, totalItemCount;

    private MyAdapter_search.RecyclerViewClickListener mListener;

    public MyAdapter_search(RecyclerView recyclerView, Context activity, List<Item> items, MyAdapter_search.RecyclerViewClickListener listener){
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
                        loadMore.onLoadMore_search();
                    }
                }
                isLoading=true;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setLoadMore(LoadMoreItems_search loadMore){
        this.loadMore =loadMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_ITEM){
            Log.d("item","hey");
            View view = LayoutInflater.from(activity).inflate(R.layout.item_layout_search, parent, false);
            return  new ItemViewHolder_search(view, mListener);
        }
        else if(viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading_search, parent, false);
            return  new LoadingViewHolder_search(view);

        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if ( holder instanceof ItemViewHolder_search){
            final String MyPREFERENCES = "ArticlePrefs" ;
            Item item = items.get(position);
            final ItemViewHolder_search viewHolder =(ItemViewHolder_search) holder;
            Log.d("context for search card",String.valueOf(viewHolder.context));
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
            viewHolder.section.setText(formatSectionName(String.valueOf(items.get(position).getSection())));
            //viewHolder.articleId.setText(String.valueOf(items.get(position).getArticleId()));

            final String articleID=items.get(position).getArticleId(); //articleID
            final String articleImage= items.get(position).getImage();
            final String articleTitle=items.get(position).getTitle();
            final String articleOriginalTime=items.get(position).getArticleTime();//original date. not 3m ago or time difference
            final String articleSectionName= items.get(position).getSection();

            //get from shared preferences
            SharedPreferences sharedpreferences= viewHolder.context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            Map<String, String> allEntries = (Map<String, String>) sharedpreferences.getAll();
            int articleInBookMarked=0;
            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                if(entry.getKey().equals(articleID)){
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
        else if(holder instanceof LoadingViewHolder_search){
            LoadingViewHolder_search loadingViewHolder =(LoadingViewHolder_search) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

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
    public String formatSectionName(String sectionName){
        String formatted_str=sectionName.substring(0,1).toUpperCase()+sectionName.substring(1,sectionName.length()).toLowerCase();
        return formatted_str;
    }
}