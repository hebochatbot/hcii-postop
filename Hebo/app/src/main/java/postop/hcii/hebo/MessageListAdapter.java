package postop.hcii.hebo;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/*
 * Message adapter: class that handles what Message to show next.
 * We use an "Adapter" for this because there can be an arbitrary amount of "messages"
 * that are exchanged between Hebo and the user. Adapter classes handle inflating an
 * arbitrary amount of elements... in our case, an arbitrary amount of conversation
 * exchanges between the user and Hebo.
 *
 * Currently there are 4 types:
 * SENT - when the user asks Hebo a question
 * TEXT - when Hebo is responding to the user in plaintext
 * VISUAL - when Hebo is responding to the user with a visual answer
 * TIMER - when Hebo is responding to the user with a timer
 */

public class MessageListAdapter extends RecyclerView.Adapter{
    private static final int VIEW_TYPE_MESSAGE_SENT = Config.MESSAGE_SENT;
    private static final int VIEW_TYPE_MESSAGE_HEBO_TEXT = Config.MESSAGE_HEBO_TEXT;
    private static final int VIEW_TYPE_MESSAGE_HEBO_VISUAL = Config.MESSAGE_HEBO_VISUAL;
    private static final int VIEW_TYPE_MESSAGE_HEBO_TIMER = Config.MESSAGE_HEBO_TIMER;

    private Context mContext;
    private List<Message> mMessageList;
    private Timer timer;

    public MessageListAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);
        return message.getMessageType();
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageHolder(view);
            case VIEW_TYPE_MESSAGE_HEBO_TEXT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_hebo_text, parent, false);
                return new HeboTextMessageHolder(view);
            case VIEW_TYPE_MESSAGE_HEBO_VISUAL:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_hebo_visual, parent, false);
                return new HeboVisualMessageHolder(view);
            case VIEW_TYPE_MESSAGE_HEBO_TIMER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_hebo_timer, parent, false);
                return new HeboTimerMessageHolder(view);
            default: return null;
        }
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_HEBO_TEXT:
                ((HeboTextMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_HEBO_VISUAL:
                ((HeboVisualMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_HEBO_TIMER:
                ((HeboTimerMessageHolder) holder).bind(message);
                break;
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        RecyclerView mResponseRecycler;
        ResponseListAdapter mResponseAdapter;
        View view;

        SentMessageHolder(View itemView) {
            super(itemView);
            view = itemView;
            mResponseRecycler = (RecyclerView) itemView.findViewById(R.id.recyclerview_sent_response);
        }

        void bind(Message message) {
            mResponseAdapter = new ResponseListAdapter(view.getContext(), message.getResponses());
            mResponseRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
            mResponseRecycler.setAdapter(mResponseAdapter);
        }
    }

    private class HeboTextMessageHolder extends RecyclerView.ViewHolder {
        RecyclerView mResponseRecycler;
        ResponseListAdapter mResponseAdapter;
        View view;

        HeboTextMessageHolder(View itemView) {
            super(itemView);
            view = itemView;
            mResponseRecycler = (RecyclerView) itemView.findViewById(R.id.recyclerview_hebo_response);
        }

        void bind(Message message) {
            mResponseAdapter = new ResponseListAdapter(view.getContext(), message.getResponses());
            mResponseRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
            mResponseRecycler.setAdapter(mResponseAdapter);
        }
    }

    private class HeboVisualMessageHolder extends RecyclerView.ViewHolder {
        RecyclerView mResponseRecycler;
        ResponseListAdapter mResponseAdapter;
        TextView title;
        View view;

        HeboVisualMessageHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.visual_title);
            mResponseRecycler = (RecyclerView) itemView.findViewById(R.id.recyclerview_visual_response);
        }

        void bind(Message message) {
            mResponseAdapter = new ResponseListAdapter(view.getContext(), message.getResponses());
            mResponseRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
            mResponseRecycler.setAdapter(mResponseAdapter);
            title.setText(message.getMessageTitle());
        }
    }

    private class HeboTimerMessageHolder extends RecyclerView.ViewHolder {
        TextView timerText;
        Button cancelButton;

        HeboTimerMessageHolder(View itemView) {
            super(itemView);
            timerText = (TextView) itemView.findViewById(R.id.timer);
            cancelButton = (Button) itemView.findViewById(R.id.timerCancelButton);
        }

        void bind(Message message) {
            List<Response> responses = message.getResponses();
            Response r = responses.get(0);
            String isSecond = r.getResponse();
            timer = new Timer(itemView.getContext(), timerText, cancelButton, Boolean.valueOf(isSecond));
            timer.startTimer();
        }
    }

    public Timer getTimer() { return timer; }
}
