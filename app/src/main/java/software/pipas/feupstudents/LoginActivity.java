package software.pipas.feupstudents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity
{
    private EditText inputPassword;
    private EditText inputUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputPassword = findViewById(R.id.inputPassword);
        inputUser = findViewById(R.id.inputUser);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String sUsername = inputUser.getText().toString();
                String sPassword = inputPassword.getText().toString();

                if (sUsername.matches(""))
                {
                    Toast.makeText(LoginActivity.this, "Username vazio", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (sPassword.matches(""))
                {
                    Toast.makeText(LoginActivity.this, "Password vazia", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("FEUPDEBUG", "Set preferences " + sUsername + " " + sPassword);

                SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("gameSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString(getString(R.string.saved_username), sUsername);
                editor.putString(getString(R.string.saved_password), sPassword);
                editor.apply();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("username", sUsername);
                returnIntent.putExtra("password", sPassword);

                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        TextView skipLogin = findViewById(R.id.skipLogin);
        skipLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
            }
        });
    }
}
