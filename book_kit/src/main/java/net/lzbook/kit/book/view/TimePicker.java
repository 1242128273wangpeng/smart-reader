package net.lzbook.kit.book.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.AppLog;

import java.util.Calendar;

public class TimePicker extends LinearLayout {
    private View myPickerView;
    private ImageButton start_hour_plus;
    private TextView start_hour_display;
    private ImageButton start_hour_minus;
    private ImageButton start_min_plus;
    private TextView start_min_display;
    private ImageButton start_min_minus;
    private Calendar startcal;
    private Calendar stopcal;
    public static final int HOUR_12 = 12;
    public static final int HOUR_24 = 24;
    private static final int HOURS_IN_HALF_DAY = 12;
    private int currentTimeFormate = HOUR_24;
    protected Context context;
    private ImageButton stop_hour_plus;
    private TextView stop_hour_display;
    private ImageButton stop_hour_minus;
    private ImageButton stop_min_plus;
    private TextView stop_min_display;
    private ImageButton stop_min_minus;

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context mContext) {
        this.context = mContext;
        LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myPickerView = inflator.inflate(R.layout.dialog_time_picker, null);
        this.addView(myPickerView);
        initializeReference();
    }

    private void initializeReference() {
        start_hour_plus = (ImageButton) myPickerView.findViewById(R.id.start_hour_plus);
        start_hour_plus.setOnClickListener(start_hour_plus_listener);
        start_hour_display = (TextView) myPickerView.findViewById(R.id.start_hour_display);
        start_hour_display.addTextChangedListener(start_hour_watcher);
        start_hour_minus = (ImageButton) myPickerView.findViewById(R.id.start_hour_minus);
        start_hour_minus.setOnClickListener(start_hour_minus_listener);
        start_min_plus = (ImageButton) myPickerView.findViewById(R.id.start_min_plus);
        start_min_plus.setOnClickListener(start_min_plus_listener);
        start_min_display = (TextView) myPickerView.findViewById(R.id.start_min_display);
        start_min_display.addTextChangedListener(start_min_watcher);
        start_min_minus = (ImageButton) myPickerView.findViewById(R.id.start_min_minus);
        start_min_minus.setOnClickListener(start_min_minus_listener);
        stop_hour_plus = (ImageButton) myPickerView.findViewById(R.id.stop_hour_plus);
        stop_hour_plus.setOnClickListener(stop_hour_plus_listener);
        stop_hour_display = (TextView) myPickerView.findViewById(R.id.stop_hour_display);
        stop_hour_display.addTextChangedListener(stop_hour_watcher);
        stop_hour_minus = (ImageButton) myPickerView.findViewById(R.id.stop_hour_minus);
        stop_hour_minus.setOnClickListener(stop_hour_minus_listener);
        stop_min_plus = (ImageButton) myPickerView.findViewById(R.id.stop_min_plus);
        stop_min_plus.setOnClickListener(stop_min_plus_listener);
        stop_min_display = (TextView) myPickerView.findViewById(R.id.stop_min_display);
        stop_min_display.addTextChangedListener(stop_min_watcher);
        stop_min_minus = (ImageButton) myPickerView.findViewById(R.id.stop_min_minus);
        stop_min_minus.setOnClickListener(stop_min_minus_listener);
        startcal = Calendar.getInstance();
        stopcal = Calendar.getInstance();
        initData();
        initFilterNumericDigit();
    }


    private void initData() {

        if (currentTimeFormate == HOUR_12) {
            start_hour_display.setText(String.valueOf(startcal.get(Calendar.HOUR)));
            sendToDisplay();
        } else {
            start_hour_display.setText(String.valueOf(startcal.get(Calendar.HOUR_OF_DAY)));
            sendToDisplay();
        }
        if (currentTimeFormate == HOUR_12) {
            stop_hour_display.setText(String.valueOf(stopcal.get(Calendar.HOUR)));
            sendToDisplay();
        } else {
            stop_hour_display.setText(String.valueOf(stopcal.get(Calendar.HOUR_OF_DAY)));
            sendToDisplay();
        }

        start_min_display.setText(String.valueOf(startcal.get(Calendar.MINUTE)));

        stop_min_display.setText(String.valueOf(stopcal.get(Calendar.MINUTE)));
    }

    private void initFilterNumericDigit() {

        try {
            if (currentTimeFormate == HOUR_12) {
                start_hour_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 11)});
            } else {
                start_hour_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 23)});
            }

            start_min_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 59)});

            if (currentTimeFormate == HOUR_12) {
                stop_hour_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 11)});
            } else {
                stop_hour_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 23)});
            }

            stop_min_display.setFilters(new InputFilter[]{new InputFilterMinMax(0, 59)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    OnClickListener start_hour_plus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            start_hour_display.requestFocus();
            try {
                if (currentTimeFormate == HOUR_12) {
                    startcal.add(Calendar.HOUR, 1);
                } else {
                    startcal.add(Calendar.HOUR_OF_DAY, 1);
                }
                sendToDisplay();
            } catch (Exception e) {
                AppLog.d("TAG", e.toString());

            }
        }
    };
    OnClickListener stop_hour_plus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stop_hour_display.requestFocus();
            try {
                if (currentTimeFormate == HOUR_12) {
                    stopcal.add(Calendar.HOUR, 1);
                } else {
                    stopcal.add(Calendar.HOUR_OF_DAY, 1);
                }
                sendToDisplay();
            } catch (Exception e) {
                AppLog.d("TAG", e.toString());

            }
        }
    };
    OnClickListener start_hour_minus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            start_hour_display.requestFocus();
            try {
                if (currentTimeFormate == HOUR_12) {
                    startcal.add(Calendar.HOUR, -1);
                } else {
                    startcal.add(Calendar.HOUR_OF_DAY, -1);
                }
                sendToDisplay();
            } catch (Exception e) {
                AppLog.d("TAG", e.toString());
            }
        }
    };
    OnClickListener stop_hour_minus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stop_hour_display.requestFocus();
            try {
                if (currentTimeFormate == HOUR_12) {
                    stopcal.add(Calendar.HOUR, -1);
                } else {
                    stopcal.add(Calendar.HOUR_OF_DAY, -1);
                }
                sendToDisplay();
            } catch (Exception e) {
                AppLog.d("TAG", e.toString());
            }
        }
    };

    OnClickListener start_min_plus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            start_min_display.requestFocus();
            try {
                // cal.add(Calendar.MINUTE, 1);
                int value = startcal.get(Calendar.MINUTE) + 1;
                if (value == 60) {
                    startcal.set(Calendar.MINUTE, 0);
                } else {
                    startcal.set(Calendar.MINUTE, value);
                }
                sendToDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    OnClickListener stop_min_plus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stop_min_display.requestFocus();
            try {
                // cal.add(Calendar.MINUTE, 1);
                int value = stopcal.get(Calendar.MINUTE) + 1;
                if (value == 60) {
                    stopcal.set(Calendar.MINUTE, 0);
                } else {
                    stopcal.set(Calendar.MINUTE, value);
                }
                sendToDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    OnClickListener start_min_minus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            start_min_display.requestFocus();
            try {
                // cal.add(Calendar.MINUTE, -1);
                int value = startcal.get(Calendar.MINUTE) - 1;
                if (value == -1) {
                    startcal.set(Calendar.MINUTE, 59);
                } else {
                    startcal.set(Calendar.MINUTE, value);
                }
                sendToDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    OnClickListener stop_min_minus_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stop_min_display.requestFocus();
            try {
                // cal.add(Calendar.MINUTE, -1);
                int value = stopcal.get(Calendar.MINUTE) - 1;
                if (value == -1) {
                    stopcal.set(Calendar.MINUTE, 59);
                } else {
                    stopcal.set(Calendar.MINUTE, value);
                }
                sendToDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input)) {
                    return null;
                }
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    synchronized private void sendToStartListener() {
        if (mStartTimeWatcher != null) {
            if (currentTimeFormate == HOUR_12) {
                mStartTimeWatcher.onTimeChanged(startcal.get(Calendar.HOUR), startcal.get(Calendar.MINUTE), startcal.get(Calendar.AM_PM));
            } else {
                mStartTimeWatcher.onTimeChanged(startcal.get(Calendar.HOUR_OF_DAY), startcal.get(Calendar.MINUTE), -1);
            }
        }

    }

    synchronized private void sendToStopListener() {
        if (mStopTimeWatcher != null) {
            if (currentTimeFormate == HOUR_12) {
                mStopTimeWatcher.onTimeChanged(stopcal.get(Calendar.HOUR), stopcal.get(Calendar.MINUTE), stopcal.get(Calendar.AM_PM));
            } else {
                mStopTimeWatcher.onTimeChanged(stopcal.get(Calendar.HOUR_OF_DAY), stopcal.get(Calendar.MINUTE), -1);
            }
        }
    }

    private void sendToDisplay() {
        if (currentTimeFormate == HOUR_12) {
            start_hour_display.setText(String.valueOf(startcal.get(Calendar.HOUR)));
        } else {
            start_hour_display.setText(String.valueOf(startcal.get(Calendar.HOUR_OF_DAY)));
        }
        start_min_display.setText(String.valueOf(startcal.get(Calendar.MINUTE)));

        if (currentTimeFormate == HOUR_12) {
            stop_hour_display.setText(String.valueOf(stopcal.get(Calendar.HOUR)));
        } else {
            stop_hour_display.setText(String.valueOf(stopcal.get(Calendar.HOUR_OF_DAY)));
        }
        stop_min_display.setText(String.valueOf(stopcal.get(Calendar.MINUTE)));
    }

    TimeWatcher mStartTimeWatcher = null;
    TimeWatcher mStopTimeWatcher = null;

    public interface TimeWatcher {
        void onTimeChanged(int h, int m, int am_pm);
    }

    TextWatcher start_hour_watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (s.toString().length() > 0) {
                    if (currentTimeFormate == HOUR_12) {
                        startcal.set(Calendar.HOUR, Integer.parseInt(s.toString()));
                    } else {
                        startcal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.toString()));
                    }

                    sendToStartListener();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    TextWatcher stop_hour_watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (s.toString().length() > 0) {
                    if (currentTimeFormate == HOUR_12) {
                        stopcal.set(Calendar.HOUR, Integer.parseInt(s.toString()));
                    } else {
                        stopcal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.toString()));
                    }

                    sendToStopListener();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    TextWatcher start_min_watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (s.toString().length() > 0) {
                    startcal.set(Calendar.MINUTE, Integer.parseInt(s.toString()));
                    sendToStartListener();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    TextWatcher stop_min_watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (s.toString().length() > 0) {
                    stopcal.set(Calendar.MINUTE, Integer.parseInt(s.toString()));
                    sendToStopListener();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public int getCurrentStartHour() {
        return startcal.get(Calendar.HOUR_OF_DAY);
    }

    public int getCurrentStopHour() {
        return stopcal.get(Calendar.HOUR_OF_DAY);
    }

    public void setCurrentStartHour(int currentHour) {
        if (currentHour != -1 && currentHour != getCurrentStartHour()) {
            if (currentTimeFormate == HOUR_12) {
                if (currentHour >= HOURS_IN_HALF_DAY) {
                    if (currentHour > HOURS_IN_HALF_DAY) {
                        currentHour = currentHour - HOURS_IN_HALF_DAY;
                    }
                    startcal.set(Calendar.AM_PM, Calendar.PM);
                } else {
                    if (currentHour == 0) {
                        currentHour = HOURS_IN_HALF_DAY;
                    }
                    startcal.set(Calendar.AM_PM, Calendar.AM);
                }
                startcal.set(Calendar.HOUR, currentHour);
            } else {
                startcal.set(Calendar.HOUR_OF_DAY, currentHour);
            }

        } else {
            return;
        }
        initFilterNumericDigit();
        sendToDisplay();
    }

    public void setCurrentStopHour(int currentHour) {
        if (currentHour == -1 || currentHour == getCurrentStopHour()) {
            return;
        }
        if (currentTimeFormate == HOUR_12) {
            if (currentHour >= HOURS_IN_HALF_DAY) {
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
                stopcal.set(Calendar.AM_PM, Calendar.PM);
            } else {
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
                stopcal.set(Calendar.AM_PM, Calendar.AM);
            }
            stopcal.set(Calendar.HOUR, currentHour);
        } else {
            stopcal.set(Calendar.HOUR_OF_DAY, currentHour);
        }
        initFilterNumericDigit();
        sendToDisplay();
    }

    public int getCurrentStartMinute() {
        return startcal.get(Calendar.MINUTE);
    }

    public int getCurrentStopMinute() {
        return stopcal.get(Calendar.MINUTE);
    }

    public void setCurrentStartMinute(int currentMinute) {
        if (currentMinute == getCurrentStartMinute()) {
            return;
        }
        startcal.set(Calendar.MINUTE, currentMinute);
        initFilterNumericDigit();
        sendToDisplay();
    }

    public void setCurrentStopMinute(int currentMinute) {
        if (currentMinute == getCurrentStopMinute()) {
            return;
        }
        stopcal.set(Calendar.MINUTE, currentMinute);
        initFilterNumericDigit();
        sendToDisplay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) {
            return;
        }
        super.setEnabled(enabled);
        start_min_display.setEnabled(enabled);
        start_min_minus.setEnabled(enabled);
        start_min_plus.setEnabled(enabled);
        start_hour_display.setEnabled(enabled);
        start_hour_minus.setEnabled(enabled);
        start_hour_plus.setEnabled(enabled);
        stop_min_display.setEnabled(enabled);
        stop_min_minus.setEnabled(enabled);
        stop_min_plus.setEnabled(enabled);
        stop_hour_display.setEnabled(enabled);
        stop_hour_minus.setEnabled(enabled);
        stop_hour_plus.setEnabled(enabled);
    }
}
