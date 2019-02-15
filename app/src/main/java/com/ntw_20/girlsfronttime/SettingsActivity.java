package com.ntw_20.girlsfronttime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private Switch intelligence_task;
    private Switch dailyTask;
    private Switch friendUse;
    private Switch like;
    private Switch battery;
    private Switch eventTask;
    private Spinner dropdown;
    private boolean isFirst = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        Language lang = new Language();
        super.attachBaseContext(lang.attachBaseContext(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        SharedPreferences pref = getSharedPreferences("setting", MODE_PRIVATE);

        this.intelligence_task = (Switch) findViewById(R.id.Intelligence_task);
        this.intelligence_task.setChecked(pref.getBoolean("intelligenceTask", true));
        setOnCheckedChangeListener(intelligence_task);

        this.dailyTask = (Switch) findViewById(R.id.Daily_task);
        this.dailyTask.setChecked(pref.getBoolean("dailyTask", true));
        setOnCheckedChangeListener(dailyTask);

        this.friendUse = (Switch) findViewById(R.id.Friend_use);
        this.friendUse.setChecked(pref.getBoolean("friendUse", true));
        setOnCheckedChangeListener(friendUse);

        this.like = (Switch) findViewById(R.id.Like);
        this.like.setChecked(pref.getBoolean("like", true));
        setOnCheckedChangeListener(like);

        this.battery = (Switch) findViewById(R.id.Battery);
        this.battery.setChecked(pref.getBoolean("battery", true));
        setOnCheckedChangeListener(battery);

        this.eventTask = (Switch) findViewById(R.id.Event_task);
        this.eventTask.setChecked(pref.getBoolean("eventTask", true));
        setOnCheckedChangeListener(eventTask);


        this.dropdown = (Spinner)findViewById(R.id.spinnerLanguage);
        String[] items = new String[]{getResources().getString(R.string.tw), getResources().getString(R.string.zh), getResources().getString(R.string.ja)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        String lang = pref.getString("lang", "tw");

        switch (lang){
            case "tw":
                dropdown.setSelection(0);
                break;
            case "zh":
                dropdown.setSelection(1);
                break;
            case "ja":
                dropdown.setSelection(2);
                break;
        }

        final Context finalContext = this;
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.i("ntw-20.com","onItemSelected");

                if(isFirst){
                    setIsFirst(false);
                return;
                }

                SharedPreferences pref = getSharedPreferences("setting", MODE_PRIVATE);

                String[] lang = {"tw","zh","ja"};

                pref.edit().putString("lang", lang[position]).apply();

                Toast.makeText(parentView.getContext(),getResources().getString(R.string.success),Toast.LENGTH_SHORT).show();

                Log.i("ntw-20.com","attachBaseContext");

                Intent intent = Intent.makeRestartActivityTask(new ComponentName(finalContext, MainActivity.class));
                startActivity(intent);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Log.i("ntw-20.com","onNothingSelected");
            }
        });
    }


    private void setIsFirst(Boolean isFirst){
        this.isFirst = isFirst;

    }

    private void setOnCheckedChangeListener(Switch switchObj){
        switchObj.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String name = "";
                switch (buttonView.getId()) {
                    case R.id.Daily_task:
                        name = "dailyTask";
                        break;
                    case R.id.Friend_use:
                        name = "friendUse";
                        break;
                    case R.id.Like:
                        name = "like";
                        break;
                    case R.id.Battery:
                        name = "battery";
                        break;
                    case R.id.Event_task:
                        name = "eventTask";
                        break;
                    case R.id.Intelligence_task:
                        name = "intelligenceTask";
                        break;
                    default:
                        return;
                }


                SharedPreferences pref = getSharedPreferences("setting", MODE_PRIVATE);
                pref.edit().putBoolean(name, buttonView.isChecked()).apply();

                Toast.makeText(SettingsActivity.this,  getResources().getText(R.string.success), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.setting);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
