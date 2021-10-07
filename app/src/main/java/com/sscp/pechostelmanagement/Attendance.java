package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Attendance extends AppCompatActivity {

    XSSFWorkbook workbook;
    XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
    File filePath;
    DatabaseReference ref;

    List<String> times;
    HashMap<String, String> pre;
    HashMap<String, String> abs;
    HashMap<String, String> tot;
    HashMap<String, HashMap<String, StudentClass>> studentDetails;
    HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>> attendanceData;

    ImageView consolidated_reports, specific_reports, check_attendance;
    TextView total, presentees, absentees;
    ProgressDialog pd, pd1;
    AlertDialog alert;

    private int mMonth, mDay,  mYear, totalStudents, totalPresentees, totalAabsentees, n = 1;
    String y = null, student_batch, p, a, t, datePicked, time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_attendance);

        requestForPermission();

        Initialization();
        pd1 = new ProgressDialog(this);
        pd1.setTitle("Retrieving Data");
        pd1.setMessage("Please Wait while downloading Attendance data");
        pd1.setCancelable(false);
        pd1.show();

        retrieveStudent();

        consolidated_reports.setOnClickListener(v-> openDialog());

        specific_reports.setOnClickListener(v->{
            openAttendanceDialog();
        });
        check_attendance.setOnClickListener(v-> startActivity(new Intent(Attendance.this, AttendanceDateDetails.class)));

    }

    private void openAttendanceDialog() {

        View v=getLayoutInflater().inflate(R.layout.specific_attendance_report,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(Attendance.this);

        builder.setView(v);

        alert=builder.create();

        LinearLayout layout = v.findViewById(R.id.attendance_count);
        Spinner sp = v.findViewById(R.id.select_batch);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                student_batch = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        total = v.findViewById(R.id.total_students);
        presentees = v.findViewById(R.id.presentees);
        absentees = v.findViewById(R.id.absentees);
        Button chooseDate = v.findViewById(R.id.choose_date);
        ListView timesListView = v.findViewById(R.id.times);
        chooseDate.setOnClickListener(view -> {

            totalStudents = 0;
            totalAabsentees = 0;
            totalPresentees = 0;
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (vi, year, monthOfYear, dayOfMonth) -> {
                        String m = ""+(monthOfYear + 1), d = ""+dayOfMonth;
                        if((monthOfYear + 1) < 10)
                            m = "0"+(monthOfYear + 1);
                        if(dayOfMonth < 10)
                            d = "0"+dayOfMonth;
                        datePicked = year + "-" + m + "-" + d;

                        retrieveTimeDetails(String.valueOf(mYear), datePicked, timesListView, layout);
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();


        });
        presentees.setOnClickListener(textView->{
            Intent i = new Intent(Attendance.this, StudentAttendanceSpecific.class);
            i.putExtra("presents", p);
            i.putExtra("type", "P");
            i.putExtra("batch",student_batch);
            i.putExtra("year", String.valueOf(mYear));
            i.putExtra("time", time);
            i.putExtra("date", datePicked);

            startActivity(i);
        });

        absentees.setOnClickListener(textView->{
            Intent i = new Intent(Attendance.this, StudentAttendanceSpecific.class);
            i.putExtra("absents", a);
            i.putExtra("type", "A");
            i.putExtra("batch",student_batch);
            i.putExtra("year", String.valueOf(mYear));
            i.putExtra("time", time);
            i.putExtra("date", datePicked);

            startActivity(i);
        });

        alert.show();

    }

    private void retrieveTimeDetails(String currentYear, String datePicked, ListView timesListView, LinearLayout layout) {
        times = new ArrayList<>();
        ref.child("Attendance").child(student_batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot year:snapshot.getChildren()){

                    if(year.getKey().equals(currentYear)){
                        for(DataSnapshot date:year.getChildren()){
                            if(date.getKey().equals(datePicked)){
                                for(DataSnapshot time:date.getChildren())
                                    times.add(time.getKey());
                            }
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter <>(Attendance.this, android.R.layout.simple_list_item_1, times);
                timesListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();


                timesListView.setOnItemClickListener((adapterView, view, i, l) -> {

                    time = adapterView.getItemAtPosition(i).toString();
                    getAttendanceCount(time, timesListView, layout, currentYear);
                    timesListView.setVisibility(View.GONE);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAttendanceCount(String time, ListView timesListView, LinearLayout layout, String currentYear) {
        totalStudents = attendanceData.get(student_batch).keySet().size();
        for(String roll:new ArrayList<>(attendanceData.get(student_batch).keySet())){
            for(String year:new ArrayList<>(attendanceData.get(student_batch).get(roll).keySet())){
                if(year.equals(currentYear)){
                    for(String date:new ArrayList<>(attendanceData.get(student_batch).get(roll).get(year).keySet())){
                        for(String t:new ArrayList<>(attendanceData.get(student_batch).get(roll).get(year).get(date).keySet())){
                            if(t.equals(time)){
                                String val = attendanceData.get(student_batch).get(roll).get(year).get(date).get(t);
                                if(val.equals("Present"))
                                    totalPresentees++;
                                else
                                    totalAabsentees++;
                            }
                        }
                    }
                }
            }
        }
        timesListView.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
        total.setText(""+totalStudents);
        presentees.setText(""+totalPresentees);
        absentees.setText(""+totalAabsentees);


    }

    private void Initialization() {
        consolidated_reports = findViewById(R.id.consolidated_attendance_report);
        specific_reports = findViewById(R.id.specific_attendance_report);
        check_attendance = findViewById(R.id.check_att);
        ref = FirebaseDatabase.getInstance().getReference();
        studentDetails = new HashMap<>();
        workbook = new XSSFWorkbook();
        pd = new ProgressDialog(this);
    }

    private void openDialog() {
        View v=getLayoutInflater().inflate(R.layout.consolidated_reports_inputs,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(Attendance.this);

        builder.setView(v);

        alert=builder.create();
        EditText year = v.findViewById(R.id.choose_year);
        Button submit = v.findViewById(R.id.retrive_consolidated_reports);

        submit.setOnClickListener(view->{
            String y = year.getText().toString();
            boolean isYearExist = false;
            if(!y.equals("")){
                List<String> b = new ArrayList<>(attendanceData.keySet());
                for(String bat:b){
                    List<String> rolls = new ArrayList<>(attendanceData.get(bat).keySet());
                    for(String roll:rolls){
                        List<String> ye = new ArrayList<>(attendanceData.get(bat).get(roll).keySet());
                        if(ye.contains(y))
                            isYearExist = true;
                    }
                }
                if(isYearExist){
                    pd.setTitle("Downloading");
                    pd.setMessage("Please wait while downloading Excel file");
                    pd.setCancelable(false);
                    pd.show();
                    filePath = new File(Environment.getExternalStorageDirectory()+"/PEC HAC Report in "+y+".xls");
                    addDataToExcel(y);
                }
                else
                    Toast.makeText(getApplicationContext(), "Enter valid year", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Please enter correct year you want to retrieve", Toast.LENGTH_SHORT).show();

        });

        alert.show();
    }

    private void addDataToExcel(String year) {

        XSSFFont presentFont = workbook.createFont();
        presentFont.setColor(IndexedColors.GREEN.getIndex());

        XSSFFont absentFont = workbook.createFont();
        absentFont.setColor(IndexedColors.RED.getIndex());

        List<String> batches = new ArrayList<>(attendanceData.keySet());

        for(String batch:batches){

            int sheetCount = workbook.getNumberOfSheets();

            boolean flag = false;
            for(int i = 0;i<sheetCount;i++){
                if(workbook.getSheetName(i).equals(batch)) {
                    flag = true;
                    sheet = workbook.getSheet(batch);
                }
            }
            if(!flag)
                sheet = workbook.createSheet(batch);
            XSSFCellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.CENTER);

            row = sheet.createRow(0);

            cell = row.createCell(0);
            cell.setCellValue("Name");

            cell = row.createCell(1);
            cell.setCellValue("Roll No");

            cell = row.createCell(2);
            cell.setCellValue("RoomNo");

            cell = row.createCell(3);
            cell.setCellValue("Mobile No");

            List<String> keys = new ArrayList<>(attendanceData.get(batch).keySet());

            for(String rollNo:keys){

                if(attendanceData.get(batch).get(rollNo).keySet().contains(year)){

                    List<String> dates = new ArrayList<>(Objects.requireNonNull(attendanceData.get(batch).get(rollNo)).get(year).keySet());

                    int c = 4;

                    for(String date:dates){

                        List<String> times = new ArrayList<>(attendanceData.get(batch).get(rollNo).get(year).get(date).keySet());
                        String val = null;

                        for(String time:times){
                            val = time+date;

                            cell = row.createCell(c);
                            sheet.setColumnWidth(c, 5000);
                            cell.setCellValue(date+" - "+time);
                            c++;
                        }
                    }
                }
            }

            for(String rollNo:keys){

                if(attendanceData.get(batch).get(rollNo).keySet().contains(year)){

                    row = sheet.createRow(n);

                    sheet.setColumnWidth(0, 4000);
                    cell = row.createCell(0);
                    cell.setCellStyle(style);
                    cell.setCellValue(Objects.requireNonNull(studentDetails.get(batch).get(rollNo)).getStudentname());

                    sheet.setColumnWidth(1, 4000);
                    cell = row.createCell(1);
                    cell.setCellStyle(style);
                    cell.setCellValue(rollNo);

                    sheet.setColumnWidth(2, 4000);
                    cell = row.createCell(2);
                    cell.setCellStyle(style);
                    cell.setCellValue(Objects.requireNonNull(studentDetails.get(batch).get(rollNo)).getRoom_no());

                    sheet.setColumnWidth(3, 4000);
                    cell = row.createCell(3);
                    cell.setCellStyle(style);
                    cell.setCellValue(Objects.requireNonNull(studentDetails.get(batch).get(rollNo)).getMobile());

                    List<String> dates = new ArrayList<>(Objects.requireNonNull(attendanceData.get(batch).get(rollNo)).get(year).keySet());

                    int c = 4;


                    for(String date:dates){

                        sheet.setColumnWidth(c, 5000);
                        List<String> times = new ArrayList<>(attendanceData.get(batch).get(rollNo).get(year).get(date).keySet());
                        String att = null;

                        for(String time:times){
                            att = Objects.requireNonNull(Objects.requireNonNull(attendanceData.get(batch).get(rollNo)).get(year).get(date)).get(time);
                            cell = row.createCell(c);
                            cell.setCellValue(att);
                            cell.setCellStyle(style);

                            if(att.equalsIgnoreCase("Present"))
                                CellUtil.setFont(cell, presentFont);
                            else
                                CellUtil.setFont(cell, absentFont);

                            c++;
                        }
                    }

                    n++;
                }
            }

            for(String rollNo: keys){
                row = sheet.createRow(n);
                if(attendanceData.get(batch).get(rollNo).keySet().contains(year)){
                    List<String> dates = new ArrayList<>(Objects.requireNonNull(attendanceData.get(batch).get(rollNo)).get(year).keySet());
                    int c = 4;

                    for(String date:dates){

                        sheet.setColumnWidth(c, 5000);
                        List<String> times = new ArrayList<>(attendanceData.get(batch).get(rollNo).get(year).get(date).keySet());

                        for(String time:times){
                            cell = row.createCell(c);
                            cell.setCellValue(pre.get(time+date)+" - "+abs.get(time+date)+" - "+tot.get(time+date));
                            cell.setCellStyle(style);
                            c++;
                        }
                    }
                }
            }

        }


        try {
            if(!filePath.exists()){
                filePath.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(filePath);
            workbook.write(fOut);
            Toast.makeText(getApplicationContext(), "Excel file downloaded successfully", Toast.LENGTH_SHORT).show();
            alert.dismiss();
            pd.dismiss();
            fOut.flush();
            fOut.close();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception:"+e, Toast.LENGTH_SHORT).show();
        }

    }

    private void retrieveStudent() {
        ref.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot batch:snapshot.getChildren()){
                    studentDetails.put(batch.getKey(), new HashMap<>());
                    for (DataSnapshot rNo:batch.getChildren())
                        studentDetails.get(batch.getKey()).put(rNo.getKey(), rNo.getValue(StudentClass.class));
                }
                retrieveAttendance();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveAttendance() {
        attendanceData = new HashMap<>();
        ref.child("Students").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ba:snapshot.getChildren()){
                    attendanceData.put(ba.getKey(), new HashMap<>());
                    for(DataSnapshot rollNo:ba.getChildren()){
                        attendanceData.get(ba.getKey()).put(rollNo.getKey(), new HashMap<>());
                        for(DataSnapshot child:rollNo.getChildren()){
                            if(child.getKey().equals("Attendance")){
                                for(DataSnapshot year:child.getChildren()){
                                    attendanceData.get(ba.getKey()).get(rollNo.getKey()).put(year.getKey(), new HashMap());
                                    for(DataSnapshot date:year.getChildren()){
                                        attendanceData.get(ba.getKey()).get(rollNo.getKey()).get(year.getKey()).put(date.getKey(), new HashMap<>());
                                        for(DataSnapshot time:date.getChildren()){
                                            attendanceData.get(ba.getKey()).get(rollNo.getKey()).get(year.getKey()).get(date.getKey()).put(time.getKey(), time.getValue(String.class));
                                        }
                                    }
                                }
                            }
                        }
                    }


                }

                List<String> batches = new ArrayList<>(attendanceData.keySet());

                for(String batch:batches){
                    ref.child("Attendance").child(batch).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pre = new HashMap<>();
                            abs = new HashMap<>();
                            tot = new HashMap<>();
                            for(DataSnapshot year:snapshot.getChildren()){
                                for(DataSnapshot date: year.getChildren()){
                                    for(DataSnapshot time: date.getChildren()){
                                        for(DataSnapshot child:time.getChildren()){
                                            if(child.getKey().equals("count")){
                                                p = String.valueOf(child.child("presents").getValue(String.class));
                                                a = String.valueOf(child.child("absents").getValue(String.class));
                                                t = String.valueOf(child.child("total").getValue(String.class));
                                            }
                                            pre.put(time.getKey()+date.getKey(), p);
                                            abs.put(time.getKey()+date.getKey(), a);
                                            tot.put(time.getKey()+date.getKey(), t);

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

                pd1.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void requestForPermission() {
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
    }
}