package com.itpvt.uberclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    private Button bLogin,bRegister;
    private EditText eEmail,ePassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        bLogin=(Button)findViewById(R.id.login);
        bRegister=(Button)findViewById(R.id.register);
        eEmail=(EditText)findViewById(R.id.email);
        ePassword=(EditText)findViewById(R.id.password);


        mAuth =FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    Intent intent=new Intent(DriverLoginActivity.this,DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
        };
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email=eEmail.getText().toString();
                final String password=ePassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                         if (!task.isSuccessful())
                         {
                             Toast.makeText(DriverLoginActivity.this,"Sign Error",Toast.LENGTH_LONG).show();

                         }else
                             {
                                 String user_id=mAuth.getCurrentUser().getUid();
                                 DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users")
                                         .child("Drivers").child(user_id).child("name");
                                 current_user_db.setValue(email);

                             }

                    }
                });
            }
        });
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email=eEmail.getText().toString();
                final String password=ePassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(DriverLoginActivity.this,"Sign Error",Toast.LENGTH_LONG).show();

                        }else
                        {


                        }

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}
