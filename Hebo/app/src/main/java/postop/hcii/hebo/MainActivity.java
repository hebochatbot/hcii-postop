package postop.hcii.hebo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIEvent;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.AIRequest;
import ai.api.model.Result;
import ai.api.AIDataService;
import ai.api.RequestExtras;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int VISUAL_ANSWER_OFFSET = 150;
    private SharedPreferences sharedPref;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private boolean isFollowUp = false;
    private String BODY_PART, DATE_TIME;
    private boolean gaveConsent;

    private ImageView listenButton;
    private android.os.Handler mHandler = new android.os.Handler();
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private java.util.List<Message> messageList;
    private AIService aiService;
    private AIDataService aiDataService;
    private TextToSpeech textToSpeech;
    private Config CONFIG;
    private Button consentCancel, consentAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar c = Calendar.getInstance();
        String currentDate = getDate(c);
        String currentTime = getTime(c);

        sharedPref = this.getSharedPreferences("profile", Context.MODE_PRIVATE);
        DATE_TIME = sharedPref.getString("date", currentDate) + " at " + sharedPref.getString("time", currentTime);
        BODY_PART = sharedPref.getString("bodyPart", Config.DEFAULT_SITE);
        gaveConsent = sharedPref.getBoolean("consent", false);

        messageList = new LinkedList<>();
        listenButton = (ImageView) findViewById(R.id.listenButton);
        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        // Configure speech to text
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000); // min 10 sec of silence
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);


        // Configure Dialogflow
        final AIConfiguration config = new AIConfiguration(CONFIG.DIALOGFLOW_API_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);

        // Configure text to speech
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // if first time user, bring up onboarding activity
        boolean isUserFirstTime = Boolean.valueOf(sharedPref.getString("isFirstTimeUser", "true"));
        Intent introIntent = new Intent(MainActivity.this, Onboarding.class);
//        if (isUserFirstTime) { TODO: REMOVE WHEN NOT TESTING
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear().commit();
            startActivity(introIntent);
//        }

        // bring up consent & permissions
        if (!gaveConsent) createConsentDialog();
        if (doesNotHavePermission()) getPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    addTextMessage("Hello there! How are you doing, and how can I help?", true);
                } else {
                    addTextMessage("Sorry, I can't help you unless you accept microphone permissions!", true);
                }
                return;
            }
        }
    }

    private void getPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        };
    }

    private boolean doesNotHavePermission() {
        return (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED);
    }

    public void addTextMessage(String msg, boolean isHebo) {
        int messageType = (isHebo) ? Config.MESSAGE_HEBO_TEXT : Config.MESSAGE_SENT;
        Response response = new Response(msg, false);
        List<Response> responseList = new ArrayList<>();
        responseList.add(response);
        Message myMessage = new Message(responseList, messageType);
        messageList.add(myMessage);
        int position = mMessageAdapter.getItemCount() - 1;
        mMessageAdapter.notifyItemInserted(position);
        mMessageRecycler.scrollToPosition(position);
    }

    public void addVisualMessage(List<String> stringResponses) {
        List<Response> responseList = new ArrayList<>();
        String title = stringResponses.get(0); // first response is always the title
        populateResponseList(responseList, stringResponses);
        Message myMessage = new Message(responseList, Config.MESSAGE_HEBO_VISUAL, title);
        messageList.add(myMessage);
        int position = mMessageAdapter.getItemCount() - 1;
        mMessageAdapter.notifyItemInserted(position);
        mMessageRecycler.scrollBy(0, VISUAL_ANSWER_OFFSET);
    }

    public void populateResponseList(List<Response> responseList, List<String> stringList) {
        for (int i = 1; i < stringList.size(); i++) {
            String text = stringList.get(i);
            boolean isImage = (text.charAt(0) == '_');
            Response r = new Response(text, isImage);
            responseList.add(r);
        }
    }

    public void listenButtonOnClick(final View view) {
        gaveConsent = sharedPref.getBoolean("consent", false);
        if (gaveConsent) {
            if (doesNotHavePermission()) {
                getPermissions();
            } else {
                speech.startListening(recognizerIntent);
            }
        }
        else createConsentDialog();
    }

    public void listenProfileOnClick(final View view) {
        // go to profile page
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int errorCode) {
        if (errorCode != SpeechRecognizer.ERROR_CLIENT) {
            String errorMessage = getErrorText(errorCode);
            addTextMessage(errorMessage, true);
        }
    }

    @Override
    public void onEvent(int arg0, Bundle b) {
    }

    @Override
    public void onPartialResults(Bundle b) {
    }

    @Override
    public void onReadyForSpeech(Bundle b) {
    }

    @Override
    public void onResults(Bundle results) {
        java.util.ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String speech_text = matches.get(0);
        String param_text = "";

        Calendar c = Calendar.getInstance();
        String currentDate = getDate(c);
        String currentTime = getTime(c);

        DATE_TIME = sharedPref.getString("date", currentDate) + " at " + sharedPref.getString("time", currentTime);
        BODY_PART = sharedPref.getString("bodyPart", "head");

        if (isFollowUp) {
            param_text = speech_text;
        } else {
            param_text = "(" + BODY_PART + ")" + " (" + DATE_TIME + ") " + speech_text;
        }
        sendRequest(param_text);
        isFollowUp = false; // reset follow up
    }

    private void sendRequest(final String queryString) {
        Log.d("sent request", queryString);
        final String eventString = null;
        final String contextString = null;
        final android.os.AsyncTask<String, Void, AIResponse> task = new android.os.AsyncTask<String, Void, AIResponse>() {
            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                String event = params[1];

                if (!android.text.TextUtils.isEmpty(query)) request.setQuery(query);
                if (!android.text.TextUtils.isEmpty(event)) request.setEvent(new AIEvent(event));
                final String contextString = params[2];
                ai.api.RequestExtras requestExtras = null;
                if(!android.text.TextUtils.isEmpty(contextString)) {
                    final java.util.List<AIContext> contexts = java.util.Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final ai.api.AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };
        task.execute(queryString, eventString, contextString);
    }

    public void onResult(final AIResponse response) {
        Result result = response.getResult();

//        // Get parameters
//        String parameterString = "";
//        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
//            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
//                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
//            }
//        }

        String resolvedQuery = result.getResolvedQuery();
        Log.d("Me", resolvedQuery);
        Log.d("Hebo", result.getFulfillment().getSpeech().toString());
        if (resolvedQuery.charAt(0) == '(') {
            int firstInd = resolvedQuery.indexOf(")");
            int secondInd = resolvedQuery.indexOf(")", firstInd+1);
            resolvedQuery = resolvedQuery.substring(secondInd+2);
        }

        String speech = result.getFulfillment().getSpeech().toString();
        String displayText = result.getFulfillment().getDisplayText();
        displayText = (displayText == null) ? speech : displayText;

        // follow up question, so do not send parameters next time
        isFollowUp = (displayText.charAt(displayText.length()-1) == '?');

        // display my text input
        addTextMessage(resolvedQuery, false);

        // is a visual answer, display visual response from Hebo
        if (displayText.charAt(0) == '(' && displayText.charAt(displayText.length()-1) == ')') {
            String visual_key = displayText.substring(1, displayText.length()-1);
            Resources res = this.getResources();
            int responsesId = res.getIdentifier(visual_key, "array", getPackageName());
            final List<String> responses = Arrays.asList(res.getStringArray(responsesId));
            addVisualMessage(responses);
        } else {
            // display text response from Hebo
            addTextMessage(displayText, true);
        }

        // Read out the response
        String toSpeak = speech;
        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addTextMessage(error.toString(), false);
            }
        });
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Permission error";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "Sorry! I didn't catch that. Please try again.";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    private String getDate(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        return Integer.toString(month+1) + "/" + Integer.toString(day) + "/" + Integer.toString(year);
    }

    private String getTime(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        return Integer.toString(hour) + ":" + Integer.toString(minute) + am_pm;
    }

    private void createConsentDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.activity_consent, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        consentAgree = (Button) mView.findViewById(R.id.agreeButton);
        consentCancel = (Button) mView.findViewById(R.id.cancelButton);

        consentAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("consent", true);
                editor.commit();
            }
        });
        consentCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}

