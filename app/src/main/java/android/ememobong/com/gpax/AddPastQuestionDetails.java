package android.ememobong.com.gpax;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPastQuestionDetails extends AppCompatActivity{

    ImageButton cancelImageButton;
    ImageButton addAnotherButton;
    ImageButton finishButton;
    ImageButton retakeCurrentImageButton;
    ImageView viewImage;

    public static String userSchoolName = " ";

    Intent intent;

    private DatabaseReference mFirebaseDatabaseReference;

    private StorageReference mStorageRef;
    private Uri mFileUri = null;

    StorageReference courseNameRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    ProgressDialog mProgressDialog;

    String userUniqueId = " ";
    boolean calledAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_past_question_details);

        userSchoolName = MainActivity.userSchoolAppPref.getString("userSchool", null);

        cancelImageButton = (ImageButton) findViewById(R.id.cancel_current_operation);
        addAnotherButton = (ImageButton) findViewById(R.id.add_to_current_operation);
        finishButton = (ImageButton) findViewById(R.id.finish_current_operation);
        retakeCurrentImageButton = (ImageButton) findViewById(R.id.retake_past_question_picture);
        viewImage = (ImageView) findViewById(R.id.show_snapped_imageview);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolName)
                .child(Constants.PAST_QUESTION_NODE);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        intent = getIntent();

        if (mFirebaseUser != null){
            userUniqueId = mFirebaseUser.getUid();
        }

        String filePath = intent.getStringExtra(OcrCaptureActivity.PAST_QUESTION_IMAGE_FILE_PATH);

        File newImageFile = new File(filePath);

        if (newImageFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            viewImage.setImageBitmap(myBitmap);
            mFileUri = Uri.fromFile(newImageFile);

        }



    }

    public void onStart(){
        super.onStart();

        if(intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE) != null) {
            if (intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE).equals("updateCourse")) {
                cancelImageButton.setVisibility(View.INVISIBLE);
            }
        }else{

        }

        cancelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View v = view;
//TODO: here we will cancel whatever operation the user has made on the node and go to the mainActivity
               cancelOperation();

            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE) != null) {
                if(intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE).equals("updateCourse")){

                    showProgressDialog();

                    courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE)
                            .child(PastQuestionCardFragment.UPDATE_COURSE_CODE)
                            .child(mFileUri.getLastPathSegment());
                    // [END get_child_ref]

                    // Upload file to Firebase Storage
                    // [START_EXCLUDE]
                    courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                    PastQuestionResource newPastQuestionResource = new PastQuestionResource();

//                                    String timeStamp = new SimpleDateFormat("HH:mm:ss")
//                                            .format(new Date());

                                    long timeStamp = System.currentTimeMillis() * -1;
//                                    final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance()
//                                            .getReference(intent.getStringExtra(PastQuestionCardFragment.NODE_KEY)).getParent();

                                    final DatabaseReference nodeKeyReference = PastQuestionCardFragment.mFirebaseDatabaseUpdateReference;
                                     nodeKeyReference.child("timeOfPost").setValue(timeStamp);
                                    nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                    nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());

                                    hideProgressDialog();
                                    final AlertDialog.Builder finishAlert = new AlertDialog.Builder(AddPastQuestionDetails.this)
                                            .setTitle("Successful!!!");
                                    finishAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(AddPastQuestionDetails.this, MainActivity.class);

                                            startActivity(intent);
                                            finish();

                                            deleteCurrentImage();
                                            PastQuestionCardFragment.mFirebaseDatabaseUpdateReference = null;

                                        }
                                    });
                                    finishAlert.show();

                                }
                            }).addOnFailureListener(AddPastQuestionDetails.this,
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
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
                }else {
//TODO: here we will add the recently added image to the node we are working on but ensure to check if the user node is not empty else you will have
//                to create the node
//                now that all is ready show the user a summary in a dialog but nsure the user can't go back here
                    showProgressDialog();
                    if (OcrCaptureActivity.NODE_KEY_TO_UPLOAD == " ") {
//                    upload to storage then create the node;
                        courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE).child(MainActivity.courseNameToUpload)
                                .child(mFileUri.getLastPathSegment());
                        // [END get_child_ref]

                        // Upload file to Firebase Storage
                        // [START_EXCLUDE]
                        courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                        CustomPastQuestionResourceForFirebase newPastQuestionResource = new CustomPastQuestionResourceForFirebase();
                                        CustomHashMap hashy = new CustomHashMap();
                                        hashy.setString("");

//                                    String timeStamp = new SimpleDateFormat("HH:mm:ss")
//                                            .format(new Date());

                                        long timeStamp = System.currentTimeMillis() * -1;

                                        String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                                                .format(new Date());

                                        newPastQuestionResource.setUserUID(userUniqueId);
                                        newPastQuestionResource.setImageNamesOfFiles(hashy);
                                        newPastQuestionResource.setComments(hashy);
                                        newPastQuestionResource.setDateOfPost(dateStamp);
                                        newPastQuestionResource.setLikes(hashy);
                                        newPastQuestionResource.setTimeOfPost(timeStamp);
                                        newPastQuestionResource.setImageUrls(hashy);
                                        final DatabaseReference nodeKeyReference = mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload).push();
                                        OcrCaptureActivity.NODE_KEY_TO_UPLOAD = nodeKeyReference.getKey();

                                        nodeKeyReference.setValue(newPastQuestionResource).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
//                                                we can comments, imageurls and likes(for later)
//                                                we intend to push like to be readable by a custom hashmap
                                                    nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).push();
                                                    nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).setValue("1");
                                                    nodeKeyReference.child("likes").child("string").setValue(null);
                                                    nodeKeyReference.child("comments").push().setValue(mFirebaseUser.getDisplayName() + ":" + MainActivity.commentToUpload);
                                                    nodeKeyReference.child("comments").child("string").setValue(null);
                                                    nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                                    nodeKeyReference.child("imageUrls").child("string").setValue(null);
                                                    nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());
                                                    nodeKeyReference.child("imageNamesOfFiles").child("string").setValue(null);
                                                    hideProgressDialog();

                                                    final AlertDialog.Builder finishAlert = new AlertDialog.Builder(AddPastQuestionDetails.this)
                                                            .setTitle("Successful!!!");
                                                    finishAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            startActivity(new Intent(AddPastQuestionDetails.this, MainActivity.class));
                                                            finish();

                                                            deleteCurrentImage();

                                                        }
                                                    });
                                                    finishAlert.show();
                                                    mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload)
                                                            .child(OcrCaptureActivity.NODE_KEY_TO_UPLOAD).child("imageUrls").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final long childrenCount = dataSnapshot.getChildrenCount();
                                                            finishAlert.setMessage("Thanks for supporting " + MainActivity.courseNameToUpload + "with " + childrenCount + " Resources");

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }

                                            }
                                        });


                                    }
                                }).addOnFailureListener(AddPastQuestionDetails.this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                        hideProgressDialog();
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


                    } else {
//                    user is already adding resource to a node
                        courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE).child(MainActivity.courseNameToUpload)
                                .child(mFileUri.getLastPathSegment());
                        // [END get_child_ref]

                        // Upload file to Firebase Storage
                        // [START_EXCLUDE]
                        courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                        final DatabaseReference nodeKeyReference = mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload).child(OcrCaptureActivity.NODE_KEY_TO_UPLOAD);

                                        nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                        nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());


                                        hideProgressDialog();
                                        deleteCurrentImage();
                                        //                      TODO: add dialog that shows summarry and start main activity


                                        final AlertDialog.Builder finishAlert = new AlertDialog.Builder(AddPastQuestionDetails.this)
                                                .setTitle("Successful!!!");
                                        finishAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                startActivity(new Intent(AddPastQuestionDetails.this, MainActivity.class));
                                                finish();
                                                OcrCaptureActivity.NODE_KEY_TO_UPLOAD = " ";
                                                deleteCurrentImage();
                                            }
                                        });
                                        finishAlert.show();
                                        mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload)
                                                .child(OcrCaptureActivity.NODE_KEY_TO_UPLOAD).child("imageUrls").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                final long childrenCount = dataSnapshot.getChildrenCount();
                                                finishAlert.setMessage("Thanks for supporting " + MainActivity.courseNameToUpload + "with " + childrenCount + " Resources");

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }

                                        });
                                     }
                                }).addOnFailureListener(AddPastQuestionDetails.this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                        hideProgressDialog();
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
            }
        });

        addAnotherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE) != null) {
                    if (intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE).equals("updateCourse")) {

                        showProgressDialog();

                        courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE)
                                .child(PastQuestionCardFragment.UPDATE_COURSE_CODE)
                                .child(mFileUri.getLastPathSegment());
                        // [END get_child_ref]

                        // Upload file to Firebase Storage
                        // [START_EXCLUDE]
                        courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                        String nodeKey = intent.getStringExtra(PastQuestionCardFragment.NODE_KEY);

                                        long timeStamp = System.currentTimeMillis() * -1;
//                                        final DatabaseReference nodeKeyReference = FirebaseDatabase.getInstance()
//                                                .getReference(nodeKey).getParent();
                                        final DatabaseReference nodeKeyReference = PastQuestionCardFragment.mFirebaseDatabaseUpdateReference;

                                        nodeKeyReference.child("timeOfPost").setValue(timeStamp);
                                        nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                        nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());

                                        hideProgressDialog();

                                        Intent intent = new Intent(AddPastQuestionDetails.this, OcrCaptureActivity.class);
//                        pass in the node update is to be made
                                        intent.putExtra(PastQuestionCardFragment.NODE_KEY, nodeKey);
                                        intent.putExtra(PastQuestionCardFragment.UPDATE_COURSE, "updateCourse");
                                        intent.putExtra(PastQuestionCardFragment.COURSE_CODE, intent.getStringExtra(PastQuestionCardFragment.COURSE_CODE));
                                        startActivity(intent);
                                        finish();
                                        deleteCurrentImage();
                                    }
                                }).addOnFailureListener(AddPastQuestionDetails.this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                        hideProgressDialog();
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
                }else {
//                TODO: here if this button is clicked on the first time you will have to create the node and upload neccessary data
//                elseif tis is not the case then you may have just have to upload to the user current node of operation
                    showProgressDialog();
                    if (OcrCaptureActivity.NODE_KEY_TO_UPLOAD == " ") {
//                    upload to storage then create the node;
                        courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE).child(MainActivity.courseNameToUpload)
                                .child(mFileUri.getLastPathSegment());
                        // [END get_child_ref]

                        // Upload file to Firebase Storage
                        // [START_EXCLUDE]
                        courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                        CustomPastQuestionResourceForFirebase newPastQuestionResource = new CustomPastQuestionResourceForFirebase();
                                        CustomHashMap hashy = new CustomHashMap();
                                        hashy.setString("");

//                                    String timeStamp = new SimpleDateFormat("HH:mm:ss")
//                                            .format(new Date());

                                        long timeStamp = System.currentTimeMillis() * -1;

                                        String dateStamp = new SimpleDateFormat("E, dd MMM yyyy")
                                                .format(new Date());

                                        newPastQuestionResource.setUserUID(userUniqueId);
                                        newPastQuestionResource.setImageNamesOfFiles(hashy);
                                        newPastQuestionResource.setComments(hashy);
                                        newPastQuestionResource.setDateOfPost(dateStamp);
                                        newPastQuestionResource.setLikes(hashy);
                                        newPastQuestionResource.setTimeOfPost(timeStamp);
                                        newPastQuestionResource.setImageUrls(hashy);

                                        final DatabaseReference nodeKeyReference = mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload).push();
                                        OcrCaptureActivity.NODE_KEY_TO_UPLOAD = nodeKeyReference.getKey();

                                        nodeKeyReference.setValue(newPastQuestionResource).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
//                                                we can comments, imageurls and likes(for later)


                                                    nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).push();
                                                    nodeKeyReference.child("likes").child(mFirebaseUser.getUid()).setValue("1");
                                                    nodeKeyReference.child("likes").child("string").setValue(null);
                                                    nodeKeyReference.child("comments").push().setValue(mFirebaseUser.getDisplayName() + ":" + MainActivity.commentToUpload);
                                                    nodeKeyReference.child("comments").child("string").setValue(null);
                                                    nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                                    nodeKeyReference.child("imageUrls").child("string").setValue(null);
                                                    nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());
                                                    nodeKeyReference.child("imageNamesOfFiles").child("string").setValue(null);

//                                                start capture activity again

                                                    startActivity(new Intent(AddPastQuestionDetails.this, OcrCaptureActivity.class));
                                                    finish();
                                                    deleteCurrentImage();
                                                    hideProgressDialog();
                                                }
                                            }
                                        });

                                    }
                                }).addOnFailureListener(AddPastQuestionDetails.this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                        hideProgressDialog();
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

                    } else {
//                    user is already adding resource to a node
                        courseNameRef = mStorageRef.child(userSchoolName).child(Constants.PAST_QUESTION_NODE).child(MainActivity.courseNameToUpload)
                                .child(mFileUri.getLastPathSegment());
                        // [END get_child_ref]

                        // Upload file to Firebase Storage
                        // [START_EXCLUDE]
                        courseNameRef.putFile(mFileUri).addOnSuccessListener(AddPastQuestionDetails.this,
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final String downloadUrlForImage = taskSnapshot.getMetadata().getDownloadUrl().toString();
//                                    we now can create the node in our db for past questions resource

                                        Toast.makeText(AddPastQuestionDetails.this, "We have succesfully uploaded the image and about to add url and name to database", Toast.LENGTH_SHORT).show();

                                        final DatabaseReference nodeKeyReference = mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload).child(OcrCaptureActivity.NODE_KEY_TO_UPLOAD);

                                        nodeKeyReference.child("imageUrls").push().setValue(downloadUrlForImage);
                                        nodeKeyReference.child("imageNamesOfFiles").push().setValue(mFileUri.getLastPathSegment());

//                                                start capture activity again
                                        startActivity(new Intent(AddPastQuestionDetails.this, OcrCaptureActivity.class));
                                        finish();

                                        deleteCurrentImage();
                                        hideProgressDialog();
                                    }
                                }).addOnFailureListener(AddPastQuestionDetails.this,
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPastQuestionDetails.this, "The upload failed, try again!!!", Toast.LENGTH_SHORT).show();
                                        hideProgressDialog();
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
            }

        });
        retakeCurrentImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE) != null) {
                    if (intent.getStringExtra(PastQuestionCardFragment.UPDATE_COURSE).equals("updateCourse")) {

                        Intent intent = new Intent(AddPastQuestionDetails.this, OcrCaptureActivity.class);
//                        pass in the node update is to be made
                        String nodeKey = intent.getStringExtra(PastQuestionCardFragment.NODE_KEY);
                        intent.putExtra(PastQuestionCardFragment.NODE_KEY, nodeKey);
                        intent.putExtra(PastQuestionCardFragment.UPDATE_COURSE, "updateCourse");
                        intent.putExtra(PastQuestionCardFragment.COURSE_CODE, intent.getStringExtra(PastQuestionCardFragment.COURSE_CODE));
                        startActivity(intent);
                        finish();
                        deleteCurrentImage();
                    }
                }else {
//                TODO: cancel the last image snapped to enable the user take another snap shot
                    startActivity(new Intent(AddPastQuestionDetails.this, OcrCaptureActivity.class));
                    finish();
                    deleteCurrentImage();
                }
            }
        });



    }

    @Override
    public void onBackPressed() {

    }

    private void cancelOperation(){
        if(OcrCaptureActivity.NODE_KEY_TO_UPLOAD != " "){
            new AlertDialog.Builder(AddPastQuestionDetails.this)
                    .setTitle("Cancel operation")
                    .setTitle("Do you want to cancel all uploads to " + MainActivity.courseNameToUpload)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // we are currently working on a node so we can delete it
                            mFirebaseDatabaseReference.child(MainActivity.courseNameToUpload).child(OcrCaptureActivity.NODE_KEY_TO_UPLOAD).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(AddPastQuestionDetails.this, "Operation has been cancelled please try again", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AddPastQuestionDetails.this, MainActivity.class));
                                    finish();
                                    deleteCurrentImage();
                                    OcrCaptureActivity.NODE_KEY_TO_UPLOAD = " ";
                                    deleteCurrentImage();

                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }    }

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

    private void deleteCurrentImage(){
        File file = new File(mFileUri.getPath());
        if(file.exists()){
            boolean delete = file.delete();
        }
    }

    }

