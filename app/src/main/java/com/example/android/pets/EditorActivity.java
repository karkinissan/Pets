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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetContract.PetEntry.GENDER_UNKNOWN;
    private Uri mCurrentPetUri;

    private PetDbHelper mDbHelper;

    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PET_LOADER = 0;

    /*Boolean to check whether the data has changed.*/
    private boolean mHasPetChanged = false;

    //check if any changes were made
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mHasPetChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        // Get the Intent that started this activity and extract the uri
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();
        if (mCurrentPetUri != null) {
            //If there is a URI then it means that we need to edit the pet
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, EditorActivity.this);
        } else {
            //If there is no URI then it meant that we need to add a pet
            setTitle(getString(R.string.editor_activity_title_new_pet));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            // We are basically deleting the options and creating a new one.
            invalidateOptionsMenu();
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; //Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; //Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; //Unknown

                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGender = PetEntry.GENDER_UNKNOWN; //Unknown
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //If we are adding a new pet, then we can hide the delete menu item.
        if (mCurrentPetUri == null) {
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

//        //If we are adding a new pet, then we can hide the delete menu item.
//        if (mCurrentPetUri == null) {
//            MenuItem item = menu.findItem(R.id.action_delete);
//            item.setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                //Close activity and return to parent activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Delete the pet.
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mHasPetChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.

                // Create a click listener to handle the user confirming that changes should be discarded.
                // and pass that listener to the showUnsavedChangesDialog() method.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /*
    *Get text from the EditText fields and save it to the database
    */
    private void savePet() {

        //If the name field is left blank, return to CatalogActivity.
        if (TextUtils.isEmpty(mNameEditText.getText().toString().trim())) {
            Log.v(LOG_TAG, "No name value. Returning.");
            return;
        }

        mDbHelper = new PetDbHelper(this);
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim()); //trim() removes starting and trailing whitespaces
        values.put(PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        try {
            values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
        } catch (NumberFormatException e) {
            values.put(PetEntry.COLUMN_PET_WEIGHT, 0);

        }
        if (mCurrentPetUri == null) {
            //If the uri is null that means we are inserting a new pet
            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Pet insertion error. ID: " + uri);
            } else {
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "New row inserted. URI: " + uri);
            }
        } else {
            //If the url is not null then it means we are editing a pet.
            int rowsUpdated = getContentResolver().update(mCurrentPetUri, values, null, null);
            if (rowsUpdated != 0) {
                Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Pet updated. ID: " + ContentUris.parseId(mCurrentPetUri));
            } else {
                Toast.makeText(this, "Pet update Failed", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Pet update Failed. URI: " + mCurrentPetUri);
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_single_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        getContentResolver().delete(mCurrentPetUri, null, null);
        Toast.makeText(this, "Pet Deleted", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, "Pet Deleted. ID: " + ContentUris.parseId(mCurrentPetUri));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };
        return new CursorLoader(this, mCurrentPetUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        cursor.moveToFirst();
        String petName = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME));
        String petBreed = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
        int gender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
        int petWeight = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT));
        mNameEditText.setText(petName);
        mBreedEditText.setText(petBreed);
        mWeightEditText.setText(Integer.toString(petWeight));

        // Gender is a dropdown spinner, so map the constant value from the database
        // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
        // Then call setSelection() so that option is displayed on screen as the current selection.
        switch (gender) {
            case PetEntry.GENDER_MALE:
                mGenderSpinner.setSelection(1);
                break;
            case PetEntry.GENDER_FEMALE:
                mGenderSpinner.setSelection(2);
                break;
            default:
                mGenderSpinner.setSelection(0);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0); // Select "Unknown" gender


    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //discardButtonClickListener tells us whether to call finish() or call NavUtils.navigateUpFromSameTask()

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        //We either need to navigate up from the EditorActivity because of pressing the back ARROW,
        //or we need to go back to CatalogActivity because of pressing the back BUTTON
        //discardButtonClickListener determines which of the above to do
        //Hence it is passed as a parameter to showUnsavedChangesDialog()
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);

        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mHasPetChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        // We pressed the back button
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };
        //Show dialog that there are unsaved changes.
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}