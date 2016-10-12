package android.ememobong.com.gpax;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class PasswordActivity extends AppCompatActivity {

    private static final String LOG_TAG = PasswordActivity.class.getSimpleName();
    DatabaseReference mFirebaseUsersDatabaseReference;
    FirebaseAuth mFirebaseAuth;
    private String userUniqueNodeKey = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Entered onCreate of PasswordActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users_profile");

        final EditText passwordEdittext = (EditText) findViewById(R.id.password_edit_text);

        passwordEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;

                if(i == EditorInfo.IME_ACTION_GO){

                    String password = passwordEdittext.getText().toString();
                    boolean isPasswordValid = checkIsPasswordValid(password);
                    Log.i(LOG_TAG, "our password status is " + isPasswordValid);

                    if (isPasswordValid){

                        mFirebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                        String userUID = mFirebaseUser.getUid();
                        String userEmail = mFirebaseUser.getEmail();
                        Log.i(LOG_TAG, "this is our userid and email" + userUID + " & " + userEmail);
//                        queryUser(userUID);
                        setUpUserwithPassword(password, userUID, userEmail);
                        handled = true;
                }

            }
                return handled;
        }


    });
    }
//
//
//    public void queryUserForPassword(String userUID, final String userPassword){
//        Log.i(LOG_TAG, "Entered queryUserForPassword method");
//
//        mFirebaseUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
////        Query query =  mFirebaseUsersDatabaseReference.orderByChild("userUniqueId").equalTo(userUID);
//        Query query =  mFirebaseUsersDatabaseReference.orderByChild("userUniqueId");
//        Log.i(LOG_TAG, "User Query is about to be made with addChildEventListener");
//
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i(LOG_TAG, "About to check if datasnapshot has children");
//
//
//                if(dataSnapshot.getChildrenCount() == 1){
//                    userUniqueNodeKey = dataSnapshot.getKey();
//                    Log.i(LOG_TAG, "User Query has 1 child with key: " + userUniqueNodeKey);
//
//                    Log.i(LOG_TAG, "About to make another network connection for password");
//
////                    mFirebaseUsersDatabaseReference.child(userUniqueNodeKey).addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            User user = dataSnapshot.getValue(User.class);
////                            String password = user.getPassword();
////                            if(userPassword.equals(password)){
////                                Log.i(LOG_TAG, "the password is equal");
////                                checkUserProfileStatus(userUniqueNodeKey);
////                            }
////                            else{
////                                Log.i(LOG_TAG, "password is not equal");
////                                passwordCheckFailed();
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
//
//                }
//                else{
//                    Log.i(LOG_TAG, "about to set new user with password");
////                    setUpUserwithPassword(userPassword);
//                    startUserAcademicProfile();
//                }
//
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        Log.i(LOG_TAG, "exiting query usr method");
//
//    }
//
//    public void passwordCheckFailed(){
//        Toast.makeText(this, "Forgot Password? Contact Support or Try Again", Toast.LENGTH_LONG).show();
//    }
//
//
//    public void checkUserProfileStatus(String userNode){
//        Log.i(LOG_TAG, "entered check user for profile status");
//        mFirebaseUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userNode);
//        Log.i(LOG_TAG, "just about to make a query to the user_academic_details");
//        Query nextQuery = mFirebaseUsersDatabaseReference.orderByChild("users_academic_details");
//        nextQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                if (dataSnapshot.hasChildren()){
//                    Log.i(LOG_TAG, "query user details exist ie datasnapshot has children " + dataSnapshot.getChildrenCount() );
//                    startActivity(new Intent(PasswordActivity.this, MainActivity.class));
//                    finish();
//                }
//                else{
//                    Log.i(LOG_TAG, "query user details does not exist ie datasnapshot has no children " + dataSnapshot.getChildrenCount() );
//                    startActivity(new Intent(PasswordActivity.this, UserDetailsActivity.class));
//                    finish();
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//
//    }

    public boolean checkIsPasswordValid(String password){

        boolean passwordValidState = false;
        if(password.length() > 3 && password.length() <= 255 ){
            passwordValidState = true;
        }
        else {
            passwordValidState = false;
            Toast.makeText(this, "Password is not valid, please try again", Toast.LENGTH_LONG).show();

        }

        return passwordValidState;
    }

    public void setUpUserwithPassword(String password, String userUID, String email){

        Log.i(LOG_TAG, "enterd setupusetwithpassword method111");

        Intent intent = getIntent();

        mFirebaseUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users_profile").child(userUID);
        User newUser = new User(userUID, email);
        mFirebaseUsersDatabaseReference.push().setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(LOG_TAG, "A new user has just been setup");
            }
        });




    }

//    public void queryUser(String userUID){
//        Log.i(LOG_TAG, "Entered queryUser method");
//
//        Query query =  mFirebaseUsersDatabaseReference.orderByChild("userUniqueId").equalTo(userUID);
//        Log.i(LOG_TAG, "User Query is about to be made with addChildEventListener");
//
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                long numOfChildrenRet = dataSnapshot.getChildrenCount();
//                Log.i(LOG_TAG, "datasnaphot has " + numOfChildrenRet + "children");
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        Log.i(LOG_TAG, "exiting queryUserMethod");
//    }

    public void startUserAcademicProfile(){

        startActivity(new Intent(PasswordActivity.this, UserDetailsActivity.class));
        finish();
    }

}
