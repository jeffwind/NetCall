package com.netcall.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.netcall.Callback;
import com.netcall.Response;
import com.netcall.test.call.CallTest;

public class MainActivity extends Activity {

    private TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        resultTv = findViewById(R.id.result_tv);
        findViewById(R.id.send_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = findViewById(R.id.input);
                String text = tv.getText().toString();
//                CallTest callTest = new CallTest(text);
//                callTest.call(callback);
                CallTest callTest = new CallTest(text);
                callTest.call(new Callback() {
                    @Override
                    public void onResp(Response response) {
                        if (response.isSuccess()) {
                            resultTv.setText(response.getBean().toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Error：" + response.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private Callback callback = new Callback() {
        @Override
        public void onResp(Response response) {
            if (response.isSuccess()) {
System.out.println("~~~~~~~~~~~~~isAfterNet " + response.isAfterNet());
                resultTv.setText(response.getBean().toString());
            } else {
//                resultTv.setText(response.getException().getMessage());
                Toast.makeText(MainActivity.this, "出错：" + response.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
