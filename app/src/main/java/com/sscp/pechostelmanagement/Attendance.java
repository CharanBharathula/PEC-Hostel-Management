package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Attendance extends AppCompatActivity {

    ImageView consolidated_reports, specific_reports, check_attendance;
    String batch, y = null;

    XSSFWorkbook workbook;
    XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
    File filePath;
    ProgressDialog pd;
    AlertDialog alert;

    DatabaseReference ref;
    HashMap<String, StudentClass> studentDetails;
    HashMap<String, HashMap<String, HashMap<String, String>>> data;
    private boolean flag;
    int count = 0, totalChilds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_attendance);

        consolidated_reports = findViewById(R.id.consolidated_attendance_report);
        specific_reports = findViewById(R.id.specific_attendance_report);
        check_attendance = findViewById(R.id.check_att);
        ref = FirebaseDatabase.getInstance().getReference();
        data = new HashMap<>();
        studentDetails = new HashMap<>();
        workbook = new XSSFWorkbook();
        pd = new ProgressDialog(this);

        consolidated_reports.setOnClickListener(v->{
            openDialog();
        });

        specific_reports.setOnClickListener(v->{

        });
        check_attendance.setOnClickListener(v->{
            startActivity(new Intent(Attendance.this, AttendanceDateDetails.class));
        });

    }

    private void openDialog() {
        View v=getLayoutInflater().inflate(R.layout.consolidated_reports_inputs,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(Attendance.this);

        builder.setView(v);

        alert=builder.create();
        Spinner sp = v.findViewById(R.id.select_batch);
        EditText year = v.findViewById(R.id.choose_year);
        Button submit = v.findViewById(R.id.retrive_consolidated_reports);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                batch = adapterView.getItemAtPosition(i).toString();
                if(!adapterView.getItemAtPosition(i).toString().equals("Choose Batch *")){
                    batch = adapterView.getItemAtPosition(i).toString();
                    retrieveStudent();
                    flag = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        submit.setOnClickListener(view->{
            pd.setTitle("Downloading");
            pd.setMessage("Please wait the data was downloading");
            pd.show();
            y = year.getText().toString();
            if(y != null && flag){
                retrieveData(y);
                filePath = new File(Environment.getExternalStorageDirectory()+"/PEC Hostel Attendance Consolidated"+batch+", "+y+".xls");
            }
            else
                Toast.makeText(getApplicationContext(), "Please choose batch", Toast.LENGTH_SHORT).show();
        });

        alert.show();
    }

    private void retrieveData(String year) {

        ref.child("Students").child(batch).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rollNo:snapshot.getChildren()){
                    totalChilds = (int) snapshot.getChildrenCount();
                    count++;
                    data.put(rollNo.getKey(), new HashMap<>());
                    for(DataSnapshot child:rollNo.getChildren()){
                        if(child.getKey().equals("Attendance")){
                            for(DataSnapshot ye:child.getChildren()){
                                if(ye.getKey().equals(year)){
                                    for(DataSnapshot date:ye.getChildren()){
                                        data.get(rollNo.getKey()).put(date.getKey(), new HashMap<>());
                                        for(DataSnapshot time:date.getChildren()){
                                            data.get(rollNo.getKey()).get(date.getKey()).put(time.getKey(), time.getValue(String.class));
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if(data != null)
                        if(count >= totalChilds) {
                            addDataToExcel();
                            alert.dismiss();
                            pd.dismiss();
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addDataToExcel() {

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

        row = sheet.createRow(0);

        cell = row.createCell(0);
        cell.setCellValue("Name");

        cell = row.createCell(1);
        cell.setCellValue("Roll No");

        cell = row.createCell(2);
        cell.setCellValue("RoomNo");

        cell = row.createCell(3);
        cell.setCellValue("Mobile No");

        cell = row.createCell(4);
        cell.setCellValue("Date and Time");

        int n = 1;
        List<String> keys = new ArrayList<>(data.keySet());

        for(String rollNo:keys){

            row = sheet.createRow(n);

            sheet.setColumnWidth(0, 4000);
            cell = row.createCell(0);
            cell.setCellValue(studentDetails.get(rollNo).getStudentname());

            sheet.setColumnWidth(1, 4000);
            cell = row.createCell(1);
            cell.setCellValue(rollNo);

            sheet.setColumnWidth(2, 4000);
            cell = row.createCell(2);
            cell.setCellValue(studentDetails.get(rollNo).getRoom_no());

            sheet.setColumnWidth(3, 4000);
            cell = row.createCell(3);
            cell.setCellValue(studentDetails.get(rollNo).getMobile());

            List<String> dates = new ArrayList<>(data.get(rollNo).keySet());

            int c = 4;

            for(String date:dates){
                sheet.setColumnWidth(c, 6000);
                cell = row.createCell(c);
                List<String> times = new ArrayList<>(data.get(rollNo).get(date).keySet());
                StringBuilder val = new StringBuilder("");
                for(String time:times){
                    val.append(time);
                    val.append("-");
                    String att = data.get(rollNo).get(date).get(time);
                    val.append(att);
                }
                cell.setCellValue(date+"\n"+val+"\n");
                cell.setCellStyle(style);
                c++;
            }

            n++;
        }

        try {
            if(!filePath.exists()){
                filePath.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(filePath);
            workbook.write(fOut);
            Toast.makeText(getApplicationContext(), "Excel file downloaded successfully", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            fOut.flush();
            fOut.close();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception:"+e, Toast.LENGTH_SHORT).show();
        }

    }

    private void retrieveStudent() {
        ref.child("Students").child(batch).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot rNo:snapshot.getChildren()){
                    studentDetails.put(rNo.getKey(), rNo.getValue(StudentClass.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}