package com.example.progressnotes.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progressnotes.R
import com.example.progressnotes.adapter.WorkoutAdapter
import com.example.progressnotes.data.WorkoutAndExerciseDatabase
import com.example.progressnotes.data.dao.OfflineWorkoutDAO
import com.example.progressnotes.data.dao.WorkoutDAO
import com.example.progressnotes.data.entitie.Workout
import com.example.progressnotes.databinding.ActivityHomeBinding
import com.example.progressnotes.repository.HomeRepository
import com.example.progressnotes.signUp.LoginActivity
import com.example.progressnotes.viewmodel.HomeVeiwModel
import com.example.progressnotes.viewmodelfactories.HomeViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var dayOfWeekName: String
    private lateinit var binding: ActivityHomeBinding
    private val adapter = WorkoutAdapter()

    private lateinit var editor : SharedPreferences.Editor
    private lateinit var switchItem : MenuItem
    private lateinit var logoutItem : MenuItem
    private lateinit var navView : NavigationView
    private lateinit var logoutBT : MaterialButton
    private lateinit var themeSwitch : MaterialSwitch
    private lateinit var sharedPref : SharedPreferences

    private lateinit var viewModel: HomeVeiwModel
    private lateinit var repository: HomeRepository
    private lateinit var workoutDao: WorkoutDAO
    private lateinit var offlineWorkoutDao: OfflineWorkoutDAO
    private lateinit var database: WorkoutAndExerciseDatabase

    lateinit var updatedDate: EditText
    lateinit var calender: CalendarView
    lateinit var workoutRV: RecyclerView
    lateinit var updateWorkoutCard: CardView
    lateinit var updateWorkOutButton: Button
    lateinit var updatedWorkoutName: EditText
    lateinit var updateCalenderButton: ImageButton
    lateinit var cancelUpdateWorkOutButton: Button

    lateinit var cancelWorkoutDeleteButton: Button
    lateinit var workoutDeleteConfirmButton: Button
    lateinit var workoutDeleteConfirmationCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navView = binding.navView
        calender = binding.calender
        calender.visibility = View.GONE
        workoutRV = binding.recyclerView
        cancelWorkoutDeleteButton = binding.cancelTaskDeleteButton
        workoutDeleteConfirmButton = binding.taskDeleteConfirmButton
        workoutDeleteConfirmationCard = binding.taskDeleteConfirmationCard

        updatedDate = binding.updatedDate
        updateWorkOutButton = binding.updateButton
        updateWorkoutCard = binding.updateWorkoutCard
        updatedWorkoutName = binding.updatedWorkoutName
        updateCalenderButton = binding.updateCalenderButton
        cancelUpdateWorkOutButton = binding.cancelUpdateButton

        switchItem  = navView.menu.findItem(R.id.menu_switch)
        themeSwitch = switchItem.actionView as MaterialSwitch
        logoutItem = navView.menu.findItem(R.id.menu_logout)
        logoutBT = logoutItem.actionView as MaterialButton
        logoutBT.text = getString(R.string.logout_text)
        logoutBT.background = ContextCompat.getDrawable(this, R.drawable.bt_box)

        sharedPref = getSharedPreferences("theme_pref",MODE_PRIVATE)
        editor = sharedPref.edit()
        themeSwitch.isChecked = sharedPref.getBoolean("theme_switch_is_checked",true)

        workoutRV.adapter = adapter
        workoutRV.layoutManager = LinearLayoutManager(this)

        database = WorkoutAndExerciseDatabase.getDatabase(applicationContext)
        workoutDao = database.workoutDAO()
        offlineWorkoutDao = database.offlineWorkoutDAO()
        repository = HomeRepository(workoutDao, offlineWorkoutDao)
        viewModel = ViewModelProvider(this, HomeViewModelFactory(repository))[HomeVeiwModel::class.java]

        loggedInOrNot()
        loadWorkouts()
        loadTheme(switchItem,editor)
        viewModel.syncWorkout(this)
        logoutBT.setOnClickListener { binding.drawer.close();logout() }
        themeSwitch.setOnClickListener { changeTheme(themeSwitch.isChecked,switchItem,editor) }

        binding.apply {
            setting.setOnClickListener { drawer.open() }
            addWorkoutButton.setOnClickListener { getDate(date,addWorkoutCard,workoutRV) }
            saveButton.setOnClickListener { insertWorkout(workoutName,date,addWorkoutCard,workoutRV) }
            cancelButton.setOnClickListener { cancelWorkOut(workoutName,date,addWorkoutCard,workoutRV) }
            date.setOnClickListener { addWorkoutCard.visibility=View.GONE;calender.visibility = View.VISIBLE }
            calenderButton.setOnClickListener { addWorkoutCard.visibility=View.GONE;calender.visibility = View.VISIBLE }
            calender.setOnDateChangeListener { _, year, month, dayOfMonth -> onDateSelected(year, month, dayOfMonth,date,calender,addWorkoutCard) }
        }
    }
    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }


    private fun changeTheme(isChecked: Boolean, switch: MenuItem, editor: SharedPreferences.Editor) {
        editor.putBoolean("theme_switch_is_checked", isChecked)
        loadTheme(switch, editor)
        editor.apply()
    }

    private fun loadTheme(switch: MenuItem, editor: SharedPreferences.Editor) {
        val isChecked = themeSwitch.isChecked
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            switch.setIcon(R.drawable.dark_mode_icon)
            switch.title = "Dark Mode"
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            switch.setIcon(R.drawable.light_mode_icon)
            switch.title = "Light Mode"
        }
        editor.apply()
    }

    private fun loggedInOrNot() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadWorkouts() {
        viewModel.getWorkouts().observe(this) { days ->
            Log.d("Observer", "Observer triggered. Tasks: $days")
            days?.let {
                // Submit tasks to the adapter
                adapter.submitList(it)
            }
        }
    }

    private fun insertWorkout(name: EditText, date: EditText, cardView: CardView, recyclerView: RecyclerView) {
        if (name.text.isNullOrEmpty()) {
            name.error = "Please enter the workout name"
        } else if (date.text.isNullOrEmpty()) {
            date.error = "Please enter correct date"
        } else {
            val workOut = Workout(
                0,
                name.text.toString().uppercase(),
                dayOfWeekName,
                date.text.toString().uppercase()
            )
            viewModel.insertWorkout(workOut, this)
            name.setText("")
            date.setText("")
            cardView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun cancelWorkOut(name: EditText, date: EditText, cardView: CardView, recyclerView: RecyclerView) {
        name.setText("")
        date.setText("")
        cardView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun getDate(date: EditText,cardView: CardView,recyclerView: RecyclerView) {
        val today = LocalDate.now()
        val formattedToday = DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(today)
        dayOfWeekName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        date.setText(formattedToday)
        recyclerView.visibility = View.GONE
        cardView.visibility = View.VISIBLE
    }

    private fun onDateSelected(year: Int, month: Int, dayOfMonth: Int,date: EditText,calendarView: CalendarView,cardView: CardView) {
        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        val formattedDate = DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(selectedDate)
        date.setText(formattedDate)
        dayOfWeekName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        calendarView.visibility = View.GONE
        cardView.visibility = View.VISIBLE
    }
}