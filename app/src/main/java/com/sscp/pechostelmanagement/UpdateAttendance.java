package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class UpdateAttendance extends AppCompatActivity {

    Button time_choose, date_choose;
    Spinner yr;
    ImageView upload;

    ExpandingList mExpandingList;
    DatabaseReference ref;
    HashMap<String, StudentClass> studentDetails;
    AlertDialog alert;
    List<String> times;

    String batch, datePicked, currentYear, time;
    String[] newString;
    int mYear, mMonth, mDay;
    HashMap<String, HashMap<String, String>> attendance;
    HashMap<String, HashMap<String, String>> attendanceForStudent;
    boolean isUploaded = false;

    ProgressDialog pd;

    boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_update_attendance);

        Initialization();
        getCurrentDate();

        time_choose.setOnClickListener(view -> {
            if(batch.equals("Choose Batch"))
                Toast.makeText(getApplicationContext(), "Please choose batch first", Toast.LENGTH_SHORT).show();
            else
                openDialog();
        });

        date_choose.setOnClickListener(view -> {

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (vi, year, monthOfYear, dayOfMonth) -> {
                        String m = null, d = null;
                        if(monthOfYear+1 < 10)
                            m = "0"+(monthOfYear+1);
                        if(dayOfMonth < 10)
                            d = "0"+dayOfMonth;
                        datePicked = year + "-" + m + "-" + dayOfMonth;
                        currentYear = String.valueOf(year);

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        });

        upload.setOnClickListener(v->{
            pd.setTitle("Uploading Updated Attendance");
            pd.setMessage("Please wait while uploading the attendance");
            pd.setCancelable(false);
            pd.show();

            addAttendance();
        });

        yr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                batch = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void Initialization() {
        time_choose = findViewById(R.id.time_choose);
        date_choose = findViewById(R.id.date_choose);
        upload = findViewById(R.id.upload_updated_attendance);
        yr = findViewById(R.id.select_yr);
        mExpandingList = findViewById(R.id.expanding_list_students);
        ref = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(this);
        attendance = new HashMap<>();
        studentDetails = new HashMap<>();
        attendanceForStudent = new HashMap<>();
    }

    private void getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        newString = currentDateAndTime.split(",");
        newString[0] = newString[0].replace('.', '-');
        currentYear = Arrays.asList(newString[0].split("-")).get(0);
        datePicked = newString[0];
    }

    private void openDialog() {

        View v=getLayoutInflater().inflate(R.layout.specific_attendance_report,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(UpdateAttendance.this);

        builder.setView(v);

        alert=builder.create();

        alert.setTitle("Choose Time");

        ListView timesListView = v.findViewById(R.id.times);
        Spinner sp = v.findViewById(R.id.select_batch);
        Button bt = v.findViewById(R.id.choose_date);
        TextView txt = v.findViewById(R.id.timesTitle);

        sp.setVisibility(View.GONE);
        bt.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);

        retrieveTimeDetails(currentYear, datePicked, timesListView);

        alert.show();

    }

    private void retrieveTimeDetails(String currentYear, String datePicked, ListView timesListView) {
        times = new ArrayList<>();
        ref.child("Attendance").child(batch).child(currentYear)
                .child(datePicked).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot time:snapshot.getChildren()){
                    times.add(time.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdateAttendance.this, android.R.layout.simple_list_item_1, times);
                timesListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                timesListView.setOnItemClickListener((adapterView, view, i, l) -> {
                    time = adapterView.getItemAtPosition(i).toString();
                    retrieveData(datePicked, time);
                    alert.dismiss();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void retrieveData(String searched_date, String search_time){
        flag = false;
        ref.child("Attendance").child(batch).child(currentYear).child(searched_date).child(search_time).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomNumbers:snapshot.getChildren()){
                    attendance.put(roomNumbers.getKey(), new HashMap<>());
                    flag = true;
                    for(DataSnapshot rollNumber:roomNumbers.getChildren()){
                        String rNo = roomNumbers.getKey();
                        attendance.get(rNo).put(rollNumber.getKey(), rollNumber.getValue(String.class));
                    }
                }

                if(flag){
                    createItems();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Attendance was not taken on "+ datePicked, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rollNo:snapshot.getChildren()){
                    studentDetails.put(rollNo.getKey(), rollNo.getValue(StudentClass.class));
                    attendanceForStudent.put(rollNo.getKey(), new HashMap<>());
                    for(DataSnapshot child:rollNo.getChildren()){
                        if(child.getKey().equals("Attendance")){
                            for(DataSnapshot year:child.getChildren()){
                                for(DataSnapshot date:year.getChildren()){
                                    for(DataSnapshot times:date.getChildren()){
                                        if(times.getKey().equals(time))
                                            attendanceForStudent.get(rollNo.getKey()).put(times.getKey(), times.getValue(String.class));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createItems(){
        if(batch == null)
            Toast.makeText(getApplicationContext(), "Please choose year", Toast.LENGTH_SHORT).show();
        else{
            int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};
            List<String> keys = new ArrayList<>(attendance.keySet());
            for(String roomNo:keys) {
                int rnd = new Random().nextInt(colors.length);
                List<String> rolls = new ArrayList<>(attendance.get(roomNo).keySet());
                List<String> absentees = new ArrayList<>();
                for(String rollNo:rolls){
                    if(attendance.get(roomNo).get(rollNo).equals("Absent"))
                        absentees.add(rollNo);
                }
                if(absentees.size() > 0)
                    addItem(roomNo, absentees, colors[rnd]);
            }

        }

    }

    private void addItem(String s, List<String> arr1, int color) {

        ExpandingItem item = mExpandingList.createNewItem(R.layout.expanding_layout);
        if(item != null){
            item.setIndicatorColorRes(color);
            item.setIndicatorIconRes(R.drawable.ic_person);
            TextView title = item.findViewById(R.id.title);
            title.setText(s);
            item.createSubItems(arr1.size());

            for(int i = 0;i<item.getSubItemsCount();i++){
                View view = item.getSubItemView(i);
                configureSubItem(item, view, arr1.get(i), title.getText().toString());
            }

            View rmv = item.findViewById(R.id.remove_item);
            rmv.setOnClickListener(view -> mExpandingList.removeItem(item));

        }
    }

    private void configureSubItem(ExpandingItem item, View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);
        text.setText(s);
        text.setTextColor(getResources().getColor(R.color.red));
        text.setOnClickListener(v->{
            retrieveStudent(s);
        });

        CheckBox check = view.findViewById(R.id.select_student);
        LinearLayout lay = view.findViewById(R.id.sub_item_layout);
        lay.setOnClickListener(view1 -> {
            if(check.isChecked()){
                check.setChecked(false);
                if(attendance.containsKey(title)){
                    attendance.get(title).put(s, "Absent");
                    attendanceForStudent.get(s).put(time, "Absent");
                }
            }
            else{
                check.setChecked(true);

                if(attendance.containsKey(title)){
                    if(attendance.get(title) != null){
                        attendance.get(title).put(s, "Present");
                        attendanceForStudent.get(s).put(time, "Present");
                    }
                }
                //item.removeSubItem(view);
            }
        });

    }

    private void addAttendance() {
        String[] splitted = newString[0].split("-");
        String currentYear = splitted[0];
        for(String roomNo:attendance.keySet()){
            HashMap<String, Object> map = new HashMap<>();
            for(Map.Entry<String, String> entry:attendance.get(roomNo).entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
            ref.child("Attendance").child(batch).child(currentYear)
                    .child(datePicked).child(time).child(roomNo).updateChildren(map);
        }

        ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rollNo:snapshot.getChildren()){
                    StudentClass student = rollNo.getValue(StudentClass.class);
                    HashMap<String, Object> map = null;
                    map.put(time, attendanceForStudent.get(rollNo.getKey()).get(time));
                    ref.child("Students").child(batch).child(rollNo.getKey())
                            .child("Attendance")
                            .child(currentYear)
                            .child(datePicked).child(time)
                            .updateChildren(map)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    isUploaded = true;
                                }
                            });
                }
                if(isUploaded){
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "Attendance was successfully Uploaded", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateAttendance.this, WardenHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void retrieveStudent(String rollNo) {
        
        StudentClass student = studentDetails.get(rollNo);

        View view=getLayoutInflater().inflate(R.layout.student_layout,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(UpdateAttendance.this);
        builder.setView(view);

        final AlertDialog alert=builder.create();
        alert.show();

        TextView name = view.findViewById(R.id.std_name);
        assert student != null;
        name.setText(student.getStudentname());
        TextView mobile = view.findViewById(R.id.std_mobile);
        mobile.setText(student.getMobile());
        TextView roll = view.findViewById(R.id.std_roll);
        roll.setText(student.getRoll_no());
        TextView branch = view.findViewById(R.id.std_branch);
        branch.setText(student.getBranch());
        TextView room = view.findViewById(R.id.std_room);
        room.setText(student.getRoom_no());
    }


}