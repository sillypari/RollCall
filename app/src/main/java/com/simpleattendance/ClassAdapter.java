package com.simpleattendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {
    
    private List<ClassModel> classList;
    private OnClassClickListener listener;

    public interface OnClassClickListener {
        void onClassClick(ClassModel classModel);
    }

    public ClassAdapter(List<ClassModel> classList, OnClassClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);
        holder.bind(classModel, listener);
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView classNameText;
        TextView createdDateText;

        ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            classNameText = itemView.findViewById(R.id.classNameText);
            createdDateText = itemView.findViewById(R.id.createdDateText);
        }

        void bind(ClassModel classModel, OnClassClickListener listener) {
            classNameText.setText(classModel.getDisplayName());
            createdDateText.setText("Created: " + classModel.getCreatedDate());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClassClick(classModel);
                    }
                }
            });
        }
    }
}