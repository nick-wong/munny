package com.example.nick.munny;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditBalanceActivity extends Activity {
    String balanceStr;
    Button closeDialog;
    EditText inputBalance;
    Button mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_dialog);
        closeDialog = (Button) findViewById(R.id.closeDialog);
        inputBalance = (EditText) findViewById(R.id.inputBalance);
        mCancel = (Button) findViewById(R.id.cancel_edit_balance);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceStr = inputBalance.getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("newBalance", balanceStr);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
