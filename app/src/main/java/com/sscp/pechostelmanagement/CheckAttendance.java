package com.sscp.pechostelmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
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
import java.util.Random;

public class CheckAttendance extends AppCompatActivity {

    ExpandingList mExpandingList;
    HashMap<String, List<String>> map;
    HashMap<String, HashMap<String, String>> attendance;
    DatabaseReference ref;
    HashMap<String, StudentClass> studentDetails;
    HashMap<String, String> data;

    boolean flag;
    String datePicked, timePicked;
    ImageView downloadAttendance;
    String year;

    XSSFWorkbook workbook;
    XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
    File filePath;
    private String batch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_check_attendance);

        requestForPermission();
        Initialization();
        retrieveData(datePicked, timePicked);

        downloadAttendance.setOnClickListener(view -> {
            if(datePicked == null || timePicked == null)
                Toast.makeText(getApplicationContext(), "Please choose date and time first", Toast.LENGTH_SHORT).show();
            else{
                addDataToExcel();
            }

        });
    }

    private void Initialization() {
        mExpandingList = findViewById(R.id.expanding_list);
        attendance = new HashMap<>();
        ref = FirebaseDatabase.getInstance().getReference();
        downloadAttendance = findViewById(R.id.download_attendance);
        studentDetails = new HashMap<>();
        data = new HashMap<>();

        Intent i = getIntent();
        datePicked = i.getStringExtra("date");
        timePicked = i.getStringExtra("time");
        batch = i.getStringExtra("batch");
        year = i.getStringExtra("year");

        filePath = new File(Environment.getExternalStorageDirectory()+"/Attendance on "+datePicked+".xls");

        map = new HashMap<>();
        //Initializing objects for creating excel File
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();


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

    private void addDataToExcel() {
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
        cell.setCellValue("Attendance");


        int n = 1;
        List<String> keys = new ArrayList<>(attendance.keySet());

        for(int i = 0;i<attendance.size();i++){

            String roomNo = keys.get(i);
            for(String rollNo:new ArrayList<>(Objects.requireNonNull(attendance.get(roomNo)).keySet())){

                row = sheet.createRow(n);

                sheet.setColumnWidth(0, 4000);
                cell = row.createCell(0);
                cell.setCellValue(Objects.requireNonNull(studentDetails.get(rollNo)).getStudentname());

                sheet.setColumnWidth(1, 4000);
                cell = row.createCell(1);
                cell.setCellValue(rollNo);

                sheet.setColumnWidth(2, 4000);
                cell = row.createCell(2);
                cell.setCellValue(roomNo);

                sheet.setColumnWidth(3, 4000);
                cell = row.createCell(3);
                cell.setCellValue(Objects.requireNonNull(studentDetails.get(rollNo)).getMobile());

                sheet.setColumnWidth(4, 4000);
                cell = row.createCell(4);
                cell.setCellValue(datePicked+"\n"+timePicked+"-"+ Objects.requireNonNull(attendance.get(roomNo)).get(rollNo));
                cell.setCellStyle(style);
                n++;
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

    public void createItems(){

        int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

        List<String> keys = new ArrayList<>(attendance.keySet());
        for(String roomNo:keys) {
            int rnd = new Random().nextInt(colors.length);
            addItem(roomNo, attendance.get(roomNo), colors[rnd]);
        }

    }

    public void retrieveData(String searched_date, String search_time){

        flag = false;
        ref.child("Attendance").child(batch).child(year).child(searched_date).child(search_time).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomNumbers:snapshot.getChildren()){
                    attendance.put(roomNumbers.getKey(), new HashMap<>());
                    flag = true;
                    for(DataSnapshot rollNumber:roomNumbers.getChildren()){
                        String rNo = roomNumbers.getKey();
                        Objects.requireNonNull(attendance.get(rNo)).put(rollNumber.getKey(), rollNumber.getValue(String.class));
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
                for(DataSnapshot rNo:snapshot.getChildren()){
                    studentDetails.put(rNo.getKey(), rNo.getValue(StudentClass.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addItem(String s, HashMap<String, String> arr1, int color) {

        ExpandingItem item = mExpandingList.createNewItem(R.layout.expanding_layout);
        if(item != null){
            item.setIndicatorColorRes(color);
            item.setIndicatorIconRes(R.drawable.ic_person);
            TextView title = item.findViewById(R.id.title);
            title.setText(s);
            item.createSubItems(arr1.size());
            List<String> keys = new ArrayList<>(arr1.keySet());
            for(int i = 0;i<item.getSubItemsCount();i++){
                View view = item.getSubItemView(i);
                configureSubItem(view, keys.get(i), title.getText().toString());
            }
            View rmv = (ImageView)item.findViewById(R.id.remove_item);
            rmv.setVisibility(View.GONE);
        }
    }

    private void configureSubItem(View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);
        String isAbsent = Objects.requireNonNull(attendance.get(title)).get(s);

        assert isAbsent != null;
        if(isAbsent.equalsIgnoreCase("Absent"))
            text.setTextColor(getResources().getColor(R.color.red));
        else
            text.setTextColor(getResources().getColor(R.color.green));

        text.setText(s);
        text.setOnClickListener(view1 -> retrieveStudent(text.getText().toString()));
        ImageView imageView = view.findViewById(R.id.remove_sub_item);
        CheckBox check = view.findViewById(R.id.select_student);
        check.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);


    }

    private void retrieveStudent(String rollNo) {
        StudentClass student = studentDetails.get(rollNo);

        View view=getLayoutInflater().inflate(R.layout.student_layout,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(CheckAttendance.this);
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
        branch.setText(student.getRoom_no());
    }

}
