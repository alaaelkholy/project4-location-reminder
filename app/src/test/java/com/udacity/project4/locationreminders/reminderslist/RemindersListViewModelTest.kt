package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.MainCoroutineRule
import com.udacity.project4.locationreminders.savereminder.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application

    private lateinit var dataSource: FakeDataSource

    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(application)
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(application, dataSource)
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO("Title", "Description", "Location", 19.0, 20.2)
        dataSource.saveReminder(reminder)

        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false)
        )
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        dataSource.setReturnsError(true)
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            Matchers.`is`(Matchers.notNullValue())
        )
    }
}