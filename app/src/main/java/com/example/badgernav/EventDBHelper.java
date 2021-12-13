package com.example.badgernav;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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
        SQLiteStatement stmt = sqLiteDatabase.compileStatement("INSERT INTO events (name, address, hours) VALUES (?, ?, ?)");
        stmt.bindString(1, event.getTitle());
        stmt.bindString(2, event.getBuilding());
        stmt.bindString(3, event.getTime());
        stmt.executeInsert();
    }

    public void removeEvent(Event event){
        SQLiteStatement stmt = sqLiteDatabase.compileStatement("DELETE FROM events WHERE name = ? AND address = ? AND hours = ?");
        stmt.bindString(1, event.getTitle());
        stmt.bindString(2, event.getBuilding());
        stmt.bindString(3, event.getTime());
        stmt.executeUpdateDelete();
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
