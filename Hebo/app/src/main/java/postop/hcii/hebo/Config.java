package postop.hcii.hebo;

/**
 * Created by Danielle Hu on 3/24/2018.
 */

 final class Config {
    private Config() {}
    public static final String DIALOGFLOW_API_KEY = "9db2ebf5fd0c406980227d238be8fcca";
    public static final String PACKAGE_NAME = "postop.hcii.hebo";
    public static final String VISUAL_IMAGES_URL = "https://raw.githubusercontent.com/daniellehu/hcii-postop/master/Hebo/app/src/main/assets/";

    public static final int PROFILE_SITE = 0x1;
    public static final int PROFILE_DATE = 0x2;
    public static final int PROFILE_TIME = 0x4;
    public static final int PROFILE_CLINIC = 0x8;
    public static final int PROFILE_COMPLETE = 0xF;

    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_HEBO_TEXT = 2;
    public static final int MESSAGE_HEBO_VISUAL = 3;

    public static final String DEFAULT_SITE = "head";
}
