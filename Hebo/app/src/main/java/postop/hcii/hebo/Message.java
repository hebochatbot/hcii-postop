package postop.hcii.hebo;

import java.util.Date;

class Message {
    String message;
    Date createdAt;
    boolean isHebo;

    Message(String msg, boolean heboStatus) {
        message = msg;
        Date t = new Date();
        createdAt = t;
        isHebo = heboStatus;
    }

    public boolean isHebo() {
        return isHebo;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt.toString();
    }
}
