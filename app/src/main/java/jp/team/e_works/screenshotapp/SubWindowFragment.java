package jp.team.e_works.screenshotapp;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class SubWindowFragment extends Fragment {
    private View mView;
    private MainActivity mActivity;

    public View loadView(MainActivity activity) {
        mActivity = activity;

        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mView = inflater.inflate(R.layout.sub_window, null);

        Button btnCap = (Button) mView.findViewById(R.id.btn_capture);
        btnCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("xxxxx", "sub window button clicked.");
                mActivity.getScreenshot();
            }
        });

        return mView;
    }

    public View getVariableView() {
        return mView;
    }

    public void removeVariableView() {
        mView = null;
    }
}
