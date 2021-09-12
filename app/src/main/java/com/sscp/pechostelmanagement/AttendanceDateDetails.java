package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AttendanceDateDetails extends AppCompatActivity {

    DatabaseReference ref;
    ListView listView;
    ArrayList<String> dates;
    ArrayAdapter<String> adapter;
    HashMap<String, StudentClass> studentDetails;
    String datePicked;

    ImageView download;
    int n = 0;
    HSSFWorkbook workbook;
    HSSFSheet sheet;
    HSSFRow row;
    HSSFCell cell;

    File filePath;
    HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> attendanceDetails;
    Button chooseDate;
    private int mMonth, mDay,  mYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_attendance_date_details);

        chooseDate = findViewById(R.id.datePick);
        listView = findViewById(R.id.date_details);
        ref = FirebaseDatabase.getInstance().getReference();
        download = findViewById(R.id.download_entire_attendance);
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet();
        studentDetails = new HashMap<>();
        download = findViewById(R.id.download_entire_attendance);

        download.setOnClickListener(v -> {
            if(dates.size() != 0)
                addDataToExcel();
            else
                Toast.makeText(getApplicationContext(), "There is data about attendance", Toast.LENGTH_SHORT).show();
        });
        chooseDate.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(AttendanceDateDetails.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String m = null, d = null;
                        if(monthOfYear+1 < 10)
                            m = "0"+(monthOfYear+1);
                        if(dayOfMonth < 10)
                            d = "0"+dayOfMonth;
                        datePicked = year + "-" + (m) + "-" + d;

                        Intent i = new Intent(AttendanceDateDetails.this, AttendanceTimeDetails.class);
                        i.putExtra("date", datePicked);
                        startActivity(i);

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        });

        retrieveData();

        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            Intent intent = new Intent(AttendanceDateDetails.this, AttendanceTimeDetails.class);
            String selectedItem = (String) adapterView.getItemAtPosition(i);
            intent.putExtra("date", selectedItem);
            startActivity(intent);

        });

        filePath = new File(Environment.getExternalStorageDirectory()+"/PEC Hostel Attendance Consolidated.xls");


    }


    public void retrieveData(){
        dates = new ArrayList<>();
        attendanceDetails = new HashMap<>();

        ref.child("Attendance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot date: snapshot.getChildren()){
                    dates.add(date.getKey());
                    attendanceDetails.put(date.getKey(), new HashMap<>());
                    for (DataSnapshot time: date.getChildren()){
                        attendanceDetails.get(date.getKey()).put(time.getKey(), new HashMap<>());
                        for(DataSnapshot roomNo: time.getChildren()){
                            Objects.requireNonNull(
                                    attendanceDetails.get(date.getKey()).get(time.getKey())).put(roomNo.getKey(), new HashMap<>()
                            );
                            for(DataSnapshot roll:roomNo.getChildren()){
                                Objects.requireNonNull(
                                        Objects.requireNonNull(attendanceDetails.get(date.getKey())).get(time.getKey()).get(roomNo.getKey())).put(roll.getKey(), roll.getValue(String.class)
                                );
                            }
                        }
                    }
                }
                adapter = new ArrayAdapter<>(AttendanceDateDetails.this, android.R.layout.simple_list_item_1, dates);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    studentDetails.put(snap.getKey(), snap.getValue(StudentClass.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addDataToExcel() {
        row = sheet.createRow(0);

        cell = row.createCell(0);
        cell.setCellValue("Name");

        cell = row.createCell(1);
        cell.setCellValue("Roll No");

        cell = row.createCell(2);
        cell.setCellValue("Time");

        cell = row.createCell(3);
        cell.setCellValue("Date");

        cell = row.createCell(4);
        cell.setCellValue("Attendance");

        cell = row.createCell(5);
        cell.setCellValue("RoomNo");

        cell = row.createCell(6);
        cell.setCellValue("Mobile No");

        int n = 1;
        List<String> dates = new ArrayList<>(attendanceDetails.keySet());

        for(String date:dates){
            List<String> times = new ArrayList<>(attendanceDetails.get(date).keySet());
            for(String time:times){
                List<String> rooms =  new ArrayList<>(attendanceDetails.get(date).get(time).keySet());
                for(String roomNo:rooms){
                    List<String> rolls =  new ArrayList<>(attendanceDetails.get(date).get(time).get(roomNo).keySet());
                    for(String rollNo:rolls){
                        row = sheet.createRow(n);
                        sheet.setColumnWidth(0, 4000);
                        cell = row.createCell(0);
                        cell.setCellValue(studentDetails.get(rollNo).getStudentname());

                        sheet.setColumnWidth(1, 4000);
                        cell = row.createCell(1);
                        cell.setCellValue(rollNo);

                        sheet.setColumnWidth(2, 4000);
                        cell = row.createCell(2);
                        cell.setCellValue(time);

                        sheet.setColumnWidth(3, 4000);
                        cell = row.createCell(3);
                        cell.setCellValue(date);

                        sheet.setColumnWidth(4, 4000);
                        cell = row.createCell(4);
                        cell.setCellValue(attendanceDetails.get(date).get(time).get(roomNo).get(rollNo));

                        sheet.setColumnWidth(5, 4000);
                        cell = row.createCell(5);
                        cell.setCellValue(roomNo);

                        sheet.setColumnWidth(6, 4000);
                        cell = row.createCell(6);
                        cell.setCellValue(studentDetails.get(rollNo).getMobile());

                        n++;
                    }

                }

                row = sheet.createRow(n + 1 );
                cell = row.createCell(0);
                cell.setCellValue("");
                cell = row.createCell(1);
                cell.setCellValue("");
                cell = row.createCell(2);
                cell.setCellValue("");
                cell = row.createCell(3);
                cell.setCellValue("");
                cell = row.createCell(4);
                cell.setCellValue("");
                cell = row.createCell(5);
                cell.setCellValue("");
                cell = row.createCell(6);
                cell.setCellValue("");

            }
        }


        try {
            if(!filePath.exists()){
                filePath.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(filePath);
            workbook.write(fOut);
            Toast.makeText(getApplicationContext(), "Excel file downloaded successfully", Toast.LENGTH_SHORT).show();
            fOut.flush();
            fOut.close();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception:"+e, Toast.LENGTH_SHORT).show();
        }



    }


}