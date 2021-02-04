package com.vca.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vca.R;
import com.vca.activity.homeScreen.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity {

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final String token;

        final EditText emailId = (EditText) findViewById(R.id.et_Email);
        final EditText password = (EditText) findViewById(R.id.et_Pass);
        final TextView signuplink = (TextView) findViewById(R.id.tv_signup);

        signuplink.setMovementMethod(LinkMovementMethod.getInstance());

        final Button login = (Button) findViewById(R.id.btn_login);

        final String org_email = emailId.getText().toString();
        final String passwrd = password.getText().toString();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (emailId.getText().toString().isEmpty()) {
                    emailId.setError("Email Id is required");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailId.getText().toString()).matches()) {
                    emailId.setError("Give proper Email Id");
                } else if (password.getText().toString().isEmpty()) {
                    password.setError("Password  is required");
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),

                            "EmailId =" + emailId.getText().toString() + "Password =" + password.getText().toString()
                            , Toast.LENGTH_LONG);
                      //toast.show();
                }

                sendPost();


            }

            public void sendPost() {
                final ProgressDialog loading = new ProgressDialog(LoginActivity.this);
                loading.setMessage("Please Wait...");
                loading.setCanceledOnTouchOutside(false);
                loading.show();

                JSONObject object = new JSONObject();
                try {
                    //input your API parameters
                    object.put("org_email", emailId.getText().toString());
                    object.put("password", password.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Enter the correct url for your api service site
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.1.16:8000/api/org/login/", object,
                        response -> {
                        //    Toast.makeText(LoginActivity.this, "String Response : " + response.toString(), Toast.LENGTH_LONG).show();
                            Log.d("JSON", String.valueOf(response));
                            loading.dismiss();

                            success(response);
                            

                            try {

                                String token1 =response.getString("token");
                              //  Toast.makeText(LoginActivity.this, "Token : " + token1, Toast.LENGTH_LONG).show();

                              //  Toast.makeText(LoginActivity.this, "Email : " + email, Toast.LENGTH_LONG).show();


                                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("token",token1);
                                intent.putExtra("email",email);

                                startActivity(intent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                        , error -> {
                    loading.dismiss();
                    VolleyLog.d("Error", "Error: " + error.getMessage());
                    Toast.makeText(LoginActivity.this, "Invalid Credentials or account not activated" ,Toast.LENGTH_LONG).show();
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(jsonObjectRequest);

            }

            private void success(JSONObject response) {


                email = extractEmail(response);

//                String  username = extractUserName(response);
//
//
//                String    accountant = extractAccountant(response);
//
//
//                String  allow_accountant = extractAllowAccountant(response);
            }

            private String extractEmail(JSONObject response) {
                String emaildata = null;
                try{
                    JSONObject org= new JSONObject(String.valueOf(response)).getJSONObject("data");
                     emaildata=org.getString("org_email");

                }catch (Exception e) {
                    e.printStackTrace();
                }


              return emaildata;
            }


        });


    }



}