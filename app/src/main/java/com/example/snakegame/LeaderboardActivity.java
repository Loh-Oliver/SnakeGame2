//package com.example.snakegame;
//
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//
//
//public class LeaderboardActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        RecyclerView recyclerView = findViewById(R.id.recyclerview);
//
//        List<Item> items = new ArrayList<Item>();
//        items.add(new Item("John wick","john.wick@email.com",R.drawable.a));
//        items.add(new Item("Robert j","robert.j@email.com",R.drawable.b));
//        items.add(new Item("James Gunn","james.gunn@email.com",R.drawable.c));
//        items.add(new Item("Ricky tales","rickey.tales@email.com",R.drawable.d));
//        items.add(new Item("Micky mose","mickey.mouse@email.com",R.drawable.e));
//        items.add(new Item("Pick War","pick.war@email.com",R.drawable.f));
//        items.add(new Item("Leg piece","leg.piece@email.com",R.drawable.g));
//        items.add(new Item("Apple Mac","apple.mac@email.com",R.drawable.g));
//        items.add(new Item("John wick","john.wick@email.com",R.drawable.a));
//        items.add(new Item("Robert j","robert.j@email.com",R.drawable.b));
//        items.add(new Item("James Gunn","james.gunn@email.com",R.drawable.c));
//        items.add(new Item("Ricky tales","rickey.tales@email.com",R.drawable.d));
//        items.add(new Item("Micky mose","mickey.mouse@email.com",R.drawable.e));
//        items.add(new Item("Pick War","pick.war@email.com",R.drawable.f));
//        items.add(new Item("Leg piece","leg.piece@email.com",R.drawable.g));
//        items.add(new Item("Apple Mac","apple.mac@email.com",R.drawable.g));
//
//
//
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(new MyAdapter(getApplicationContext(),items));
//
//    }
//
//}
