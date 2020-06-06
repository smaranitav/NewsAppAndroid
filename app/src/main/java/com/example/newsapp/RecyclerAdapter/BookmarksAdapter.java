package com.example.newsapp.RecyclerAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsapp.DetailedArticleActivity;
import com.example.newsapp.Item;
import com.example.newsapp.MainActivity;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {
    Context context;
    List<Item> bookmarkItems;

    public BookmarksAdapter(Context context, List<Item> bookmarkItems){
        this.context=context;
        this.bookmarkItems=bookmarkItems;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater =LayoutInflater.from(context);
        view=inflater.inflate(R.layout.bookmark_item, parent, false);

        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, final int position) {
        final String MyPREFERENCES = "ArticlePrefs" ;
        holder.title.setText(String.valueOf(bookmarkItems.get(position).getTitle()));
        try {
            Picasso.with(context).load(String.valueOf(bookmarkItems.get(position).getImage())).resize(holder.articleImg.getWidth(),150).into(holder.articleImg);
        }
        catch(Exception e){
            e.printStackTrace();

        }
        String date_and_section=findTime(String.valueOf(bookmarkItems.get(position).getArticleTime()))+" | " +formatSectionName(String.valueOf(bookmarkItems.get(position).getSection()));
        holder.articleDateAndSection.setText(date_and_section);
        //holder.section.setText(formatSectionName(String.valueOf(bookmarkItems.get(position).getSection())));

        //set on click for bookmark symbol to remove from bookmarks
        holder.savedBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove this article and refresh the page

                String bookmarkRemove="\""+ bookmarkItems.get(position).getTitle() +"\""+" was removed from Bookmarks";
                Toast.makeText(context, bookmarkRemove, Toast.LENGTH_LONG).show();


                //removing this article from shared preferences
                SharedPreferences removeSharedPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                String articleID=bookmarkItems.get(position).getArticleId(); //articleID
                SharedPreferences.Editor editor = removeSharedPrefs.edit();
                editor.remove(articleID);
                editor.commit();
                // after removing the article, we need to call the on create method again/
                // so the only way to do this is call an intent back to the same activity
                context.startActivity(new Intent(context, context.getClass()));
            }
        });

        //onclick for card
        holder.bookmarkCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on clicking , open detailed article activity
                String detailedArticleID=bookmarkItems.get(position).getArticleId();
                Intent intent= new Intent(context, DetailedArticleActivity.class);
                intent.putExtra("articleID",detailedArticleID);
                intent.putExtra("image",bookmarkItems.get(position).getImage());
                context.startActivity(intent);

            }
        });
        //long click for card
        holder.bookmarkCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Toast.makeText(getApplicationContext(), "Long click " + "yes", Toast.LENGTH_SHORT).show();
                // Create dialog object
                final Dialog dialog = new Dialog(context);
                // Include share_dialog.xml file
                dialog.setContentView(R.layout.share_dialog);

                ImageView dialog_image = (ImageView) dialog.findViewById(R.id.imageDialog);
                try {

                    Picasso.with(context).load(bookmarkItems.get(position).getImage()).resize(dialog_image.getWidth(), 160).into(dialog_image);
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.d("picasso","error in dialog image because canvas is too big");

                }

                // set values for custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                text.setText(String.valueOf(bookmarkItems.get(position).getTitle()));

                //whenever in bookmark page, its always bookmarked/saved
                final ImageButton savedBookmarkButton =(ImageButton) dialog.findViewById(R.id.bookmark_save); //clicked
                savedBookmarkButton.setVisibility(View.VISIBLE);
                final ImageButton unsavedBookmarkButton =(ImageButton) dialog.findViewById(R.id.bookmark_unsave); //unclicked

                dialog.show();

                ImageButton twitterShareButton = (ImageButton) dialog.findViewById(R.id.twitter_share);
                twitterShareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String twitterText="text=Check out this Link:";
                        String twitterURL="url=https://www.theguardian.com/"+bookmarkItems.get(position).getArticleId();;
                        String twitterHashtag ="hashtags=CSCI571NewsSearch";
                        String url = "https://twitter.com/intent/tweet?"+twitterText+"&"+twitterURL+"&"+twitterHashtag;
                        Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
                        twitterIntent.setData(Uri.parse(url));
                        context.startActivity(twitterIntent);
                        dialog.dismiss();
                    }
                });

                //remove an article which was saved by clicking on savedbookmark symbol in the share dialog
                savedBookmarkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unsavedBookmarkButton.setVisibility(View.VISIBLE);
                        savedBookmarkButton.setVisibility(View.GONE);

                        String bookmarkRemove="\""+ bookmarkItems.get(position).getTitle() +"\""+" was removed from Bookmarks";
                        Toast.makeText(context, bookmarkRemove, Toast.LENGTH_LONG).show();

                        //removing this article from shared preferences
                        SharedPreferences removeSharedPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                        String article_id_to_delete=bookmarkItems.get(position).getArticleId();; //articleID
                        SharedPreferences.Editor editor = removeSharedPrefs.edit();
                        editor.remove(article_id_to_delete);
                        editor.commit();

                        dialog.dismiss();
                        // after removing the article, we need to call the on create method again/
                        // so the only way to do this is call an intent back to the same activity
                        context.startActivity(new Intent(context, context.getClass()));

                    }
                });

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return bookmarkItems.size();
    }


    public static class BookmarkViewHolder extends RecyclerView.ViewHolder{
        public TextView title, articleDateAndSection;
        public ImageView articleImg;
        public ImageView savedBookmark;
        public Context context;
        CardView bookmarkCardView;

        //private BookmarksAdapter.RecyclerViewClickListener mListener;
        //Context context;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            context=itemView.getContext();
            bookmarkCardView=(CardView)itemView.findViewById(R.id.bookmarkCard);
            articleImg=(ImageView) itemView.findViewById(R.id.bookmarkImg);
            title=(TextView) itemView.findViewById(R.id.bookmarkTitle);
            articleDateAndSection=(TextView) itemView.findViewById(R.id.bookmarkDateAndSection);
            //section=(TextView)itemView.findViewById(R.id.bookmarkSection);
            savedBookmark=(ImageView)itemView.findViewById(R.id.clickedSymbol);

        }
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

            String final_date = ddDate + " " + mmm.charAt(0)+mmm.substring(1,3).toLowerCase();

            return final_date;
        }

        return "";
    }
    public String formatSectionName(String sectionName){
        //capitalise first letter
        String formatted_str=sectionName.substring(0,1).toUpperCase()+sectionName.substring(1,sectionName.length()).toLowerCase();
        return formatted_str;
    }

}
