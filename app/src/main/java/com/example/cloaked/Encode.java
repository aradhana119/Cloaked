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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Encode extends AppCompatActivity {
    private static final int SELECT_PICTURE = 10;

    private ImageView coverImage;
    private EditText message;
    private EditText pwd;
    private TextView isEncoded;

    private Bitmap encodedImage = null;
    private Bitmap originalImage = null;

    private Button startEncoding;
    private Button saveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        isEncoded = findViewById(R.id.isEncoded);
        coverImage = findViewById(R.id.coverImageView);
        message = findViewById(R.id.messageText);
        pwd = findViewById(R.id.pwdText);

        Button uploadImage = findViewById(R.id.uploadCoverImage);
        startEncoding = findViewById(R.id.startEncoding);
        saveImage = findViewById(R.id.saveImage);

        checkAndRequestPermissions();

        uploadImage.setOnClickListener(v -> chooseImage());

        startEncoding.setOnClickListener(v -> {
            int pwdLen = pwd.getText().toString().length();
            int msgLen = message.getText().toString().length();

            if(msgLen == 0 || msgLen > 100)
                Toast.makeText(this, "Please enter a valid message up to 100 characters for best results", Toast.LENGTH_SHORT).show();
            else if (pwdLen < 6 || pwdLen > 12)
                Toast.makeText(this, "Password length should be between 6 and 12 characters", Toast.LENGTH_SHORT).show();
            else
                encodeText();
        });

        saveImage.setOnClickListener(v -> {
            coverImage.setImageBitmap(encodedImage);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) coverImage.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            try {
                saveToExternalStorage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occurred while saving image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Choose a cover image from gallery
    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/jpg", "image/jpeg", "image/png"};
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    //Upload a cover image from gallery
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
                coverImage.setImageBitmap(originalImage);
                isEncoded.setText(R.string.upload_success);
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

    //Save encoded image to external storage
    private void saveToExternalStorage(Bitmap bitmap) throws IOException {
        OutputStream fos = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "img_" + System.currentTimeMillis() + ".png");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Steganography");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Objects.requireNonNull(fos);

                Toast.makeText(this, "Encoded image is saved", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred while saving image. Please try again.", Toast.LENGTH_SHORT).show();
        } finally {
            assert fos != null;
            fos.close();
        }
    }

    //Request permissions to read from and write to external storage
    private void checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

    //Convert the message string into an array of bits
    private int[] convertStringToBits(String str) {
        int[] strBits = new int[str.length() * 8]; //SizeOfFirstCharacter*8 + SizeOfSecondCharacter*8 + ... + SizeOfNthCharacter*8 = (SizeOfMessage)*8
        int k = 0;

        for (int i = 0; i < str.length(); i++) {
            int asciiVal = str.charAt(i);
            String bitString = Integer.toBinaryString(asciiVal);

            bitString = convertTo8BitForm(bitString); //Convert bit representation of each character into 8bit format
            for (int j = 0; j < 8; j++) {
                strBits[k++] = Integer.parseInt(String.valueOf(bitString.charAt(j))); //msgBits contains a bitString where every 8bit section is one character
            }
        }
        return strBits;
    }

    //Encode the text into an image using LSB encoding
    private void encodeText() {
        int[] msgBits = convertStringToBits(message.getText().toString());
        int[] pwdBits = convertStringToBits(pwd.getText().toString());

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        encodedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int pwdRows = pwdBits.length / 8;
        String pwdBitSpan = Integer.toBinaryString(pwdRows);
        if (pwdBitSpan.length() < 8)
            pwdBitSpan = convertTo8BitForm(pwdBitSpan);

        String msgBitSpan = Integer.toBinaryString(msgBits.length / 8);
        if (msgBitSpan.length() < 8)
            msgBitSpan = convertTo8BitForm(msgBitSpan);

        int m = 0, p = 0, i = 0, j = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = originalImage.getPixel(x, y);
                int a = (pixel >> 24) & 0xff;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                int aNewBit;
                int rNewBit;
                int gNewBit;
                int bNewBit;

                String aBitString = Integer.toBinaryString(a);
                String aNewBitString = aBitString.substring(0, aBitString.length() - 1);
                String rBitString = Integer.toBinaryString(r);
                String rNewBitString = rBitString.substring(0, rBitString.length() - 1);
                String gBitString = Integer.toBinaryString(g);
                String gNewBitString = gBitString.substring(0, gBitString.length() - 1);
                String bBitString = Integer.toBinaryString(b);
                String bNewBitString = bBitString.substring(0, bBitString.length() - 1);

                if (p < pwdBitSpan.length() && y == 0 && x < 2) {
                    aNewBitString += pwdBitSpan.charAt(p++);
                    aNewBit = Integer.parseInt(aNewBitString, 2);
                    rNewBitString += pwdBitSpan.charAt(p++);
                    rNewBit = Integer.parseInt(rNewBitString, 2);
                    gNewBitString += pwdBitSpan.charAt(p++);
                    gNewBit = Integer.parseInt(gNewBitString, 2);
                    bNewBitString += pwdBitSpan.charAt(p++);
                    bNewBit = Integer.parseInt(bNewBitString, 2);

                    encodedImage.setPixel(x, y, Color.argb(aNewBit, rNewBit, gNewBit, bNewBit));
                } else if (j < pwdBits.length && y > 0 && x < 2) {
                    aNewBitString += pwdBits[j++];
                    aNewBit = Integer.parseInt(aNewBitString, 2);
                    rNewBitString += pwdBits[j++];
                    rNewBit = Integer.parseInt(rNewBitString, 2);
                    gNewBitString += pwdBits[j++];
                    gNewBit = Integer.parseInt(gNewBitString, 2);
                    bNewBitString += pwdBits[j++];
                    bNewBit = Integer.parseInt(bNewBitString, 2);

                    encodedImage.setPixel(x, y, Color.argb(aNewBit, rNewBit, gNewBit, bNewBit));
                } else if (m < msgBitSpan.length() && y == pwdRows + 1 && x < 2) {
                    aNewBitString += msgBitSpan.charAt(m++);
                    aNewBit = Integer.parseInt(aNewBitString, 2);
                    rNewBitString += msgBitSpan.charAt(m++);
                    rNewBit = Integer.parseInt(rNewBitString, 2);
                    gNewBitString += msgBitSpan.charAt(m++);
                    gNewBit = Integer.parseInt(gNewBitString, 2);
                    bNewBitString += msgBitSpan.charAt(m++);
                    bNewBit = Integer.parseInt(bNewBitString, 2);

                    encodedImage.setPixel(x, y, Color.argb(aNewBit, rNewBit, gNewBit, bNewBit));
                } else if (i < msgBits.length && y > pwdRows + 1 && x < 2) {
                    aNewBitString += msgBits[i++];
                    aNewBit = Integer.parseInt(aNewBitString, 2);
                    rNewBitString += msgBits[i++];
                    rNewBit = Integer.parseInt(rNewBitString, 2);
                    gNewBitString += msgBits[i++];
                    gNewBit = Integer.parseInt(gNewBitString, 2);
                    bNewBitString += msgBits[i++];
                    bNewBit = Integer.parseInt(bNewBitString, 2);

                    encodedImage.setPixel(x, y, Color.argb(aNewBit, rNewBit, gNewBit, bNewBit));
                } else {
                    encodedImage.setPixel(x, y, Color.argb(a, r, g, b));
                }
            }
        }

        isEncoded.setText(R.string.processing_complete);
        startEncoding.setVisibility(View.GONE);
        saveImage.setVisibility(View.VISIBLE);
    }

    //Convert a string into an 8bit form
    private String convertTo8BitForm(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() != 8)
            sb.insert(0, '0');
        return sb.toString();
    }

}
