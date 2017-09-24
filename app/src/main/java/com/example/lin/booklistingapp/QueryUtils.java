package com.example.lin.booklistingapp;

import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A Utility class
 * The main purpose of this class is to set up HTTP connections,
 * and parse any possible results into {@link Book} objects
 */
public final class QueryUtils {

    public static final String LOG_TAG = MainActivity.class.getName();

    private QueryUtils(){}

    public static URL createURL (String stringUrl){
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) return jsonResponse;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200 ) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    public static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();

            }
        }
        return output.toString();
    }

    // Create a list of {@link Book} from the response file.
    public static ArrayList<Book> parseBooks(String jsonResponse){

        if (jsonResponse == null | jsonResponse == "") return null;
        ArrayList<Book> books = new ArrayList<>();

        try{
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for(int i = 0;i< jsonArray.length(); i++ ){
                JSONObject currentBook = jsonArray.getJSONObject(i);
                JSONObject currentBookInfo = currentBook.getJSONObject("volumeInfo");
                JSONObject currentSaleInfo = currentBook.getJSONObject("saleInfo");

                // Create a book with the title (if subtitle exist, add it to the title as well. Format "title[ - subtitle]")
                String title = currentBookInfo.getString("title");
                if (currentBookInfo.has("subtitle")) title += " - " + currentBookInfo.getString("subtitle");
                Book newBook = new Book(title);

                // Set the authors
                JSONArray authors = currentBookInfo.getJSONArray("authors");
                for(int j = 0; j < authors.length(); j++ ) newBook.setAuthor(authors.getString(j));

                // Set the identifiers
                JSONArray identifiers = currentBookInfo.getJSONArray("industryIdentifiers");
                for(int j = 0; j < identifiers.length(); j++ ) {
                    newBook.setIdentifier( new Pair<>(
                            identifiers.getJSONObject(j).getString("type"),
                            identifiers.getJSONObject(j).getString("identifier")
                    ));
                }
                // Set the publisher
                if(currentBookInfo.has("publisher")) newBook.setPublisher(currentBookInfo.getString("publisher"));
                else newBook.setPublisher("N/A");
                // Set the category
                if(currentBookInfo.has("categories")) newBook.setCategory(currentBookInfo.getJSONArray("categories").getString(0));
                else newBook.setCategory("N/A");
                // Set the pages
                if(currentBookInfo.has("pageCount")) newBook.setPages(currentBookInfo.getInt("pageCount"));
                else newBook.setPages(0);
                // Set the description
                if(currentBookInfo.has("description")) newBook.setDescription(currentBookInfo.getString("description"));
                // Set the thumbnail
                if(currentBookInfo.has("imageLinks")) newBook.setThumbnailUrl(currentBookInfo.getJSONObject("imageLinks").getString("thumbnail"));
                // Set the listing price
                String saleStatus = currentSaleInfo.getString("saleability");
                newBook.setSaleStatus(saleStatus);
                switch (saleStatus){
                    case "FOR_SALE":
                        newBook.setPrice(new Pair<>(
                                currentSaleInfo.getJSONObject("listPrice").getString("currencyCode"),
                                currentSaleInfo.getJSONObject("listPrice").getDouble("amount")));
                        break;
                    case "NOT_FOR_SALE":
                    case "FREE":
                        newBook.setPrice(new Pair<>("USD",0.0));
                        break;
                }

                // Set the published date
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dfYear = new SimpleDateFormat("yyyy");
                try {
                    if(currentBookInfo.getString("publishedDate").length() == 4 ){
                        newBook.setPublishedDate(dfYear.parse(currentBookInfo.getString("publishedDate")));
                    }
                    else if (currentBookInfo.getString("publishedDate").length() == 10 ){
                        newBook.setPublishedDate(df.parse(currentBookInfo.getString("publishedDate")));
                    }
                } catch (ParseException pe) {pe.printStackTrace();}

                books.add(newBook);
            }
        }catch (JSONException jse) {jse.printStackTrace();}
        return books;
    }

}
