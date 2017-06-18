package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.example.android.pets.data.PetContract.CONTENT_AUTHORITY;
import static com.example.android.pets.data.PetContract.PATH_PETS;
import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by Nissan on 6/16/2017.
 */

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /*Database helper object*/
    private PetDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PETS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS, PETS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS + "/#", PET_ID);

    }

    @Override
    public boolean onCreate() {
        //Create and initialize a PetDbHelper object to gain access to the pets database.
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {

            //Call this if the operation is to be done on an entire table.
            //i.e. if the ID is NOT provided in the URI
            //Eg: content://com.example.android.pets/pets
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.

                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder, null);
                break;

            //Call this if the ID is provided in the URI
            //Eg: content://com.example.android.pets/pets/5
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.

                //Select (projection) from pets where (selection) = ?
                selection = PetEntry._ID + "=?";
                //Convert the last segment of the Uri into an integer value and put it as value
                //for the ? above
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 5 to return a
                // Cursor containing that row of the table.
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null,
                        sortOrder, null);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query. Unknown URI " + uri);
        }
        return cursor;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */

    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        String petName = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (petName == null || petName.equals("") || petName.equals(" ")) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer petGender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (petGender == null || !PetEntry.isValidGender(petGender)) {
            throw new IllegalArgumentException("Invalid gender value: " + petGender);
        }
        Integer petWeight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (petWeight == null || petWeight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }

        //Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Deletion is not supported for URI: " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String petName = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (petName == null || petName.equals("") || petName.equals(" ")) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer petGender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (petGender == null || !PetEntry.isValidGender(petGender)) {
                throw new IllegalArgumentException("Invalid gender value: " + petGender);
            }
        }
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer petWeight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (petWeight == null || petWeight < 0) {
                throw new IllegalArgumentException("Weight cannot be negative");
            }
        }

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(values, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is nor supported for URI: " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
