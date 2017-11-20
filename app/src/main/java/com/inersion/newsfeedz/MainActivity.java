/*
 * MainActivity.java
 */

/*
 * An application that displays news feeds based on user selection and sorting options.
 * @author Parthiv Shah
 */

package com.inersion.newsfeedz;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // keys for reading data from SharedPreferences
    public static final String SOURCES = "pref_Sources";
    public static final String SORT = "pref_Sort";

    private String source_choice = "business-insider"; //Default news source
    private String sort_choice = "top"; //Default new sort by choice

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferencesChanged = true; // did preferences change?

    private List<News> newsList = new ArrayList<>(); //Gather list of article data

    // ArrayAdapter for binding News objects to a ListView
    private NewsArrayAdapter newsArrayAdapter;
    private ListView newsListView; // displays newsfeed

    String urlString; //capture the url to get News API data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        newsListView = (ListView) findViewById(R.id.newsListView);
        newsArrayAdapter = new NewsArrayAdapter(this, newsList);
        newsListView.setAdapter(newsArrayAdapter);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);
    }

    // called after onCreate completes execution
    @Override
    protected void onStart() {
        super.onStart();
        newsList.clear();
        newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView
        newsListView.smoothScrollToPosition(0); // scroll to top

        source_choice = source_choice.toLowerCase();
        sort_choice = sort_choice.toLowerCase();

        if(source_choice.equals("business insider")){
            source_choice = "business-insider";
        }

        if(sort_choice.equals("top headlines")){
            sort_choice = "top";
        }
        if(sort_choice.equals("latest news")){
            sort_choice = "latest";
        }

        createURL(source_choice,sort_choice);

        new MainActivity.GetNewsFeed().execute(urlString);

        if (preferencesChanged) {
            // now that the default preferences have been set,
            // create ArrayAdapter to bind weatherList to the newsListView


            newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView

            updatePrefs(
                    PreferenceManager.getDefaultSharedPreferences(this));


            newsArrayAdapter = new NewsArrayAdapter(this, newsList);
            newsListView.setAdapter(newsArrayAdapter);
            preferencesChanged = false;
        }
    }

    // update news source and sort by based on values in SharedPreferences
    public void updatePrefs(SharedPreferences sharedPreferences) {
        // get the news source and sort by choices
        source_choice =
                sharedPreferences.getString(MainActivity.SOURCES, "business-insider");

        sort_choice =
                sharedPreferences.getString(MainActivity.SORT, "top");

        newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView
        newsListView.smoothScrollToPosition(0); // scroll to top
    }

    // create newsapi.org web service URL
    private String createURL(String source_setting, String sort_setting) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        urlString="";

        Log.i("source",source_setting);
        Log.i("sort",sort_setting);


        try {
            // create URL for specified news source and sort setting
            urlString = baseUrl + "source=" +
                    source_setting.toLowerCase() + "&sortBy=" + sort_setting + "&apiKey=" + apiKey;

            return urlString;
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get news data and
    // saves the data to a local HTML file
    @TargetApi(19)
    private class GetNewsFeed
            extends AsyncTask<String, Void, String> {



        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;



            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                String response = streamToString(urlConnection.getInputStream());
                parseResult(response);
                return result;

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }


        String streamToString(InputStream stream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String data;
            String result="";


            while ((data = bufferedReader.readLine()) != null){

                result += data;
            }
            if(null != stream){
                stream.close();
            }

            return result;
        }


       private void parseResult(String result){
           JSONObject response = null;
           JSONObject article = null;

           try {

               response = new JSONObject(result);

               JSONArray articles = response.optJSONArray("articles");

               String source = response.optString("source");


               for(int i =0; i<articles.length(); i++){
                   article = articles.optJSONObject(i);

                   String author = article.optString("author");
                   String title = article.optString("title");
                   String desc = article.optString("description");
                   String url = article.optString("url");
                   String pAt = article.optString("publishedAt");

                   Log.i("Author",author);
                   Log.i("Title",title);
                   Log.i("Description",desc);
                   Log.i("URL",url);
                   Log.i("Date Published",pAt);
                   if(author.equals("null")){
                       author = source.replace(source.charAt(0),source.toUpperCase().charAt(0));
                   }
                   if(desc.equals("")){
                       desc = "See article for more...";
                   }
                   newsList.add(new News(
                           author,
                           title,
                           desc,
                           url,
                           pAt));
               }

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView
                       newsListView.smoothScrollToPosition(0); // scroll to top
                       newsListView.setAdapter(newsArrayAdapter);
                   }
               });
           }
           catch (Exception e){
               e.printStackTrace();
           }
       }
    }

    // show menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT || orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // inflate the menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return false;
    }

    // displays the SettingsActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    // listener for changes to the app's SharedPreferences
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true; // user changed app setting

                    if (key.equals(SOURCES)) { // news source to display changed
                        updatePrefs(sharedPreferences);
                        resetNews();
                    }
                    else if (key.equals(SORT)) { // sort option changed
                        updatePrefs(sharedPreferences);
                        resetNews();
                    }

                    Toast.makeText(MainActivity.this,
                            R.string.restarting_news,
                            Toast.LENGTH_SHORT).show();
                }
            };

    public void resetNews() {

        newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView
        newsListView.smoothScrollToPosition(0); // scroll to top

        newsList.clear();

        newsArrayAdapter = new NewsArrayAdapter(this, newsList);
        newsListView.setAdapter(newsArrayAdapter);
    }
}