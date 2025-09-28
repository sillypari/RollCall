package com.simpleattendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorySessionAdapter extends RecyclerView.Adapter<HistorySessionAdapter.ViewHolder> {

    private List<AttendanceSession> sessionList;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(AttendanceSession session);
    }

    public HistorySessionAdapter(List<AttendanceSession> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
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
        holder.sessionSubject.setText("General"); // Since we removed subject selection
        holder.sessionDate.setText(formatDate(session.getDate()) + " • " + session.getTime());
        holder.presentCount.setText(session.getPresentCount() + " Present");
        holder.absentCount.setText(session.getAbsentCount() + " Absent");
        holder.attendancePercentage.setText(String.format("%.0f%%", session.getAttendancePercentage()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(session);
            }
        });
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original if parsing fails
        }
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