package com.example.progressnotes.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progressnotes.adapter.ExerciseAdapter
import com.example.progressnotes.data.dao.ExerciseDAO
import com.example.progressnotes.data.WorkoutAndExerciseDatabase
import com.example.progressnotes.data.dao.OfflineExerciseDAO
import com.example.progressnotes.data.entitie.Exercise
import com.example.progressnotes.databinding.ActivityExerciseBinding
import com.example.progressnotes.repository.ExerciseRepository
import com.example.progressnotes.viewmodel.ExerciseViewModel
import com.example.progressnotes.viewmodelfactories.ExerciseViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExerciseActivity : AppCompatActivity() {


    private var workoutId : Long = 0
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel : ExerciseViewModel
    private lateinit var adapter : ExerciseAdapter
    private lateinit var exerciseDao : ExerciseDAO
    private lateinit var binding : ActivityExerciseBinding
    private lateinit var database : WorkoutAndExerciseDatabase
    private lateinit var offlineExerciseDao : OfflineExerciseDAO

    private lateinit var toolbarTV : TextView
    private lateinit var backButton : ImageButton

    private lateinit var setsET : EditText
    private lateinit var repsET : EditText
    private lateinit var weightsET : EditText
    private lateinit var exerciseNameET : EditText
    private lateinit var addExerciseCard : CardView
    private lateinit var saveExerciseButton : Button
    private lateinit var cancelExerciseButton : Button
    private lateinit var addExerciseButton : FloatingActionButton

    lateinit var updateSetsET : EditText
    lateinit var updateRepsET : EditText
    lateinit var exerciseRV : RecyclerView
    lateinit var updateWeightsET : EditText
    lateinit var updateExerciseCard : CardView
    lateinit var updateExerciseButton : Button
    lateinit var updateExerciseNameET : EditText
    lateinit var cancelUpdateExerciseButton : Button

    lateinit var cancelExerciseDeleteButton : Button
    lateinit var exerciseDeleteConfirmButton : Button
    lateinit var exerciseDeleteConfirmationCard : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarTV = binding.toolbarTv
        backButton = binding.backButton
        exerciseRV = binding.exerciseRv

        setsET = binding.setsET
        repsET = binding.repsET
        weightsET = binding.weightsET
        saveExerciseButton = binding.saveButton
        exerciseNameET = binding.exerciseNameET
        addExerciseCard = binding.addExerciseCard
        addExerciseButton = binding.addStepsButton
        cancelExerciseButton = binding.cancelButton

        updateSetsET = binding.updateSetsET
        updateRepsET = binding.updateRepsET
        updateWeightsET = binding.updateWeightsET
        updateExerciseButton = binding.updateButton
        updateExerciseCard = binding.updateExerciseCard
        updateExerciseNameET = binding.updateExerciseNameET
        cancelUpdateExerciseButton = binding.cancelUpdateButton

        cancelExerciseDeleteButton = binding.cancelExerciseDeleteButton
        exerciseDeleteConfirmButton = binding.exerciseDeleteConfirmButton
        exerciseDeleteConfirmationCard = binding.exerciseDeleteConfirmationCard

        workoutId = intent.getLongExtra("id",0)
        toolbarTV.text = intent.getStringExtra("exerciseName")!!

        adapter = ExerciseAdapter()
        exerciseRV.adapter = adapter
        exerciseRV.layoutManager = LinearLayoutManager(this)

        database = WorkoutAndExerciseDatabase.getDatabase(applicationContext)
        exerciseDao = database.exerciseDAO()
        offlineExerciseDao = database.offlineExerciseDAO()
        repository = ExerciseRepository(exerciseDao,offlineExerciseDao)
        viewModel = ViewModelProvider(this, ExerciseViewModelFactory(repository))[ExerciseViewModel::class.java]

        loadExercise()
        viewModel.syncExercise(this)
        backButton.setOnClickListener { goToHome() }
        addExerciseButton.setOnClickListener {exerciseCardVisibility()}
        saveExerciseButton.setOnClickListener {saveExercise()}
        cancelExerciseButton.setOnClickListener { cancelExercise() }

    }

    private fun loadExercise(){
        viewModel.getExercise(workoutId).observe(this) { workoutWithExerciseList ->
            Log.d("Observer", "Observer triggered. TaskWithSteps: $workoutWithExerciseList")
            workoutWithExerciseList?.let { workoutWithExercise ->
                val steps = workoutWithExercise.exercise // Access the List<Step> directly
                adapter.submitList(steps)
            }
        }
    }

    private fun saveExercise(){
        if (exerciseNameET.text.isNullOrEmpty()){
            exerciseNameET.error = "Enter the name of the Exercise"
        }else if(setsET.text.isNullOrEmpty()){
            setsET.error = "Enter the number of sets"
        }else if(repsET.text.isNullOrEmpty()){
            repsET.error = "Enter the number of reps"
        }else if(weightsET.text.isNullOrEmpty()){
            weightsET.error = "Enter the amount of weight"
        }else{
            val newExercise = Exercise(0,
                exerciseNameET.text.toString().uppercase(),
                setsET.text.toString(),
                repsET.text.toString(),
                weightsET.text.toString(),
                workoutId)

            viewModel.insertExercise(newExercise,this)
            exerciseNameET.setText("")
            setsET.setText("")
            repsET.setText("")
            weightsET.setText("")

            addExerciseCard.visibility = View.GONE
            exerciseRV.visibility = View.VISIBLE
        }
    }

    private fun cancelExercise(){
        exerciseNameET.setText("")
        setsET.setText("")
        repsET.setText("")
        weightsET.setText("")

        addExerciseCard.visibility = View.GONE
        exerciseRV.visibility = View.VISIBLE
    }

    private fun exerciseCardVisibility(){
        if(addExerciseCard.visibility == View.GONE){
            exerciseRV.visibility = View.GONE
            addExerciseCard.visibility = View.VISIBLE
        }else{
            addExerciseCard.visibility = View.GONE
            exerciseRV.visibility = View.VISIBLE
        }
    }

    private fun goToHome(){
        startActivity(Intent(this@ExerciseActivity, HomeActivity::class.java))
        finish()
    }
}