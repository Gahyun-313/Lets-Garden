package com.example.garden1

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.garden1.databinding.ActivityIssueBinding

class IssueActivity : AppCompatActivity() {

    private lateinit var binding2: ActivityIssueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = ActivityIssueBinding.inflate(layoutInflater)

        setContentView(binding2.root)

        binding2.exitButton.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            this.finish()
        }

    }
}