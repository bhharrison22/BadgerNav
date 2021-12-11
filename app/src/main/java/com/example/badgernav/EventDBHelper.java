package com.example.badgernav;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import com.example.badgernav.ui.buildinginfo.BuildingInfo;

import java.util.ArrayList;

public class EventDBHelper {
    SQLiteDatabase sqLiteDatabase;
    public final String TABLE_NAME = "events";

    public EventDBHelper(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public void createTable() {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // delete old Building Info Table TODO: remove this line once db is set up
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (id INTEGER PRIMARY KEY, name TEXT, address TEXT, hours TEXT)"); // Create Building Info Table
    }

    public void addEvent(Event event) {
        sqLiteDatabase.execSQL(String.format("INSERT INTO " + TABLE_NAME + " (name, address, hours) VALUES ('%s', '%s', '%s')", event.getTitle(), event.getBuilding(), event.getTime()));
    }

    public void removeEvent(Event event){
        sqLiteDatabase.execSQL(String.format("DELETE FROM " + TABLE_NAME + " WHERE name = 's' AND address = 's' AND hours = 's'", event.getTitle(), event.getBuilding(), event.getTime()));
    }

    public ArrayList<Event> getEvents() {
        createTable();
        ArrayList<Event> events = new ArrayList<>();
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        int nameIndex = c.getColumnIndex("name");
        int addressIndex = c.getColumnIndex("address");
        int hoursIndex = c.getColumnIndex("hours");

        c.moveToFirst();

        while (!c.isAfterLast()) {
            Event event = new Event(c.getString(nameIndex), c.getString(addressIndex), c.getString(hoursIndex));
            events.add(event);
            c.moveToNext();
        }

        c.close();

        return events;
    }
}
