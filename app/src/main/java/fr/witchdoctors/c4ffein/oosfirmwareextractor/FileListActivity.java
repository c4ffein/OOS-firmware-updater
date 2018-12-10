package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;

public class FileListActivity extends AppCompatActivity {
    FileListRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        Intent intent = getIntent();
        ArrayList<LevelAndName> fileList = intent.getParcelableArrayListExtra("fileList");
        String updaterScript = intent.getStringExtra("updaterScript");

        TextView updaterScriptView = findViewById(R.id.updaterScriptView);
        updaterScriptView.setText(updaterScript);
        updaterScriptView.setHorizontallyScrolling(true); // Apparently need this since XML is buggy

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileListRecyclerViewAdapter(this, fileList);
        recyclerView.setAdapter(adapter);
    }
}