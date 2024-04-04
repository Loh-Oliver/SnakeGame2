//package com.example.snakegame;
//
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class LeaderboardActivity extends AppCompatActivity {
//
//    FirebaseFirestore firestore;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dead_page);
//
//        firestore=FirebaseFirestore.getInstance();
//
//        Map<String,Object> user = new HashMap<>();
//
//        user.put("firstName","easy");
//        user.put("lastName","tuto");
//        user.put("description","Subscribe");
//
//        firestore.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//             Toast.makeText(getApplicationContext(),"Success", Toast.LENGTH_LONG).show();
//            }
//
//        }).addOnFailureListener(new OnFailureListener(){
//            @Override
//            public void onFailure(@NonNull Exception e){
//                Toast.makeText(getApplicationContext(),"Failure",Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }
//}
