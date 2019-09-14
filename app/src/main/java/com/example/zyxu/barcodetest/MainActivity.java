package com.example.zyxu.barcodetest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Button scanbutton,manualbutton;
    EditText edit_actnum,edit_stid;
    Spinner act_sp;
    TextView numpeople;
    String act_id="";
    String stid="";
    ArrayList list = new ArrayList();
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    private String url_checkin ="http://ts1.cmrdb.cs.pu.edu.tw/~s1032818/cmrdb_website/index.php/student/checkin";
    private String url_act_load ="http://ts1.cmrdb.cs.pu.edu.tw/~s1032818/cmrdb_website/index.php/student/query";
    private String TAG_ACTIVITY_ID="activity_id";
    private String TAG_STUDENT_ID="student_id";
    private String TAG_SUCCESS="success";
    private String TAG_QUERY="q";
    private String TAG_ACTIVITY_NAME="activity_name";
    private String[] act_id_array ;
    private String[] act_name_array ;
    final Activity activity = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            new activity_load().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        scanbutton = (Button) this.findViewById(R.id.scanbutton);
        manualbutton= (Button) findViewById(R.id.munal_button);
//        edit_actnum= (EditText) findViewById(R.id.editText_act_num);
        edit_stid= (EditText) findViewById(R.id.manual_edit_ID);
        numpeople= (TextView) findViewById(R.id.total_number);
        act_sp= (Spinner) findViewById(R.id.act_spinner);


        manualbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stid=edit_stid.getText().toString().substring(0,9);
                insert();
            }
        });
        scanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                //获取屏幕尺寸
//                DisplayMetrics dm = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(dm);

                //条形码
//                int width = dm.heightPixels / 2;
//                int height = dm.widthPixels / 2;
//                integrator.setScanningRectangle(width, height);//扫描框
//                integrator.setPrompt("请对准条形码进行扫描");
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);//条形码

                //二维码
//            int len = dm.heightPixels / 2;
//            integrator.setScanningRectangle(len, len);//扫描框
//            integrator.setPrompt("请对准二维码进行扫描");
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);//二维码


//                integrator.setResultDisplayDuration(0);//扫描框内横线的时间
//                integrator.setCameraId(0);
//                integrator.initiateScan();
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }
        });






    }
    void insert(){


        Log.d(act_id+"@OPOP", stid);
        boolean has=false;
        for(int i=0; i <list.size();i++){
            if(list.get(i).equals(stid)) {
                has = true;
                break;
            }
        }
        if(!has){
            try {
                new checkin().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            list.add(stid);
            numpeople.setText(""+list.size());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                stid=result.getContents().toString().substring(0,9);
                insert();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
/*
innerclass
 */

    class checkin extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("傳送中...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        boolean open_catch = false;
        boolean is_successs=false;
        protected String doInBackground(String... args) {


            try {

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair(TAG_STUDENT_ID,stid));
                params.add(new BasicNameValuePair(TAG_ACTIVITY_ID,act_id ));
                Log.d("PAGE JSONstid: ", stid);
                Log.d("PAGE JSONact_id: ",act_id);

                // getting JSON string from URL
               JSONObject json = jParser.makeHttpRequest(url_checkin, "POST", params);

                // Check your log cat for JSON reponse

                Log.d("PAGE JSON: ", json.toString());

                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1)
                    is_successs=true;
            } catch (Exception e) {

                e.printStackTrace();
                open_catch = true;


            }

            return null;
        }
        /**
         * After completing background task Dismiss the progress dialog
         **/

        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    if (open_catch == false) {

                        if (is_successs) {

                            Toast.makeText(getApplicationContext(), "歡迎參加", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "新增失敗", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "連線異常", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

    }
    /*
innerclass
 */

    class activity_load extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        private String c_id;
        private String c_name;
        String id_str="";
        String name_str="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("傳送中...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        boolean open_catch = false;

        protected String doInBackground(String... args) {


            try {


                List<NameValuePair> params = new ArrayList<NameValuePair>();
                ///////////////////////////////////////////////////////////////////tmp

                params.add(new BasicNameValuePair(TAG_QUERY,"act"));

                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_act_load, "GET", params);

                // Check your log cat for JSON reponse

                Log.d("PAGE JSON: ", json.toString());

                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {


                    JSONArray json_act_contents = json.getJSONArray("student");

                    for (int i = 0; i < json_act_contents.length(); i++) {
                        JSONObject c = json_act_contents.getJSONObject(i);
                        c_id=c.getString(TAG_ACTIVITY_ID);
                        c_name=c.getString(TAG_ACTIVITY_NAME);
                        id_str+=(c_id+" ");
                        name_str+=(c_name+" ");
                    }

                }


            } catch (Exception e) {

                e.printStackTrace();
                open_catch = true;


            }

            return null;
        }
        /**
         * After completing background task Dismiss the progress dialog
         **/

        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    if (open_catch == false) {
                        act_id_array=id_str.substring(0,id_str.length()).split(" ");
                        act_name_array=name_str.substring(0,name_str.length()).split(" ");
                        /*spinner*/
                        ArrayAdapter<String> actList = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, act_name_array);
                        act_sp.setAdapter(actList);
                        act_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                act_id=act_id_array[i];
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "活動載入失敗", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

    }
}
