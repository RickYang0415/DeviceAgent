package company.rick.deviceagent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DeviceInformation dm;
    String address = "192.168.67.48";
    int port = 5488;
    Client client;
    Button button;
    final String DISCONNECT = "Disconnect";
    final String STOP = "Stop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.editText)).setText(address + ":" + String.valueOf(port));
        ((TextView) findViewById(R.id.editText2)).setMovementMethod(new ScrollingMovementMethod());
        ((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client = new Client(MainActivity.this, new UpdateCallBack() {
                    @Override
                    public void UpdateUIText(String str) {
                        TextView textView = (TextView) findViewById(R.id.editText2);
                        String tmp = textView.getText().toString();
                        if (tmp.equals(""))
                            tmp = str;
                        else
                            tmp = String.format("%s\n%s", tmp, str);
                        textView.setText(tmp);
                    }
                });
                client.m_ip = address;
                client.m_port = port;
                new Thread(client).start();
                ((Button) findViewById(R.id.button1)).setEnabled(false);
            }
        });
        button = ((Button) findViewById(R.id.button2));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = button.getText().toString();
                if (client != null)
                    client.Stop();
                ((Button) findViewById(R.id.button1)).setEnabled(true);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
