package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.diegodobelo.expandingview.ExpandingList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AttendanceTimeDetails extends AppCompatActivity {

    DatabaseReference ref;
    ListView listView;
    ArrayList<String> times;
    ArrayAdapter<String> adapter;
    HashMap<String, StudentClass> studentDetails;
    String datePicked, batch, year;

    ImageView download;
    int n = 0;
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;

    File filePath;
    HashMap<String, HashMap<String, String>> attendanceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_attendance_time_details);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Toast.makeText(getApplicationContext(), "Please enable the manage permission", Toast.LENGTH_SHORT).show();
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }
        }

        Initialization();

        listView.setOnItemClickListener((adapterView, view, i1, l) -> {
            Intent intent = new Intent(AttendanceTimeDetails.this, CheckAttendance.class);
            String selectedItem = (String) adapterView.getItemAtPosition(i1);

            intent.putExtra("date", datePicked);
            intent.putExtra("time", selectedItem);
            intent.putExtra("batch", batch);
            intent.putExtra("year", year);

            intent.putExtras(intent);
            startActivity(intent);
        });

        retrieveAttendanceOnDate(datePicked);

        download.setOnClickListener(v -> {
            addDataToExcel();
        });
    }

    private void Initialization() {
        Intent i = getIntent();
        datePicked = i.getStringExtra("date");
        batch = i.getStringExtra("batch");
        year = i.getStringExtra("year");

        listView = findViewById(R.id.time_details);
        ref = FirebaseDatabase.getInstance().getReference();
        //download = findViewById(R.id.download_date_attendance);
        workbook = new XSSFWorkbook();
        studentDetails = new HashMap<>();

        filePath = new File(Environment.getExternalStorageDirectory()+"/PEC Hostel Attendance Consolidated on "+datePicked+".xls");
    }

    private void addDataToExcel() {
        row = sheet.createRow(0);

        cell = row.createCell(0);
        cell.setCellValue("Name");

        cell = row.createCell(1);
        cell.setCellValue("Roll No");

        cell = row.createCell(2);
        cell.setCellValue("Date");

        cell = row.createCell(3);
        cell.setCellValue("Attendance and Time");

        cell = row.createCell(4);
        cell.setCellValue("RoomNo");

        cell = row.createCell(5);
        cell.setCellValue("Mobile No");

        int n = 1;
        List<String> rolls = new ArrayList<>(attendanceDetails.keySet());
        sheet = workbook.createSheet(batch);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

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
            cell.setCellValue(datePicked);

            for(String time:times){
                sheet.setColumnWidth(2, 4000);
                cell = row.createCell(2);
                String val = time+" - "+attendanceDetails.get(rollNo).get(time)+"\n";
                cell.setCellValue(val);
                cell.setCellStyle(style);
            }

            sheet.setColumnWidth(4, 4000);
            cell = row.createCell(4);
            cell.setCellValue(studentDetails.get(rollNo).getRoom_no());

            sheet.setColumnWidth(6, 4000);
            cell = row.createCell(6);
            cell.setCellValue(studentDetails.get(rollNo).getMobile());

            n++;
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

    private void retrieveAttendanceOnDate(String datePicked) {

        attendanceDetails = new HashMap<>();
        times = new ArrayList<>();

        ref.child("Attendance").child(batch).child(year).child(datePicked).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot time:snapshot.getChildren()){
                    times.add(time.getKey());
                }
                adapter = new ArrayAdapter<>(AttendanceTimeDetails.this, android.R.layout.simple_list_item_1, times);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rollNo: snapshot.getChildren()){
                    studentDetails.put(snapshot.getKey(), snapshot.getValue(StudentClass.class));
                    attendanceDetails.put(rollNo.getKey(), new HashMap<>());
                    for(DataSnapshot snap: rollNo.getChildren()){
                        if(snap.getKey().equals("Attendance"))
                            for(DataSnapshot date:snap.getChildren())
                                for(DataSnapshot time:date.getChildren())
                                    attendanceDetails.get(rollNo).put(time.getKey(), time.getValue(String.class));
                    }
                }
                Toast.makeText(getApplicationContext(), ""+attendanceDetails, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        */

    }

}