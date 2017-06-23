/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int URL_LOADER = 0;

    private PetCursorAdapter mPetCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        //Find and set empty view on the ListView so that it only shows when the list has
        //0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of pet data in the Cursor.
        //Ths is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mPetCursorAdapter);

        //Open Editor Activity when we click on a pet. Pass the Uri
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //view = the particular view of the item
                //position  = position of the item in the list view
                //id = id of the item
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                //Forms a URI that represents the specific pet that was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                //Pass the URI with the intent
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        //Delete the list item on long click
        petListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //id is the id of the list item in the database.

                //Forms a URI that represents the specific pet that was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                getContentResolver().delete(currentPetUri, null, null);

                Log.v(LOG_TAG, "Entry deleted. ID: " + id);
                Toast.makeText(CatalogActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //Kick off the loader
        getLoaderManager().initLoader(URL_LOADER, null, CatalogActivity.this);

//        PetDbHelper mDbHelper = new PetDbHelper(this);
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/menu_catalog.xml
        //This will add a menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //What to do when a user clicks on a menu item?
        switch (item.getItemId()) {
            //Respond to a click on the "Insert Dummy Data" menu option.
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            //Respond to a click on the "Delete all entries" menu option.
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        PetDbHelper petDbHelper = new PetDbHelper(this);
        SQLiteDatabase db = petDbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO pets ( name, breed, gender, weight) VALUES ( \"Tommy\", \"Pomeranian\", 1, 4);");
        db.execSQL("INSERT INTO pets (name, breed, gender, weight) VALUES (\"Binx\", \"Bombay\", 1, 6);");
        db.execSQL("INSERT INTO pets (name, breed, gender, weight)  VALUES ( \"Lady\", \"Cocker Spaniel\", 2, 14);");
        db.execSQL("INSERT INTO pets (name, breed, gender, weight) VALUES (\"Duke\", \"Unknown\", 1, 70);");
        db.execSQL("INSERT INTO pets (name, breed, gender, weight) VALUES (\"Cat\", \"Tabby\", 0, 7);");
        db.execSQL("INSERT INTO pets (name, breed, gender, weight) VALUES (\"Baxter\", \"Border Terrier\", 1, 8);");
        db.execSQL("INSERT INTO pets (name, gender, weight) VALUES (\"Arlene\", 2, 5);");
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Garfield");
        values.put(PetEntry.COLUMN_PET_BREED, "Tabby");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 5);
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        Toast.makeText(this, R.string.dummy_data_inserted, Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "Dummy Data Inserted. ID = " + uri);

    }

    private void deleteAllPets() {
        //int numberOfRowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,
        //        PetEntry.COLUMN_PET_NAME+"=?",new String[]{"Garfield"});
        int numberOfRowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Rows deleted: " + numberOfRowsDeleted, Toast.LENGTH_SHORT).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
        };
        //This loader wil execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                   //Parent activity context
                PetEntry.CONTENT_URI,   //Provider content URI to query
                projection,             //Columns to include n the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection Arguments
                null);                  //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {


        //Update PetCursorAdapter with this cursor containing updated pets dat
        mPetCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deletec
        mPetCursorAdapter.swapCursor(null);
    }
}
