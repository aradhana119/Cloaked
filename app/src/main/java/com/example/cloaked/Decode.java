/*
 * Copyright (c) 2021 Aradhana Ghosh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.cloaked;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Decode extends AppCompatActivity {
    private static final int SELECT_PICTURE = 10;

    private ImageView encodedImage;
    private TextView extractedMsg;
    private EditText pwd;
    private TextView isDecoded;

    private Bitmap originalImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        isDecoded = findViewById(R.id.isDecoded);
        encodedImage = findViewById(R.id.encodedImageView);
        extractedMsg = findViewById(R.id.extractedMessageText);
        pwd = findViewById(R.id.pwdVerificationText);

        Button uploadImage = findViewById(R.id.uploadEncodedImage);
        Button startDecoding = findViewById(R.id.startDecoding);

        checkAndRequestPermissions();

        uploadImage.setOnClickListener(v -> chooseImage());

        startDecoding.setOnClickListener(v -> decodeImage());
    }

    //Choose the encoded image from gallery
    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/png");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    //Upload the encoded image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), selectedImage);
                    originalImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true);
                }
                encodedImage.setImageBitmap(originalImage);
                isDecoded.setText(R.string.upload_success);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(this, "Error occurred while uploading. Please try again.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(this, "Error occurred while uploading. Please try again.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    //Request permission to read from external storage
    private void checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }

    //For creating a help icon in the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    //For displaying user manual on clicking the help icon
    public void onComposeAction(MenuItem mi) {
        Intent myIntent = new Intent(this, HelpInformation.class);
        this.startActivity(myIntent);
    }

    //Decode the secret message from the encoded image
    private void decodeImage() {
        if (!verifyPassword())
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
        else {

            StringBuilder msg = new StringBuilder();
            StringBuilder rowsBin = new StringBuilder();
            int index = getActualPasswordSize();

            for (int x = 0; x < 2; x++) {
                int pixel = originalImage.getPixel(x, index + 1);
                int a = (pixel >> 24) & 0xff;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                String aBitString = Integer.toBinaryString(a);
                String rBitString = Integer.toBinaryString(r);
                String gBitString = Integer.toBinaryString(g);
                String bBitString = Integer.toBinaryString(b);

                rowsBin.append(aBitString.charAt(aBitString.length() - 1));
                rowsBin.append(rBitString.charAt(rBitString.length() - 1));
                rowsBin.append(gBitString.charAt(gBitString.length() - 1));
                rowsBin.append(bBitString.charAt(bBitString.length() - 1));
            }

            int rows = Integer.parseInt(rowsBin.toString(), 2);
            int lim = index + 1 + rows;

            for (int y = index + 2; y <= lim; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < 2; x++) {
                    int pixel = originalImage.getPixel(x, y);
                    int a = (pixel >> 24) & 0xff;
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;

                    String aBitString = Integer.toBinaryString(a);
                    String rBitString = Integer.toBinaryString(r);
                    String gBitString = Integer.toBinaryString(g);
                    String bBitString = Integer.toBinaryString(b);

                    sb.append(aBitString.charAt(aBitString.length() - 1));
                    sb.append(rBitString.charAt(rBitString.length() - 1));
                    sb.append(gBitString.charAt(gBitString.length() - 1));
                    sb.append(bBitString.charAt(bBitString.length() - 1));
                }
                int asciiVal = Integer.parseInt(sb.toString(), 2);
                char c = (char) asciiVal;
                msg.append(c);
            }
            extractedMsg.setText(msg.toString());
            isDecoded.setText(R.string.processing_complete);
        }
    }

    //Return the size of the actual password
    private int getActualPasswordSize() {
        StringBuilder pwdRowsBin = new StringBuilder();

        for (int x = 0; x < 2; x++) {
            int pixel = originalImage.getPixel(x, 0);
            int a = (pixel >> 24) & 0xff;
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;

            String aBitString = Integer.toBinaryString(a);
            String rBitString = Integer.toBinaryString(r);
            String gBitString = Integer.toBinaryString(g);
            String bBitString = Integer.toBinaryString(b);

            pwdRowsBin.append(aBitString.charAt(aBitString.length() - 1));
            pwdRowsBin.append(rBitString.charAt(rBitString.length() - 1));
            pwdRowsBin.append(gBitString.charAt(gBitString.length() - 1));
            pwdRowsBin.append(bBitString.charAt(bBitString.length() - 1));
        }
        return Integer.parseInt(pwdRowsBin.toString(), 2);
    }

    //Verify the entered password with the encoded password
    private boolean verifyPassword() {
        int pwdSize = getActualPasswordSize();
        String enteredPwd = pwd.getText().toString();
        if (pwdSize != enteredPwd.length()) return false;

        StringBuilder actPwd = new StringBuilder();

        for (int y = 1; y <= pwdSize; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < 2; x++) {
                int pixel = originalImage.getPixel(x, y);
                int a = (pixel >> 24) & 0xff;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                String aBitString = Integer.toBinaryString(a);
                String rBitString = Integer.toBinaryString(r);
                String gBitString = Integer.toBinaryString(g);
                String bBitString = Integer.toBinaryString(b);

                sb.append(aBitString.charAt(aBitString.length() - 1));
                sb.append(rBitString.charAt(rBitString.length() - 1));
                sb.append(gBitString.charAt(gBitString.length() - 1));
                sb.append(bBitString.charAt(bBitString.length() - 1));
            }
            int asciiVal = Integer.parseInt(sb.toString(), 2);
            char c = (char) asciiVal;
            actPwd.append(c);
        }
        return enteredPwd.equals(actPwd.toString());
    }
}