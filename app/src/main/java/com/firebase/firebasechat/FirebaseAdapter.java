package com.firebase.firebasechat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Pravesh Sharma on 09-10-2018.
 */

public class FirebaseAdapter extends FirebaseRecyclerAdapter<ChatMessageModel, FirebaseAdapter.MessageViewHolder> {

    Context mActivity;
    progressManager progressManager;

    public FirebaseAdapter(@NonNull FirebaseRecyclerOptions<ChatMessageModel> options, Activity mActivity, progressManager progressManager) {
        super(options);
        this.mActivity = mActivity;
        this.progressManager = progressManager;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessageModel model) {
        progressManager.showHideProgressBar(true);
        if (model.getText() != null) {
            holder.messageTextView.setText(model.getText());
            holder.messengerTextView.setText(model.getName());
            setImageToView(holder.messengerImageView, model.getPhotoUrl());
        }
    }


    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }


    @Override
    public void onError(@NonNull DatabaseError error) {
        super.onError(error);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.messengerImageView)
        CircleImageView messengerImageView;
        @BindView(R.id.messageTextView)
        TextView messageTextView;
        @BindView(R.id.messageImageView)
        ImageView messageImageView;
        @BindView(R.id.messengerTextView)
        TextView messengerTextView;

        MessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private void setImageToView(ImageView view, String url) {
        if (url!=null)
            Glide.with(mActivity).load(url).into(view);

    }


    public interface progressManager {
        void showHideProgressBar(boolean flag);
    }

}
