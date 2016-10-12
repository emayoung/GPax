package android.ememobong.com.gpax.background;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.ememobong.com.gpax.Constants;
import android.ememobong.com.gpax.Course;
import android.ememobong.com.gpax.CustomNewsResource;
import android.ememobong.com.gpax.MainActivity;
import android.ememobong.com.gpax.R;
import android.ememobong.com.gpax.ResourcesCardFragment;
import android.ememobong.com.gpax.ViewCourseResourcesActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ememobong on 19/09/2016.
 */
public class NotificationService extends IntentService {
    private NotificationManager mNotificationManager;

    private String mNewsBodyMessage = "";
    NotificationCompat.Builder builder;

    ArrayList<Long> timeStamp = new ArrayList<Long>();
    ArrayList<String> courseCode = new ArrayList<String>();

    public static String TAG = NotificationService.class.getSimpleName();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference mFirebaseDatabaseCourseReference;

    public static SharedPreferences userSchoolAppPref;
    public static SharedPreferences notifTimePref;
    public static String userSchoolName = "";
    private int countLimit = 0;
    private long numChild = 0;
    long latestTimeStamp = 0;


    public NotificationService() {

        // The super call is required. The background thread that IntentService
        // starts is labeled with the string argument you pass.
        super("android.ememobong.com.gpax");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // USE THE INTENT PASSED ON TO YOU TO ISSUE A NOTIFICATION
        Log.i(TAG, "oh " +  "onhand");
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
//        am i to give a notification
        long timeStampPref = notifTimePref.getLong("timeStamp", 0);
        String message = intent.getStringExtra("n");
        Log.i(TAG, "message " + message );

        if(timeStampPref < latestTimeStamp){
//            don't issue notification
        }else {
            issueNotification(intent, message + "\ntried");
        }

    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if(!MainActivity.calledAlready){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            MainActivity.calledAlready = true;
        }
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        userSchoolAppPref = getSharedPreferences(getString(R.string.user_school_preference), Context.MODE_PRIVATE);
        userSchoolName = userSchoolAppPref.getString("userSchool", null);

        if(mFirebaseUser != null){
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolName)
            .child(Constants.USERS_SUBSCRIBED_RESOURCES_NODE).child(mFirebaseUser.getUid());

            FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolName)
                    .child(Constants.NEWS_NODE).child(Constants.OFFICIAL_NEWS_NODE).orderByChild("timeOfPost").limitToFirst(1)
                    .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    CustomNewsResource newCustomNews = dataSnapshot.getValue(CustomNewsResource.class);
                    if(newCustomNews == null){

                    }else{

                        mNewsBodyMessage = mNewsBodyMessage +  "Official: " + newCustomNews.getHeading() + "\n";
                        timeStamp.add(newCustomNews.getTimeOfPost() * -1);
                        Log.i(TAG, "newsbdy " + mNewsBodyMessage );
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

            FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolName)
                    .child(Constants.NEWS_NODE).child(Constants.GENERAL_NEWS_NODE).orderByChild("timeOfPost").limitToFirst(1)
                    .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    CustomNewsResource newCustomNews = dataSnapshot.getValue(CustomNewsResource.class);
                    if(newCustomNews == null){

                    }else{

                        mNewsBodyMessage = mNewsBodyMessage + "Trending: " + newCustomNews.getHeading() + "\n";
                        timeStamp.add(newCustomNews.getTimeOfPost() * -1);
                        Log.i(TAG, "newsbdy " + mNewsBodyMessage );
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

            mFirebaseDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    numChild = dataSnapshot.getChildrenCount();

                    Log.i(TAG, "numchild"  + dataSnapshot.getChildrenCount() );
                   final Course newCourse = dataSnapshot.getValue(Course.class);

                    Query query = FirebaseDatabase.getInstance().getReference().child(Constants.SCHOOLS_NODE).child(userSchoolName)
                            .child(Constants.COURSE_NEWS).child(newCourse.getCourse_code()).limitToLast(1);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            countLimit++;
                            Log.i(TAG, "counLimit"  + countLimit + "numChild" + numChild );
                            if (countLimit > numChild){
                                Log.i(TAG, "counLimit"  + countLimit + " is greater than numChild" + numChild );
//                        create and start Handle  intent
//                        loop through and update the current timeStamp

                                for(Long time: timeStamp){
                                    if(time > latestTimeStamp){
                                        latestTimeStamp = time;
                                    }
                                }
//                        write this to shared preference
                                notifTimePref = getSharedPreferences(getString(R.string.notif_time_stamp), Context.MODE_PRIVATE);

//        am i to give a notification
                                long timeStampPref = notifTimePref.getLong("timeStamp", 0);
                                Log.i(TAG, "message " + mNewsBodyMessage );

                                if(timeStampPref > latestTimeStamp){
//            don't issue notification
                                }else {
                                    issueNotification(intent, mNewsBodyMessage);
                                }
                                mNewsBodyMessage = "";
                                countLimit = 0;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            CustomNewsResource newCustomNews = dataSnapshot.getValue(CustomNewsResource.class);
                            if(newCustomNews == null){

                            }else{
                                Log.i(TAG, "Heading " + newCourse.getCourse_code()  + newCustomNews.getHeading() );
                                mNewsBodyMessage = mNewsBodyMessage + newCourse.getCourse_code() + " - " +
                                        newCustomNews.getHeading() + "\n";
                                timeStamp.add(newCustomNews.getTimeOfPost() * -1);
                                Log.i(TAG, "newsbdy " + mNewsBodyMessage );
                            }
                            courseCode.add(newCourse.getCourse_code());

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
        }

        return START_STICKY;
    }

    private void issueNotification(Intent intent, String msg) {
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Log.i(TAG, "msg " + mNewsBodyMessage );
        String reducedMessage;
        if(msg.length() <= 24){
            reducedMessage = msg;
        }else{
            reducedMessage = msg.substring(0, 24);

        }
        Log.i(TAG, "reduced msg " + mNewsBodyMessage );
        // Constructs the Builder object.
        builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(reducedMessage + " ...")
                        .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                /*
                 * Sets the big view "big text" style and supplies the
                 * text (the user's reminder message) that will be displayed
                 * in the detail area of the expanded notification.
                 * These calls are ignored by the support library for
                 * pre-4.1 devices.
                 */
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg));


        /*
         * Clicking the notification itself displays ResultActivity, which provides
         * UI for snoozing or dismissing the notification.
         * This is available through either the normal view or big view.
         */
        Intent resultIntent = new Intent(this, MainActivity.class);
        if(courseCode.size() == 1){
            resultIntent.putExtra(ResourcesCardFragment.COURSE_CODE, courseCode.get(0));
        }
        resultIntent.putExtra("timeStamp", "true");

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        issueNotification(builder);

    }


    private void issueNotification(NotificationCompat.Builder builder) {
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Including the notification ID allows you to update the notification later on.
        mNotificationManager.notify(CommonConsatants.NOTIFICATION_ID, builder.build());
    }

}
