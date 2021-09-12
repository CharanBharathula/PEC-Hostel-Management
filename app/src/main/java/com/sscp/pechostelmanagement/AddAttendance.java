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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddAttendance extends AppCompatActivity {


    DatabaseReference ref;
    ExpandingList mExpandingList;
    HashMap<String, List<String>> map;
    HashMap<String, HashMap<String, String>> attendance;
    ImageView upload;
    ProgressDialog pd;
    SimpleDateFormat _12HourSDF, _24HourSDF;
    Date _24HourDt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_attendance);

        mExpandingList = findViewById(R.id.expanding_list_main);
        upload = findViewById(R.id.uplaod_attendance);
        attendance = new HashMap<>();
        ref = FirebaseDatabase.getInstance().getReference();
        createItems();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd = new ProgressDialog(AddAttendance.this);
                pd.setTitle("Uploading");
                pd.setMessage("Please wait while the attendance is uploading");
                addAttendance();
            }
        });


    }

    public void createItems(){

        int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

        ref.child("RoomDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomNumbers:snapshot.getChildren()){

                    attendance.put(roomNumbers.getKey(), new HashMap<>());

                    for(DataSnapshot snap:roomNumbers.getChildren()){
                        //map.get(roomNumbers.getKey()).add(snap.child("1").getValue(String.class));
                        attendance.get(roomNumbers.getKey()).put(snap.getKey(), "Present");
                    }
                    int rnd = new Random().nextInt(colors.length);
                    addItem(roomNumbers.getKey(), new ArrayList<String>(attendance.get(roomNumbers.getKey()).keySet()) , colors[rnd]);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

            add.setOnClickListener(view -> showInsertDialog(str -> {
                View newSubItem = item.createSubItem();
                if(newSubItem != null)
                    configureSubItem(item, newSubItem, str, title.getText().toString());
            }));
            View rmv = (ImageView)item.findViewById(R.id.remove_item);
            rmv.setOnClickListener(view -> mExpandingList.removeItem(item));



        }
    }

    private void addAttendance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String[] newString = currentDateAndTime.split(",");
        newString[0] = newString[0].replace('.', '-');
        String[] timeArr = newString[1].split(":");
        String newTime = timeArr[0]+":"+timeArr[1];

        try {
            String _24HourTime = newTime;
            _24HourSDF = new SimpleDateFormat("HH:mm");
            _12HourSDF = new SimpleDateFormat("hh:mm a");

            _24HourDt = _24HourSDF.parse(_24HourTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String date = _12HourSDF.format(_24HourDt);
        ref.child("Attendance").child(newString[0]).child(date).setValue(attendance).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    pd.dismiss();
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
            }
        });



    }

    private void showInsertDialog(OnItemCreated onItemCreated) {
        EditText text = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(text);
        builder.setTitle("Add Item");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onItemCreated.itemCreated(text.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void configureSubItem(ExpandingItem item, View view, String s, String title) {

        TextView text = view.findViewById(R.id.sub_title);
        text.setText(s);
        ImageView imageView = view.findViewById(R.id.remove_sub_item);
        CheckBox check = view.findViewById(R.id.select_student);
        LinearLayout lay = view.findViewById(R.id.sub_item_layout);
        lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check.isChecked()){
                    check.setChecked(false);
                    if(attendance.containsKey(title)){
                        for (Map.Entry<String, HashMap<String, String>> entry : attendance.entrySet()) {
                            entry.getValue().remove(s);
                        }
                    }
                }
                else{
                    check.setChecked(true);
                    if(attendance.containsKey(title)){
                        if(attendance.get(title) != null){
                            attendance.get(title).put(s, "Absent");
                        }
                        else
                            attendance.get(title).put(s, "Present");
                    }
                    //item.removeSubItem(view);
                }
            }
        });

    }


}

interface OnItemCreated {
    public void itemCreated(String title);
}
