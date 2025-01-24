package com.kabarak.kabarakmhis.pnc.other_problems

import android.content.Intent
import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kabarak.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd

class OtherProblemsView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_other_problems)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnAdd.setOnClickListener {
            val intent = Intent(this, OtherProblemsReported::class.java)
            startActivity(intent)
        }
    }
}