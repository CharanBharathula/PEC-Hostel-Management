package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CheckAttendance extends AppCompatActivity {

    ExpandingList mExpandingList;
    HashMap<String, List<String>> map, attendance;
    DatabaseReference ref;
    EditText search_date;
    ImageView searchBtn;
    boolean flag;
    Button chooseDate;
    private int mYear, mMonth, mDay, mHour, mMinute;
    String datePicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_check_attendance);

        mExpandingList = findViewById(R.id.expanding_list);
        attendance = new HashMap<>();
        search_date = findViewById(R.id.search);
        searchBtn = findViewById(R.id.search_btn);
        ref = FirebaseDatabase.getInstance().getReference();
        chooseDate = findViewById(R.id.choose_date);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CheckAttendance.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String m = null, d = null, y;
                                if(monthOfYear+1 < 10)
                                    m = "0"+(monthOfYear+1);
                                if(dayOfMonth < 10)
                                    d = "0"+dayOfMonth;
                                datePicked = year + "-" + (m) + "-" + d;

                                retrieveData(datePicked);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();

            }
        });

        /*TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        //selectTime.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();*/
    }

    public void createItems(){
        map = new HashMap<>();

        int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

        ref.child("RoomDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomNumbers:snapshot.getChildren()){
                    map.put(roomNumbers.getKey(), new ArrayList<String>());

                    for(DataSnapshot snap:roomNumbers.getChildren()){
                        map.get(roomNumbers.getKey()).add(snap.child("1").getValue(String.class));
                    }
                }

                ArrayList<String> keys = new ArrayList<String>(map.keySet());

                for(int i = 0;i<keys.size();i++){
                    int rnd = new Random().nextInt(colors.length);
                    addItem(keys.get(i), map.get(keys.get(i)), colors[rnd]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void retrieveData(String searched_date){
        flag = false;
        ref.child("Attendance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot date:snapshot.getChildren()){

                    if(date.getKey().equals(searched_date)){
                        for(DataSnapshot time:date.getChildren()){

                            for(DataSnapshot roomNumbers:time.getChildren()){

                                attendance.put(roomNumbers.getKey(), new ArrayList<String>());

                                for(DataSnapshot key:roomNumbers.getChildren()){
                                    attendance.get(roomNumbers.getKey()).add(key.getValue(String.class));
                                }
                            }
                        }
                        flag = true;
                        break;
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
            View rmv = (ImageView)item.findViewById(R.id.remove_item);
            rmv.setVisibility(View.GONE);
        }
    }

    private void configureSubItem(ExpandingItem item, View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);
        List<String> current = attendance.get(title);
        boolean flag = false;
        if(current != null) {
            for (int j = 0; j < map.get(title).size(); j++) {
                if (current.contains(s)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                text.setTextColor(getResources().getColor(R.color.red));
            } else {
                text.setTextColor(getResources().getColor(R.color.green));
            }

            text.setText(s);
            ImageView imageView = view.findViewById(R.id.remove_sub_item);
            CheckBox check = view.findViewById(R.id.select_student);
            check.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }

    }

}