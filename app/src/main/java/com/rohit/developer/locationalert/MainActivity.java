package com.rohit.developer.locationalert;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText editText1,editText2;
    String regexStr = "[-+]?[0-9]*\\.?[0-9]*";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        editText1=(EditText) findViewById(R.id.editText);
        editText2=(EditText) findViewById(R.id.editText2);
    }

    public void onClick(View v){

        String location=editText1.getText().toString();
        String dis=editText2.getText().toString();


          if("".equals(dis) || "".equals(location)){
              Toast.makeText(this,"Empty field",Toast.LENGTH_SHORT).show();
          }
          else if(!(dis.trim().matches(regexStr))){
              Toast.makeText(this,"Enter valid distance",Toast.LENGTH_SHORT).show();
          }
        else {
              float distance = Float.parseFloat(dis);

              Intent intent = new Intent(this, MapsActivity.class);

              intent.putExtra("Loc", location);
              intent.putExtra("Dis", distance);

              startActivity(intent);
          }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.voice_search:

                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Listening...");

                startActivityForResult(i, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==1){

            if(resultCode==RESULT_OK){

                ArrayList<String> textMatchList= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if(editText1.isFocused())
                        editText1.setText(textMatchList.get(0).toString());
                    else if(editText2.isFocused())
                        editText2.setText(textMatchList.get(0).toString());


            }
        }
    }

}
