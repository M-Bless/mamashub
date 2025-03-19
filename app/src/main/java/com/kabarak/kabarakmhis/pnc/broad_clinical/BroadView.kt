package com.kabarak.kabarakmhis.pnc.broad_clinical

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.pnc.other_problems.OtherProblemsReported
import kotlinx.android.synthetic.main.activity_child_birth_view.btnAdd


class BroadView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_birth_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnAdd.setOnClickListener {
            val intent = Intent(this, BroadAdd::class.java)
            startActivity(intent)
        }


    }
}