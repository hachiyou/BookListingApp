package com.example.lin.booklistingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Page which display the detail result of a selected {@link Book}
 */
public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // De-parcelize the object that was passed in via intent
        Book newBook = getIntent().getExtras().getParcelable("book");

        // Get the image from WWW using the stored URL in the {@link Book} object
        new DownloadImageTask((ImageView) findViewById(R.id.detail_cover)).execute(newBook.getThumbnailUrl());

        // Set the title and/or subtitle of the book
        TextView tvTitle = (TextView) findViewById(R.id.detail_main_title);
        String[] titles = newBook.getTitle().split(" - ");
        tvTitle.setText(titles[0]);
        // If there are subtitles to the book, show the subtitle
        if(titles.length > 1){
            ((TextView) findViewById(R.id.detail_subtitle)).setText(titles[1]);
        }
        // if not, remove the subtitle container from view.
        else findViewById(R.id.detail_subtitle).setVisibility(View.GONE);

        // Set the publisher of the book
        TextView tvPublisher = (TextView) findViewById(R.id.detail_publisher);
        tvPublisher.setText(newBook.getPublisher());

        // Set the description of the book after stylizing.
        TextView tvDescription = (TextView) findViewById(R.id.detail_description);
        tvDescription.setText(processDescription(newBook.getDescription()));

        // Set the ISBN-10 identifier if any
        TextView tvISBN10 = (TextView) findViewById(R.id.detail_isbn10);
        tvISBN10.setText(newBook.getIdentifier("ISBN_10").second);

        // Set the ISBN-13 identifier if any
        TextView tvISBN13 = (TextView) findViewById(R.id.detail_isbn13);
        tvISBN13.setText(newBook.getIdentifier("ISBN_13").second);

        // Set the page if any
        TextView tvPage = (TextView) findViewById(R.id.detail_pages);
        tvPage.setText(String.valueOf(newBook.getPages()));

        // Building a string that contains all the authors.
        StringBuilder authors = new StringBuilder().append(newBook.getAuthors().get(0));
        for (int i = 1; i < newBook.getAuthors().size(); i++ ){
            authors.append(", " + newBook.getAuthors().get(i));
        }
        // Then show the string as the authors
        TextView tvAuthors = (TextView) findViewById(R.id.detail_authors);
        tvAuthors.setText(authors.toString());

        // Get the listing price of the book
        TextView tvPrice = (TextView) findViewById(R.id.detail_price);
        if(newBook.getSaleStatus().equals("NOT_FOR_SALE")){ tvPrice.setText("Not for Sale");}
        else if (newBook.getSaleStatus().equals("FREE")) {tvPrice.setText("Free");}
        else { tvPrice.setText(newBook.getPrice()); }

        // Set the category of the book
        TextView tvCategory = (TextView) findViewById(R.id.detail_category);
        tvCategory.setText(newBook.getCategory());

        // Set the published date of the book
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        TextView tvPublishDate = (TextView) findViewById(R.id.detail_date);
        tvPublishDate.setText(df.format(newBook.getPublishedDate()));

    }

/*
    Helper Classes & Functions
     */

    /**
     * Takes in a URL of a thumbnail and download it from the internet
     * then set the image to an ImageView
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bookCover;

        public DownloadImageTask(ImageView view){ bookCover = view; }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap thumbNail = null;

            try{
                // Get the input stream from the URL
                InputStream in = new URL(url).openStream();
                // decode the stream to obtain the image in BitMap form.
                thumbNail = BitmapFactory.decodeStream(in);
            } catch (Exception e) { e.printStackTrace(); }
            return thumbNail;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bookCover.setImageBitmap(bitmap);
        }
    }

    /**
     * Modify the descriptions so that it will show up gracefully on screen
     * @param description a string that stores the description of a book
     * @return
     */
    private String processDescription(String description){

        if(description == null) return "";
        // Create the string
        StringBuilder results = new StringBuilder().append(description);
        // find all the indexes of strings in pattern of " [any single character]."
        ArrayList<Integer> escapeIndexes = new ArrayList<>();
        Pattern p = Pattern.compile(" \\w\\.");
        Matcher m = p.matcher(results);
        while(m.find()){ escapeIndexes.add(m.start()); }

        // Find all the indexes of strings in pattern of ". "
        ArrayList<Integer> replaceIndexes = new ArrayList<>();
        Pattern p2 = Pattern.compile("\\. ");
        Matcher m2 = p2.matcher(results);
        while(m2.find()){ replaceIndexes.add(m2.start()); }

        //  Compare the list of indexes for pattern 1 and pattern 2,
        // then remove any pattern 2 indexes that matches pattern 1 indexes.
        for(int i = 0; i < escapeIndexes.size(); i++){
            for( int j = 0; j < replaceIndexes.size(); j++ ){
                if (escapeIndexes.get(i) == (replaceIndexes.get(j) - 2)){
                    replaceIndexes.remove(j);
                    j = -1;
                }}}
        // Replace the space with a newline character at indexes for pattern 1 + offset
        for(int index : replaceIndexes){ results.replace(index+1, index+2, "\n"); }
        return results.toString();
    }
}
