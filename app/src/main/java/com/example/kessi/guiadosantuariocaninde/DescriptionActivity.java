package com.example.kessi.guiadosantuariocaninde;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class DescriptionActivity extends AppCompatActivity {
    private TextView tTitle;
    private TextView tContent;
    private TextView tBusinessHours;

    private String title;
    private String text;
    private String image;
    private String horario;

    private ImageView mImageView;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* //Views
        tTitle = (TextView) findViewById(R.id.tTitle);
        tContent = (TextView) findViewById(R.id.tContent);
        tBusinessHours = (TextView) findViewById(R.id.tBusinessHours);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mImageView = (ImageView) findViewById(R.id.imageView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("title");
            text = extras.getString("text");
            image = extras.getString("image");
            horario = extras.getString("horario");
        }

        downloadFile(image);
        tTitle.setText(title);
        tContent.setText(text);
        tBusinessHours.setText(horario);
        */

    }

    private void downloadFile(String image) {
        mProgressBar.setVisibility(View.VISIBLE);
        Picasso.with(DescriptionActivity.this).load(image)
                .into(mImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

}
