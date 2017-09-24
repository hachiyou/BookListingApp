package com.example.lin.booklistingapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * {@link BookAdapter} is an {@link ArrayList} that can provide the layout for list item
 * based on a data source, which is a list of {@link Book} object
 */
public class BookAdapter extends ArrayAdapter {

    // Default Constructor
    public BookAdapter(Activity context,ArrayList<Book> books){ super(context, 0, books); }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null ) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        // Get the {@link Book} object located at this position in the list;
        Book current = (Book) getItem(position);

        // Use helper class to obtain the the image from the internet
        // using the URL stored in the current {@link Book} object
        new DownloadImageTask((ImageView) convertView.findViewById(R.id.book_cover)).execute(current.getThumbnailUrl());

        // Get the title from the current {@link Book} object and set it onto the view.
        TextView tvTitle = convertView.findViewById(R.id.book_title);
        tvTitle.setText(current.getTitle());

        // Get the list of authors from the current {@link Book} object
        // build a string with the list of authors,
        // then set the string onto the view.
        TextView tvAuthor = convertView.findViewById(R.id.book_author);
        StringBuilder authors = new StringBuilder(current.getAuthors().get(0));
        for(int i = 1; i < current.getAuthors().size(); i++ ){
            authors.append(", " + current.getAuthors().get(i));
        }
        tvAuthor.setText(authors);

        // Get the publisher of the current {@link Book} object and set it onto the view.
        TextView tvPublisher = convertView.findViewById(R.id.book_publisher);
        tvPublisher.setText(current.getPublisher());

        // Get the publish date of the current {@link Book} object and set it onto the view.
        TextView tvPublishDate = convertView.findViewById(R.id.book_date);
        tvPublishDate.setText(formatDate(current.getPublishedDate()));

        return convertView;
    }

/*
    Helper Functions & Classes
     */

    /**
     * Takes in a date and format it to "LLL dd, yyyy" format.
     */
    private String formatDate (Date dateObject){
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

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
                InputStream in = new URL(url).openStream();
                thumbNail = BitmapFactory.decodeStream(in);
            } catch (Exception e) { e.printStackTrace(); }
            return thumbNail;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bookCover.setImageBitmap(bitmap);
        }
    }

}
