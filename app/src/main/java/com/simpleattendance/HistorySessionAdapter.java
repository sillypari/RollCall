package com.simpleattendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistorySessionAdapter extends RecyclerView.Adapter<HistorySessionAdapter.ViewHolder> {

    private List<AttendanceSession> sessionList;

    public HistorySessionAdapter(List<AttendanceSession> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_history_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceSession session = sessionList.get(position);
        
        holder.sessionTitle.setText(session.getClassName());
        holder.sessionSubject.setText(session.getSubject());
        holder.sessionDate.setText(session.getDate());
        holder.presentCount.setText(session.getPresentCount() + " Present");
        holder.absentCount.setText(session.getAbsentCount() + " Absent");
        holder.attendancePercentage.setText(session.getAttendancePercentage() + "%");
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sessionTitle, sessionSubject, sessionDate;
        TextView presentCount, absentCount, attendancePercentage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionTitle = itemView.findViewById(R.id.sessionTitle);
            sessionSubject = itemView.findViewById(R.id.sessionSubject);
            sessionDate = itemView.findViewById(R.id.sessionDate);
            presentCount = itemView.findViewById(R.id.presentCount);
            absentCount = itemView.findViewById(R.id.absentCount);
            attendancePercentage = itemView.findViewById(R.id.attendancePercentage);
        }
    }
}