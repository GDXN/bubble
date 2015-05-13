package com.nkanaev.comics.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import com.nkanaev.comics.R;


public class AboutDialog extends Dialog {
    public AboutDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        setContentView(R.layout.dialog_about);

        Button closeButton = (Button) findViewById(R.id.dialog_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
