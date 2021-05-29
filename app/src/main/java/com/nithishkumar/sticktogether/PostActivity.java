package com.nithishkumar.sticktogether;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;


import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private String imageUrl;

    private ImageView close;
    private  ImageView image_added;
    private TextView post;
    SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
        CropImage.activity().start(PostActivity.this);

    }

    private void upload() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final StorageReference filepath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            StorageTask uploadtask = filepath.putFile(imageUri);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    imageUrl = downloadUri.toString();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    String postId = ref.push().getKey();

                    Log.i("dffdf" , "dg");

                    HashMap<String , Object> map = new HashMap<>();
                    map.put("postid" , postId);
                    map.put("imageurl" , imageUrl);
                    map.put("description" , description.getText().toString());
                    map.put("publisher" , Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                    assert postId != null;
                    ref.child(postId).setValue(map);

                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                    List<String> hashTags = description.getHashtags();
                    if (!hashTags.isEmpty()) {
                        for (String tag : hashTags) {
                            map.clear();

                            map.put("tag", tag.toLowerCase());
                            map.put("postid", postId);

                            mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                        }
                    }
                    pd.dismiss();
                    //Toast.makeText(PostActivity.this, "image uploaded successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PostActivity.this , MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "No image was selected", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(imageUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();

            image_added.setImageURI(imageUri);
        }else {
            Toast.makeText(this, "Try again !!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this , MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ArrayAdapter<Hashtag> hashtagArrayAdapter = new HashtagArrayAdapter<>(getApplicationContext());
        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    hashtagArrayAdapter.add(new Hashtag(Objects.requireNonNull(snapshot1.getKey()), (int) snapshot1.getChildrenCount()));
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        description.setHashtagAdapter(hashtagArrayAdapter);
    }
}