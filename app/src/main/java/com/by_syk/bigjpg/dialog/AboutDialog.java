package com.by_syk.bigjpg.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import com.by_syk.bigjpg.R;

/**
 * Created by By_syk on 2017-08-06.
 */

public class AboutDialog extends BottomSheetDialogFragment {
    private View contentView;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        contentView = View.inflate(getContext(), R.layout.about, null);
        dialog.setContentView(contentView);

        contentView.findViewById(R.id.tv_dev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(getString(R.string.email_bysyk));
            }
        });
        contentView.findViewById(R.id.tv_copyright).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(getString(R.string.email_bigjpg));
            }
        });
        contentView.findViewById(R.id.tv_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitSite();
            }
        });

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

    private void sendEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", email, null));
        //intent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(intent);
    }

    private void visitSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.url_bigjpg)));
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

        // To avoid crashing
        dismiss();
    }
}
