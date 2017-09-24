package com.example.lin.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<List<Book>>{

    // Header file of the query of the GOOGLE Book API.
    private static final String GOOGLE_BOOK_HEADER =
            "https://www.googleapis.com/books/v1/volumes?q=";

    // Build the whole query string for the GOOGLE Book API.
    public static StringBuilder google_book_url = new StringBuilder();


    private ListView lvBook;

    private BookAdapter adapter;

    private TextView welcome_screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the list
        lvBook = (ListView) findViewById(R.id.list);
        // Find the initial screen
        welcome_screen = (TextView) findViewById(R.id.welcome_screen);
        // Create the adapter
        adapter = new BookAdapter(MainActivity.this, new ArrayList<Book>());
        // set the empty view for the list (a screen that will be shown when no results are returned by the query
        lvBook.setEmptyView(welcome_screen);
        // set the adapter
        lvBook.setAdapter(adapter);

        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        final LoaderManager loaderManager = getLoaderManager();

        Button searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView searchField = (TextView)findViewById(R.id.search_field);
                searchField.clearFocus();

                // After the search button is clicked, set the progress bar visible,
                // and the initial screen invisible.

                findViewById(R.id.progress).setVisibility(View.VISIBLE);
                welcome_screen.setVisibility(View.INVISIBLE);

                //Complete the query string with parameters taken from user inputs
                google_book_url = new StringBuilder();
                String searchItem = searchField.getText().toString();
                String[] searchItems = searchItem.split(" |,|;");
                google_book_url.append(GOOGLE_BOOK_HEADER+searchItems[0]);
                for(int i = 1; i < searchItems.length; i++){
                    google_book_url.append("+" + searchItems[i]);
                }
                // Check the network status, if the device is connected to the Internet,
                // then perform network task
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting() ){
                    loaderManager.destroyLoader(0);
                    loaderManager.initLoader(0, null, MainActivity.this);
                }
                // If not, alter the initial screen to warn user that they are not connected
                else {
                    welcome_screen.setText(R.string.no_network);
                    findViewById(R.id.progress).setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        return new BookLoader(MainActivity.this,google_book_url.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, final List<Book> books) {
        // Remove the progress bar
        findViewById(R.id.progress).setVisibility(View.GONE);
        // Change the text of the status screen(initial screen)
        welcome_screen.setText(R.string.no_book);
        // Clear any results from the previous search.
        adapter.clear();
        // If there are items found by the query, create an adapter that stores all the results.
        if( books != null && !books.isEmpty()) {
            adapter = new BookAdapter(MainActivity.this, (ArrayList<Book>) books);
        }
        lvBook.setAdapter(adapter);

        // When clicked on a particular book, the app will take the user to a page that contains
        // details for the book.
        lvBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent bookIntent = new Intent(MainActivity.this, BookActivity.class);
                // Save the selected book to the intent
                bookIntent.putExtra("book", books.get(i));
                startActivity(bookIntent);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.clear();
        welcome_screen.setVisibility(View.INVISIBLE);
    }

}

