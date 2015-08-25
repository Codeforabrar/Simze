package org.livesoftwaresolution.simze;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class MainActivity extends Activity implements View.OnClickListener{
    EditText fname,lname,mail,password,mobilenumber,dob,img;
    Spinner gender,nationality,type;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    Button btnPost;
    UserDetails person;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        person = new UserDetails();
        fname= (EditText) findViewById(R.id.fname);
        lname= (EditText) findViewById(R.id.lname);
        mail= (EditText) findViewById(R.id.email);
        password= (EditText) findViewById(R.id.pass);
        mobilenumber= (EditText) findViewById(R.id.mobile);
        gender= (Spinner) findViewById(R.id.gender);
        dob= (EditText) findViewById(R.id.dob);
        nationality= (Spinner) findViewById(R.id.nationality);
        type= (Spinner) findViewById(R.id.type);
        img= (EditText) findViewById(R.id.img);
        btnPost= (Button) findViewById(R.id.submit);
        btnPost.setOnClickListener(this);
        if(isConnected()){

            Toast.makeText(this,"You are connected To the internet",Toast.LENGTH_LONG).show();
        }
        else{
           Toast.makeText(this,"You are not connected to the internet",Toast.LENGTH_LONG).show();
        }
         myCalendar = Calendar.getInstance();
         dob= (EditText) findViewById(R.id.dob);
         date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



    }
    private void updateLabel() {

        String myFormat = "yyyy/mm/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dob.setText(sdf.format(myCalendar.getTime()));
    }


    @Override
    public void onClick(View view) {
        person.setFname(fname.getText().toString());
        person.setLname(lname.getText().toString());
        person.setEmail(mail.getText().toString());
        person.setPassword(password.getText().toString());
        person.setDob(dob.getText().toString());
        person.setMobileNumber(mobilenumber.getText().toString());
        person.setImg(img.getText().toString());
        person.setGender(gender.getSelectedItem().toString());
        person.setNationality(nationality.getSelectedItem().toString());
        person.setType(type.getSelectedItem().toString());
        switch(view.getId()){
            case R.id.submit:
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                // call AsynTask to perform network operation on separate thread

                new HttpAsyncTask().execute("http://livesoftwaresolution.org/simze/api/Post_registration.php");
                break;
        }
    }
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    public static String POST(String url,UserDetails person){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("firstname", person.getFname());
            jsonObject.accumulate("lastname", person.getLname());
            jsonObject.accumulate("email", person.email);
            jsonObject.accumulate("password", person.getPassword());
            jsonObject.accumulate("mobilenumber", person.getMobileNumber());
            jsonObject.accumulate("gender", person.getGender());
            jsonObject.accumulate("dob", person.getDob());
            jsonObject.accumulate("type", person.getType());
            jsonObject.accumulate("img", person.getImg());
            jsonObject.accumulate("nationality", person.getNationality());
            JSONObject object=new JSONObject();
            object.accumulate("user_details",jsonObject);
            JSONObject first=new JSONObject();
            first.accumulate("LSS",object);


            // 4. convert JSONObject to JSON to String
            json = first.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {




            return POST(urls[0],person);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validate(){
        if(lname.getText().toString().trim().equals(""))
            return false;
        else if(fname.getText().toString().trim().equals(""))
            return false;
        else if(mail.getText().toString().trim().equals(""))
            return false;
        else if(password.getText().toString().trim().equals(""))
            return false;
        else if(mobilenumber.getText().toString().trim().equals(""))
            return false;
        else if(dob.getText().toString().trim().equals(""))
            return false;
        else if(img.getText().toString().trim().equals(""))
            return false;
        else
            return true;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}

