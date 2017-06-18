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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private PetDbHelper mDbHelper;

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
        mDbHelper = new PetDbHelper(this);

//        PetDbHelper mDbHelper = new PetDbHelper(this);
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.


        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };
        // Filter results WHERE "title" = 'My Title'
        String selection = PetEntry.COLUMN_PET_GENDER + " = ?";
        String[] selectionArgs = {String.valueOf(PetEntry.GENDER_MALE)};

        //Perform a query on the database using the ContentProvider.
        //Use the {@Link PetEntry.CONTENT_URI} to access the pet data
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,   //The content URI of the pets table
                projection,             //The columns to be selected
                null,                   //Selection Criteria
                null,                   //Selection Arguments
                null);                  //The sort order for the returned rows.


        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            StringBuilder sb = new StringBuilder();
            sb.append("ID - Name - Breed - Gender - Weight\n");
            while (cursor.moveToNext()) {
                int idColumnIndex = cursor.getColumnIndexOrThrow(PetEntry._ID);

                long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(PetEntry._ID));
                //Get the index of the column that name column in the database
                int nameColumnIndex = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME);
                //Use that columnIndex to get the value of the column in the row we're in.
                String petName = cursor.getString(nameColumnIndex);
                String petBreed = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
                int petGender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
                int petWeight = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT));
                sb.append(Long.toString(itemId)).append(" - ")
                        .append(petName).append(" - ")
                        .append(petBreed).append(" - ")
                        .append(Integer.toString(petGender)).append(" - ")
                        .append(Integer.toString(petWeight))
                        .append("\n");
            }
            displayView.setText("Number of rows in pets database table: " + cursor.getCount() + "\n" +
                    sb);
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
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
                displayDatabaseInfo();
                return true;
            //Respond to a clicn on the "Delete all entries" menu option.
            case R.id.action_delete_all_entries:
                deleteAllPets();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Garfield");
        values.put(PetEntry.COLUMN_PET_BREED, "Tabby");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 5);
        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        Toast.makeText(this, R.string.dummy_data_inserted, Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "New Data Inserted. ID = " + uri);

    }

    private void deleteAllPets() {
        //int numberOfRowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,
        //        PetEntry.COLUMN_PET_NAME+"=?",new String[]{"Garfield"});
        int numberOfRowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
        Toast.makeText(this,"Rows deleted: "+numberOfRowsDeleted,Toast.LENGTH_SHORT).show();
    }
}
