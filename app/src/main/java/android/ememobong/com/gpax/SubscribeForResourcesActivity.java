package android.ememobong.com.gpax;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubscribeForResourcesActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    public static ArrayAdapter<String> deptCodesAdapter;
    public static  ArrayAdapter<String> semesterCodesAdapter;

    DatabaseReference mFirebaseDatabaseUsers;
    DatabaseReference mFirebaseDatabaseUsersResources;
    DatabaseReference mFirebaseAvailableSchool;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    List<String> userCourses;
    private Button subscribeButtton;

    SharedPreferences userSchoolPref;
    static String userSchoolPrefStr = " ";
    SharedPreferences userAppPref;
    boolean noCourseSelected = true;

    ProgressDialog mProgressDialog;

    ArrayList<String> loadedSchools = new ArrayList<String>();
    long numOfAvailableUnis;
    int countOfUnis = 1;
    ArrayAdapter<String> availableSchoolsAdapter;


    public static final String LOG_TAG = SubscribeForResourcesActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_for_resources);

        setSupportActionBar((Toolbar) findViewById(R.id.first_toolbar));
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        ImageView collapsingImage = (ImageView) findViewById(R.id.collapsing_image);
        if (collapsingImage != null){
            Glide.with(this)
                    .load(R.drawable.giphy).asGif()
                    .centerCrop().placeholder(R.drawable.gentestimage).crossFade()
                    .into(collapsingImage);
        }
        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.first_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i<ContentAdapter.myCourses.length; i++){
                    if(ContentAdapter.myCourses[i] != null){
                        noCourseSelected = false;
                    }
                }
                if(noCourseSelected){
                    Snackbar.make(view, "Select some courses to continue", Snackbar.LENGTH_SHORT).show();
                }
                else{
//                    do what subsrcibe button did
                    subscribe();

                }
            }
        });

//        if user has not selected his school yet pop u dialog for the schools and  fetch the schools data

//        get school from shared preferences
        String selectedSchool = MainActivity.userSchoolAppPref.getString("userSchool", null);

        if(selectedSchool == null){

//            get the available schools from firebase disable and set hint to edittext while looping through to add

//            pop up the select schools dialog
            final Dialog dialog = new Dialog(SubscribeForResourcesActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.add_resources_popup_dialog);
            dialog.setContentView(R.layout.dialog_schools_available);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            final AutoCompleteTextView availableSchools =(AutoCompleteTextView) dialog.findViewById(R.id.schools_avialable);
            final ImageButton okUserUniversityBut = (ImageButton) dialog.findViewById(R.id.ok_schools_available_button);

            availableSchools.setEnabled(false);
            okUserUniversityBut.setEnabled(true);

            okUserUniversityBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mProgressDialog == null) {
                        mProgressDialog = new ProgressDialog(dialog.getContext());
                        mProgressDialog.setMessage("Setting up for  " + userSchoolPrefStr);
                        mProgressDialog.setIndeterminate(true);
                    }
                    else{
                        mProgressDialog.setMessage("Setting up for  " + userSchoolPrefStr);
                    }

                    mProgressDialog.show();
//                    query for the school in the scoools node. if school exist just close dialog the dialog else set up the user schol in the database
//                    this method may not scale if the user does not have a good internet connection
//                    how do i check if a user has succesfully written to the server
                    final DatabaseReference mFirebaseScholsRef = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolPrefStr);
                    Query querySchools = mFirebaseScholsRef;
                    querySchools.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getChildrenCount() == 0.0){

//                                mFirebaseScholsRef.child(Constants.COURSES_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.COURSES_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.PAST_QUESTION_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.PAST_QUESTION_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.USER_ACADEMIC_DETAILS).push().setValue("");
                                mFirebaseScholsRef.child(Constants.USER_ACADEMIC_DETAILS).setValue("");

//                                mFirebaseScholsRef.child(Constants.USERS_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.USERS_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.USERS_PROFILE_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.USERS_PROFILE_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.USERS_SUBSCRIBED_RESOURCES_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.USERS_SUBSCRIBED_RESOURCES_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.NEWS_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.NEWS_NODE).setValue("");

//                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.GENERAL_NEWS_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.GENERAL_NEWS_NODE).setValue("");

                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.COURSE_OTHER_RESOURCES_NODE).setValue("");

                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.COURSE_NEWS).setValue("");

//                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.FACULTY_NEWS_NODE).push().setValue("");
                                mFirebaseScholsRef.child(Constants.NEWS_NODE).child(Constants.FACULTY_NEWS_NODE).setValue("")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               dialog.cancel();
                                               mFirebaseScholsRef.removeEventListener(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {

                                                   }

                                                   @Override
                                                   public void onCancelled(DatabaseError databaseError) {

                                                   }
                                               });
                                                }
                                            }
                                        });


                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                            }
                            else{


                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                dialog.cancel();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });


            mFirebaseAvailableSchool = FirebaseDatabase.getInstance().getReference().child(Constants.AVAILABLE_SCHOOLS_NODE);
            Query querySchools = mFirebaseAvailableSchool;
            querySchools.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                                      get children count of this node
                    numOfAvailableUnis = dataSnapshot.getChildrenCount();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mFirebaseAvailableSchool.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    // disable the dialog components and set hint add the schools to an arraylist and finally enable the associated components
                    availableSchools.setEnabled(false);
                    availableSchools.setHint("please wait while loading supported universities");
                    okUserUniversityBut.setEnabled(false);


                    if(countOfUnis == numOfAvailableUnis){
//                        add,setup user to use data and alert them
                        loadedSchools.add(dataSnapshot.getKey());

                        String availSchools[] = new String[loadedSchools.size()];
                        for(int i=0; i<availSchools.length;i++){
                            availSchools[i] = loadedSchools.get(i);
                        }
                        availableSchoolsAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, availSchools );
                        availableSchools.setAdapter(availableSchoolsAdapter);
                        availableSchools.setThreshold(1);

//                        enable the dialog components
                        availableSchools.setEnabled(true);
                        availableSchools.setHint("\tselect university and continue");

                    }else {
                        loadedSchools.add(dataSnapshot.getKey());
                        countOfUnis++;
                    }

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

            availableSchools.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long rowId) {
//get item at the clicked position set shared pref for school and enable button

                    String selectedSchool = (String) adapterView.getItemAtPosition(pos);

                    userSchoolPrefStr = selectedSchool;
                    SharedPreferences.Editor editor = MainActivity.userSchoolAppPref.edit();
                    editor.putString("userSchool", selectedSchool);
                    editor.commit();

                    okUserUniversityBut.setEnabled(true);

                }

        });
        }
        else{
            userSchoolPrefStr = selectedSchool;
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();


        RecyclerView mCourseRecyclerView = (RecyclerView) findViewById(R.id.subscribe_recycler_view);
        mCourseRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCourseRecyclerView.setLayoutManager(mLinearLayoutManager);
        ContentAdapter adapter = new ContentAdapter(mCourseRecyclerView.getContext(), fab);
        mCourseRecyclerView.setAdapter(adapter);
//        the code below is to set the number of views the recycler view has before it  is recycled
//        mCourseRecyclerView.getRecycledViewPool().setMaxRecycledViews(R.id.card_view, 0);

        deptCodesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.departmental_codes));
        semesterCodesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.semester_codes));
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {

//            declaration of the view we are inflating in  the card
            public AutoCompleteTextView departmentCodes;
            public AutoCompleteTextView semesterCodes;
            public TextView courseCreditUnits;
            public TextView courseTitle;
            Button okAndCheckBut;
            Button addNewCourseBut;

            public ViewHolder(View v) {
                super(v);
//                initialiazation of our
                departmentCodes = (AutoCompleteTextView) v.findViewById(R.id.dept_codes);
                semesterCodes = (AutoCompleteTextView) v.findViewById(R.id.semester_codes);
                courseCreditUnits = (TextView) v.findViewById(R.id.course_credit_units);
                courseTitle  = (TextView) v.findViewById(R.id.course_title);
                okAndCheckBut = (Button) v.findViewById(R.id.ok_and_check);
                addNewCourseBut = (Button) v.findViewById(R.id.add_new_course_but);

                departmentCodes.setAdapter(deptCodesAdapter);
                semesterCodes.setAdapter(semesterCodesAdapter);

                departmentCodes.setThreshold(1);
                semesterCodes.setThreshold(1);

                okAndCheckBut.setVisibility(View.INVISIBLE);

                addNewCourseBut.setVisibility(View.INVISIBLE);



//                makinng sure text in edittext is what we want


            }
        }



        public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

            // Set numbers of Card in RecyclerView.
            private static final int LENGTH = 15;
            Context context;
            String[] dCodes = new String[15];
            String[] sCodes = new String[15];
            DatabaseReference mFirebaseDatabaseCourses;
            public static Course[] myCourses = new Course[15];
            FloatingActionButton subscribeBut;



            public ContentAdapter(final Context context, FloatingActionButton button) {
                this.context = context;
                subscribeBut = button;

            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.subscribe_cardview, parent, false);
                return  new ViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {

                //                what i intend doing here is once the course credit text view it clic
                // ked it should show a toast teling its position on the list1
                holder.setIsRecyclable(false);
                final int mPosition = position;
                final ViewHolder mHolder = holder;



//              when using textwatchers to accept input- activte the ok button and check, this button checks and activates add as new course button
                holder.departmentCodes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                holder.departmentCodes.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                            String dCodeStr = charSequence.toString();
                            dCodes[mPosition] = dCodeStr.toLowerCase();
                            //                        check if our user has selected course code previously
                            if(sCodes[mPosition] != null) {
//    activate ok button
                               holder.okAndCheckBut.setVisibility(View.VISIBLE);
                                holder.courseTitle.setText("Click Ok to add course");
                                holder.courseTitle.setTextColor(Color.BLACK);

                                if(holder.addNewCourseBut.getVisibility() == View.VISIBLE){
                                    holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                                }
                            }
                        } else {
                            holder.okAndCheckBut.setVisibility(View.INVISIBLE);
                            holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                            holder.courseTitle.setText("No course selected!!!");
                            holder.courseTitle.setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                holder.semesterCodes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                holder.semesterCodes.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                            String sCodeStr =charSequence.toString();
                            sCodes[mPosition] = sCodeStr;
                            if(dCodes[mPosition] != null){
//    activate ok button
                                holder.okAndCheckBut.setVisibility(View.VISIBLE);
                                holder.courseTitle.setText("Click Ok to add course");
                                holder.courseTitle.setTextColor(Color.BLACK);

                                if(holder.addNewCourseBut.getVisibility() == View.VISIBLE){
                                    holder.addNewCourseBut.setVisibility(View.INVISIBLE);

                                }
                            }
                        } else {
                            holder.okAndCheckBut.setVisibility(View.INVISIBLE);
                            holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                            holder.courseTitle.setText("No course selected!!!");
                            holder.courseTitle.setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });


                holder.departmentCodes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long rowId) {

                        String dCodeStr = (String) adapterView.getItemAtPosition(pos);
                        dCodes[mPosition] = dCodeStr.toLowerCase();
                        //                        check if our user has selected course code previously
                        if(sCodes[mPosition] != null){
//                            you can now query the firebase to check for the course
                            String courseName = dCodes[mPosition].toLowerCase() + sCodes[mPosition];
                            mHolder.courseTitle.setText("Checking ...");
                            mHolder.courseTitle.setTextColor(Color.BLUE);

                            mFirebaseDatabaseCourses = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                    .child(Constants.COURSES_NODE).child(courseName);
                            mFirebaseDatabaseCourses.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (subscribeBut.isEnabled() || holder.okAndCheckBut.getVisibility() == View.VISIBLE ||
                                            holder.addNewCourseBut.getVisibility() == View.VISIBLE){
                                        subscribeBut.setEnabled(false);
                                        holder.okAndCheckBut.setVisibility(View.INVISIBLE);
                                        holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                        Course thisCourse = new Course();

                                        mHolder.courseTitle.setText(courseName);
                                        mHolder.courseCreditUnits.setText(""+courseCreditHour);
                                        mHolder.courseTitle.setTextColor(Color.BLACK);
                                        mHolder.courseCreditUnits.setTextColor(Color.BLACK);
                                        for(int i = 0; i<myCourses.length; i++) {

                                            try {

                                                if(myCourses[i] != null){
                                                    Log.i(LOG_TAG, "instance of course returned true");
                                                    if(i != mPosition){
                                                        if (((myCourses[i].getCourse_code())).toString().equals(courseCode)) {

                                                            myCourses[mPosition] = null;
                                                            mHolder.courseTitle.setText(courseName + " is Already selected!!!");
                                                            mHolder.courseTitle.setTextColor(Color.RED);


                                                        }else{
                                                            thisCourse.setCourse_code(courseCode);
                                                            thisCourse.setName(courseName);
                                                            thisCourse.setCredit_hour(courseCreditHour);

                                                            myCourses[mPosition] = thisCourse;
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                        }

                                    }
                                    else {

                                        myCourses[mPosition] = null;
                                        mHolder.courseTitle.setText("Course not Available");
                                        mHolder.courseCreditUnits.setText("X");
                                        mHolder.courseTitle.setTextColor(Color.RED);
                                        mHolder.courseCreditUnits.setTextColor(Color.RED);

//                                        make the add new course button visible here where the user cann addd the course
                                        holder.addNewCourseBut.setVisibility(View.VISIBLE);
                                    }
                                    if (!subscribeBut.isEnabled()){
                                        subscribeBut.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                });
                holder.semesterCodes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                      String sCodeStr = (String) adapterView.getItemAtPosition(pos);
                        sCodes[mPosition] = sCodeStr;

                        if(dCodes[mPosition] != null){
//                            you can now query the firebase to check for the course
                            String courseName = dCodes[mPosition].toLowerCase() + sCodes[mPosition];
                            mHolder.courseTitle.setText("Checking ...");
                            mHolder.courseTitle.setTextColor(Color.BLUE);

                            mFirebaseDatabaseCourses = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                    .child(Constants.COURSES_NODE).child(courseName);
                            mFirebaseDatabaseCourses.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (subscribeBut.isEnabled() || holder.okAndCheckBut.getVisibility() == View.VISIBLE ||
                                            holder.addNewCourseBut.getVisibility() == View.VISIBLE){
                                        subscribeBut.setEnabled(false);
                                        holder.okAndCheckBut.setVisibility(View.INVISIBLE);
                                        holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);


                                        Course thisCourse = new Course();
                                        thisCourse.setCourse_code(courseCode);
                                        thisCourse.setName(courseName);
                                        thisCourse.setCredit_hour(courseCreditHour);

                                        myCourses[mPosition] = thisCourse;
                                        mHolder.courseTitle.setText(courseName);
                                        mHolder.courseCreditUnits.setText(""+courseCreditHour);

                                        mHolder.courseTitle.setTextColor(Color.BLACK);
                                        mHolder.courseCreditUnits.setTextColor(Color.BLACK);
                                        for(int i = 0; i< myCourses.length; i++){

                                            try {
                                                if(myCourses[i] != null){
                                                    Log.i(LOG_TAG, "instance of Course returned true");
                                                    if(i != mPosition){
                                                        if(((myCourses[i].getCourse_code())).toString().equals(courseCode)){

                                                            myCourses[mPosition] = null;
                                                            mHolder.courseTitle.setText(courseName + " is Already selected!!!");
                                                            mHolder.courseTitle.setTextColor(Color.RED);

                                                        }else{
                                                            thisCourse.setCourse_code(courseCode);
                                                            thisCourse.setName(courseName);
                                                            thisCourse.setCredit_hour(courseCreditHour);

                                                            myCourses[mPosition] = thisCourse;
                                                        }
                                                    }


                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                        }


                                    }
                                    else {

                                        myCourses[mPosition] = null;
                                        mHolder.courseTitle.setText("Course not Available");
                                        mHolder.courseCreditUnits.setText("X");
                                        mHolder.courseTitle.setTextColor(Color.RED);
                                        mHolder.courseCreditUnits.setTextColor(Color.RED);


//                                        make the add new course button visible here where the user cann addd the course
                                        holder.addNewCourseBut.setVisibility(View.VISIBLE);
                                    }

                                    if (!subscribeBut.isEnabled()){
                                        subscribeBut.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }


                    }
                });

                holder.okAndCheckBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.okAndCheckBut.setVisibility(View.INVISIBLE);
//                        check gocpurses and up ui apropritely

                        if(sCodes[mPosition] != null && dCodes[mPosition] != null){
//                            you can now query the firebase to check for the course
                            String courseName = dCodes[mPosition].toLowerCase() + sCodes[mPosition];
                            mHolder.courseTitle.setText("Checking ...");
                            mHolder.courseTitle.setTextColor(Color.BLUE);

                            mFirebaseDatabaseCourses = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                    .child(Constants.COURSES_NODE).child(courseName);
                            mFirebaseDatabaseCourses.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (subscribeBut.isEnabled() || holder.okAndCheckBut.getVisibility() == View.VISIBLE ||
                                            holder.addNewCourseBut.getVisibility() == View.VISIBLE){
                                        subscribeBut.setEnabled(false);
                                        holder.okAndCheckBut.setVisibility(View.INVISIBLE);
                                        holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                        Course thisCourse = new Course();

                                        mHolder.courseTitle.setText(courseName);
                                        mHolder.courseCreditUnits.setText(""+courseCreditHour);
                                        mHolder.courseTitle.setTextColor(Color.BLACK);
                                        mHolder.courseCreditUnits.setTextColor(Color.BLACK);

                                        ArrayList<String> courseCodesList = new ArrayList<String>();
                                        boolean courseCodeExists = false;
                                        for(Course eachCourse: myCourses){
                                            if(eachCourse != null){
                                                courseCodesList.add(eachCourse.getCourse_code());
                                            }
                                        }
                                        if(courseCodesList.size() != 0){
                                            for(int i = 0; i < courseCodesList.size(); i++){
                                                if(courseCode.equals(courseCodesList.get(i))){
//                                                    Toast.makeText(context, "selected course already exists", Toast.LENGTH_SHORT).show();
                                                    courseCodeExists = true;
                                                }
                                            }
                                        }
                                        else{
//                                            Toast.makeText(context, "selected course does not exist", Toast.LENGTH_SHORT).show();
                                            courseCodeExists = false;
                                        }

                                        if(courseCodeExists){
                                            myCourses[mPosition] = null;
                                            mHolder.courseTitle.setText(courseName + " is Already selected!!!");
                                            mHolder.courseTitle.setTextColor(Color.RED);
                                        }else{
                                            thisCourse.setCourse_code(courseCode);
                                            thisCourse.setName(courseName);
                                            thisCourse.setCredit_hour(courseCreditHour);

                                            myCourses[mPosition] = thisCourse;
                                        }


//                                        for(int i = 0; i<myCourses.length; i++) {
//
//                                            try {
//
//                                                if(myCourses[i] != null){
//                                                    Log.i(LOG_TAG, "instance of course returned true");
//                                                    if(i != mPosition){
//                                                        if (((myCourses[i].getCourse_code())).toString().equals(courseCode)) {
//
//                                                            myCourses[mPosition] = null;
//                                                            mHolder.courseTitle.setText(courseName + " is Already selected!!!");
//                                                            mHolder.courseTitle.setTextColor(Color.RED);
//
//
//                                                        }else{
//                                                            thisCourse.setCourse_code(courseCode);
//                                                            thisCourse.setName(courseName);
//                                                            thisCourse.setCredit_hour(courseCreditHour);
//
//                                                            myCourses[mPosition] = thisCourse;
//                                                        }
//                                                    }else{
//                                                        thisCourse.setCourse_code(courseCode);
//                                                        thisCourse.setName(courseName);
//                                                        thisCourse.setCredit_hour(courseCreditHour);
//
//                                                        myCourses[mPosition] = thisCourse;
//                                                    }
//
//
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//
//
//                                        }

                                    }
                                    else {

                                        myCourses[mPosition] = null;
                                        mHolder.courseTitle.setText("Course not Available");
                                        mHolder.courseCreditUnits.setText("X");
                                        mHolder.courseTitle.setTextColor(Color.RED);
                                        mHolder.courseCreditUnits.setTextColor(Color.RED);

//                                        make the add new course button visible here where the user cann addd the course
                                        holder.addNewCourseBut.setVisibility(View.VISIBLE);
                                    }
                                    if (!subscribeBut.isEnabled()){
                                        subscribeBut.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                });

                holder.addNewCourseBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        pop up dialog at this position

                        holder.addNewCourseBut.setVisibility(View.INVISIBLE);
                        final Dialog dialog = new Dialog(view.getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.add_resources_popup_dialog);
                        dialog.setContentView(R.layout.dialog_add_course_to_database);
                        dialog.show();

                        final TextView courseCodeTextView = (TextView) dialog.findViewById(R.id.new_coure_code_textview);
                        final EditText courseNameEditText = (EditText) dialog.findViewById(R.id.new_course_name_edit_text);
                        final EditText courseCreditUnitsEditText = (EditText) dialog.findViewById(R.id.new_course_creditunit_edit_text);

                        final ImageButton addNewCourse = (ImageButton) dialog.findViewById(R.id.add_new_course);

                        courseCodeTextView.setText((holder.departmentCodes.getText().toString()).toLowerCase() + holder.semesterCodes.getText().toString());

                        addNewCourse.setEnabled(false);

                        courseNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(65)});
                        courseNameEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (charSequence.toString().trim().length() > 0) {
                                    if(!addNewCourse.isEnabled()){
                                        addNewCourse.setEnabled(true);
                                    }

                                } else {

                                    addNewCourse.setEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });

                        courseCreditUnitsEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                        courseCreditUnitsEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                if (charSequence.toString().trim().length() == 1) {
                                    if(!addNewCourse.isEnabled()){
                                        addNewCourse.setEnabled(true);
                                    }
                                } else {
                                    addNewCourse.setEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });

                        addNewCourse.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(courseCreditUnitsEditText.getText().toString().isEmpty()){
                                    Snackbar.make(holder.courseCreditUnits, "Credit Units cannot be empty", Snackbar.LENGTH_SHORT).show();
                                }else {
                                    NewCourse newCourse = new NewCourse();
                                    newCourse.setCredit_hour(Integer.parseInt(courseCreditUnitsEditText.getText().toString()));
                                    newCourse.setName(courseNameEditText.getText().toString());
//add course to firebase database
                                    mFirebaseDatabaseCourses = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                            .child(Constants.COURSES_NODE);
                                    mFirebaseDatabaseCourses.child(courseCodeTextView.getText().toString()).setValue(newCourse);
                                    dialog.cancel();
                                }
                            }
                        });


                    }
                });

            }

            @Override
            public int getItemCount() {
                return LENGTH;
            }
        }

    public void onStart(){
        super.onStart();

        if (mFirebaseUser != null) {
            Log.i(LOG_TAG, "about to load currently subscribe for resources to the ui");
//            TODO: use firebase's ecyclerview to do this - PS: i did notuse this implementation pattern again
            loadCurrentlySubscribedForResources(mFirebaseUser.getUid());
        }
        else{
            Log.i(LOG_TAG, "Maintaining the status quo for now as user is a new user");
        }



        }

    public void loadCurrentlySubscribedForResources(String userID){

    }

    public void subscribe(){
        Log.i(LOG_TAG, "subscribe button has been clicked");

        userAppPref = getSharedPreferences(getString(R.string.user_key_preference), Context.MODE_PRIVATE);

        String userUniqueId = userAppPref.getString("userID", null);

//                        if new user userUniqueId will be null and tis was started from the main activity
        if (userUniqueId == null){
//                            we should authenticate our user, setup user at respective nodes subscribe him to courses set the shared preference string and start main activity
//                            remember to call finish on this activity
            createNewUserAndStart();

        }


    }

    public void updateUserCurrentlySubscribedResources(String userID){
        Log.i(LOG_TAG, "entered update currently subscribed for resources");

    }

    public void createNewUserAndStart(){
        Log.i(LOG_TAG, "enterd login new user");

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);


            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                showProgressDialog();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(LOG_TAG, "Google Sign In failed.");
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        Toast.makeText(this, "Entered on back pressed method ",Toast.LENGTH_SHORT).show();
////        if user school pref is still null finish the activity
//        String userSchoolPreference = MainActivity.userSchoolAppPref.getString("userSchool", null);
//        if(userSchoolPreference == null){
//            finish();
//        }
//    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            hideProgressDialog();
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SubscribeForResourcesActivity.this, "Authentication failed." + task.getException() + "\nPosibble Solution: Enable Background Service for Google Play Services ",
                                    Toast.LENGTH_SHORT).show();
                        } else {

//                            TODO: create new user nodes at users and at the subscribe node.
                            mFirebaseAuth = FirebaseAuth.getInstance();
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            final String userUID = mFirebaseUser.getUid();
                            final String userEmail = mFirebaseUser.getEmail();
                            final User newUser = new User(userUID, userEmail);

                            SharedPreferences.Editor editor = userAppPref.edit();
                            editor.putString("userID", userUID);
                            editor.commit();

                            mFirebaseDatabaseUsers = FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                    .child(Constants.USERS_NODE);
                            mFirebaseDatabaseUsersResources = FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.SCHOOLS_NODE).child(SubscribeForResourcesActivity.userSchoolPrefStr)
                                    .child(Constants.USERS_SUBSCRIBED_RESOURCES_NODE);
                            mFirebaseDatabaseUsersResources.child(userUID).setValue(" ");

                            mFirebaseDatabaseUsers.child(userUID).push().setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFirebaseDatabaseUsers.child(userUID).setValue(newUser);
                                        ArrayList<String> courseCodes  = new ArrayList<String>();
                                        ArrayList<Course> courses = new ArrayList<Course>();

                                        for(Course eachCourse: ContentAdapter.myCourses){
                                            if(eachCourse != null){
                                                if(courseCodes.contains(eachCourse.getCourse_code())){
//                                                don't add eachcourse to courses

                                                }else{
//                                                add
                                                    courses.add(eachCourse);
                                                }
                                            }

                                        }

                                        for (int i = 0; i<courses.size(); i++){
                                            if(courses.get(i) != null){
                                                mFirebaseDatabaseUsersResources.child(userUID).push().setValue(courses.get(i));
                                            }
                                        }

                                        hideProgressDialog();
                                        startActivity(new Intent(SubscribeForResourcesActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            });

                        }
                    }
                });
    }

    public void checkCourseExists(String course){
//        TODO: check course selected in database
//        TODO: add course to list of user courses
//        TODO: set the text view to their appropriate references

    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Starting Up ");
            mProgressDialog.setIndeterminate(true);
        }
        else{
            mProgressDialog.setMessage("Starting Up ");
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }



    }

