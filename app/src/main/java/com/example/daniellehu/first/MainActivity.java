package com.example.daniellehu.first;

import android.content.Intent;
import android.net.Uri;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements AIListener {

    private FloatingActionButton listenButton;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private List<Message> messageList;
    private AIService aiService;
    private TextToSpeech textToSpeech;

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

        final AIConfiguration config = new AIConfiguration("9db2ebf5fd0c406980227d238be8fcca",
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

        addMessage("Hello there! How can I help?", true);

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

    public void simulationButtonOnClick(final View view) {
        startActivity(new Intent(getApplicationContext(), Simulation.class));
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