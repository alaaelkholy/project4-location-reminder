package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initRepository() {
        stopKoin()
        application = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get())  }
            single { LocalDB.createRemindersDao(application) }
        }

        startKoin {
            modules(listOf(myModule))
        }
        // Get  repository
        repository = GlobalContext.get().koin.get()

        // clear the data
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun stopKoinAfterTest() = stopKoin()

    @Test
    fun clickOnFAB_navigatesToSaveReminder() {
        // GIVEN - on ReminderList
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun twoRemindersInDB_UIshowsTwo() {
        runBlocking {
            repository.saveReminder(ReminderDTO("Title1", "Description1", "Location1", 15.0, 14.05))
            repository.saveReminder(ReminderDTO("Title2", "Description2", "Location2", 25.8, 19.0))
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText("Title1")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Title1")).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun emptyDB_noDataShows() {
        runBlocking {
            repository.deleteAllReminders()
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }
}