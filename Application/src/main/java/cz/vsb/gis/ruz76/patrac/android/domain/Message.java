package cz.vsb.gis.ruz76.patrac.android.domain;

/**
 * Class for handling messages.
 */

public class Message {
    private String message;
    private String filename;
    private String from;

    public Message(String message, String filename, String from) {
        this.message = message;
        this.filename = filename;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
