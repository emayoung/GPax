package android.ememobong.com.gpax;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by ememobong on 08/10/2016.
 */
public class AdvertCardFragment  extends Fragment {

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    private RecyclerView commentRecyclerView;
    private LinearLayoutManager commentLinearLayoutManager;
    private FirebaseRecyclerAdapter<String, CommentViewHolder>
            mFirebaseAdapterComment;

    static boolean calledAlready = false;

    public static class ResourcesViewHolder extends RecyclerView.ViewHolder {

        public ImageView advertImage;
        public TextView cardTimePassedTextView;
        public TextView cardCommNumTextView;
        public ImageButton commentButton;

        public ResourcesViewHolder(View v) {
            super(v);
            advertImage = (ImageView) itemView.findViewById(R.id.advert_image);
            cardTimePassedTextView = (TextView) itemView.findViewById(R.id.time_passed_adv_text_view);
            cardCommNumTextView = (TextView) itemView.findViewById(R.id.number_of_adv_comments_text_view);
            commentButton = (ImageButton) itemView.findViewById(R.id.adv_comment_image_button);


        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView commentTextView;


        public CommentViewHolder(View v) {
            super(v);
            commentTextView = (TextView) itemView.findViewById(R.id.comment_text_view);

        }
    }


    RecyclerView recyclerView;
    private DatabaseReference mFirebaseDatabaseReference;

    private FirebaseRecyclerAdapter<AdvertsJOPO, ResourcesViewHolder>
            mFirebaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.view_resources_recycler_view, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userUID = mFirebaseUser.getUid();

        final Intent intent = getActivity().getIntent();

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                .child(MainActivity.userSchoolName).child(Constants.NEWS_NODE).child(Constants.SCHOOL_ADVERTS);

        mFirebaseDatabaseReference.keepSynced(true);

        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount() == 0.00){

                    Toast.makeText(getActivity(),"No Resource available for course yet. Support by adding one", Toast.LENGTH_LONG ).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mFirebaseAdapter = new FirebaseRecyclerAdapter<AdvertsJOPO, ResourcesViewHolder>(
                AdvertsJOPO.class,
                R.layout.advert_list,
                ResourcesViewHolder.class,
                mFirebaseDatabaseReference.orderByChild("advertID")) {

            @Override
            public DatabaseReference getRef(int position) {
                return super.getRef(position);
            }

            @Override
            protected void populateViewHolder(final ResourcesViewHolder viewHolder,
                                              final AdvertsJOPO course, final int position) {

                viewHolder.advertImage.setImageDrawable(getResources().getDrawable(R.drawable.gentestimage));
                final int width = getActivity().getResources().getDisplayMetrics().widthPixels;
                final int height = getActivity().getResources().getDisplayMetrics().heightPixels;
                String advUrl = course.getAdvertLink();

                Glide.with(getActivity()).load(advUrl).placeholder(R.mipmap.ic_launcher)
                        .error(R.drawable.gentestimage).override(width, height/2)
                        .into(viewHolder.advertImage);

                long timeStamp = course.getTimeOfPost() * -1;

                long timePassedSec = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeStamp);
                long timePassedMin = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timeStamp);
                long timePassedHr = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timeStamp);
                long timePassedDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timeStamp);
                if(timePassedSec <= 60){
                    viewHolder.cardTimePassedTextView.setText("" + timePassedSec + "s");
                }else if(timePassedMin <= 60){
                    viewHolder.cardTimePassedTextView.setText("" + timePassedMin + "m");
                }else if(timePassedHr <= 24){
                    viewHolder.cardTimePassedTextView.setText("" + timePassedHr + "h");
                }else if(timePassedDay >= 1 && timePassedDay < 7){
                    viewHolder.cardTimePassedTextView.setText("" + timePassedDay + "d");
                }else if(timePassedDay >= 7){
                    viewHolder.cardTimePassedTextView.setText("" + timePassedDay%7 + "w");
                }


                Query numberOfCommentsQuery = getRef(position).child("comments");
                numberOfCommentsQuery.addValueEventListener(new ValueEventListener()  {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 1.00){
                            viewHolder.cardCommNumTextView.setText("" + (dataSnapshot.getChildrenCount() - 1));

                        }
                        else{
                            viewHolder.cardCommNumTextView.setText("" + (dataSnapshot.getChildrenCount() - 1));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                viewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_for_comment);
                        dialog.show();

                        commentRecyclerView = (RecyclerView) dialog.findViewById(R.id.commentRecyclerView);
                        final EditText commentEditText = (EditText) dialog.findViewById(R.id.commentEditText);
                        final Button sendButton = (Button) dialog.findViewById(R.id.sendButton);

                        commentLinearLayoutManager = new LinearLayoutManager(getActivity());
                        commentLinearLayoutManager.setStackFromEnd(true);

                        final DatabaseReference commentRef = getRef(position).child("comments");
//                        get comments at this node

                        mFirebaseAdapterComment = new FirebaseRecyclerAdapter<String, CommentViewHolder>(
                                String.class,
                                R.layout.item_for_dialog_comment,
                                CommentViewHolder.class,
                                commentRef
                        ) {
                            @Override
                            protected void populateViewHolder(CommentViewHolder viewHolder, String model, int position) {

                                viewHolder.commentTextView.setText(model);
                            }
                        };

                        mFirebaseAdapterComment.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                super.onItemRangeInserted(positionStart, itemCount);
                                int commentCount = mFirebaseAdapterComment.getItemCount();
                                int lastVisiblePosition =
                                        commentLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                                // If the recycler view is initially being loaded or the
                                // user is at the bottom of the list, scroll to the bottom
                                // of the list to show the newly added message.
                                if (lastVisiblePosition == -1 ||
                                        (positionStart >= (commentCount - 1) &&
                                                lastVisiblePosition == (positionStart - 1))) {
                                    commentRecyclerView.scrollToPosition(positionStart);
                                }
                            }
                        });

                        commentRecyclerView.setLayoutManager(commentLinearLayoutManager);
                        commentRecyclerView.setAdapter(mFirebaseAdapterComment);

                        commentEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
                        commentEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (charSequence.toString().trim().length() > 0) {
                                    sendButton.setEnabled(true);
                                } else {
                                    sendButton.setEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });

                        sendButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                push to firebase now at the comment node
                                commentRef.push().setValue(mFirebaseUser.getDisplayName() + ": " + commentEditText.getText().toString());
                                commentEditText.setText("");
                            }
                        });

                    }
                });

            }
        };



        recyclerView.setAdapter(mFirebaseAdapter);
        return recyclerView;
    }


}
