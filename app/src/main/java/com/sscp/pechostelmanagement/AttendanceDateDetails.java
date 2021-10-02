package com.sscp.pechostelmanagement;

import static org.apache.poi.hssf.usermodel.HSSFFont.*;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AttendanceDateDetails extends AppCompatActivity {

    DatabaseReference ref;
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
    File filePath;
    ProgressDialog pd, pd1;

    HashMap<String, List<String>> dates;
    HashMap<String, StudentClass> studentDetails;
    HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> data;

    String fromDatePicked;
    String batch;
    private int mMonth, mDay,  mYear;

    ImageView download;
    ListView listView;
    Spinner sp;
    Button chooseFromDate, chooseToDate;
    ExpandingList list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_attendance_date_details);

        Initialization();
        pd1 = new ProgressDialog(this);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                batch = adapterView.getItemAtPosition(i).toString();
                if(!batch.equals("Choose Batch")){
                    filePath = new File(Environment.getExternalStorageDirectory()+"/PEC HAC Report Batch Wise for "+batch+".xls");

                    pd1.setTitle("Retrieving Data");
                    pd1.setMessage("Please Wait while fetching the attendance data");
                    pd1.setCancelable(false);
                    pd1.show();
                    retrieveData(batch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        download.setOnClickListener(v -> {
            if(dates.size() != 0){
                pd.setTitle("Downloading");
                pd.setMessage("Please wait the data was downloading");
                pd.setCancelable(false);
                pd.show();
                addDataToExcel();
            }
            else
                Toast.makeText(getApplicationContext(), "There is data about attendance", Toast.LENGTH_SHORT).show();
        });

        /*chooseToDate.setOnClickListener(v->{
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
                        toDatePicked = year + "-" + (m) + "-" + dayOfMonth;
                        retrieveData(y, fromDatePicked, toDatePicked);

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });*/

        chooseFromDate.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String m = null, d = null;
                        if(monthOfYear+1 < 10)
                            m = "0"+(monthOfYear+1);
                        if(dayOfMonth < 10)
                            d = "0"+dayOfMonth;
                        fromDatePicked = year + "-" + m + "-" + dayOfMonth;
                        retrieveTimeDetails(String.valueOf(year), fromDatePicked);
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        });

    }

    private void Initialization() {
        chooseFromDate = findViewById(R.id.fromDatePick);
        chooseToDate = findViewById(R.id.toDatePick);
        listView = findViewById(R.id.date_details);
        ref = FirebaseDatabase.getInstance().getReference();
        download = findViewById(R.id.download_entire_attendance);
        workbook = new XSSFWorkbook();
        studentDetails = new HashMap<>();
        download = findViewById(R.id.download_entire_attendance);
        sp = findViewById(R.id.select_y);
        list = findViewById(R.id.expanding);
        dates = new HashMap<>();
        pd = new ProgressDialog(this);
    }

    public void retrieveData(String batch){
        dates = new HashMap<>();

        if(batch != null) {
            ref.child("Attendance").child(batch).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot year:snapshot.getChildren()){
                        dates.put(year.getKey(), new ArrayList<>());
                        for (DataSnapshot date : year.getChildren()) {
                            dates.get(year.getKey()).add(date.getKey());
                        }
                    }
                    createItems();
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
                    retrieveAttendance();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else
            Toast.makeText(getApplicationContext(), "Please choose year", Toast.LENGTH_SHORT).show();

    }

    private void addDataToExcel() {

        List<String> years = new ArrayList<>(dates.keySet());

        for(String year:years)
        {
            int sheetCount = workbook.getNumberOfSheets();

            boolean flag = false;
            for(int i = 0;i<sheetCount;i++){
                if(workbook.getSheetName(i).equals(batch)) {
                    flag = true;
                    sheet = workbook.getSheet(batch);
                }
            }
            if(!flag)
                sheet = workbook.createSheet(year);
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
                if(data.get(rollNo).keySet().contains(year)){

                    row = sheet.createRow(n);

                    sheet.setColumnWidth(0, 4000);
                    cell = row.createCell(0);
                    cell.setCellValue(Objects.requireNonNull(studentDetails.get(rollNo)).getStudentname());

                    sheet.setColumnWidth(1, 4000);
                    cell = row.createCell(1);
                    cell.setCellValue(rollNo);

                    sheet.setColumnWidth(2, 4000);
                    cell = row.createCell(2);
                    cell.setCellValue(studentDetails.get(rollNo).getRoom_no());

                    sheet.setColumnWidth(3, 4000);
                    cell = row.createCell(3);
                    cell.setCellValue(Objects.requireNonNull(studentDetails.get(rollNo)).getMobile());

                    List<String> currentDates = new ArrayList<>(data.get(rollNo).get(year).keySet());

                    int c = 4;

                    for(String date:currentDates){
                        sheet.setColumnWidth(c, 6000);
                        cell = row.createCell(c);
                        List<String> times = new ArrayList<>(data.get(rollNo).get(year).get(date).keySet());
                        StringBuilder val = new StringBuilder("");
                        for(String time:times){
                            val.append(time);
                            val.append("-");
                            String att = data.get(rollNo).get(year).get(date).get(time);
                            val.append(att);
                        }
                        cell.setCellValue(date+"\n"+val+"\n");
                        cell.setCellStyle(style);
                        c++;
                    }

                    n++;
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
            pd.dismiss();
            fOut.flush();
            fOut.close();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception:"+e, Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }

    }

    private void retrieveAttendance() {
        List<String> keys = new ArrayList<>(studentDetails.keySet());
        data = new HashMap<>();
        if(keys.size() > 0){
            for(String rollNo:keys){
                ref.child("Students").child(batch).child(rollNo).child("Attendance").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        data.put(rollNo, new HashMap<>());
                        for(DataSnapshot year:snapshot.getChildren()){
                            data.get(rollNo).put(year.getKey(), new HashMap<>());
                            for(DataSnapshot date:year.getChildren()){
                                data.get(rollNo).get(year.getKey()).put(date.getKey(), new HashMap<>());
                                for(DataSnapshot time:date.getChildren()){
                                    data.get(rollNo).get(year.getKey()).get(date.getKey()).put(time.getKey(), time.getValue(String.class));
                                }
                            }
                        }
                        pd1.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        else
            Toast.makeText(getApplicationContext(), "No Students are there", Toast.LENGTH_SHORT).show();
    }

    public void createItems(){

        int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

        List<String> keys = new ArrayList<>(dates.keySet());
        for(String year:keys) {
            int rnd = new Random().nextInt(colors.length);
            addItem(year, dates.get(year), colors[rnd]);
        }

    }

    private void addItem(String s, List<String> d, int color) {

        ExpandingItem item = list.createNewItem(R.layout.expanding_layout);
        if(item != null){
            item.setIndicatorColorRes(color);
            item.setIndicatorIconRes(R.drawable.ic_person);
            TextView title = item.findViewById(R.id.title);
            title.setText(s);

            item.createSubItems(d.size());
            for(int i = 0;i<item.getSubItemsCount();i++){
                View view = item.getSubItemView(i);
                configureSubItem(view, d.get(i), s);
            }
            View rmv = (ImageView)item.findViewById(R.id.remove_item);
            rmv.setVisibility(View.GONE);
        }
    }

    private void configureSubItem(View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);

        text.setText(s);

        text.setOnClickListener(view1 -> {
            String selectedItem = text.getText().toString();
            retrieveTimeDetails(title, selectedItem);

        });

        ImageView imageView = view.findViewById(R.id.remove_sub_item);
        CheckBox check = view.findViewById(R.id.select_student);
        check.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
    }

    private void retrieveTimeDetails(String year, String date) {
        View v=getLayoutInflater().inflate(R.layout.activity_attendance_time_details,null);

        AlertDialog.Builder builder=new AlertDialog.Builder(AttendanceDateDetails.this);

        builder.setView(v);

        final AlertDialog alert=builder.create();

        ListView l = v.findViewById(R.id.time_details);
        alert.show();

        ArrayList<String> times = new ArrayList<>();

        ref.child("Attendance").child(batch).child(year).child(date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot time:snapshot.getChildren()){
                    times.add(time.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AttendanceDateDetails.this, android.R.layout.simple_list_item_1, times);
                l.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        l.setOnItemClickListener((adapterView, view, i1, le) -> {
            Intent intent = new Intent(AttendanceDateDetails.this, CheckAttendance.class);
            String time = adapterView.getItemAtPosition(i1).toString();

            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("batch", batch);
            intent.putExtra("year", year);
            intent.putExtras(intent);
            alert.dismiss();
            startActivity(intent);

        });
    }

    /*
    public static void readExcelFromStorage(Context context, String fileName) {
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + file);

            // Create instance having reference to .xls file
            workbook = new HSSFWorkbook(fileInputStream);

            // Fetch sheet at position 'i' from the workbook
            sheet = workbook.getSheetAt(0);

            // Iterate through each row
            for (Row row : sheet) {
                if (row.getRowNum() > 0) {
                    // Iterate through all the cells in a row (Excluding header row)
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        // Check cell type and format accordingly
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                // Print cell value
                                System.out.println(cell.getNumericCellValue());
                                break;

                            case Cell.CELL_TYPE_STRING:
                                System.out.println(cell.getStringCellValue());
                                break;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error Reading Exception: ", e);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to read file due to Exception: ", e);
                } finally {
                    try {
                        if (null != fileInputStream) {
                            fileInputStream.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
*/


}