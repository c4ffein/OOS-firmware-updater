package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

// Adapted from https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
public class FileListRecyclerViewAdapter extends RecyclerView.Adapter<FileListRecyclerViewAdapter.ViewHolder> {
    private List<LevelAndName> mData;
    private LayoutInflater mInflater;

    FileListRecyclerViewAdapter(Context context, List<LevelAndName> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LevelAndName levelAndName = mData.get(position);
        holder.myTextView.setText(levelAndName.getName());

        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(holder.myTextView.getLayoutParams());
        marginParams.setMargins(levelAndName.getLevel() * 62, 0, 0, 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);
        holder.myTextView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvAnimalName);
        }
    }
}