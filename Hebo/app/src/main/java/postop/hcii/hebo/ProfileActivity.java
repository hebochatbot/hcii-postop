package postop.hcii.hebo;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView dateText;
    private TextView timeText;
    private SharedPreferences sharedPref;
    private Spinner bodyPartSpinner;

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
        String date = sharedPref.getString("date", currentDate);
        String time = sharedPref.getString("time", currentTime);
        String bodyPart = sharedPref.getString("bodyPart", "NOT SELECTED");
        int bodyPartIndex = sharedPref.getInt("bodyPartIndex", -1);
        Log.d("BODY PART INIT", bodyPart);
        Log.d("BODY PART INDEX", Integer.toString(bodyPartIndex));

        dateText.setText(date);
        timeText.setText(time);

        bodyPartSpinner = (Spinner) findViewById(R.id.surgery_area);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.body_parts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bodyPartSpinner.setAdapter(adapter);
        if (bodyPartIndex != -1) {
            bodyPartSpinner.setSelection(bodyPartIndex);
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
        editor.putString("date", date);
        editor.putString("time", time);
        editor.putString("bodyPart", selectedBodyPart);
        editor.putInt("bodyPartIndex", selectedBodyPartIndex);
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
}
