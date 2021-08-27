 package com.example.whatsapp;

 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.Toolbar;
 import androidx.viewpager.widget.ViewPager;


 import com.example.whatsapp.Settings.SettingsActivity;
 import com.example.whatsapp.helper.TabAceesorAdapter;
 import com.example.whatsapp.loginsignup.LogInActivity;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.Task;
 import com.google.android.material.tabs.TabLayout;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.auth.FirebaseUser;
 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.ValueEventListener;

 import java.util.Objects;

 public class MainActivity extends AppCompatActivity {
   private Toolbar mainToolBar;
   ViewPager mainViewPager;
   TabLayout mainTabLayout;
   TabAceesorAdapter mainTabAcc;
   FirebaseUser currentUser;
   FirebaseAuth mauth;
   DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mauth = FirebaseAuth.getInstance();
        currentUser = mauth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();
        if (currentUser == null) {
            sendUserToLOginActivity();
        }
        else {
            setContentView(R.layout.activity_main);
            mainToolBar = findViewById(R.id.main_page_toolbar);
            mainViewPager = findViewById(R.id.man_tab_viewpager);
            mainTabLayout = findViewById(R.id.main_tabs);
            mainTabAcc = new TabAceesorAdapter(getSupportFragmentManager());
            mainViewPager.setAdapter(mainTabAcc);
            setSupportActionBar(mainToolBar);
            mainTabLayout.setupWithViewPager(mainViewPager
            );
            Objects.requireNonNull(getSupportActionBar()).setTitle("Whatsapp");
        }
    }
     @Override
     protected void onStart() {
         super.onStart();
         if(currentUser==null)
         {
             sendUserToLOginActivity();
         }
         else
         {
             VerifyUserExistence();
         }
     }

     public void VerifyUserExistence() {
        String currentUserId=mauth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child("name").exists()))
                {
                  //  Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSeetingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        

     }

     @Override
     public void onBackPressed() {
         Intent a = new Intent(Intent.ACTION_MAIN);
         a.addCategory(Intent.CATEGORY_HOME);
         a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(a);
     }

     private void sendUserToLOginActivity() {
        Intent mainIntent = new Intent(MainActivity.this, LogInActivity.class);
         mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(mainIntent);
         finish();
     }
     private void sendUserToSeetingsActivity() {
         Intent mainIntent = new Intent(MainActivity.this, SettingsActivity.class);
         mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(mainIntent);
         finish();
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
          super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          super.onOptionsItemSelected(item);
          if(item.getItemId()==R.id.main_logout)
          {
              mauth.signOut();
              sendUserToLOginActivity();
          }
         if(item.getItemId()==R.id.main_seetings)
         {
             sendUserToSeetingsActivity();


         }
         if(item.getItemId()==R.id.main_create_group)
         {
            RequestNewGroup();


         }
         if(item.getItemId()==R.id.main_find_friends)
         {

         }
         return true;

     }

     private void RequestNewGroup() {
         AlertDialog.Builder builder= new AlertDialog.Builder(this,R.style.AlertDialog);
         builder.setTitle("Enter Group Name :");
         final EditText groupNameField=new EditText(this);
         groupNameField.setHint("  e.g.  NullClass");
         groupNameField.setGravity(View.TEXT_ALIGNMENT_CENTER);
         builder.setView(groupNameField);
         builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 String groupName=groupNameField.getText().toString();
                 if(TextUtils.isEmpty(groupName))
                 {
                     Toast.makeText(getApplicationContext(), "Please write Group Name", Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                          CreateNewGroup(groupName);
                 }
             }
         });
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.cancel();
                 dialogInterface.dismiss();
             }
             });
          builder.show();


     }

     private void CreateNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(), groupName+" group is Created Successfully ...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
     }
 }

