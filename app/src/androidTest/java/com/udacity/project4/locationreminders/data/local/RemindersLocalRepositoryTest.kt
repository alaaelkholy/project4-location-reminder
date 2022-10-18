package com.udacity.project4.locationreminders.data.local

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setupRepository() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun insertEqualsRetrieve() = runBlocking {
        val reminder = ReminderDTO("Title", "Description", "Location", 19.0, 20.2)

        remindersLocalRepository.saveReminder(reminder)
        val reminder2: Result.Success<ReminderDTO> = remindersLocalRepository.getReminder(reminder.id) as Result.Success

        assertThat(reminder2.data, `is`(reminder))
    }

    @Test
    fun noReminderError() = runBlocking {
        val reminder = ReminderDTO("Title", "Description", "Location", 19.0, 20.2)
        val id = reminder.id
        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminder(id) as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }

}