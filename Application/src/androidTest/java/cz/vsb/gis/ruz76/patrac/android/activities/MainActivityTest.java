package cz.vsb.gis.ruz76.patrac.android.activities;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cz.vsb.gis.ruz76.patrac.android.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void A_WelcomeMessageDisplayed_test() throws InterruptedException {
        onView(withText(getResourceString(R.string.welcome_message))).check(matches(isDisplayed()));
        Thread.sleep(2000);
    }

    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

    @Test
    public void B_MenuItemsPresented_test() throws InterruptedException {
        onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        try {
            onView(withText("MAPA")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("NASTAVENÍ")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("ODESLAT ZPRÁVU")).check(matches(isDisplayed()));
        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Nastavení")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void C_AppToolbarName_test() {
        onView(withText("Pátrač Monitor")).check(matches(isDisplayed()));
    }

    @Test
    public void D_MessageSent_negative_test() throws InterruptedException {
        //With no connection to server
        try {
    onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(2000);
    onView(withText("ODESLAT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
    onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
        Thread.sleep(2000);
    onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(10000);
        //Todo Actual result app Crash
        //Expected result: App should not Crash but put some error message
        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(2000);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(2000);
            onView(withText("Štáb")).perform(click());
            Thread.sleep(5000);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(2000);
            //Todo Actual result app Crash
            //Expected result: App should not Crash but put some error message
        }
}

    @Test
    public void E_MessageSent_positive_test() throws InterruptedException {
        //With connection to server
        try {
        onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("PŘIPOJIT")).perform(click());
        Thread.sleep(2000);
        onView(withText("ODPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(2000);
        onView(withText("ODESLAT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(2000);
        onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
        Thread.sleep(2000);
        onView(withId(R.id.send_message_action)).perform(click());
            //Todo, not able to check toast messages, which has no visible(no id, no text)
//            Thread.sleep(1000);
//            onView(withText("NO RECIPIENT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Štáb")).check(matches(isDisplayed()));
        onView(withText("Štáb")).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(1000);
            //Todo, not able to check toast messages, which has no visible(no id, no text)
//            onView(withText("Zpráva byla odeslána")).check(matches(isDisplayed()));
//            Thread.sleep(5000);

    } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(2000);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(2000);
            onView(withId(R.id.send_message_action)).perform(click());
            //Todo, not able to check toast messages, which has no visible(no id, no text)
//            Thread.sleep(1000);
//            onView(withText("NO RECIPIENT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Štáb")).check(matches(isDisplayed()));
            onView(withText("Štáb")).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(1000);
            //Todo, not able to check toast messages, which has no visible(no id, no text)
//            onView(withText("Zpráva byla odeslána")).check(matches(isDisplayed()));
//            Thread.sleep(5000);

        }
    }

    @Test
    public void F_AttachmentSent_positive_test() throws InterruptedException {
        //With connection to server
        try {
            onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("PŘIPOJIT")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(2000);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(2000);
            onView(withId(R.id.send_message_attachment_action)).perform(click());
            Thread.sleep(2000);
            onView(withText("sdcard")).perform(click());
            Thread.sleep(2000);
            onView(withText("DCIM")).perform(click());
            Thread.sleep(2000);
//            Neccessary step in terminal: adb push android_attachment.jpg /sdcard/DCIM
//            onView(ViewMatchers.withId(R.id.fileList)).perform(ViewActions.swipeDown());
            onView(withText("android_attachment.jpg")).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.select)).perform(click());
            Thread.sleep(2000);
            onView(withText("Štáb")).check(matches(isDisplayed()));
            onView(withText("Štáb")).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(5000);
        }   catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(2000);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(2000);
            onView(withId(R.id.send_message_attachment_action)).perform(click());
            Thread.sleep(2000);
            onView(withText("sdcard")).perform(click());
            Thread.sleep(2000);
            onView(withText("DCIM")).perform(click());
            Thread.sleep(2000);
//            Neccessary step in terminal: adb push android_attachment.jpg /sdcard/DCIM
//            onView(ViewMatchers.withId(R.id.fileList)).perform(ViewActions.swipeDown());
            onView(withText("android_attachment.jpg")).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.select)).perform(click());
            Thread.sleep(2000);
            onView(withText("Štáb")).check(matches(isDisplayed()));
            onView(withText("Štáb")).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(5000);
        }
    }

    @Test
    public void G_Connection_test() throws InterruptedException {
        //Todo Bug on devices with resolution 400 x 800
        onView(withText("Čekám na pátrání")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("PŘIPOJIT")).perform(click());
        Thread.sleep(2000);
        onView(withText("ODPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Seznam zpráv")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Sleduji pohyb")).check(matches(isDisplayed()));
        Thread.sleep(2000);
    }

    @Test
    public void H_Settings_test() throws InterruptedException {
        try {
            onView(withText("NASTAVENÍ")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("NASTAVENÍ")).perform(click());
            Thread.sleep(2000);
            // Set Name
            onView(withText("Jméno")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Jméno")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Set ID
            onView(withText("Identifikátor pátrání")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Identifikátor pátrání")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("XXX"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Set System ID
            onView(withText("Systémový identifikátor")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Systémový identifikátor")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Navigate back to home screen
            onView(withContentDescription("Navigate up")).perform(click());
            Thread.sleep(2000);
            //Todo Bug on devices with resolution 400 x 800
            onView(withText("Čekám na pátrání")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("PŘIPOJIT")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Seznam zpráv")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Sleduji pohyb")).check(matches(isDisplayed()));
            Thread.sleep(2000);
        }   catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(2000);
            onView(withText("Nastavení")).check(matches(isDisplayed()));
            onView(withText("Nastavení")).perform(click());
            Thread.sleep(2000);
            onView(withText("Obecné")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Obecné")).perform(click());
            // Set Name
            onView(withText("Jméno")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Jméno")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Set ID
            onView(withText("Identifikátor pátrání")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Identifikátor pátrání")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("XXX"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Set System ID
            onView(withText("Systémový identifikátor")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Systémový identifikátor")).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(click());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(2000);
            onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
            Thread.sleep(2000);
            onView(withText("OK")).perform(click());
            Thread.sleep(2000);
            // Navigate back to home screen
            onView(withContentDescription("Navigate up")).perform(click());
            Thread.sleep(2000);
            //Todo Bug on devices with resolution 400 x 800
            onView(withText("Čekám na pátrání")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("PŘIPOJIT")).perform(click());
            Thread.sleep(2000);
            onView(withText("ODPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Seznam zpráv")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Sleduji pohyb")).check(matches(isDisplayed()));
            Thread.sleep(2000);

        }
    }

    @Test
    public void I_TapOnMap_negative_test() throws InterruptedException {
        try {
            //With no External Location app open at background
            onView(withText("MAPA")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("MAPA")).perform(click());
            Thread.sleep(10000);
    } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            onView(withText("Mapa")).perform(click());
            Thread.sleep(2000);
        }
    }

    @Test
    public void J_TapOnMap_positive_test() throws InterruptedException {
        //With no External Location app open at background
        try {
        onView(withText("MAPA")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("MAPA")).perform(click());
        Thread.sleep(2000);
        onView(withText("Stopy a pozice")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Lokální stopa")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Poslední pozice pátračů")).check(matches(isDisplayed()));
        Thread.sleep(2000);
        onView(withText("Stopy pátračů")).check(matches(isDisplayed()));
        Thread.sleep(2000);
//        onView(withText("Lokální stopa")).perform(click());
//        Thread.sleep(10000);
        //Todo Actual result: app Crash
        //Expected result: App should not Crash but put some error message
//        onView(withText("Poslední pozice pátračů")).perform(click());
//        Thread.sleep(5000);
//        onView(withText("Stopy pátračů")).perform(click());
//        Thread.sleep(5000);
        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            onView(withText("Mapa")).perform(click());
            Thread.sleep(2000);
            onView(withText("Stopy a pozice")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Lokální stopa")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Poslední pozice pátračů")).check(matches(isDisplayed()));
            Thread.sleep(2000);
            onView(withText("Stopy pátračů")).check(matches(isDisplayed()));
            Thread.sleep(2000);
//        onView(withText("Lokální stopa")).perform(click());
//        Thread.sleep(10000);
            //Todo Actual result: app Crash
            //Expected result: App should not Crash but put some error message
//            onView(withText("Poslední pozice pátračů")).perform(click());
//            Thread.sleep(5000);
//            onView(withText("Stopy pátračů")).perform(click());
//            Thread.sleep(5000);
//        }
    }
}