package android.ememobong.com.gpax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

/**
 * Created by ememobong on 31/07/2016.
 */
public class


ResourcesCardFragment extends Fragment {

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    public static Activity activity;

    static boolean calledAlready = false;

    public static String COURSE_CODE = " ";

    public int[] textviewBackgroud = {R.drawable.textview_blue, R.drawable.textview_green,
            R.drawable.textview_grey, R.drawable.textview_lightblue, R.drawable.textview_purple};


    public static class ResourcesViewHolder extends RecyclerView.ViewHolder {
        public TextView cardImage;
        public TextView cardCodeTextView;
        public TextView cardNameTextView;

        public ResourcesViewHolder(View v) {
            super(v);
            cardCodeTextView = (TextView) itemView.findViewById(R.id.card_title);
            cardNameTextView = (TextView) itemView.findViewById(R.id.card_text);
            cardImage = (TextView) itemView.findViewById(R.id.card_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ViewCourseResourcesActivity.class);
                    intent.putExtra(COURSE_CODE, cardCodeTextView.getText().toString().toLowerCase());
                    context.startActivity(intent);
                    activity.finish();

                }
            });
        }
    }

    RecyclerView recyclerView;
    private DatabaseReference mFirebaseDatabaseReference;

    private FirebaseRecyclerAdapter<Course, ResourcesViewHolder>
            mFirebaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.view_resources_recycler_view, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userUID = mFirebaseUser.getUid();

        activity = getActivity();

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.USERS_SUBSCRIBED_RESOURCES_NODE).child(userUID);

        mFirebaseDatabaseReference.keepSynced(true);


        mFirebaseAdapter = new FirebaseRecyclerAdapter<Course,
                ResourcesViewHolder>(
                Course.class,
                R.layout.item_card,
                ResourcesViewHolder.class,
                mFirebaseDatabaseReference) {

            @Override
            public DatabaseReference getRef(int position) {
                return super.getRef(position);
            }

            @Override
            protected void populateViewHolder(ResourcesViewHolder viewHolder,
                                              Course course, int position) {
                String courseName = course.getCourse_code();
                String initialLetter = courseName.substring(0,1);
                viewHolder.cardImage.setText(initialLetter.toUpperCase());

                int randomBackground = new Random().nextInt(textviewBackgroud.length);
                viewHolder.cardImage.setBackgroundResource(textviewBackgroud[randomBackground]);

                viewHolder.cardCodeTextView.setText((course.getCourse_code()).toUpperCase());
                viewHolder.cardNameTextView.setText(course .getName());

            }
        };

        recyclerView.setAdapter(mFirebaseAdapter);
        return recyclerView;
    }




}
