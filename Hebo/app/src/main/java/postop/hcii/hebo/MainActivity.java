package postop.hcii.hebo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
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

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SharedPreferences sharedPref;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private boolean isFollowUp = false;
    private String BODY_PART;
    private String DATE_TIME;

    private FloatingActionButton listenButton;
    private android.os.Handler mHandler = new android.os.Handler();
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private java.util.List<Message> messageList;
    private AIService aiService;
    private AIDataService aiDataService;
    private TextToSpeech textToSpeech;
    private Config CONFIG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar c = Calendar.getInstance();
        String currentDate = getDate(c);
        String currentTime = getTime(c);

        sharedPref = this.getSharedPreferences("profile", Context.MODE_PRIVATE);
        DATE_TIME = sharedPref.getString("date", currentDate) + " at " + sharedPref.getString("time", currentTime);
        BODY_PART = sharedPref.getString("bodyPart", "head");

        messageList = new LinkedList<>();
        listenButton = (FloatingActionButton) findViewById(R.id.listenButton);
        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        // Grant permissions
        if (android.support.v4.content.ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.RECORD_AUDIO)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            if (android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.RECORD_AUDIO)) {
                addMessage("Hello there! My name is Hebo, in order to talk to me, please grant microphone permissions", true);
                android.support.v4.app.ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            addMessage("Hello there! How are you doing, and how can I help?", true);
        }


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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    addMessage("Hello there! How are you doing, and how can I help?", true);
                } else {
                    addMessage("Sorry, I can't help you unless you accept microphone permissions!", true);
                }
                return;
            }
        }
    }

    public void addMessage(String msg, boolean isHebo) {
        Message myMessage = new Message(msg, isHebo);
        messageList.add(myMessage);
        int position = mMessageAdapter.getItemCount() - 1;
        mMessageAdapter.notifyItemInserted(position);
        mMessageRecycler.scrollToPosition(position);
    }


    public void listenButtonOnClick(final View view) {
        speech.startListening(recognizerIntent);
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
            addMessage(errorMessage, false);
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

        if (isFollowUp) {
            param_text = speech_text;
        } else {
            param_text = "(" + BODY_PART + ")" + " (" + DATE_TIME + ") " + speech_text;
        }
        sendRequest(param_text);
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
        if (speech.charAt(speech.length()-1) == '?') {
            isFollowUp = true;
        }

        addMessage(resolvedQuery, false);
        addMessage(speech, true);

        // Read out the response
        String toSpeak = speech;
        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addMessage(error.toString(), false);
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
                message = "Insufficient permissions";
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
                message = "No speech input";
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

}