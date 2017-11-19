package software.pipas.feupstudents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

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
                    Toast.makeText(LoginActivity.this, R.string.no_username, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (sPassword.matches(""))
                {
                    Toast.makeText(LoginActivity.this, R.string.no_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("username", sUsername);
                returnIntent.putExtra("password", sPassword);

                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        TextView isItSafe = findViewById(R.id.is_it_safe);
        isItSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new LovelyStandardDialog(LoginActivity.this)
                        .setTopColorRes(R.color.primary)
                        .setTitle(R.string.is_it_safe)
                        .setMessage(getString(R.string.is_it_safe_tooltip))
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton(R.string.github, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://github.com/pipas/FeupStudents"));
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(R.string.about, new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://developer.android.com/training/articles/keystore.html"));
                                startActivity(i);
                            }
                        })
                        .show();
            }
        });
    }
}
