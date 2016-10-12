package android.ememobong.com.gpax;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



/*
* This class is responsible for  managing the user profile and updating it in th database
 */

public class UserDetailsActivity extends AppCompatActivity {


    private DatabaseReference mFirebaseDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        EditText userName = (EditText) findViewById(R.id.username_edittext);
        EditText fullName = (EditText) findViewById(R.id.fullname_edittext);
        EditText regNumber = (EditText) findViewById(R.id.regNumber);
        Spinner faculty = (Spinner) findViewById(R.id.faculty_spinner);
        Spinner department = (Spinner) findViewById(R.id.department_spinner);
        Spinner gender = (Spinner) findViewById(R.id.gen_spinner);
        Spinner currentLevel =(Spinner) findViewById(R.id.level_spinner);
        Button updateButton =(Button) findViewById(R.id.update_button);


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<CharSequence> facultyAdapter = ArrayAdapter.createFromResource(this, R.array.faculties, android.R.layout.simple_spinner_item);
        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        faculty.setAdapter(facultyAdapter);

        ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(this, R.array.department, android.R.layout.simple_spinner_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(departmentAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> currentLevelAdapter = ArrayAdapter.createFromResource(this, R.array.level, android.R.layout.simple_spinner_item);
        currentLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentLevel.setAdapter(currentLevelAdapter);



    }
}
