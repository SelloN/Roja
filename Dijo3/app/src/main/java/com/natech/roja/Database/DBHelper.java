package com.natech.roja.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tshepo on 2016/02/05.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cities";
    private static final int DB_VERSION = 4;
    private static final String TAG = DBHelper.class.getSimpleName();
    private final DatabaseUtilities databaseUtilities;
    private final Context context;

    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
        databaseUtilities = new DatabaseUtilities();
        this.context = context;

    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(databaseUtilities.createLocalitiesTable());
        populateLocalities(sqLiteDatabase);
        //checkList(sqLiteDatabase);
        Log.i(TAG,"tables created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        deleteOldData(sqLiteDatabase);
        populateLocalities(sqLiteDatabase);
        Log.i(TAG,"tables updated");
        //checkList(sqLiteDatabase);

    }

    private void populateLocalities(SQLiteDatabase sqLiteDatabase){
        //sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String [] localityArray = databaseUtilities.getLocalityArray();

        for(int x = 0; x < localityArray.length; x++){
            contentValues.put(databaseUtilities.getLocalityId(),localityArray[x].substring(0, localityArray[x].indexOf('_')));
            contentValues.put(databaseUtilities.getLocality(),localityArray[x].substring(localityArray[x].indexOf('_')+1));
            sqLiteDatabase.insert(databaseUtilities.getLocalitiesTable(),null,contentValues);
        }

    }

    private void deleteOldData(SQLiteDatabase sqLiteDatabase){

        sqLiteDatabase.execSQL(databaseUtilities.deleteOldList());

    }

    public String getLocID(String locality){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor results = sqLiteDatabase.rawQuery(databaseUtilities.getLocalityId(locality),null);
        String locID = null;
        Log.i(TAG,"getting locID");
        if(results != null && results.moveToFirst()){
            while (!results.isAfterLast()){

                Log.i(TAG,results.getString(0));
                locID = results.getString(0);
                results.moveToNext();
            }
        }
        return locID;
    }

    public void checkList(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor results = sqLiteDatabase.rawQuery(databaseUtilities.getList(),null);

        if(results != null && results.moveToFirst()){
            while (!results.isAfterLast()){

                Log.i(TAG,""+results.getInt(1));
                Log.i(TAG,results.getString(0));
                results.moveToNext();
            }
        }
    }
}
