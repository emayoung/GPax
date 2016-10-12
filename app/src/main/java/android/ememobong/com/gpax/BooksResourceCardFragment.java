package android.ememobong.com.gpax;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ememobong on 01/09/2016.
 */
public class BooksResourceCardFragment extends Fragment {

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    private RecyclerView commentRecyclerView;
    private LinearLayoutManager commentLinearLayoutManager;
    private FirebaseRecyclerAdapter<String, CommentViewHolder>
            mFirebaseAdapterComment;
    public static String COURSE_CODE = " ";

    String courseCode = "";

    private String userWhoPosted = "";

    public static class ResourcesViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView userDetailsTextView;
        public TextView dateTextview;
        public TextView commentTextview;
        public TextView  numberOfLikesTextview;
        public TextView  numberOfCommentTextview;
        public ImageButton likesImageButton;
        public ImageButton commentImageButton;
        public Button clickToView;

        public ResourcesViewHolder(View v) {
            super(v);
            profileImage = (CircleImageView) itemView.findViewById(R.id.others_profile_image_thumbnail);
            userDetailsTextView = (TextView) itemView.findViewById(R.id.others_profile_details_of_contributor);
            dateTextview = (TextView) itemView.findViewById(R.id.others_date_resource_is_contributed);
            commentTextview = (TextView) itemView.findViewById(R.id.others_comment_by_contributor);
            numberOfCommentTextview = (TextView) itemView.findViewById(R.id.others_number_of_comments_text_view);
            numberOfLikesTextview = (TextView) itemView.findViewById(R.id.others_number_of_likes_text_view);
            likesImageButton = (ImageButton) itemView.findViewById(R.id.others_likes_image_button);
            commentImageButton = (ImageButton) itemView.findViewById(R.id.others_comment_image_button);
            clickToView = (Button) itemView.findViewById(R.id.others_checkout_uploads_for_course);

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

    private FirebaseRecyclerAdapter<CustomOtherResource, ResourcesViewHolder>
            mFirebaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.view_resources_recycler_view, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userUID = mFirebaseUser.getUid();

        Intent intent = getActivity().getIntent();
        courseCode = intent.getStringExtra(ResourcesCardFragment.COURSE_CODE);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                .child(MainActivity.userSchoolName).child(Constants.COURSE_OTHER_RESOURCES_NODE).child(courseCode);

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

        mFirebaseAdapter = new FirebaseRecyclerAdapter<CustomOtherResource,
                ResourcesViewHolder>(
                CustomOtherResource.class,
                R.layout.item_course_others,
                ResourcesViewHolder.class,
                mFirebaseDatabaseReference.orderByChild("timeOfPost").limitToFirst(150)) {

            @Override
            public DatabaseReference getRef(int position) {
                return super.getRef(position);
            }

            @Override
            protected void populateViewHolder(final ResourcesViewHolder viewHolder,
                                              final CustomOtherResource course, final int position) {

                Query postUser = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName)
                        .child(Constants.USERS_PROFILE_NODE).child(course.getUserUID());
                postUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 0.0){
//                            user has no profile make name annonymous
                            userWhoPosted = "Annonymous";

                        }else {
//                            get user name and
                            userWhoPosted = "";
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.userDetailsTextView.setText(userWhoPosted + " posted to this resource");
                viewHolder.dateTextview.setText(course.getDateOfPost() + " by " + course.getTimeOfPost());

                Query commentQuery = getRef(position).child("comments").limitToFirst(1);
                commentQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String contributorCommentKey = dataSnapshot.getValue().toString();
                        viewHolder.commentTextview.setText(contributorCommentKey);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query likesQuery = getRef(position).child("likes");
                likesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 1.00){
                            viewHolder.numberOfLikesTextview.setText("" + dataSnapshot.getChildrenCount() + " like");
                        }
                        else {
                            viewHolder.numberOfLikesTextview.setText("" + dataSnapshot.getChildrenCount() + " likes");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query numberOfCommentsQuery = getRef(position).child("comments");
                numberOfCommentsQuery.addValueEventListener(new ValueEventListener()  {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 1.00){
                            viewHolder.numberOfCommentTextview.setText("" + dataSnapshot.getChildrenCount() + " comment");

                        }
                        else{
                            viewHolder.numberOfCommentTextview.setText("" + dataSnapshot.getChildrenCount() + " comments");

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.likesImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getRef(position).child("likes").child(mFirebaseUser.getUid()).push();
                        getRef(position).child("likes").child(mFirebaseUser.getUid()).setValue("1");
                    }
                });

                viewHolder.commentImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        pop a dialog showing the comments and ability to add the comments
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


                viewHolder.clickToView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String url = course.getFileUrl();
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setData(Uri.parse(url));
                        startActivity(viewIntent);
                    }
                });


            }
        };

        recyclerView.setAdapter(mFirebaseAdapter);
        return recyclerView;
    }




}
