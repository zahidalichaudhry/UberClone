package com.itpvt.uberclone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverSettingActivity extends AppCompatActivity {
    private EditText mNameField,mPhoneField,mCarField;
    private Button mBack,nConfirm;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID,nName,nPhone,mProfileImageuRl,mCar,mServics;
    private Uri resultUri;
    private ImageView mprofileImage;
    private RadioGroup mRadioGroup;
    private final int IMG_REQUEST=9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);
        mNameField=(EditText)findViewById(R.id.name);
        mPhoneField=(EditText)findViewById(R.id.phone);
        mCarField=(EditText)findViewById(R.id.car);
        mBack=(Button)findViewById(R.id.back);
        nConfirm=(Button)findViewById(R.id.confirm);
        mprofileImage=(ImageView)findViewById(R.id.profileimage);
        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        mprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent =new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent,5);
                selectImage();
            }
        });
        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();
        mDriverDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(userID);
        nConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
        getUserinfo();
    }

    private void getUserinfo()
    {
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0)
                {
                    Map<String ,Object> map=(Map<String,Object>) dataSnapshot
                            .getValue();
                    if (map.get("name")!=null)
                    {
                        nName=map.get("name").toString();
                        mNameField.setText(nName);
                    }
                    if (map.get("phone")!=null)
                    {
                        nPhone=map.get("phone").toString();
                        mPhoneField.setText(nPhone);
                    }
                    if (map.get("car")!=null)
                    {
                        mCar=map.get("car").toString();
                        mCarField.setText(mCar);
                    }
                    if (map.get("Service")!=null)
                    {
                        mServics=map.get("Service").toString();
//                        mCarField.setText(mCar);
                        switch (mServics)
                        {
                            case "UberX":
                                mRadioGroup.check(R.id.UberX);
                                break;
                            case "UberBlack":
                                mRadioGroup.check(R.id.UberBlack);
                                break;
                            case "UberXl":
                                mRadioGroup.check(R.id.UberXl);
                                break;
                        }
                    }
                    if (map.get("profileImageUrl")!=null)
                    {
                        mProfileImageuRl=map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageuRl).into(mprofileImage);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void saveUserInformation()
    {
        nName=mNameField.getText().toString();
        nPhone=mPhoneField.getText().toString();
        mCar=mCarField.getText().toString();

        int selectId=mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton=(RadioButton)findViewById(selectId);
        if (radioButton.getText()==null)
        {
            return;
        }
        mServics=radioButton.getText().toString();

        Map userinfo= new HashMap();
        userinfo.put("name",nName);
        userinfo.put("phone",nPhone);
        userinfo.put("car",mCar);
        userinfo.put("Service",mServics);
        mDriverDatabase.updateChildren(userinfo);

        if (resultUri!=null)
        {
            StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile_images")
                    .child(userID);
            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream boas=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);
            byte [] data=boas.toByteArray();
            UploadTask uploadTask=filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Uri downloadUrl=taskSnapshot.getDownloadUrl();
                    Map newImage=new HashMap();
                    newImage.put("profileImageUrl",downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        }else
        {
            finish();

        }

    }
    private void selectImage() {
        Intent intent=new Intent();
        intent.setType("image/*");//this define the type of intent
        intent.setAction(Intent.ACTION_GET_CONTENT);//this define the action of the intenet
        startActivityForResult(intent,IMG_REQUEST);//this call for result when the image is selected
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==IMG_REQUEST && resultCode== Activity.RESULT_OK)
        {
            final Uri imageUri=data.getData();
            resultUri=imageUri;
            mprofileImage.setImageURI(resultUri);
        }
    }
}
