package postop.hcii.hebo;

import java.util.List;

public class Message {
    List<Response> mResponses;
    int mType;
    String mTitle;

    Message(List<Response> responses, int messageType, String title) {
        mResponses = responses;
        mType = messageType;
        mTitle = title;
    }

    Message(List<Response> responses, int messageType) {
        mResponses = responses;
        mType = messageType;
        mTitle = null;
    }

    public int getMessageType() {
        return mType;
    }

    public String getMessageTitle() {
        return mTitle;
    }

    public List<Response> getResponses() {
        return mResponses;
    }

}
