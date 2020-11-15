package ie.tcd.asepaint2020.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import ie.tcd.asepaint2020.R;
import ie.tcd.asepaint2020.event.EnterLobbyEvent;

public class ScoreDialog extends Dialog {

    TextView[] tvNames = new TextView[4];
    TextView[] tvScores = new TextView[4];

    Button btnBack;

    public ScoreDialog(@NonNull Context context) {
        super(context);
    }

    public ScoreDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ScoreDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_score);
        setCanceledOnTouchOutside(false);
        tvNames[0] = findViewById(R.id.tv_name1);
        tvNames[1] = findViewById(R.id.tv_name2);
        tvNames[2] = findViewById(R.id.tv_name3);
        tvNames[3] = findViewById(R.id.tv_name4);

        tvScores[0] = findViewById(R.id.tv_score1);
        tvScores[1] = findViewById(R.id.tv_score2);
        tvScores[2] = findViewById(R.id.tv_score3);
        tvScores[3] = findViewById(R.id.tv_score4);

        btnBack = findViewById(R.id.btn_back_lobby);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new EnterLobbyEvent());
                dismiss();
            }
        });

    }

    public void showScores(Map<String, Integer> nameScoreMap) {
        if (nameScoreMap == null) {
            return;
        }
        int i = 0;
        for (Map.Entry<String, Integer> entry : nameScoreMap.entrySet()) {
            tvNames[i].setText(entry.getKey());
            tvScores[i].setText(entry.getValue());
            i++;
        }
        show();
    }

    public void setBtnBackClickListener(View.OnClickListener listener) {
        btnBack.setOnClickListener(listener);
    }
}
