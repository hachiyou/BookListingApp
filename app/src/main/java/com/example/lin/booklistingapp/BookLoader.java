package com.example.lin.booklistingapp;

import android.app.Activity;
import android.content.AsyncTaskLoader;

import java.io.IOException;


/**
 * A {@link AsyncTaskLoader} that process an URL and
 * the json Response file from a query with GOOGLE BOOK API
 */
public class BookLoader extends AsyncTaskLoader {

    // the URL field
    private String mURL;

    // Default constructor
    public BookLoader(Activity context,String url){
        super(context);
        mURL = url;
    }

    @Override
    public Object loadInBackground() {
        // Handle the null case
        if (mURL == null ) return null;

        String jsonResponse = "";
        try{
            // Create the HTTP connection with the stored URL
            // Then save the response to a string
            jsonResponse = QueryUtils.makeHttpRequest(QueryUtils.createURL(mURL));
        } catch (IOException ioe) {}
        return QueryUtils.parseBooks(jsonResponse);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
