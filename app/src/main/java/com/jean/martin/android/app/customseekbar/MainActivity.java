package com.jean.martin.android.app.customseekbar;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toggle) Button btnToggle;
    @Bind(R.id.seekaxis) Seekaxis seekaxis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_seek_axis);
        ButterKnife.bind(this);



        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean expanded = seekaxis.getExpanded();
                seekaxis.setExpanded(!expanded);
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)seekaxis.getLayoutParams();

                int[] values;
                if(expanded) values = new int[] {100,0};
                else values =new int[] {0,100};
                ValueAnimator animator = ValueAnimator.ofInt(values);
                animator.setDuration(1500);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        params.rightMargin = (Integer) animation.getAnimatedValue();
                        seekaxis.requestLayout();
                    }
                });
                animator.start();
            }
        });
    }
}
