package postop.hcii.hebo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ResponseListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;

    private Context mContext;
    private List<Response> mResponseList;

    public ResponseListAdapter(Context context, List<Response> responseList) {
        mContext = context;
        mResponseList = responseList;
    }

    @Override
    public int getItemCount() { return mResponseList.size(); }

    @Override
    public int getItemViewType(int position) {
        Response response = (Response) mResponseList.get(position);

        if (response.isImage()) {
            return VIEW_TYPE_IMAGE;
        } else {
            return VIEW_TYPE_TEXT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_IMAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_response_img, parent, false);
            return new ImageResponseHolder(view);
        } else if (viewType == VIEW_TYPE_TEXT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_response_text, parent, false);
            return new TextResponseHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Response response = (Response) mResponseList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_IMAGE:
                ((ImageResponseHolder) holder).bind(response);
                break;
            case VIEW_TYPE_TEXT:
                ((TextResponseHolder) holder).bind(response);
                break;
        }
    }


    private class ImageResponseHolder extends RecyclerView.ViewHolder {
        ImageView img;

        ImageResponseHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.image_response);
        }

        void bind(Response response) {
            Picasso.with(mContext)
                    .load(Config.VISUAL_IMAGES_URL + response.getResponse())
                    .into(img);
        }
    }

    private class TextResponseHolder extends RecyclerView.ViewHolder {
        TextView text;

        TextResponseHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text_response);
        }

        void bind(Response response) {
            text.setText(response.getResponse());
        }
    }
}
