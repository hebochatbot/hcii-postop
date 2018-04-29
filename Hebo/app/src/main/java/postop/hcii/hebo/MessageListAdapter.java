package postop.hcii.hebo;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter{
    private static final int VIEW_TYPE_MESSAGE_SENT = Config.MESSAGE_SENT;
    private static final int VIEW_TYPE_MESSAGE_HEBO_TEXT = Config.MESSAGE_HEBO_TEXT;
    private static final int VIEW_TYPE_MESSAGE_HEBO_VISUAL = Config.MESSAGE_HEBO_VISUAL;

    private Context mContext;
    private List<Message> mMessageList;

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
}
