/*
 * BOCES
 *
 * Authors: Evan Black, Elizabeth Stanton
 */
package com.boces.black_stanton_boces.activity.teacher;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.boces.black_stanton_boces.R;
import com.boces.black_stanton_boces.persistence.PersistenceInteractor;
import com.boces.black_stanton_boces.persistence.model.Teacher;

/**
 * Collects Account Information From User And Creates New Teacher
 */
public class AdminAddTeacherActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText phone;
    private ImageView imageView;
    private Bitmap image;
    private PersistenceInteractor persistence;

    private static final int EXTERNAL_STORAGE_REQUEST = 0;
    private static final int RESULT_LOAD_IMAGE = 1;

    /**
     * Gathers Input References
     * @param savedInstanceState
     * Unused/Null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_teacher);

        // Get Input References
        firstName = findViewById(R.id.inputTeacherFirstName);
        lastName = findViewById(R.id.inputTeacherLastName);
        email = findViewById(R.id.inputTeacherEmail);
        phone = findViewById(R.id.inputTeacherPhone);
        imageView = findViewById(R.id.imgTeacher);

        persistence = new PersistenceInteractor(this);
    }

    /**
     * Checks If Required Field Is Empty Before For Saving
     * @param v
     * Current View
     */
    public void onSave(View v) {
        Teacher teacher = new Teacher();
        boolean hasError = false;

        if (firstName.getText().toString().trim().isEmpty()) {
            hasError = true;
            firstName.setError("First Name Is Required");
        } else
            teacher.setFirstName(firstName.getText().toString());

        if (lastName.getText().toString().trim().isEmpty()) {
            hasError = true;
            lastName.setError("Last Name Is Required");
        } else
            teacher.setLastName(lastName.getText().toString());


        teacher.setEmail(email.getText().toString());
        teacher.setPhoneNumber(phone.getText().toString());

        if (image != null)
            teacher.setImage(image);

        if (hasError)
            return;

        persistence.addTeacher(teacher);
        finish();//Ends activity
    }

    /**
     * Bring Up A Media Picker To Choose A Picture
     * If Not Already Granted, Requests WRITE_EXTERNAL_STORAGE Permission
     * Reenters onActivityResult With Result RESULT_LOAD_IMAGE
     * @param v
     * Current View, May Be Null
     */
    public void onSelectImage(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST);
            return;
        }
        Intent mediaIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(mediaIntent, RESULT_LOAD_IMAGE);
    }

    /**
     * Reentrant Point For onSelectImage
     * Enters With RESULT_LOAD_IMAGE
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();
            String filePathColumn[] = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            String path = "";

            if (cursor == null)
                return;

            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            }
            cursor.close();

            if (path.isEmpty())
                return;

            image = BitmapFactory.decodeFile(path);
            if (image == null)
                return;

            imageView.setImageBitmap(image);
        }
    }

    /**
     * Reentrant Point If Permission Must Be Requested
     * Enters With EXTERNAL_STORAGE_REQUEST
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onSelectImage(null);
                }
                break;
        }
    }

}
