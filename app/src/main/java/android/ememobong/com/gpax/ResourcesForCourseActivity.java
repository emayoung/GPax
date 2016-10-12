package android.ememobong.com.gpax;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ResourcesForCourseActivity extends AppCompatActivity {


    static boolean calledAlready = false;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;



    public static class ResourcesViewHolder extends RecyclerView.ViewHolder {
        public ImageView cardImage;

        public ResourcesViewHolder(View v) {
            super(v);
            cardImage = (ImageView) itemView.findViewById(R.id.card_image2);
        }
    }

    RecyclerView recyclerView;
    private DatabaseReference mFirebaseDatabaseReference;
    private LinearLayoutManager mLinearLayoutManager;


    private FirebaseRecyclerAdapter<String, ResourcesViewHolder>
            mFirebaseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources_for_course);

        recyclerView = (RecyclerView) findViewById(R.id.resource_for_course_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String userUID = mFirebaseUser.getUid();
        Intent intent = getIntent();
        String courseCode = intent.getStringExtra(PastQuestionCardFragment.COURSE_CODE);
        String nodeKey = intent.getStringExtra(PastQuestionCardFragment.NODE_KEY);


        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, ResourcesViewHolder>(
                String.class,
                R.layout.item_resource_for_course,
                ResourcesViewHolder.class,
                PastQuestionCardFragment.mFirebaseDatabaseCourseReference) {

            @Override
            public DatabaseReference getRef(int position) {
                return super.getRef(position);
            }

            @Override
            protected void populateViewHolder(final ResourcesViewHolder viewHolder,
                                              String imageUrl, int position) {
                final int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
                final int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
                final String resourceImageUrl = imageUrl;


                Glide.with(ResourcesForCourseActivity.this).load(resourceImageUrl).placeholder(R.mipmap.ic_launcher)
                        .error(R.drawable.gentestimage).override(width, height)
                        .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(viewHolder.cardImage);

            }
        };

        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mFirebaseAdapter);

    }
}
