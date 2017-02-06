package company.rick.deviceagent;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    DeviceInformation dm;
    String address = "";
    int port;
    Client client;
    final String DISCONNECT = "Disconnect";
    final String STOP = "Stop";
    Button button1;
    Button button2;
    EditText textView1;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        textView1 = (EditText) findViewById(R.id.editText1);
        textView2 = ((TextView) findViewById(R.id.editText2));
        textView2.setMovementMethod(new ScrollingMovementMethod());
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = textView1.getText().toString();
                if (value.equals(""))
                    return;
                String[] arg = value.split(":", -1);
                if (arg.length != 2)
                    Toast.makeText(getApplicationContext(), "Address format error.", Toast.LENGTH_LONG).show();
                address = arg[0];
                port = Integer.valueOf(arg[1]);
                client = new Client(MainActivity.this, new UpdateCallBack() {
                    @Override
                    public void UpdateUIText(String str) {
                        String tmp = textView2.getText().toString();
                        if (tmp.equals(""))
                            tmp = str;
                        else
                            tmp = String.format("%s\n%s", tmp, str);
                        textView2.setText(tmp);
                    }
                });
                client.m_ip = address;
                client.m_port = port;
                new Thread(client).start();
                button1.setEnabled(false);
                textView1.setEnabled(false);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client != null)
                    client.Stop();
                button1.setEnabled(true);
                textView1.setEnabled(true);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (client != null) {
            client.Stop();
            client = null;
        }
        button1.setEnabled(true);
        textView1.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null)
            client.Pause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (client != null)
            client.Resume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
