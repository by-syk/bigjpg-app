package com.by_syk.bigjpg.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.by_syk.bigjpg.R;
import com.by_syk.bigjpg.bean.UserBean;
import com.by_syk.lib.sp.SP;
import com.by_syk.lib.texttag.TextTag;

/**
 * Created by By_syk on 2017-08-06.
 */

public class LoginDialog extends BottomSheetDialogFragment {
    private SP sp;

    private View contentView;
    private TextInputEditText etUser;
    private TextInputEditText etPwd;

    private OnLoginListener onLoginListener;

    public interface OnLoginListener {
        void onLogin();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        contentView = View.inflate(getContext(), R.layout.login, null);
        dialog.setContentView(contentView);

        sp = new SP(getContext());

        etUser = (TextInputEditText) contentView.findViewById(R.id.et_user);
        etPwd = (TextInputEditText) contentView.findViewById(R.id.et_pwd);

        etUser.setText(sp.getString("user"));
        etPwd.setText(sp.getString("pwd"));

        contentView.findViewById(R.id.tv_reg_desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reg();
            }
        });

        contentView.findViewById(R.id.bt_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                if (user.isEmpty() || pwd.isEmpty()) {
                    return;
                }
                SP sp = new SP(getContext());
                sp.put("user", user).put("pwd", pwd)
                        .remove("cookie").remove("configScale")
                        .save();
                if (onLoginListener != null) {
                    onLoginListener.onLogin();
                }
            }
        });

        if (sp.contains("cookie")) {
            TextView tvHint = (TextView) contentView.findViewById(R.id.tv_hint);
            int userGrade = getArguments().getInt("userGrade");
            if (userGrade != UserBean.GRADE_UNDEFINED && userGrade != UserBean.GRADE_FREE) {
                tvHint.setText(new TextTag.Builder()
                        .text(getString(R.string.hint_user_vip))
                        .tag(" " + UserBean.parseGrade(getContext(), userGrade) + " ")
                        .bgColor(ContextCompat.getColor(getContext(), R.color.tag))
                        .sizeRatio(0.8f)
                        .build().render());
            } else {
                tvHint.setText(R.string.hint_user_free);
            }
            tvHint.setVisibility(View.VISIBLE);
        }

        boolean isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        if (isLand) {
            // In landscape, STATE_EXPANDED doesn't make sheet expanded.
            // Maybe it's a bug. So do this to fix it.
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior
                    .from((View) contentView.getParent());
            contentView.measure(0, 0);
            bottomSheetBehavior.setPeekHeight(contentView.getMeasuredHeight());
//        }
        if (isLand) {
            ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);
            contentView.findViewById(R.id.view_outside_1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            contentView.findViewById(R.id.view_outside_2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // To avoid crashing
        dismiss();
    }

    private void reg() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.url_bigjpg)));
        startActivity(intent);
    }

    public void setOnLoginListener(OnLoginListener onLoginListener) {
        this.onLoginListener = onLoginListener;
    }

    public static LoginDialog newInstance(@UserBean.Grade int userGrade) {
        LoginDialog dialog = new LoginDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("userGrade", userGrade);
        dialog.setArguments(bundle);

        return dialog;
    }
}
