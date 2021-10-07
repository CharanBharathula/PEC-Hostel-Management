package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddAttendance extends AppCompatActivity {


    ExpandingList mExpandingList;
    Spinner year_selection;
    ImageView upload;
    ProgressDialog pd;

    DatabaseReference ref;
    SimpleDateFormat _12HourSDF, _24HourSDF;
    Date _24HourDt;
    HashMap<String, StudentClass> studentDetails;
    HashMap<String, HashMap<String, String>> attendance;
    HashMap<String, String> attendanceData;
    List<String> check;

    String key;
    String batch;
    String newTime;
    String[] newString;
    String currentYear;
    int present, absent;
    String[] months = {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_attendance);

        Initialization();

        getCurrentDateAndTime();

        upload.setOnClickListener(view -> {
            pd = new ProgressDialog(AddAttendance.this);
            pd.setTitle("Uploading");
            pd.setMessage("Please wait while the attendance is uploading");
            addAttendance();
            pd.dismiss();
        });

        year_selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                batch = adapterView.getItemAtPosition(i).toString();
                createItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void Initialization() {
        mExpandingList = findViewById(R.id.expanding_list_main);
        upload = findViewById(R.id.uplaod_attendance);
        attendance = new HashMap<>();
        attendanceData = new HashMap<>();
        studentDetails = new HashMap<>();
        ref = FirebaseDatabase.getInstance().getReference();
        key = ref.push().getKey();
        year_selection = findViewById(R.id.select_year);
        check = new ArrayList<>();

    }

    private void getCurrentDateAndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        newString = currentDateAndTime.split(",");
        newString[0] = newString[0].replace('.', '-');
        String[] timeArr = newString[1].split(":");
        newTime = timeArr[0]+":"+timeArr[1];

        try {
            String _24HourTime = newTime;
            _24HourSDF = new SimpleDateFormat("HH:mm");
            _12HourSDF = new SimpleDateFormat("hh:mm a");

            _24HourDt = _24HourSDF.parse(_24HourTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
        newTime = _12HourSDF.format(_24HourDt);
        currentYear = Arrays.asList(newString[0].split("-")).get(0);
    }

    public void createItems(){
        if(batch == null)
            Toast.makeText(getApplicationContext(), "Please choose year", Toast.LENGTH_SHORT).show();
        else{
            int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

            ref.child("RoomDetails").child(batch).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot roomNumbers:snapshot.getChildren()){

                        attendance.put(roomNumbers.getKey(), new HashMap<>());
                        for(DataSnapshot snap:roomNumbers.getChildren()){
                            attendance.get(roomNumbers.getKey()).put(snap.getKey(), "Present");
                            attendanceData.put(snap.getKey(), "Present");
                            present++;
                        }

                        int rnd = new Random().nextInt(colors.length);
                        addItem(roomNumbers.getKey(), new ArrayList<>(attendance.get(roomNumbers.getKey()).keySet()) , colors[rnd]);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void addItem(String s, ArrayList<String> arr1, int color) {

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
            ImageView add = item.findViewById(R.id.add_more_sub_items);

            View rmv = item.findViewById(R.id.remove_item);
            rmv.setOnClickListener(view -> mExpandingList.removeItem(item));

        }
    }

    private void addAttendance() {
        String[] splitted = newString[0].split("-");
        String currentYear = splitted[0];
        ref.child("Attendance").child(batch).child(currentYear).child(newString[0]).child(newTime).setValue(attendance).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Attendance was successfully Uploaded", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddAttendance.this, WardenHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            else{
                FirebaseAuthException e = (FirebaseAuthException )task.getException();
                Toast.makeText(AddAttendance.this, "Uploading Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
        HashMap<String, String> aCount = new HashMap<>();
        aCount.put("presents", String.valueOf(present));
        aCount.put("absents", String.valueOf(absent));
        aCount.put("total", String.valueOf(present+absent));

        ref.child("Attendance").child(batch).child(currentYear).child(newString[0]).child(newTime).child("count").setValue(aCount);

        ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rollNo:snapshot.getChildren()){
                    ref.child("Students").child(batch).child(rollNo.getKey()).child("Attendance").child(currentYear).child(newString[0]).child(newTime).setValue(attendanceData.get(rollNo.getKey()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void configureSubItem(ExpandingItem item, View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);
        text.setText(s);
        CheckBox check = view.findViewById(R.id.select_student);
        LinearLayout lay = view.findViewById(R.id.sub_item_layout);
        lay.setOnClickListener(view1 -> {
            if(check.isChecked()){
                check.setChecked(false);
                if(attendance.containsKey(title)){
                    for (Map.Entry<String, HashMap<String, String>> entry : attendance.entrySet()) {
                        entry.getValue().remove(s);
                    }
                    attendanceData.put(s, "Present");
                    present++;
                    absent--;
                }
            }
            else{
                check.setChecked(true);

                if(attendance.containsKey(title)){
                    if(attendance.get(title) != null){
                        attendance.get(title).put(s, "Absent");
                        attendanceData.put(s, "Absent");
                        present--;
                        absent++;

                    }
                    else {
                        attendance.get(title).put(s, "Present");
                        attendanceData.put(s, "Present");
                    }
                }
                //item.removeSubItem(view);
            }
        });

    }

}

interface OnItemCreated {
    public void itemCreated(String title);
}
