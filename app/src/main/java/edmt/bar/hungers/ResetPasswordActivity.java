package edmt.bar.hungers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edmt.bar.hungers.Model.Users;
import edmt.bar.hungers.Prevalent.Prevalent;

public class ResetPasswordActivity extends AppCompatActivity
{
    private String check = "";
    private TextView page_title, titleQuestions;
    private EditText find_phone_number, question_1, question_2;
    private Button verify_btn;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);


        check = getIntent().getStringExtra("check");

        page_title = findViewById(R.id.page_title);
        titleQuestions = findViewById(R.id.titleQuestions);
        find_phone_number = findViewById(R.id.find_phone_number);
        question_1 = findViewById(R.id.question_1);
        question_2 = findViewById(R.id.question_2);
        verify_btn = findViewById(R.id.verify_btn);
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        find_phone_number.setVisibility(View.GONE);
        if (check.equals("settings"))
        {
            page_title.setText("Set Questions");
            titleQuestions.setText("Please set the Answers for the Following Security Questions?");

            verify_btn.setText("Set");


            displayPreviousanswers();
            verify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    setAnswers();

                }
            });

        }
        else if (check.equals("login"))
        {
            find_phone_number.setVisibility(View.VISIBLE);

            verify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    verifyUser();
                }
            });
        }
    }
    private void setAnswers()
    {
        String Answer1 = question_1.getText().toString().toLowerCase();
        String Answer2 = question_2.getText().toString().toLowerCase();
        if (question_1.equals("") && question_2.equals(""))
        {
            Toast.makeText(ResetPasswordActivity.this, "Please answer the security question", Toast.LENGTH_SHORT);
        }
        else
        {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Users")
                    .child(Prevalent.currentOnlineUser.getPhone());
            HashMap<String, Object> userdataMap = new HashMap<>();
            userdataMap.put("Answer1", Answer1);
            userdataMap.put("Answer2", Answer2);

            ref.child("Security Questions").updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ResetPasswordActivity.this, "Verified", Toast.LENGTH_SHORT);
                        Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }



    private void displayPreviousanswers()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());
        ref.child("Security Questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
              if (dataSnapshot.exists())
              {
                  String ans1 =dataSnapshot.child("Answer1").getValue().toString();
                  String ans2 =dataSnapshot.child("Answer2").getValue().toString();
                  question_1.setText(ans1);
                  question_2.setText(ans2);
              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
private void verifyUser() {
    final String phone = find_phone_number.getText().toString();
    final String Answer1 = question_1.getText().toString().toLowerCase();
    final String Answer2 = question_2.getText().toString().toLowerCase();


    if (!phone.equals("") && !Answer1.equals("") && !Answer2.equals(""))
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(phone);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String mPhone = dataSnapshot.child("phone").getValue().toString();

                    if (dataSnapshot.hasChild("Security Questions")) {
                        String ans1 = dataSnapshot.child("Security Questions").child("Answer1").getValue().toString();
                        String ans2 = dataSnapshot.child("Security Questions").child("Answer2").getValue().toString();

                        if (!ans1.equals(Answer1)) {
                            Toast.makeText(ResetPasswordActivity.this, "Answer one is incorrect!", Toast.LENGTH_SHORT);
                        } else if (!ans2.equals(Answer2)) {
                            Toast.makeText(ResetPasswordActivity.this, "Answer two is incorrect!", Toast.LENGTH_SHORT);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                            builder.setTitle("New password");

                            final EditText newPassword = new EditText(ResetPasswordActivity.this);
                            newPassword.setHint("New Password");
                            builder.setView(newPassword);
                            builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!newPassword.getText().toString().equals("")) {
                                        ref.child("password")
                                                .setValue(newPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ResetPasswordActivity.this, "Password Changed Succeessfully", Toast.LENGTH_SHORT);
                                                            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                });

                                    }

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                               dialog.cancel();
                                }
                            });



                            builder.show();
                        }



                    }
                    else
                    {
                        Toast.makeText(ResetPasswordActivity.this, "You have not set the security questions", Toast.LENGTH_SHORT);
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
    else
    {
        Toast.makeText(ResetPasswordActivity.this, "Please complete the form", Toast.LENGTH_SHORT);
    }
}


}
