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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button encode = findViewById(R.id.encodeOption);
        Button decode = findViewById(R.id.decodeOption);

        encode.setOnClickListener((v -> startActivity(new Intent(getApplicationContext(), Encode.class))));
        decode.setOnClickListener((v -> startActivity(new Intent(getApplicationContext(), Decode.class))));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    public void onComposeAction(MenuItem mi) {
        Intent myIntent = new Intent(this, HelpInformation.class);
        this.startActivity(myIntent);
    }
}