package postop.hcii.hebo;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MainActivity extends AppCompatActivity implements AIListener {

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private FloatingActionButton listenButton;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private java.util.List<Message> messageList;
    private AIService aiService;
    private TextToSpeech textToSpeech;
    private Config CONFIG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            } else {
                android.support.v4.app.ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            addMessage("Hello there! How are you doing, and how can I help?", true);
        }

        // Configure Dialogflow
        final AIConfiguration config = new AIConfiguration(CONFIG.DIALOGFLOW_API_KEY,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

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

    @Override
    public void onError(final AIError error) {
        addMessage(error.toString(), false);
    }

    @Override
    public void onListeningStarted() {}

    @Override
    public void onListeningCanceled() {}

    @Override
    public void onListeningFinished() {}

    @Override
    public void onAudioLevel(final float level) {}

    public void listenButtonOnClick(final View view) {
        aiService.startListening();
    }

    public void onResult(final AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        addMessage(result.getResolvedQuery(), false);
        addMessage(result.getFulfillment().getSpeech(), true);

        // Read out the response
        String toSpeak = result.getFulfillment().getSpeech().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }


}