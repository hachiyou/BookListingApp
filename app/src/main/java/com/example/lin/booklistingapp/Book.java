package com.example.lin.booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A representation of Book
 * Implements the {@link Parcelable} so the object can be pass along with intents.
 */
public class Book implements Parcelable {

/*
 * Field Initialization
 */
    private String mTitle, mPublisher, mDescription, mCategory, mThumbnailUrl, mSaleStatus = "";

    private int mPages = 0;

    private Pair<String, Double> mPrice = new Pair<>("USD", 0.0);

    private ArrayList<String> mAuthors = new ArrayList<>();

    private Map<String, String> mIdentifiers = new HashMap<>();

    private Date mPublishedDate;

    /**
     * Default Constructor
     * @param title    represents the title of the book
     */
    public Book(String title){
        mTitle = title;
        mIdentifiers.put("ISBN_10", "");
        mIdentifiers.put("ISBN_13", "");
        mPublishedDate = new Date();
    }

/*
 * Getter methods
 */

    public String getTitle() { return mTitle; }

    public ArrayList<String> getAuthors() { return mAuthors; }

    public String getPublisher() { return mPublisher; }

    public String getDescription() { return mDescription; }

    public int getPages() { return mPages; }

    public String getCategory() { return mCategory; }

    public String getThumbnailUrl() { return mThumbnailUrl; }

    public String getSaleStatus () { return mSaleStatus; }

    public Date getPublishedDate() { return mPublishedDate; }

    public String getPrice() {
        return Currency.getInstance(mPrice.first).getSymbol()
                + (mPrice.second > 9.99 ? String.format("%.2f", mPrice.second) : " " + String.format("%.2f", mPrice.second));
    }

    public Pair<String,String> getIdentifier(String key) {
        if(mIdentifiers.containsKey(key)){ return new Pair<>(key, mIdentifiers.get(key)); }
        else{ return null; }
    }

/*
Setter methods
*/

    public void setPublisher(String publisher) { mPublisher = publisher; }

    public void setDescription(String description) { mDescription = description; }

    public void setPages(int pages) { mPages = pages; }

    public void setCategory(String category) { mCategory = category; }

    public void setThumbnailUrl(String url) { mThumbnailUrl = url; }

    public void setSaleStatus(String saleStatus) { mSaleStatus = saleStatus; }

    public void setPrice (Pair<String, Double> price) { this.mPrice = price; }

    public void setIdentifier(Pair<String, String> identifier) {
        if(mIdentifiers.containsKey(identifier.first)) {
            mIdentifiers.put(identifier.first, identifier.second);
        }
    }

    public void setPublishedDate(Date publishedDate) { mPublishedDate = publishedDate; }

    public void setAuthor(String author) { mAuthors.add(author); }

/*
Parcelable override methods
 */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeList(mAuthors);
        parcel.writeString(mPublisher);
        parcel.writeLong(mPublishedDate.getTime());

        parcel.writeInt(mIdentifiers.size());
        for(Map.Entry<String, String> entry : mIdentifiers.entrySet()){
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }

        parcel.writeInt(mPages);

        parcel.writeString(mSaleStatus);
        parcel.writeString(mPrice.first);
        parcel.writeDouble(mPrice.second);

        parcel.writeString(mCategory);
        parcel.writeString(mThumbnailUrl);
        parcel.writeString(mDescription);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Creator<Book>(){
        @Override
        public Book createFromParcel(Parcel parcel) {
            Book newBook = new Book(parcel.readString()); // title
            // author
            ArrayList<String> authors = new ArrayList<>();
            parcel.readList(authors, null);
            for(int i = 0; i <authors.size(); i++){
                newBook.setAuthor(authors.get(i));
            }
            newBook.setPublisher(parcel.readString()); // publisher
            newBook.setPublishedDate(new Date(parcel.readLong())); // publish date

            // identifiers
            int mapSize = parcel.readInt();
            for(int i = 0; i < mapSize; i++ ){
                newBook.setIdentifier(new Pair<>(parcel.readString(), parcel.readString()));
            }

            newBook.setPages(parcel.readInt()); // pages
            newBook.setSaleStatus(parcel.readString()); // Sale Status
            newBook.setPrice(new Pair<>(parcel.readString(), parcel.readDouble())); // price
            newBook.setCategory(parcel.readString()); // category
            newBook.setThumbnailUrl(parcel.readString()); // thumbnail url string
            newBook.setDescription(parcel.readString()); // description

            return newBook;
        }

        @Override
        public Book[] newArray(int i) {
            return new Book[i];
        }
    };
}
