package postop.hcii.hebo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String am_pm = "";
        Calendar datetime = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);

        if (datetime.get(Calendar.AM_PM) == Calendar.AM) {
            am_pm = "AM";
        } else if (datetime.get(Calendar.AM_PM) == Calendar.PM) {
            am_pm = "PM";
        }

        if (hourOfDay > 12) {
            hourOfDay = hourOfDay - 12;
        } else if (hourOfDay == 0) {
            hourOfDay = 12;
        }

        String minuteText;
        if (minute < 10) {
            minuteText = "0" + Integer.toString(minute);
        } else {
            minuteText = Integer.toString(minute);
        }

        TextView timeText = (TextView) getActivity().findViewById(R.id.timeText);
        String time = Integer.toString(hourOfDay)  + ":" +  minuteText + " " + am_pm;
        timeText.setText(time);
    }
}
