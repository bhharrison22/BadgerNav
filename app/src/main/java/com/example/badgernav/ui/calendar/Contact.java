package com.example.badgernav.ui.calendar;

import androidx.annotation.NonNull;

public class Contact {
    private final String name;
    private final String contactId;
    private final String phone;

    Contact(String contactId, String name, String phone) {
        this.name = name;
        this.contactId = contactId;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getContactId() {
        return contactId;
    }

    public String getPhone() {
        return phone;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\ncontactId: " + this.contactId + ",\n name: " + this.name + ",\n phone: " + this.phone + "\n}";
    }
}
