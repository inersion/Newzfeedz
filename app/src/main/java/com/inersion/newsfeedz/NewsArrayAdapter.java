// WeatherArrayAdapter.java
// An ArrayAdapter for displaying a List<Weather>'s elements in a ListView
package com.inersion.newsfeedz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NewsArrayAdapter extends ArrayAdapter<News> {
   // class for reusing views as list items scroll off and onto the screen
   private static class ViewHolder {
      TextView authorTextView;
      TextView titleTextView;
      TextView descriptionTextView;
      TextView urlTextView;
      TextView publishedAtTextView;
   }


   // constructor to initialize superclass inherited members
   public NewsArrayAdapter(Context context, List<News> news) {
      super(context, -1, news);
   }

   // creates the custom views for the ListView's items
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      // get articles object for this specified ListView position
      News articles = getItem(position);

      ViewHolder viewHolder; // object that reference's list item's views

      // check for reusable ViewHolder from a ListView item that scrolled
      // offscreen; otherwise, create a new ViewHolder
      if (convertView == null) { // no reusable ViewHolder, so create one
         viewHolder = new ViewHolder();
         LayoutInflater inflater = LayoutInflater.from(getContext());
         convertView =
            inflater.inflate(R.layout.list_item, parent, false);
         viewHolder.authorTextView =
            (TextView) convertView.findViewById(R.id.authorTextView);
         viewHolder.titleTextView =
            (TextView) convertView.findViewById(R.id.titleTextView);
         viewHolder.descriptionTextView =
            (TextView) convertView.findViewById(R.id.descriptionTextView);
         viewHolder.urlTextView =
            (TextView) convertView.findViewById(R.id.urlTextView);
         viewHolder.publishedAtTextView =
                 (TextView) convertView.findViewById(R.id.publishedAtTextView);
         convertView.setTag(viewHolder);
      }
      else { // reuse existing ViewHolder stored as the list item's tag
         viewHolder = (ViewHolder) convertView.getTag();
      }

      // get other data from articles object and place into views
      Context context = getContext(); // for loading String resources
      viewHolder.authorTextView.setText(
              context.getString(R.string.author, articles.author));
      viewHolder.titleTextView.setText(
         context.getString(R.string.title, articles.title));
      viewHolder.descriptionTextView.setText(
         context.getString(R.string.description, articles.description));
      viewHolder.urlTextView.setText(
         context.getString(R.string.url, articles.url));
      viewHolder.publishedAtTextView.setText(
              context.getString(R.string.publishedAt, articles.publishedAt));

      return convertView; // return completed list item to display
   }
}
