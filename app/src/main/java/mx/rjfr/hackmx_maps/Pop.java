package mx.rjfr.hackmx_maps;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;


public class Pop extends Activity {

    private Button btnAccept;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        btnAccept = (Button)findViewById(R.id.acceptbtn);
        btnCancel = (Button)findViewById(R.id.cancelbtn);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsActivity.accepted = true;
                finish();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsActivity.accepted = false;
                finish();

            }
        });

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(  (int)width, (int)height);

    }
}
