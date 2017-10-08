package client.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class Message {

    public StringProperty author;
    public StringProperty message;
    public ObjectProperty<LocalDateTime> dateTime;

    public Message(String author, String message) {
        this.author = new SimpleStringProperty(author);
        this.message = new SimpleStringProperty(message);
        this.dateTime  = new SimpleObjectProperty<>(LocalDateTime.now());
    }
}
