package android.ememobong.com.gpax;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
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
import com.firebase.client.Firebase;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ememobong on 01/09/2016.
 */
public class PastQuestionCardFragment extends Fragment {

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    private RecyclerView commentRecyclerView;
    private LinearLayoutManager commentLinearLayoutManager;
    private FirebaseRecyclerAdapter<String, CommentViewHolder>
            mFirebaseAdapterComment;

    static boolean calledAlready = false;

    static final String NODE_KEY = "nodeKey";
    static String COURSE_CODE = "courseCode";
    static String UPDATE_COURSE = "updateCourse";
    static String UPDATE_COURSE_CODE = "";

    String courseCode = "";

    private String userWhoPosted = "";

    public static class ResourcesViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView userDetailsTextView;
        public TextView dateTextview;
        public TextView commentTextview;
        public ImageView firstImageUploaded;
        public TextView  numberOfLikesTextview;
        public TextView  numberOfCommentTextview;
        public TextView  numberOfResourcesTextview;
        public TextView  lastCommentTextview;
        public TextView  circleBackground;
        public ImageButton likesImageButton;
        public ImageButton commentImageButton;
        public ImageButton updateImageButton;


        public ResourcesViewHolder(View v) {
            super(v);
            profileImage = (CircleImageView) itemView.findViewById(R.id.profile_image_thumbnail);
            userDetailsTextView = (TextView) itemView.findViewById(R.id.profile_details_of_contributor);
            dateTextview = (TextView) itemView.findViewById(R.id.date_resource_is_contributed);
            circleBackground = (TextView) itemView.findViewById(R.id.circle_background);
            commentTextview = (TextView) itemView.findViewById(R.id.comment_by_contributor);
            firstImageUploaded = (ImageView) itemView.findViewById(R.id.first_image_uploaded_for_course);
            numberOfLikesTextview = (TextView) itemView.findViewById(R.id.number_of_likes_text_view);
            numberOfCommentTextview = (TextView) itemView.findViewById(R.id.number_of_comments_text_view);
            lastCommentTextview = (TextView) itemView.findViewById((R.id.last_comment));
            numberOfResourcesTextview = (TextView) itemView.findViewById(R.id.number_of_course_resources_text_view);
            likesImageButton = (ImageButton) itemView.findViewById(R.id.likes_image_button);
            commentImageButton = (ImageButton) itemView.findViewById(R.id.comment_image_button);
            updateImageButton = (ImageButton) itemView.findViewById(R.id.update_image_button);

            updateImageButton.setVisibility(View.INVISIBLE);
            circleBackground.setVisibility(View.INVISIBLE);

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
    LinearLayoutManager mLinearLayout;
    private DatabaseReference mFirebaseDatabaseReference;
    public static DatabaseReference mFirebaseDatabaseCourseReference;
    public static DatabaseReference mFirebaseDatabaseUpdateReference;

    private FirebaseRecyclerAdapter<CustomPastQuestionResourceForFirebase, ResourcesViewHolder>
            mFirebaseAdapter;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        mLinearLayout = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLinearLayout);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        Intent intent = getActivity().getIntent();
        courseCode = intent.getStringExtra(ResourcesCardFragment.COURSE_CODE);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                .child(MainActivity.userSchoolName).child(Constants.PAST_QUESTION_NODE).child(courseCode);

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

        mFirebaseAdapter = new FirebaseRecyclerAdapter<CustomPastQuestionResourceForFirebase,
                ResourcesViewHolder>(
                CustomPastQuestionResourceForFirebase.class,
                R.layout.view_resource_card_view,
                ResourcesViewHolder.class,
                mFirebaseDatabaseReference.orderByChild("timeOfPost").limitToFirst(150)) {


            @Override
            public DatabaseReference getRef(int position) {
                return super.getRef(position);
            }

            @Override
            protected void populateViewHolder(final ResourcesViewHolder viewHolder,
                                              final CustomPastQuestionResourceForFirebase course, final int position) {

                viewHolder.updateImageButton.setVisibility(View.INVISIBLE);
                viewHolder.circleBackground.setVisibility(View.INVISIBLE);
                if(mFirebaseUser.getUid().equals(course.getUserUID())){
                    viewHolder.updateImageButton.setVisibility(View.VISIBLE);
                    viewHolder.circleBackground.setVisibility(View.VISIBLE);
                }
//                use user id to get user details at user_profiles node
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
                Date formatDate = new Date(course.getTimeOfPost() * -1);
                String timeStamp = new SimpleDateFormat("HH:mm")
                                            .format(formatDate);
                viewHolder.userDetailsTextView.setText(userWhoPosted + " posted to this resource");
                viewHolder.dateTextview.setText(course.getDateOfPost() + " by " + timeStamp);
                viewHolder.firstImageUploaded.setImageResource(R.drawable.gentestimage);
// Trying to inflate with values from firebase database
//                For  now we will use the persons email

                viewHolder.firstImageUploaded.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        start view pastquestions Activity here it is called ResourcezfotzcoursezActivity
                        Intent intent = new Intent(getActivity(), ResourcesForCourseActivity.class);
                        mFirebaseDatabaseCourseReference = getRef(position).child("imageUrls");
                        startActivity(intent);
                    }
                });
//                FirebaseDatabase.getInstance().getReference().child("users").child(course.getUserUID()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        User user = dataSnapshot.getValue(User.class);
//                        viewHolder.userDetailsTextView.setText(course.getUserUID() + " contributed this Resource");
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//  trying to query for the user comment while uploading

                Query commentQuery = getRef(position).child("comments").limitToFirst(1);
                commentQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String contributorCommentKey = dataSnapshot.getValue().toString();
//                        String contributorComment = dataSnapshot.child(contributorCommentKey).getValue().toString();
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
                Query lastcommentQuery = getRef(position).child("comments").limitToLast(1);
                lastcommentQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String contributorCommentKey = dataSnapshot.getValue().toString();
//                        String contributorComment = dataSnapshot.child(contributorCommentKey).getValue().toString();
                        viewHolder.lastCommentTextview.setText(contributorCommentKey);
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

//                trying to query for firstImage
                Query imageQuery = getRef(position).child("imageUrls").limitToFirst(1);
                final int width = getActivity().getResources().getDisplayMetrics().widthPixels;
                final int height = getActivity().getResources().getDisplayMetrics().heightPixels;
                imageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final String resourceImageUrl = dataSnapshot.getValue().toString();

                        Glide.with(getActivity()).load(resourceImageUrl).placeholder(R.mipmap.ic_launcher)
                                .error(R.drawable.gentestimage).override(width, height/2)
                                .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolder.firstImageUploaded);
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

                Query numberOfResourcesQuery = getRef(position).child("imageUrls");
                numberOfResourcesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 1.00){
                            viewHolder.numberOfResourcesTextview.setText("" + dataSnapshot.getChildrenCount() + " only!");
                        }else{
                            viewHolder.numberOfResourcesTextview.setText("" + dataSnapshot.getChildrenCount() + " more ");
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
                viewHolder.updateImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        start ocr to update imageurls, imagename, and timeStamp
                        mFirebaseDatabaseUpdateReference = getRef(position);
                        UPDATE_COURSE_CODE = courseCode;
                        Intent intent = new Intent(getActivity(), OcrCaptureActivity.class);
//                        pass in the node update is to be made
                        intent.putExtra(PastQuestionCardFragment.NODE_KEY, mFirebaseDatabaseCourseReference+"");
                        intent.putExtra(PastQuestionCardFragment.UPDATE_COURSE, PastQuestionCardFragment.UPDATE_COURSE);
                        intent.putExtra(PastQuestionCardFragment.COURSE_CODE, courseCode);
                        startActivity(intent);
                        getActivity().finish();
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


            }

        };



//        trying to query for the first Image that was uploaded



        recyclerView.setAdapter(mFirebaseAdapter);
        return recyclerView;


    }

}
