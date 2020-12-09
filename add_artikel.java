package com.example.babycare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.babycare.Model.Artikel;
import com.example.babycare.Model.Menumakanan;
import com.example.babycare.Model.Riwayatmakanan;
import com.example.babycare.Model.UserAktiv;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class add_artikel extends AppCompatActivity {

    private FirebaseFirestore dbG = FirebaseFirestore.getInstance();
    private CollectionReference artikelbookRef = dbG.collection("artikel");

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button mButtonChooseImage;
    private Uri mImageUri;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private StorageReference muserStorageRef;
    private StorageTask mUploadTask;

    String imageUrl = "";

    protected Cursor cursor;
    UserAktiv dbHelper;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener_anak;
    String date = "";

    private EditText edit_judul_artikel;
    private EditText edit_keterangan;

    int id = 0;
    int idambil = 0;

    String email = "";

    String judul_artikel_ambil = "";
    String keterangan_ambil = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_artikel);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        edit_judul_artikel = findViewById(R.id.txt_nama);
        edit_keterangan = findViewById(R.id.txt_isi_artikel);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        muserStorageRef = FirebaseStorage.getInstance().getReference("uploads_menu_makanan");

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        RelativeLayout btn_back_forum = findViewById(R.id.btn_back);
        btn_back_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gohome = new Intent(add_artikel.this, home_admin.class);
                startActivity(gohome);
                finish();
            }
        });


    }


    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.get()
                    .load(mImageUri)
                    .into(mImageView);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void tambah_artikel(View view){

        final StorageReference fileReference = muserStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

        mUploadTask = fileReference.putFile(mImageUri);
        Task urlTask = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(0);
                    }
                }, 500);
//                Toast.makeText(new_galangdana.this,taskSnapshot.getUploadSessionUri().toString(), Toast.LENGTH_SHORT).show();

                imageUrl = task.getResult().toString();

                judul_artikel_ambil = edit_judul_artikel.getText().toString();
                keterangan_ambil = edit_keterangan.getText().toString();
                int idfinal = id + 1;

                artikelbookRef.add(new Artikel(idfinal, imageUrl, judul_artikel_ambil, keterangan_ambil));
                Toast.makeText(add_artikel.this, "Berhasil Menambahkan", Toast.LENGTH_SHORT).show();
                Intent goartikel = new Intent(add_artikel.this, home_admin.class);
                startActivity(goartikel);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        CollectionReference kmsRef = FirebaseFirestore.getInstance().collection("artikel");
        kmsRef.orderBy("no", Query.Direction.ASCENDING).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null){
                    return;
                }

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Artikel Artikel = documentSnapshot.toObject(Artikel.class);

                    idambil = Artikel.getNo();
                    id = idambil;
                }
            }
        });

    }


}
