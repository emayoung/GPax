package android.ememobong.com.gpax;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    TextView attachmentTextView;

    private ProgressDialog mProgressDialog;

    private String headingText = "";
    private String newsBodyText = "";
    private Uri attachmentUri = null;
    Uri mAttachDowloadUrl;
    Uri mDownloadUrl = null;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.news_toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        ViewPager viewPager = (ViewPager) findViewById(R.id.news_viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.news_tabs);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.news_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(NewsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.add_news_pop_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                final LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.linear_Layout_for_news);
                final EditText newsHeading = (EditText) dialog.findViewById(R.id.news_actv_heading);
                final EditText newsBodyHeading = (EditText) dialog.findViewById(R.id.news_actv_body);
                attachmentTextView = (TextView) dialog.findViewById(R.id.attach_info_news_actv);
                final ImageButton attachmentBut = (ImageButton) dialog.findViewById(R.id.attach_news_actv_button);
                final ImageButton postBut = (ImageButton) dialog.findViewById(R.id.post_news_actv_button);

                final RadioButton officialRadBut = (RadioButton) dialog.findViewById(R.id.news_official);
                officialRadBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        make view visible
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                });

                final RadioButton saySometingRadBut = (RadioButton) dialog.findViewById(R.id.news_say_something);
                saySometingRadBut.setChecked(true);
                saySometingRadBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        linearLayout.setVisibility(View.VISIBLE);
//                        make views visible for use
                    }
                });

                final RadioButton advRadBut = (RadioButton) dialog.findViewById(R.id.news_advert);
                advRadBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        hide views
                        linearLayout.setVisibility(View.GONE);

                    }
                });

                newsHeading.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
                newsHeading.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 99) {

                            Snackbar.make(newsHeading,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                newsBodyHeading.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
                newsBodyHeading.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.toString().trim().length() == 999) {

                            Snackbar.make(newsBodyHeading,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                attachmentBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        Intent chooserIntent = Intent.createChooser(intent, "Choose image" +
                                " to attach");

                        startActivityForResult(chooserIntent, 101);
                    }
                });

                postBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(officialRadBut.isChecked()){
//                            popup dialog for for passsword
                            if(newsHeading.getText().toString().isEmpty()){
                                Snackbar.make(view, "Heading cannot be empty", Snackbar.LENGTH_SHORT).show();
                            }else if(newsBodyHeading.getText().toString().isEmpty()){
                                Snackbar.make(view, "Text Body cannot be empty", Snackbar.LENGTH_SHORT).show();
                            }else{
                                headingText = newsHeading.getText().toString();
                                newsBodyText = newsBodyHeading.getText().toString();
                                popUpOfficialPassword(dialog);
                            }

                        }else if(saySometingRadBut.isChecked()){
//                            continue as usual but check if url is available
                            if(newsHeading.getText().toString().isEmpty()){
                                Snackbar.make(view, "Heading cannot be empty", Snackbar.LENGTH_SHORT).show();
                            }else if(newsBodyHeading.getText().toString().isEmpty()){
                                Snackbar.make(view, "Text Body cannot be empty", Snackbar.LENGTH_SHORT).show();
                            }else {
                                headingText = newsHeading.getText().toString();
                                newsBodyText = newsBodyHeading.getText().toString();
                                postGeneralNews();
                                dialog.cancel();
                            }
                        }else if(advRadBut.isChecked()){
//                            popup alert dialog before popping up password
                            if(attachmentUri != null){
                                final AlertDialog.Builder finishAlert = new AlertDialog.Builder(NewsActivity.this)
                                        .setTitle("I have an Advert ID");
                                finishAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        popUpAdvId();
                                        dialog.cancel();
                                    }
                                })
                                        .setNegativeButton("No thanks, CONTINUE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                postAdvert(1);
                                                dialog.cancel();

                                            }
                                        });
                                finishAlert.show();
                            }else{
                                Snackbar.make(advRadBut, "Choose Advert Image", Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    }
                });



            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    public void popUpOfficialPassword(final Dialog pDialog) {
        final Dialog dialog = new Dialog(NewsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_official_pass);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final EditText officialPassEdit = (EditText) dialog.findViewById(R.id.official_pass_edit);
        final ImageButton okOfficial = (ImageButton) dialog.findViewById(R.id.ok_official_but);
        final ImageButton cancelOfficial = (ImageButton) dialog.findViewById(R.id.cancel_official_but);

        okOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = officialPassEdit.getText().toString();
                if (password.equals("yes")) {
                    postOfficialNews();
                    dialog.cancel();
                    pDialog.cancel();
                }else{
                    Snackbar.make(view,  "Password is not valid", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        cancelOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        officialPassEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        officialPassEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 20) {

                    Snackbar.make(officialPassEdit,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        officialPassEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;

                if (i == EditorInfo.IME_ACTION_GO) {

                    String password = officialPassEdit.getText().toString();
                    if (password.equals("yes")) {
                        postOfficialNews();
                    }else{
                        Snackbar.make(textView, "Password is not valid", Snackbar.LENGTH_SHORT).show();
                    }


                }
                return handled;
            }
        });
    }

    public void postOfficialNews(){
        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        if(attachmentUri != null){
            final StorageReference photoRef = mStorageRef.child(MainActivity.userSchoolName).child(Constants.NEWS_NODE).child(Constants.OFFICIAL_NEWS_NODE)
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
                                    newCourseResource.setHeading(headingText);
                                    newCourseResource.setComments(hashy);
                                    newCourseResource.setDateOfPost(dateStamp);
                                    newCourseResource.setLikes(" ");
                                    newCourseResource.setTimeOfPost(timeStamp);
                                    newCourseResource.setNewsBody(newsBodyText);
                                    if (attachmentUri != null) {
                                        newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
                                    } else {
                                        newCourseResource.setAttachmentLinks("");
                                    }

//                                we would have to write this url to data base

                                    final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                                            .child(Constants.OFFICIAL_NEWS_NODE).push();
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
                            Toast.makeText(NewsActivity.this, "Error: upload failed",
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
            newCourseResource.setHeading(headingText);
            newCourseResource.setComments(hashy);
            newCourseResource.setDateOfPost(dateStamp);
            newCourseResource.setLikes(" ");
            newCourseResource.setTimeOfPost(timeStamp);
            newCourseResource.setNewsBody(newsBodyText);
            if (attachmentUri != null) {
                newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
            } else {
                newCourseResource.setAttachmentLinks("");
            }

//                                we would have to write this url to data base

            final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                    .child(Constants.OFFICIAL_NEWS_NODE).push();
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

    public void postGeneralNews(){
        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        if(attachmentUri != null){
            final StorageReference photoRef = mStorageRef.child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                    .child(Constants.GENERAL_NEWS_NODE).child(System.currentTimeMillis() + attachmentUri.getLastPathSegment());
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
                                    newCourseResource.setHeading(headingText);
                                    newCourseResource.setComments(hashy);
                                    newCourseResource.setDateOfPost(dateStamp);
                                    newCourseResource.setLikes(" ");
                                    newCourseResource.setTimeOfPost(timeStamp);
                                    newCourseResource.setNewsBody(newsBodyText);
                                    if (attachmentUri != null) {
                                        newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
                                    } else {
                                        newCourseResource.setAttachmentLinks("");
                                    }

//                                we would have to write this url to data base

                                    final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                                            .child(Constants.GENERAL_NEWS_NODE).push();
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
                            Toast.makeText(NewsActivity.this, "Error: upload failed",
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
            newCourseResource.setHeading(headingText);
            newCourseResource.setComments(hashy);
            newCourseResource.setDateOfPost(dateStamp);
            newCourseResource.setLikes(" ");
            newCourseResource.setTimeOfPost(timeStamp);
            newCourseResource.setNewsBody(newsBodyText);
            if (attachmentUri != null) {
                newCourseResource.setAttachmentLinks(mAttachDowloadUrl.toString());
            } else {
                newCourseResource.setAttachmentLinks("");
            }

//                                we would have to write this url to data base

            final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                    .child(Constants.GENERAL_NEWS_NODE).push();
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

    public void postAdvert(final double advertId){

        if(attachmentUri != null){
            final StorageReference photoRef = mStorageRef.child(MainActivity.userSchoolName).child(Constants.NEWS_NODE).child(Constants.SCHOOL_ADVERTS)
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

                                    CustomHashMap hashy = new CustomHashMap();
                                    hashy.setString("");

                                    AdvertsJOPO newAdvert = new AdvertsJOPO();
                                    newAdvert.setAdvertID(advertId);
                                    newAdvert.setAdvertLink(mAttachDowloadUrl.toString());
                                    newAdvert.setComments(hashy);
                                    newAdvert.setDateOfPost(dateStamp);
                                    newAdvert.setTimeOfPost(timeStamp);
                                    newAdvert.setUserUID(mFirebaseUser.getUid());

//                                we would have to write this url to data base

                                    final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.SCHOOLS_NODE).child(MainActivity.userSchoolName).child(Constants.NEWS_NODE)
                                            .child(Constants.SCHOOL_ADVERTS).push();
                                    nodeKeyReference.setValue(newAdvert).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            Toast.makeText(NewsActivity.this, "Error: upload failed",
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

        }
    }

    public void popUpAdvId() {
        final Dialog dialog = new Dialog(NewsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_advert_id);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final EditText advertIdedit = (EditText) dialog.findViewById(R.id.advert_id_edit);
        final ImageButton okOfficial = (ImageButton) dialog.findViewById(R.id.ok_advert_id_but);
        final ImageButton cancelOfficial = (ImageButton) dialog.findViewById(R.id.cancel_adv_id_but);

        okOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double password = 0;
                try {
                    password = Double.parseDouble(advertIdedit.getText().toString());
                    if (password >= -10000000 && password <= -1) {
                        postAdvert(password);
                        dialog.cancel();

                    }else{
                        Snackbar.make(view, "Password is not valid", Snackbar.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Password is not valid", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        cancelOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        advertIdedit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        advertIdedit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 20) {

                    Snackbar.make(advertIdedit,"Text Limit Reached", Snackbar.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        advertIdedit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;

                if (i == EditorInfo.IME_ACTION_GO) {

                    double password = 0;
                    try {
                        password = Double.parseDouble(advertIdedit.getText().toString());
                        if (password >= -10000000 && password <= -1) {
                            postAdvert(password);
                            dialog.cancel();

                        }else{
                            Snackbar.make(textView, "Password is not valid", Snackbar.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Snackbar.make(textView, "Password is not valid", Snackbar.LENGTH_SHORT).show();
                    }



                }
                return handled;
            }
        });

    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(NewsActivity.this, MainActivity.class));
        finish();
    }

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
        adapter.addFragment(new OfficialCardFragment(), "Official");
        adapter.addFragment(new TrendingCardFragment(), "Trending");
        adapter.addFragment(new AdvertCardFragment(), "Adverts");

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


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

    }

