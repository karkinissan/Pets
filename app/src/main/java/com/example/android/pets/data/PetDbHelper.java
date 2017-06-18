package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by Nissan on 6/13/2017.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "shelter.db";
    public static final String SQL_CREATE_PETS_TABLE =
            "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
                    PetEntry._ID + " INTEGER PRIMARY KEY, " +
                    PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, " +
                    PetEntry.COLUMN_PET_BREED + " TEXT, " +
                    PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, " +
                    PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.v(LOG_TAG,SQL_CREATE_PETS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
