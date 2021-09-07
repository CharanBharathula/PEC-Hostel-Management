package com.sscp.pechostelmanagement;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    Context mContext;
    List<String> roomNumbers;
    HashMap<String, List<String>> rollNumbers;
    List<String> presentees;
    AddAttendance obj;
    String key;
    public ExpandableListViewAdapter(Context mContext, List<String> roomNumbers, HashMap<String, List<String>> rollNumbers, ArrayList<String> list, AddAttendance addAttendance) {
        this.mContext = mContext;
        this.roomNumbers = roomNumbers;
        this.rollNumbers = rollNumbers;
        presentees = list;
        obj = addAttendance;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.rollNumbers.get(this.roomNumbers.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_child_item, null);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.expandedListItem);
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.check_box);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Attendance");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
                String currentDateAndTime = sdf.format(new Date());
                String[] newString = currentDateAndTime.split(",");
                newString[0] = newString[0].replace('.', '-');

                if(checkbox.isChecked()) {
                    checkbox.setChecked(false);
                    presentees.remove(expandedListText);
                    ref.child(key).child(newString[0]).child(newString[1]).removeValue();
                }
                else {
                    checkbox.setChecked(true);
                    ref.child(key).child(newString[0]).child(expandedListText).setValue(newString[1]);
                    presentees.add(expandedListText);
                }
            }
        });

        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.rollNumbers.get(this.roomNumbers.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.roomNumbers.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.roomNumbers.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        key = roomNumbers.get(groupPosition);
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_header, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
