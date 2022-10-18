package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application

    private lateinit var dataSource: FakeDataSource

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(application)
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(application, dataSource)
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem("Title", "Description", "Location", 19.0, 20.2)

        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.validateAndSaveReminder(reminder)

        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(false)
        )
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {

        val reminderNoTitle = ReminderDataItem(null, "Description", "Location", 19.0, 20.2)
        saveReminderViewModel.validateAndSaveReminder(reminderNoTitle)

        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(Matchers.notNullValue())
        )
    }

}