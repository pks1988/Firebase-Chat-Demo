package com.firebase.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,FirebaseAdapter.progressManager {

    FirebaseUser mFirebaseUser;
    FirebaseAuth mFirebaseAuth;
    @BindView(R.id.messageRecyclerView)
    RecyclerView messageRecyclerView;
    @BindView(R.id.addMessageImageView)
    ImageView addMessageImageView;
    @BindView(R.id.messageEditText)
    EditText messageEditText;
    @BindView(R.id.sendButton)
    Button sendButton;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.progressBar)
    public ProgressBar progressBar;
    private String mUserName;
    private String mPhotoUrl;
    private DatabaseReference mFirebaseDatabaseReference;
    public static final String ANONYMOUS = "anonymous";
    private LinearLayoutManager mLinearLayoutManager;
    public static final String MESSAGES_CHILD = "messages";
    private SnapshotParser<ChatMessageModel> parser;
    FirebaseAdapter mFirebaseAdapter;
    FirebaseAdapter.progressManager progressManager=this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sendButton.setOnClickListener(this);

        //Initialize firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        progressBar.setVisibility(View.VISIBLE);

        verifyUser();
        createCustomParser();
        setAdapterAndManager();

    }

    private void setAdapterAndManager() {

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mFirebaseAdapter = new FirebaseAdapter(createFirebaseRecyclerQueryOptions(),this,progressManager);

        messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        messageRecyclerView.setAdapter(mFirebaseAdapter);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    messageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
    }

    private void createCustomParser() {
        parser = new SnapshotParser<ChatMessageModel>() {
            @Override
            public ChatMessageModel parseSnapshot(DataSnapshot dataSnapshot) {
                ChatMessageModel friendlyMessage = dataSnapshot.getValue(ChatMessageModel.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };
    }

    private void verifyUser() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        } else {
            mUserName = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null)
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                signOut();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void signOut() {
        mFirebaseAuth.signOut();
        mFirebaseUser = null;
        mUserName = ANONYMOUS;
        mPhotoUrl = null;
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }


    private FirebaseRecyclerOptions createFirebaseRecyclerQueryOptions() {
        return new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(createFirebaseQuery(), parser)
                .build();
    }


    private Query createFirebaseQuery() {
        return FirebaseDatabase
                .getInstance()
                .getReference()
                .child(MESSAGES_CHILD);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendButton:
                ChatMessageModel chatMessageModel = new ChatMessageModel(messageEditText.getText().toString().trim()
                        , mUserName
                        , mPhotoUrl
                        , null);

                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessageModel);
                messageEditText.setText("");

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }


    @Override
    public void showHideProgressBar(boolean flag) {
        if(flag)
            progressBar.setVisibility(View.INVISIBLE);
    }
}
