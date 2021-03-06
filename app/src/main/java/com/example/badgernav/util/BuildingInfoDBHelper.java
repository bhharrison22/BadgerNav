package com.example.badgernav.util;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.badgernav.models.BuildingInfo;

import java.util.ArrayList;

public class BuildingInfoDBHelper {

    SQLiteDatabase sqLiteDatabase;
    public final String TABLE_NAME = "building_info";

    public BuildingInfoDBHelper(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public void createTable() {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // delete old Building Info Table TODO: remove this line once db is set up
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (id INTEGER PRIMARY KEY, name TEXT, address TEXT, hours TEXT, floormap TEXT)"); // Create Building Info Table
    }

    public void populateBuildingTable(String[] buildings) {
        // TODO: make sure table is empty before doing this
        long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_NAME);

        if (count == 0) {
            for (String building : buildings) {
                building = building.replace("'", "\'\'");
                sqLiteDatabase.execSQL(String.format("INSERT INTO " + TABLE_NAME + " (name, address, hours, floormap) VALUES ('%s', '%s', '%s', '%s')", building, "Unknown Address", "Today: 12:00 AM – 11:59 PM", "¯\\_(ツ)_/¯"));
            }
        }
    }

    public ArrayList<BuildingInfo> getBuildingInfo() {
        createTable();
        ArrayList<BuildingInfo> buildingInfo = new ArrayList<>();
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        int nameIndex = c.getColumnIndex("name");
        int addressIndex = c.getColumnIndex("address");
        int hoursIndex = c.getColumnIndex("hours");
        int floormapIndex = c.getColumnIndex("floormap");

        c.moveToFirst();

        while (!c.isAfterLast()) {
            BuildingInfo building = new BuildingInfo(c.getString(nameIndex), c.getString(addressIndex), c.getString(hoursIndex), c.getString(floormapIndex), 50);
            buildingInfo.add(building);
            c.moveToNext();
        }

        c.close();
        sqLiteDatabase.close();

        return buildingInfo;
    }

}
