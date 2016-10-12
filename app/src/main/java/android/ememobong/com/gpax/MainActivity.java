package android.ememobong.com.gpax;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.ememobong.com.gpax.background.CommonConsatants;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class
MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    public static final String LOG_TAG = MainActivity.class.toString();

//SharedPreferece of the app
    public static SharedPreferences userSchoolAppPref;
    public static SharedPreferences userAppPref;
    public SharedPreferences timePref;
    public static String userSchoolName = " ";

    Intent notificationIntent;
    private NotificationManager mNotificationManager;

//    String  to hold the user courses
    List<String> userCourses;

    private DrawerLayout mDrawerLayout;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    GoogleApiClient mGoogleApiClient;

    public static boolean calledAlready = false;

    private DatabaseReference mFirebaseDatabaseReference;

    public static String courseNameToUpload = " ";
    public static String commentToUpload = " ";
//    variable for our dialog to work with our autocomplete text bla bla bla
    String courseHolderText = " ";
    String codeHolderText = " ";
    String userUniqueId = " ";

//    chooser intent for upload
    Intent chooserIntent;
    Uri mDownloadUrl = null;
    Uri mAttachDowloadUrl;
    Uri attachmentUri = null;
    private StorageReference mStorageRef;
    ProgressDialog mProgressDialog;
    TextView attachmentTextView;
    public String otherComment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!calledAlready){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(CommonConsatants.NOTIFICATION_ID);

// Initialize Firebase Storage Ref
        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // [END get_storage_ref]
//        hoping i can sync data with this code

//        setting the shared pref for the app userAppPref hold the user id of the user for the session that the user logged in
        userSchoolAppPref = getSharedPreferences(getString(R.string.user_school_preference), Context.MODE_PRIVATE);
        userAppPref = getSharedPreferences(getString(R.string.user_key_preference), Context.MODE_PRIVATE);

        userSchoolName = userSchoolAppPref.getString("userSchool", null);
        userUniqueId = userAppPref.getString("userID", null);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // Create Navigation drawer and inlfate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setChecked(true);

                        // TODO: handle navigation
                        int id  = menuItem.getItemId();


                        if(id == R.id.news_nav){
                            Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);
                            startActivity(newsIntent);
                            newsIntent.putExtra(ResourcesCardFragment.COURSE_CODE, "gst111");
                            finish();
                        }
                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Adding Floating Action Button to bottom right of main view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.add_resources_popup_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


                final RadioButton pastQuestionRadBut = (RadioButton) dialog.findViewById(R.id.past_question_radio_but);
                pastQuestionRadBut.setChecked(true);

                final RadioButton othersRadBut = (RadioButton) dialog.findViewById(R.id.other_radio_but);
                othersRadBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        popUpOthersDialog();
                    }
                });

                final RadioButton newsRadBut = (RadioButton) dialog.findViewById(R.id.news_radio_but);
                newsRadBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        popUpNewsDialog();
                    }
                });


                final EditText commentEditText = (EditText) dialog.findViewById(R.id.comment_edit_text);
                final AutoCompleteTextView thisResourceCourse =(AutoCompleteTextView) dialog.findViewById(R.id.resource_dept_codes);
                final AutoCompleteTextView thisResourceCode =(AutoCompleteTextView) dialog.findViewById(R.id.resource_semester_codes);
                final TextView resourceCourseDetailsText = (TextView) dialog.findViewById(R.id.resource_course_textview);
                final ImageButton resourceLaunchCameraButton = (ImageButton) dialog.findViewById(R.id.resource_camera_image_button);
                final Button okAndCheck = (Button) dialog.findViewById(R.id.add_res_ok_and_check);

                okAndCheck.setVisibility(View.INVISIBLE);
                resourceLaunchCameraButton.setEnabled(false);
                resourceLaunchCameraButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

                ArrayAdapter<String> deptCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.departmental_codes));
                ArrayAdapter<String> semesterCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.semester_codes));

                thisResourceCourse.setAdapter(deptCodesAdapter);
                thisResourceCode.setAdapter(semesterCodesAdapter);

                thisResourceCourse.setThreshold(1);
                thisResourceCode.setThreshold(1);

                thisResourceCourse.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                thisResourceCourse.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                            courseHolderText = charSequence.toString();

                            //                        check if our user has selected course code previously
                            if(!codeHolderText.equals(" ")) {
//    activate ok button
                                okAndCheck.setVisibility(View.VISIBLE);
                                resourceCourseDetailsText.setText("Click Ok to add course");
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                            }
                        } else {
                            courseHolderText = " ";
                            okAndCheck.setVisibility(View.INVISIBLE);
                            resourceCourseDetailsText.setText("No course selected!!!");
                            resourceCourseDetailsText.setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                thisResourceCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                thisResourceCode.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                            codeHolderText = charSequence.toString();

                            //                        check if our user has selected course code previously
                            if(!courseHolderText.equals(" ")) {
//    activate ok button
                                okAndCheck.setVisibility(View.VISIBLE);
                                resourceCourseDetailsText.setText("Click Ok to add course");
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                            }
                        } else {
                            codeHolderText = " ";
                            okAndCheck.setVisibility(View.INVISIBLE);
                            resourceCourseDetailsText.setText("No course selected!!!");
                            resourceCourseDetailsText.setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                okAndCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        okAndCheck.setVisibility(View.INVISIBLE);
                        if (!codeHolderText.equals(" ")){
                            final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                            resourceCourseDetailsText.setText("Checking ... ");
                            resourceCourseDetailsText.setTextColor(Color.BLUE);

                            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                                    .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                            mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (resourceLaunchCameraButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE){
                                        resourceLaunchCameraButton.setEnabled(false);
                                        resourceLaunchCameraButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                        okAndCheck.setVisibility(View.INVISIBLE);

                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                        MainActivity.courseNameToUpload = courseCode;

                                        resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                                    }
                                    else {

                                        resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                        resourceCourseDetailsText.setTextColor(Color.RED);
                                    }
                                    if (!resourceLaunchCameraButton.isEnabled()){
                                        resourceLaunchCameraButton.setEnabled(true);
                                        resourceLaunchCameraButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);


                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }
                });



//                set on click listeners to the to the autocomplete view authenticate courses and update coursedetails text view and enable camera button
                thisResourceCourse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        courseHolderText = (String) adapterView.getItemAtPosition(pos);
                        if (!codeHolderText.equals(" ")){
                            final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                            resourceCourseDetailsText.setText("Checking ... ");
                            resourceCourseDetailsText.setTextColor(Color.BLUE);

                            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                                    .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                            mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (resourceLaunchCameraButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE){
                                        resourceLaunchCameraButton.setEnabled(false);
                                        resourceLaunchCameraButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                        okAndCheck.setVisibility(View.INVISIBLE);


                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                        MainActivity.courseNameToUpload = courseCode;

                                        resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                                    }
                                    else {

                                       resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                        resourceCourseDetailsText.setTextColor(Color.RED);
                                    }
                                    if (!resourceLaunchCameraButton.isEnabled()){
                                        resourceLaunchCameraButton.setEnabled(true);
                                        resourceLaunchCameraButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });

                thisResourceCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        codeHolderText = (String) adapterView.getItemAtPosition(pos);
                        if (!courseHolderText.equals(" ")){
                            final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                            resourceCourseDetailsText.setText("Checking ... ");
                            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("courses").child(queryCourseCode);
                            mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (resourceLaunchCameraButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE){
                                        resourceLaunchCameraButton.setEnabled(false);
                                        resourceLaunchCameraButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                        okAndCheck.setVisibility(View.INVISIBLE);
                                    }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                                    if(dataSnapshot.getChildrenCount() != 0.00){
                                        String courseCode = dataSnapshot.getKey();
                                        String courseName = dataSnapshot.child("name").getValue(String.class);
                                        int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                        MainActivity.courseNameToUpload = courseCode;
                                        resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );

                                        if (!resourceLaunchCameraButton.isEnabled()){
                                            resourceLaunchCameraButton.setEnabled(true);
                                            resourceLaunchCameraButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                                        }

                                    }
                                    else {

                                        resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                        resourceCourseDetailsText.setTextColor(Color.RED);
                                        resourceLaunchCameraButton.setEnabled(false);
                                        resourceLaunchCameraButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });

                resourceLaunchCameraButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        start the camera capture activity and pass appropriate intent extras
                        MainActivity.commentToUpload = commentEditText.getText().toString();
                        Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
                        startActivity(intent);
                    }
                });

//                dialog.show();
            }
        });
    }

    public void popUpOthersDialog(){

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_resources_popup_dialog);
        dialog.setContentView(R.layout.add_others_popup_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        courseHolderText = " ";
        codeHolderText = " ";

        final RadioButton urlRadBut = (RadioButton) dialog.findViewById(R.id.url_link_radio_but);
        final RadioButton chooserRadBut = (RadioButton) dialog.findViewById(R.id.choose_file_radio_but);
        final EditText commentEditText = (EditText) dialog.findViewById(R.id.other_comment_edit_text);
        final EditText linkEditText = (EditText) dialog.findViewById(R.id.post_link_edit_text);
        final AutoCompleteTextView thisResourceCourse =(AutoCompleteTextView) dialog.findViewById(R.id.other_resource_dept_codes);
        final AutoCompleteTextView thisResourceCode =(AutoCompleteTextView) dialog.findViewById(R.id.other_resource_semester_codes);
        final TextView resourceCourseDetailsText = (TextView) dialog.findViewById(R.id.other_resource_course_textview);
        final ImageButton resourceLaunchChooserButton = (ImageButton) dialog.findViewById(R.id.resource_chooser_button);
        final Button okAndCheck = (Button) dialog.findViewById(R.id.others_ok_and_check_but);
        final ImageButton postLinkBut = (ImageButton) dialog.findViewById(R.id.ok_and_post_link);
        postLinkBut.setVisibility(View.INVISIBLE);
        linkEditText.setVisibility(View.INVISIBLE);

        postLinkBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(commentEditText.getText().toString().isEmpty() ){
                    Snackbar.make(view, "Please make a comment to continue ", Snackbar.LENGTH_SHORT).show();
                }else if(linkEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "Link not valid", Snackbar.LENGTH_SHORT).show();
                }else{
                    String validUrl = linkEditText.getText().toString();

                    try {
                        Uri.parse(validUrl);
                        uploadLink(commentEditText.getText().toString(), linkEditText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar.make(view, "Link not valid", Snackbar.LENGTH_SHORT).show();
                    }

                }
                dialog.cancel();

            }
        });

        resourceLaunchChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(commentEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "Please make a comment to continue ", Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(view, "resource launch clicked", Snackbar.LENGTH_SHORT).show();
                    chooseFileAndUpload(commentEditText.getText().toString());
                }
                dialog.cancel();
            }
        });

        urlRadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resourceLaunchChooserButton.setVisibility(View.INVISIBLE);
                linkEditText.setVisibility(View.VISIBLE);
                postLinkBut.setVisibility(View.VISIBLE);
            }
        });

        chooserRadBut.setChecked(true);
        chooserRadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resourceLaunchChooserButton.setVisibility(View.VISIBLE);
                linkEditText.setVisibility(View.INVISIBLE);
                postLinkBut.setVisibility(View.INVISIBLE);
            }
        });

        okAndCheck.setVisibility(View.INVISIBLE);
        resourceLaunchChooserButton.setEnabled(false);
        resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);


        ArrayAdapter<String> deptCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.departmental_codes));
        ArrayAdapter<String> semesterCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.semester_codes));

        thisResourceCourse.setAdapter(deptCodesAdapter);
        thisResourceCode.setAdapter(semesterCodesAdapter);

        thisResourceCourse.setThreshold(1);
        thisResourceCode.setThreshold(1);

        thisResourceCourse.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        thisResourceCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                    courseHolderText = charSequence.toString();

                    //                        check if our user has selected course code previously
                    if(!codeHolderText.equals(" ")) {
//    activate ok button
                        okAndCheck.setVisibility(View.VISIBLE);
                        resourceCourseDetailsText.setText("Click Ok to add course");
                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                    }
                } else {
                    courseHolderText = " ";
                    okAndCheck.setVisibility(View.INVISIBLE);
                    resourceCourseDetailsText.setText("No course selected!!!");
                    resourceCourseDetailsText.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        thisResourceCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        thisResourceCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                    codeHolderText = charSequence.toString();

                    //                        check if our user has selected course code previously
                    if(!courseHolderText.equals(" ")) {
//    activate ok button
                        okAndCheck.setVisibility(View.VISIBLE);
                        resourceCourseDetailsText.setText("Click Ok to add course");
                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                    }
                } else {
                    codeHolderText = " ";
                    okAndCheck.setVisibility(View.INVISIBLE);
                    resourceCourseDetailsText.setText("No course selected!!!");
                    resourceCourseDetailsText.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        okAndCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okAndCheck.setVisibility(View.INVISIBLE);
                if (!codeHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    resourceCourseDetailsText.setTextColor(Color.BLUE);

                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                            .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (resourceLaunchChooserButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE || postLinkBut.isEnabled()){
                                resourceLaunchChooserButton.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                okAndCheck.setVisibility(View.INVISIBLE);
                                postLinkBut.setEnabled(false);

                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;

                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                                if (!resourceLaunchChooserButton.isEnabled() || !postLinkBut.isEnabled()){
                                    resourceLaunchChooserButton.setEnabled(true);
                                    resourceLaunchChooserButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                                    postLinkBut.setEnabled(true);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                resourceLaunchChooserButton.setEnabled(false);
                                postLinkBut.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });



//                set on click listeners to the to the autocomplete view authenticate courses and update coursedetails text view and enable camera button
        thisResourceCourse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                courseHolderText = (String) adapterView.getItemAtPosition(pos);
                if (!codeHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    resourceCourseDetailsText.setTextColor(Color.BLUE);

                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                            .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (resourceLaunchChooserButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE || postLinkBut.isEnabled()){
                                resourceLaunchChooserButton.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                okAndCheck.setVisibility(View.INVISIBLE);
                                postLinkBut.setEnabled(false);


                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;

                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                                if (!resourceLaunchChooserButton.isEnabled() || !postLinkBut.isEnabled()){
                                    resourceLaunchChooserButton.setEnabled(true);
                                    resourceLaunchChooserButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                                    postLinkBut.setEnabled(true);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                resourceLaunchChooserButton.setEnabled(false);
                                postLinkBut.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        thisResourceCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                codeHolderText = (String) adapterView.getItemAtPosition(pos);
                if (!courseHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("courses").child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (resourceLaunchChooserButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE || postLinkBut.isEnabled()){
                                resourceLaunchChooserButton.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                okAndCheck.setVisibility(View.INVISIBLE);
                                postLinkBut.setEnabled(false);

                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;
                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );

                                if (!resourceLaunchChooserButton.isEnabled() || !postLinkBut.isEnabled()){
                                    resourceLaunchChooserButton.setEnabled(true);
                                    resourceLaunchChooserButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                                    postLinkBut.setEnabled(true);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                resourceLaunchChooserButton.setEnabled(false);
                                postLinkBut.setEnabled(false);
                                resourceLaunchChooserButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });



    }

    public void popUpNewsDialog(){

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_resources_popup_dialog);
        dialog.setContentView(R.layout.add_course_news_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        courseHolderText = " ";
        codeHolderText = " ";


        attachmentTextView = (TextView) dialog.findViewById(R.id.attachment_info_news);
        attachmentTextView.setVisibility(View.INVISIBLE);
        final EditText headingEditText = (EditText) dialog.findViewById(R.id.news_heading_edit_text);
        final EditText newsBodyEditText = (EditText) dialog.findViewById(R.id.news_body_edit_text);
        final AutoCompleteTextView thisResourceCourse =(AutoCompleteTextView) dialog.findViewById(R.id.news_resource_dept_codes);
        final AutoCompleteTextView thisResourceCode =(AutoCompleteTextView) dialog.findViewById(R.id.news_resource_semester_codes);
        final TextView resourceCourseDetailsText = (TextView) dialog.findViewById(R.id.news_resource_course_textview);
        final ImageButton postNewsButton = (ImageButton) dialog.findViewById(R.id.post_news_button);
        final ImageButton attachmentButton = (ImageButton) dialog.findViewById(R.id.attachment_news_button);
        final Button okAndCheck = (Button) dialog.findViewById(R.id.news_ok_and_check_but);

        okAndCheck.setVisibility(View.INVISIBLE);

        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseMultipleImages();
            }
        });

        postNewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(headingEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "No Headng for news", Snackbar.LENGTH_SHORT).show();
                }
                else if(newsBodyEditText.getText().toString().isEmpty()){
                    Snackbar.make(view, "No body for news", Snackbar.LENGTH_SHORT).show();
                }
                else{
                    postNewsToDatabase(headingEditText.getText().toString(), newsBodyEditText.getText().toString());
                    dialog.cancel();

                }
            }
        });


        ArrayAdapter<String> deptCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.departmental_codes));
        ArrayAdapter<String> semesterCodesAdapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.semester_codes));

        thisResourceCourse.setAdapter(deptCodesAdapter);
        thisResourceCode.setAdapter(semesterCodesAdapter);

        thisResourceCourse.setThreshold(1);
        thisResourceCode.setThreshold(1);

        headingEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        headingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 99) {

                    Snackbar.make(headingEditText,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        newsBodyEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        newsBodyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 300) {

                    Snackbar.make(newsBodyEditText,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        thisResourceCourse.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        thisResourceCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                    courseHolderText = charSequence.toString();

                    //                        check if our user has selected course code previously
                    if(!codeHolderText.equals(" ")) {
//    activate ok button
                        okAndCheck.setVisibility(View.VISIBLE);
                        resourceCourseDetailsText.setText("Click Ok to add course");
                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                    }
                } else {
                    courseHolderText = " ";
                    okAndCheck.setVisibility(View.INVISIBLE);
                    resourceCourseDetailsText.setText("No course selected!!!");
                    resourceCourseDetailsText.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        thisResourceCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        thisResourceCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 3) {
//                            on text equals 3 can you give focus to the next atv with the other functionality
                    codeHolderText = charSequence.toString();

                    //                        check if our user has selected course code previously
                    if(!courseHolderText.equals(" ")) {
//    activate ok button
                        okAndCheck.setVisibility(View.VISIBLE);
                        resourceCourseDetailsText.setText("Click Ok to add course");
                        resourceCourseDetailsText.setTextColor(Color.BLACK);

                    }
                } else {
                    codeHolderText = " ";
                    okAndCheck.setVisibility(View.INVISIBLE);
                    resourceCourseDetailsText.setText("No course selected!!!");
                    resourceCourseDetailsText.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        okAndCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okAndCheck.setVisibility(View.INVISIBLE);
                if (!codeHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    resourceCourseDetailsText.setTextColor(Color.BLUE);

                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                            .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (postNewsButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE ){
                                postNewsButton.setEnabled(false);
                                okAndCheck.setVisibility(View.INVISIBLE);

                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;

                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                                if (!postNewsButton.isEnabled()){
                                    postNewsButton.setEnabled(true);
                                    postNewsButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                postNewsButton.setEnabled(false);
                                postNewsButton
                                        .setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });



//                set on click listeners to the to the autocomplete view authenticate courses and update coursedetails text view and enable camera button
        thisResourceCourse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                courseHolderText = (String) adapterView.getItemAtPosition(pos);
                if (!codeHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    resourceCourseDetailsText.setTextColor(Color.BLUE);

                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE)
                            .child(MainActivity.userSchoolName).child(Constants.COURSES_NODE).child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (postNewsButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE ){
                                postNewsButton.setEnabled(false);
                                okAndCheck.setVisibility(View.INVISIBLE);

                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;

                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );
                                resourceCourseDetailsText.setTextColor(Color.BLACK);

                                if (!postNewsButton.isEnabled()){
                                    postNewsButton.setEnabled(true);
                                    postNewsButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                postNewsButton.setEnabled(false);
                                postNewsButton
                                        .setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        thisResourceCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                codeHolderText = (String) adapterView.getItemAtPosition(pos);
                if (!courseHolderText.equals(" ")){
                    final String queryCourseCode = courseHolderText.toLowerCase() + codeHolderText ;
//                            make query to seee if this course exists and update UI accordingly

                    resourceCourseDetailsText.setText("Checking ... ");
                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("courses").child(queryCourseCode);
                    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (postNewsButton.isEnabled() || okAndCheck.getVisibility() == View.VISIBLE ){
                                postNewsButton.setEnabled(false);
                                okAndCheck.setVisibility(View.INVISIBLE);

                            }
//                                    add the course retured ifit exist to the courses array and set the views respectively
                            if(dataSnapshot.getChildrenCount() != 0.00){
                                String courseCode = dataSnapshot.getKey();
                                String courseName = dataSnapshot.child("name").getValue(String.class);
                                int courseCreditHour = dataSnapshot.child("credit_hour").getValue(Integer.class);

                                MainActivity.courseNameToUpload = courseCode;
                                resourceCourseDetailsText.setText(courseName + " - " + courseCreditHour );

                                if (!postNewsButton.isEnabled()){
                                    postNewsButton.setEnabled(true);
                                    postNewsButton.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

                                }

                            }
                            else {

                                resourceCourseDetailsText.setText("Selected Course Not Available!!!");
                                resourceCourseDetailsText.setTextColor(Color.RED);
                                postNewsButton.setEnabled(false);
                                postNewsButton
                                        .setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }

    private void chooseMultipleImages(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        Intent chooserIntent = Intent.createChooser(intent, "Choose image" +
                " to attach");

        startActivityForResult(chooserIntent, 101);
    }

    private void postNewsToDatabase(final String heading, final String newsBody){

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        if(attachmentUri != null){
        final StorageReference photoRef = mStorageRef.child(userSchoolName).child(Constants.COURSE_ATTACHMENT_FILES).child(MainActivity.courseNameToUpload)
                .child(System.currentTimeMillis() + attachmentUri.getLastPathSegment());
        // [END get_child_ref]
// Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]

            photoRef.putFile(attachmentUri)
                    .addOnSuccessListener(this,
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Upload succeeded

                                    // Get the public download URL
                                    mAttachDowloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                                    mProgressDialog.setMessage("Posting ... ");
                                    long timeStamp = System.currentTimeMillis() * -1;

                                    String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                                            .format(new Date());


                                    CustomNewsResource newCourseResource = new CustomNewsResource();
                                    CustomHashMap hashy = new CustomHashMap();
                                    hashy.setString("");
                                    newCourseResource.setUserUID(mFirebaseUser.getUid());
                                    newCourseResource.setHeading(heading);
                                    newCourseResource.setComments(hashy);
                                    newCourseResource.setDateOfPost(dateStamp);
                                    newCourseResource.setLikes(" ");
                                    newCourseResource.setTimeOfPost(timeStamp);
                                    newCourseResource.setNewsBody(newsBody);
                                    if (attachmentUri != null) {
                                        newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
                                    } else {
                                        newCourseResource.setAttachmentLinks("");
                                    }

//                                we would have to write this url to data base

                                    final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.SCHOOLS_NODE).child(userSchoolName).child(Constants.COURSE_NEWS)
                                            .child(MainActivity.courseNameToUpload).push();
                                    nodeKeyReference.setValue(newCourseResource).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
//                                        push likes and comment

                                            nodeKeyReference.child("comments").push().setValue("");
                                            nodeKeyReference.child("comments").child("string").setValue(null);

                                        }
                                    });

                                    hideProgressDialog();

                                    // [END_EXCLUDE]
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Upload failed

                            mDownloadUrl = null;

                            // [START_EXCLUDE]
                            hideProgressDialog();
                            Toast.makeText(MainActivity.this, "Error: upload failed",
                                    Toast.LENGTH_SHORT).show();
                            // [END_EXCLUDE]
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progresss = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Double d = progresss;
                    mProgressDialog.setMessage("Loading .... " + d.intValue() + "%");
                    if (progresss == 100.00) {
                        mProgressDialog.setMessage("Finishing");
                    }
                }
            });

        }else{
            showProgressDialog();
            mProgressDialog.setMessage("Posting ... ");
            long timeStamp = System.currentTimeMillis() * -1;

            String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                    .format(new Date());


            CustomNewsResource newCourseResource = new CustomNewsResource();
            CustomHashMap hashy = new CustomHashMap();
            hashy.setString("");
            newCourseResource.setUserUID(mFirebaseUser.getUid());
            newCourseResource.setHeading(heading);
            newCourseResource.setComments(hashy);
            newCourseResource.setDateOfPost(dateStamp);
            newCourseResource.setLikes(" ");
            newCourseResource.setTimeOfPost(timeStamp);
            newCourseResource.setNewsBody(newsBody);
            if (attachmentUri != null) {
                newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
            } else {
                newCourseResource.setAttachmentLinks("");
            }

//                                we would have to write this url to data base

            final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.SCHOOLS_NODE).child(userSchoolName).child(Constants.COURSE_NEWS)
                    .child(MainActivity.courseNameToUpload).push();
            nodeKeyReference.setValue(newCourseResource).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                                        push likes and comment

                    nodeKeyReference.child("comments").push().setValue("");
                    nodeKeyReference.child("comments").child("string").setValue(null);

                }
            });

            hideProgressDialog();
        }
    }
    
    public void uploadLink(final String comment, String link){

        showProgressDialog();
        mProgressDialog.setMessage("Posting ... ");
        long timeStamp = System.currentTimeMillis() * -1;

        String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                .format(new Date());


        CustomOtherResource newOtherResources = new CustomOtherResource();
        CustomHashMap hashy = new CustomHashMap();
        hashy.setString("");
        newOtherResources.setUserUID(mFirebaseUser.getUid());
        newOtherResources.setFileName(" ");
        newOtherResources.setComments(hashy);
        newOtherResources.setDateOfPost(dateStamp);
        newOtherResources.setLikes(hashy);
        newOtherResources.setTimeOfPost(timeStamp);
        newOtherResources.setFileUrl(link);
//                                we would have to write this url to data base

        final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.SCHOOLS_NODE).child(userSchoolName).child(Constants.COURSE_OTHER_RESOURCES_NODE)
                .child(MainActivity.courseNameToUpload).push();
        nodeKeyReference.setValue(newOtherResources).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                                        push likes and comment
                nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).setValue("1");
                nodeKeyReference.child("likes").child("string").setValue(null);
                nodeKeyReference.child("comments").push().setValue(mFirebaseUser.getDisplayName() + ":" + comment);
                nodeKeyReference.child("comments").child("string").setValue(null);

            }
        });

        hideProgressDialog();
    }

    public void chooseFileAndUpload(String comment){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        otherComment = comment;
        intent.putExtra("comment", comment);

//        special intent for samsung phones
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("content_type","*/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sIntent.putExtra("comment",comment);

        if(getPackageManager().resolveActivity(sIntent, 0 ) != null){
            Toast.makeText(this, "You have a samsung device", Toast.LENGTH_SHORT).show();
            chooserIntent = Intent.createChooser(sIntent, "Choose file to upload");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});

        }else{
            chooserIntent = Intent.createChooser(intent, "Choose file to upload");

        }


        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Upload tip ")
                .setTitle("You need to have a File Manager Installed " + MainActivity.courseNameToUpload)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // we are currently working on a node so we can delete it
                        try {
                            startActivityForResult(chooserIntent, 100);
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "No suitable file manager was found ", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 100){
            if(resultCode == RESULT_OK){
                String comment = data.getStringExtra("comment");
                uploadFromUri(data.getData(), comment);
            }
        }
        if(requestCode == 101){
            if(resultCode == RESULT_OK){

                if(data != null){
                    attachmentUri = data.getData();
                    attachmentTextView.setText("" + attachmentUri.getLastPathSegment());
                    attachmentTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void uploadFromUri(final Uri uri, final String oTcomment){

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child(userSchoolName).child(Constants.COURSE_OTHER_RESOURCES_NODE).child(MainActivity.courseNameToUpload)
                .child(System.currentTimeMillis() + uri.getLastPathSegment());
        // [END get_child_ref]
// Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        photoRef.putFile(uri)
                .addOnSuccessListener(this,
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Upload succeeded

                                // Get the public download URL
                                mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                                long timeStamp = System.currentTimeMillis() * -1;

                                String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                                        .format(new Date());

                                CustomOtherResource newOtherResources = new CustomOtherResource();
                                CustomHashMap hashy = new CustomHashMap();
                                hashy.setString("");

                                newOtherResources.setUserUID(mFirebaseUser.getUid());
                                newOtherResources.setFileName(System.currentTimeMillis() + uri.getLastPathSegment());
                                newOtherResources.setComments(hashy);
                                newOtherResources.setDateOfPost(dateStamp);
                                newOtherResources.setLikes(hashy);
                                newOtherResources.setTimeOfPost(timeStamp);
                                newOtherResources.setFileUrl(mDownloadUrl.toString());
//                                we would have to write this url to data base

                                final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                                        .child(Constants.SCHOOLS_NODE).child(userSchoolName).child(Constants.COURSE_OTHER_RESOURCES_NODE)
                                        .child(MainActivity.courseNameToUpload).push();
                                nodeKeyReference.setValue(newOtherResources).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
//                                        push likes and comment
                                        nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).setValue("1");
                                        nodeKeyReference.child("likes").child("string").setValue(null);

                                        nodeKeyReference.child("comments").push().setValue(mFirebaseUser.getDisplayName() + ":" + otherComment);
                                        nodeKeyReference.child("comments").child("string").setValue(null);
                                    }
                                });


                                // [START_EXCLUDE]
                                hideProgressDialog();

                                // [END_EXCLUDE]
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progresss = (100.0 * taskSnapshot.getBytesTransferred())/ taskSnapshot.getTotalByteCount();
                Double d = progresss;
                mProgressDialog.setMessage("Loading .... " + d.intValue() + "%");
                if(progresss == 100.00){
                    mProgressDialog.setMessage("Finishing");
                }
            }
        });

    }
    // [END upload_from_uri]

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
        // Add Fragments to Tabs
        private void setupViewPager(ViewPager viewPager) {
            Adapter adapter = new Adapter(getSupportFragmentManager());
            adapter.addFragment(new ResourcesCardFragment(), "Resources");
            adapter.addFragment(new GpTileFragment(), "GP");

            viewPager.setAdapter(adapter);
        }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.

            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
        // Initialize Firebase Auth
//        Log.i(LOG_TAG, "Entered onStart()to check if user is using the app for the first time: ");

        userSchoolName = userSchoolAppPref.getString("userSchool", null);
        userUniqueId = userAppPref.getString("userID", null);
//        Log.i(LOG_TAG,"THis is he user unique id for the app " + userUniqueId);
//

        if (userUniqueId == null || userSchoolName == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SubscribeForResourcesActivity.class));
            finish();
            return;
        }
        else{
//            this activity was started from notifications
            notificationIntent = getIntent();
            if(notificationIntent != null){
                if(notificationIntent.hasExtra(ResourcesCardFragment.COURSE_CODE)){
                    Intent newIntent = new Intent(this, ViewCourseResourcesActivity.class);
                    newIntent.putExtra(ResourcesCardFragment.COURSE_CODE, notificationIntent.getStringExtra(ResourcesCardFragment.COURSE_CODE));

                    startActivity(newIntent);
                    finish();
                }
                if(notificationIntent.hasExtra("timeStamp")){
//                    update the timePef to user current time
                    timePref = getSharedPreferences(getString(R.string.notif_time_stamp), Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = timePref.edit();
                    editor.putLong("timeStamp", System.currentTimeMillis());
                    editor.commit();
                }
            }
        }

    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
//                mFirebaseAuth.signOut();
//                Log.i(LOG_TAG, "This is how a GoogleApiClient looks like and we are about signing this out " + mGoogleApiClient);
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SubscribeForResourcesActivity.class));
                finish();
                return true;
            } else if (id == android.R.id.home) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return super.onOptionsItemSelected(item);
        }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    }






