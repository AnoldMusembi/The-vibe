package com.majhub.thevibe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {
    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("Url");

        Picasso.get().load(imageUrl).into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

            Intent chatActivityIntent = new Intent(ImageViewerActivity.this,ChatActivity.class);
            startActivity(chatActivityIntent);
    }
}
