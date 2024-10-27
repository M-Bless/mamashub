package com.kabarak.kabarakmhis.pnc.childpostnatalcare

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kabarak.kabarakmhis.R
import kotlinx.android.synthetic.main.activity_child_pnc_view.btnSave

class ChildPncView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_child_pnc_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSave.setOnClickListener {
            val intent = Intent(this, ChildPostnatalCare::class.java)
            startActivity(intent)
        }
    }

}