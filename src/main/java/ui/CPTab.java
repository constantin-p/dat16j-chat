package ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CPTab {

    public String id;
    public StringProperty name;
    public BooleanProperty hasNotifications;

    public CPTab(String id, String name) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.hasNotifications = new SimpleBooleanProperty(false);
    }

    protected String getInitials() {
        String result = "";
        String[] words = this.name.getValue().split(" ");
        for (int i = 0; i < words.length; i++) {
            result = result + Character.toString(words[i].charAt(0));
        }
        return result;
    }
}