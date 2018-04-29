package postop.hcii.hebo;

public class Response {
    String mResponse;
    boolean mIsImage;

    Response(String response, boolean isImage) {
        mResponse = response;
        mIsImage = isImage;
    }

    public String getResponse() {
        return mResponse;
    }

    public boolean isImage() {
        return mIsImage;
    }
}
