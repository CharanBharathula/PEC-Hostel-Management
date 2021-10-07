package com.sscp.pechostelmanagement;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {
    Activity mContext;
    List<String> times;
    ListView timesListView;
    LinearLayout layout;
    TextView time, total, present, absent;
    int totalStudents, totalPresentees, totalAabsentees;
    String currentTime, student_batch, currentYear;

    HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>> attendanceData;

    public ListViewAdapter(Activity mContext, List<String> times, LinearLayout layout, ListView timesListView, HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>> attendanceData, String student_batch, String currentYear) {
        super(mContext, R.layout.listview_item, times);

        this.times = times;
        this.mContext = mContext;
        this.timesListView = timesListView;
        this.layout = layout;
        this.attendanceData = attendanceData;
        this.student_batch = student_batch;
        this.currentYear = currentYear;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=mContext.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_item, null,true);

        time = rowView.findViewById(R.id.time);
        total = rowView.findViewById(R.id.total);
        present = rowView.findViewById(R.id.present);
        absent = rowView.findViewById(R.id.absent);

        getAttendanceCount();

        return rowView;

    }

    private void getAttendanceCount() {
        totalStudents = attendanceData.get(student_batch).keySet().size();
        for(String roll:new ArrayList<>(attendanceData.get(student_batch).keySet())){
            for(String year:new ArrayList<>(attendanceData.get(student_batch).get(roll).keySet())){
                if(year.equals(currentYear)){
                    for(String date:new ArrayList<>(attendanceData.get(student_batch).get(roll).get(year).keySet())){
                        for(String t:new ArrayList<>(attendanceData.get(student_batch).get(roll).get(year).get(date).keySet())){
                            String val = attendanceData.get(student_batch).get(roll).get(year).get(date).get(t);
                            if(val.equals("Present"))
                                totalPresentees++;
                            else
                                totalAabsentees++;
                        }
                        total.setText(""+totalStudents);
                        present.setText(""+totalPresentees);
                        absent.setText(""+totalAabsentees);
                    }
                }
            }
        }
    }
}
