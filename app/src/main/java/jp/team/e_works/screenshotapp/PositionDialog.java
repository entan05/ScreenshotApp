package jp.team.e_works.screenshotapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class PositionDialog extends DialogFragment {
    private PositiveOnClickListener mListener = null;

    private int mStartValue = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.position_dialog, null, false);

        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.position_picker);
        String[] data = getResources().getStringArray(R.array.position_array);
        picker.setMaxValue(8);
        picker.setMinValue(0);
        picker.setWrapSelectorWheel(false);
        picker.setValue(mStartValue);
        picker.setDisplayedValues(data);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.temp));
        builder.setPositiveButton(getResources().getString(R.string.positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null) {
                    mListener.PositiveOnClick(picker.getValue());
                }
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.negative), null);
        builder.setView(view);
        return builder.create();
    }

    public void setValue(int value) {
        if (value >= 0 && value <= 8) {
            mStartValue = value;
        }
    }

    public void registerListener(PositiveOnClickListener listener) {
        mListener = listener;
    }

    interface PositiveOnClickListener {
        void PositiveOnClick(int pickerPosition);
    }
}
