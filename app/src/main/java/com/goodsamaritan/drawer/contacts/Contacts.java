package com.goodsamaritan.drawer.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample phone_number for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Contacts {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<ContactItem> ITEMS = new ArrayList<ContactItem>();

    private static final int COUNT = 25;




    private static void addItem(ContactItem item) {
        ITEMS.add(item);
        //ITEM_MAP.put(item.contact_name, item);
    }

    private static ContactItem createContactItem(int position) {
        return new ContactItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * Contact information
     */
    public static class ContactItem {
        public final String contact_name;
        public final String phone_number;
        public final String details;


        public ContactItem(){
            this.contact_name = "NULL";
            this.phone_number = "NULL";
            this.details = "NULL";
        }

        public ContactItem(String contact_name, String phone_number, String details) {
            this.contact_name = contact_name;
            this.phone_number = phone_number;
            this.details = details;
        }

        @Override
        public String toString() {
            return phone_number;
        }
    }
}
