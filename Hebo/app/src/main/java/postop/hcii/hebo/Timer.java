package postop.hcii.hebo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class Timer {
    private static final long START_TIME_IN_MILLIS = Config.TIMER_START_VALUE;

    private Context mContext;
    private TextView mTimerText;
    private Button mCancelButton;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning, mIsSecondTimer;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private long mEndTime;


    Timer(Context context, TextView timerText, Button cancelButton, boolean isSecondTimer) {
        mContext = context;
        mTimerText = timerText;
        mCancelButton = cancelButton;
        mIsSecondTimer = isSecondTimer;

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
            }
        });
        updateCountDownText();
    }

    public void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mCountDownTimer.cancel();
                disableCancelButton();
                mTimerText.setText("00:00");
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                if (mIsSecondTimer) {
                    intent.putExtra("bleeding_final", true);
                }
                intent.putExtra("bleeding_initial", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, Config.NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.heboicon)
                        .setContentTitle("Hebo")
                        .setContentText("How's it going? Are you still bleeding?")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                notificationManager.notify(0, mBuilder.build());
                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
            }
        }.start();

        mTimerRunning = true;
    }

    public void cancelTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        disableCancelButton();
    }

    public void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTimerText.setText(timeLeftFormatted);
    }

    public void disableCancelButton() {
        mCancelButton.setEnabled(false);
        mCancelButton.setTextColor(ContextCompat.getColor(mContext, R.color.bgColor));
    }

    public long getTimeLeftInMillis() { return mTimeLeftInMillis; }
    public boolean getTimerRunning() { return mTimerRunning; }
    public long getEndTime() { return mEndTime; }

    public void setTimeLeftInMillis(long time) {
        mTimeLeftInMillis = time;
    }

    public void setTimerRunning(boolean isRunning) {
        mTimerRunning = isRunning;
    }

    public void setEndTime(long time) {
        mEndTime = time;
    }
}
