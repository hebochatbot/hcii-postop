package postop.hcii.hebo;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class ProfileActivity extends AppCompatActivity {

    private TextView dateText, timeText;
    private SharedPreferences sharedPref;
    private Spinner bodyPartSpinner, clinicSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPref = this.getSharedPreferences("profile", Context.MODE_PRIVATE);

        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);

        Calendar c = Calendar.getInstance();
        String currentDate = getDate(c);
        String currentTime = getTime(c);

        // read from memory
        String date = sharedPref.getString("date", "Date");
        String time = sharedPref.getString("time", "Time");
        String bodyPart = sharedPref.getString("bodyPart", "Surgery site");
        String clinic = sharedPref.getString("clinic", "Clinic");
        int bodyPartIndex = sharedPref.getInt("bodyPartIndex", -1);
        int clinicIndex = sharedPref.getInt("clinicIndex", -1);
        Log.d("date", date);
        Log.d("time", time);
        Log.d("bodyPart", bodyPart);
        Log.d("clinic", clinic);

        int profileScore = 0;
        profileScore = getUpdatedProfileScore(profileScore, bodyPart, date, time, clinic);

        if (isFilled(profileScore)) {
            Button saveButton = (Button) findViewById(R.id.button);
            saveButton.setEnabled(true);
            saveButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("profileScore", profileScore);
            editor.commit();
        }

        dateText.setText(date);
        timeText.setText(time);

        // Body part spinner
        final List<String> bodyPartsList = Arrays.asList(getResources().getStringArray(R.array.body_parts));
        bodyPartSpinner = (Spinner) findViewById(R.id.surgery_area);
        setupSpinner(bodyPartSpinner, bodyPartsList, Config.PROFILE_SITE);
        if (bodyPartIndex > 0) {
            bodyPartSpinner.setSelection(bodyPartIndex);
        }

        // Clinic spinner
        final List<String> clinicPartList = Arrays.asList(getResources().getStringArray(R.array.clinics));
        clinicSpinner = (Spinner) findViewById(R.id.clinics);
        setupSpinner(clinicSpinner, clinicPartList, Config.PROFILE_CLINIC);
        if (clinicIndex > 0) {
            clinicSpinner.setSelection(clinicIndex);
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void saveProfile(View v) {
        SharedPreferences.Editor editor = sharedPref.edit();
        String date = dateText.getText().toString();
        String time = timeText.getText().toString();
        String selectedBodyPart = (String) bodyPartSpinner.getSelectedItem().toString();
        int selectedBodyPartIndex = bodyPartSpinner.getSelectedItemPosition();
        String selectedClinic = (String) clinicSpinner.getSelectedItem().toString();
        int selectedClinicIndex = clinicSpinner.getSelectedItemPosition();


        editor.putString("date", date);
        editor.putString("time", time);
        editor.putString("bodyPart", selectedBodyPart);
        editor.putInt("bodyPartIndex", selectedBodyPartIndex);
        editor.putString("clinic", selectedClinic);
        editor.putInt("clinicIndex", selectedClinicIndex);
        editor.commit();
        finish();
    }

    private String getDate(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        return Integer.toString(month+1) + "/" + Integer.toString(day) + "/" + Integer.toString(year);
    }

    private String getTime(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        return Integer.toString(hour) + ":" + Integer.toString(minute) + am_pm;
    }

    private void setupSpinner(Spinner spinner, List<String> list, final int profileConstant) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, list) {
            @Override
            public boolean isEnabled(int position){
                return (position != 0);
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    int profileScore = sharedPref.getInt("profileScore", 0);
                    profileScore |= profileConstant;

                    if (ProfileActivity.isFilled(profileScore)) {
                        Button saveButton = (Button) findViewById(R.id.button);
                        saveButton.setEnabled(true);
                        saveButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("profileScore", profileScore);
                        editor.commit();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getUpdatedProfileScore(int score, String site, String date, String time, String clinic) {
        // if not default values (meaning field is not selected yet), update profile score
        if (!date.equals("Date")) {
            score |= Config.PROFILE_DATE;
        }

        if (!time.equals("Time")) {
            score |= Config.PROFILE_TIME;
        }

        if (!site.equals("Surgery site")) {
            score |= Config.PROFILE_SITE;
        }

        if (!clinic.equals("Clinic")) {
            score |= Config.PROFILE_CLINIC;
        }

        return score;
    }

    public static boolean isFilled(int score) {
        return ((Config.PROFILE_COMPLETE & score) == Config.PROFILE_COMPLETE);
    }
}
