package postop.hcii.hebo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.DatePicker;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        TextView dateText = (TextView) getActivity().findViewById(R.id.dateText);
        String date = Integer.toString(month + 1)  + "/" + Integer.toString(day) + "/" + year;
        dateText.setText(date);
    }
}
