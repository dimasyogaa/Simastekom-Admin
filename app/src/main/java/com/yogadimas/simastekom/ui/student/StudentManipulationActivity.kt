package com.yogadimas.simastekom.ui.student

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yogadimas.simastekom.databinding.ActivityStudentManipulationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StudentManipulationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentManipulationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentManipulationBinding.inflate(layoutInflater)
        setContentView(binding.root)





        lifecycleScope.launch {
            withContext(Dispatchers.Main) {

                binding.mainProgressBar.visibility = View.VISIBLE

                delay(500)

                binding.mainProgressBar.visibility = View.INVISIBLE

                delay(10)

                // Inflate the first ViewStub immediately
                val view1 = binding.viewStubStudentManipulationTextInputs1.inflate()
                // Set initial visibility and animation properties
                view1.alpha = 0f
                view1.translationY = -100f // You can adjust this value for your needs

                // Start animation to make the view visible
                view1.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(210) // Duration of the animation in milliseconds
                    .start()

                delay(220)

                // Inflate the second ViewStub
                val view = binding.viewStubStudentManipulationTextInputs2.inflate()

                // Set initial visibility and animation properties
                view.alpha = 0f
                view.translationY = -100f // You can adjust this value for your needs

                // Start animation to make the view visible
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(210) // Duration of the animation in milliseconds
                    .start()
            }
        }

        // Inflate the HandleDataConnection ViewStub
//        binding.viewStubHandleDataConnection.inflate()



    }


}