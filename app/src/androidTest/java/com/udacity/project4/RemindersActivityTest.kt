package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    // initialing Koin
    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }


@Before
fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(dataBindingIdlingResource)
}
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addNewReminder_ReminderListShowsNewReminderAndToast() = runBlocking {
        // empty
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val activity = getActivity(activityScenario)

        // add new reminder
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Title"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Description"))
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.select_map)).perform(ViewActions.longClick())
        Espresso.onView(ViewMatchers.withId(R.id.save_button)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.`is`(activity?.window?.decorView))))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )
        Espresso.onView(ViewMatchers.withText("Title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        runBlocking {
            delay(5000)
        }
    }

    @Test
    fun addNewReminderWithoutLocation_ShowsSnackbar() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
       // no location selected
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Title"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Description"))

        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        runBlocking {
            delay(5000)
        }
    }

}
