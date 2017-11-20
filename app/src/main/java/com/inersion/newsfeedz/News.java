// Weather.java
// Maintains one day's weather information
package com.inersion.newsfeedz;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class News {
   public final String author;
   public final String title;
   public final String description;
   public final String url;
   public final String publishedAt;


   // constructor
   public News(String author, String title, String description, String url, String publishedAt) {

      this.author = author;
      this.title = title;
      this.description = description;
      this.url = url;
      this.publishedAt = publishedAt;
   }
}
