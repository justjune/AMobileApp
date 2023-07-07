package com.smallangrycoders.nevermorepayforwater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.time.LocalDateTime;
public class AddActivity extends Activity {
    private Button btSave,btCancel;
    private EditText etLoc,etLat,etLon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        btSave=(Button)findViewById(R.id.butSave);
        btCancel=(Button)findViewById(R.id.butCancel);
        etLoc=(EditText)findViewById(R.id.City);
        etLat=(EditText)findViewById(R.id.etLat);
        etLon=(EditText)findViewById(R.id.etLon);

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StCity stcity=new StCity(-1,etLoc.getText().toString(),"0", etLat.getText().toString(), etLon.getText().toString(), 1, LocalDateTime.now());
                Intent intent=getIntent();
                intent.putExtra("StCity", stcity);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
