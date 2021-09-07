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

    /*ExpandableListView expandableListView;
    ExpandableListViewAdapter expandableListViewAdapter;

    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    public ArrayList<String> list = new ArrayList<>();*/
    DatabaseReference ref;
    ExpandingList mExpandingList;
    HashMap<String, List<String>> map, attendance;
    ImageView upload;
    ProgressDialog pd;
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
        map = new HashMap<>();

        int[] colors = {R.color.black, R.color.blue, R.color.yellow, R.color.orange, R.color.pink};

        ref.child("RoomDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot roomNumbers:snapshot.getChildren()){
                    map.put(roomNumbers.getKey(), new ArrayList<String>());
                    attendance.put(roomNumbers.getKey(), new ArrayList<String>());
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
            ImageView add = item.findViewById(R.id.add_more_sub_items);

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInsertDialog(new OnItemCreated(){
                        @Override
                        public void itemCreated(String str) {
                            View newSubItem = item.createSubItem();
                            configureSubItem(item, newSubItem, str, title.getText().toString());
                        }
                    });
                }
            });
            View rmv = (ImageView)item.findViewById(R.id.remove_item);
            rmv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mExpandingList.removeItem(item);
                }
            });



        }
    }

    private void addAttendance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String[] newString = currentDateAndTime.split(",");
        newString[0] = newString[0].replace('.', '-');
        ref.child("Attendance").child(newString[0]).child(newString[1]).setValue(attendance).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        if(attendance.get(title) != null){
                            for (Map.Entry<String, List<String>> entry : attendance.entrySet()) {
                                entry.getValue().remove(s);
                            }
                        }
                    }
                }
                else{
                    check.setChecked(true);
                    if(attendance.containsKey(title)){
                        if(attendance.get(title) != null){
                            attendance.get(title).add(s);
                        }
                    }
                    //item.removeSubItem(view);
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item != null){
                    if(attendance.containsKey(title)){
                        if(attendance.get(title) != null){
                            attendance.get(title).add(s);
                        }
                    }
                    item.removeSubItem(view);
                }
            }
        });

    }


    private void retriveData() {
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RoomDetails");

        /*ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomKeys:snapshot.getChildren()){
                    expandableListDetail.put(roomKeys.getKey(), new ArrayList<>());
                    for(DataSnapshot studentKeys:roomKeys.getChildren()){
                        expandableListDetail.get(roomKeys.getKey()).add(studentKeys.child("1").getValue(String.class));
                    }
                }
                expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());

                expandableListViewAdapter = new ExpandableListViewAdapter(getApplicationContext(), expandableListTitle, expandableListDetail, list, new AddAttendance());
                expandableListView.setAdapter(expandableListViewAdapter);
                Toast.makeText(getApplicationContext(), ""+list, Toast.LENGTH_SHORT).show();
                expandableListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

}

interface OnItemCreated {
    public void itemCreated(String title);
}
