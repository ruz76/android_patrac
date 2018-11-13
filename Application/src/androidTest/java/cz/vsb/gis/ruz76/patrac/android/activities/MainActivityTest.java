package cz.vsb.gis.ruz76.patrac.android.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import cz.vsb.gis.ruz76.patrac.android.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {

    /**
     * Possible to extent time sleep to visually contro the tests execution.
     * Nice for presentations.
     * For example to:
     * private static final int SLEEP_TIME = 2000;
     * private static final int SLEEP_TIME_MID = 5000;
     * private static final int SLEEP_TIME_LONG = 10000;
     */
    private static final int SLEEP_TIME = 2000;
    private static final int SLEEP_TIME_MID = 5000;
    private static final int SLEEP_TIME_LONG = 10000;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void welcomeMessageDisplayedTest() throws InterruptedException {
        onView(withText(getResourceString(R.string.welcome_message))).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void api_getlocationsTest() {
        RequestQueue queue = Volley.newRequestQueue(mActivityRule.getActivity());
        String url = "http://gisak.vsb.cz/patrac/mserver.php?operation=getlocations&searchid=QA";
        final String[] expectedResults = {"5bdedbe4078fd;2018-11-04 12:46:12;D;Pcr1234;18.788859 49.301304;",
                "5bdeedc9c32e2;2018-11-04 14:21:02;D;Pcr1234;18.781113 49.29651;",
                "5bdf00d583b8d;2018-11-04 14:23:17;D;Pcr1234;18.781182 49.296531;",
                "5be802a7e6588;2018-11-11 10:21:28;D;pcr1234;-122.084 37.421998;"};

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        for (String temp : expectedResults) {
                            assertThat(response, containsString(temp));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(stringRequest);
    }

    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

    @Test
    public void menuItemsPresentedTest() throws InterruptedException {
        onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        try {
            onView(withText("NASTAVENÍ")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Nastavení")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void appToolbarNameTest() {
        onView(withText("Pátrač Monitor")).check(matches(isDisplayed()));
    }

    @Test
    public void messageSentNegativeTest() throws InterruptedException {
        //With no connection to server
        try {
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(SLEEP_TIME);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(SLEEP_TIME_LONG);
        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("ODESLAT")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withClassName(endsWith("EditText"))).perform(clearText());
            Thread.sleep(SLEEP_TIME);
            onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Štáb")).perform(click());
            Thread.sleep(SLEEP_TIME_MID);
            onView(withId(R.id.send_message_action)).perform(click());
            Thread.sleep(SLEEP_TIME);
        }
    }

    @Test
    public void messageSentPositiveTest() throws InterruptedException {
        //With connection to server
        try {

            connectionTest();

            onView(withId(R.id.send_message_action)).perform(click());

            checkSendMessageSimple();

        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());

            checkSendMessageSimple();

        }
    }

    @Test
    public void attachmentSentPositiveTest() throws InterruptedException {
        try {
            writeMockAttachmentFile();
        } catch (IOException e) {
            fail("Can not write attachment file to the DCIM directory");
        }
        //With connection to server
        try {

            connectionTest();

            onView(withId(R.id.send_message_action)).perform(click());

            chekSendMessageActivity();

        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("Odeslat zprávu")).check(matches(isDisplayed()));
            onView(withText("Odeslat zprávu")).perform(click());

            chekSendMessageActivity();

        }
    }

    @Test
    public void connectionTest() throws InterruptedException {
        onView(withText("Čekám na pátrání")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("PŘIPOJIT")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("ODPOJIT")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Seznam zpráv")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME_MID);
        onView(withText("Sleduji pohyb")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME_MID);
    }

    @Test
    public void settingsTest() throws InterruptedException {
        try {
            onView(withText("NASTAVENÍ")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("NASTAVENÍ")).perform(click());

            checkSettingsActivity();

        } catch (NoMatchingViewException e) {
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());

            onView(withText("Nastavení")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Nastavení")).perform(click());

            checkSettingsActivity();
        }
    }

    @Test
    public void tapOnMapNegativeTest() throws InterruptedException {
        try {
            onView(withText("PŘIPOJIT")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            Thread.sleep(SLEEP_TIME);
            onView(withText("Mapa")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Mapa")).perform(click());
            Thread.sleep(SLEEP_TIME_MID);

            checkMapActivityNotConnected();

            Thread.sleep(SLEEP_TIME_LONG);
            onView(withContentDescription("Navigate up")).perform(click());
            Thread.sleep(SLEEP_TIME_LONG);

            connectionTest();


        } catch (NoMatchingViewException e) {
            Thread.sleep(SLEEP_TIME_LONG);
            onView(withText("MAPA")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("MAPA")).perform(click());
            Thread.sleep(SLEEP_TIME_MID);

            checkMapActivityNotConnected();

            onView(withContentDescription("Navigate up")).perform(click());
            Thread.sleep(SLEEP_TIME);

            connectionTest();
        }
    }

    @Test
    public void tapOnMapPositiveTest() throws InterruptedException {
        //With no External Location app open at background
        try {

            connectionTest();

            onView(withContentDescription("More options")).check(matches(isDisplayed()));
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Mapa")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("Mapa")).perform(click());

            checkMapActivityConnected();

        } catch (NoMatchingViewException e) {
            onView(withText("MAPA")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
            onView(withText("MAPA")).perform(click());

            checkMapActivityConnected();
        }
    }

    private void checkSendMessageSimple() throws InterruptedException {
        Thread.sleep(SLEEP_TIME);
        onView(withText("ODESLAT")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("Štáb")).check(matches(isDisplayed()));
        onView(withText("Štáb")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(1000);
    }

    private void chekSendMessageActivity() throws InterruptedException {
        Thread.sleep(SLEEP_TIME);
        onView(withText("ODESLAT")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.messageTextSend)).perform(typeText("Priklad sprava"));
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.send_message_attachment_action)).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("sdcard")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("DCIM")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("android_attachment.jpg")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.select)).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("Štáb")).check(matches(isDisplayed()));
        onView(withText("Štáb")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withId(R.id.send_message_action)).perform(click());
        Thread.sleep(SLEEP_TIME_MID);
    }

    private void checkSettingsActivity() throws InterruptedException {
        onView(withText("Obecné")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Obecné")).perform(click());
        // Set Name
        onView(withText("Jméno")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Jméno")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
        Thread.sleep(SLEEP_TIME);
        onView(withText("OK")).perform(click());
        Thread.sleep(SLEEP_TIME);
        // Set ID
        onView(withText("Identifikátor pátrání")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Identifikátor pátrání")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(typeText("QA"));
        Thread.sleep(SLEEP_TIME);
        onView(withText("OK")).perform(click());
        Thread.sleep(SLEEP_TIME);
        // Set System ID
        onView(withText("Systémový identifikátor")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Systémový identifikátor")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(clearText());
        Thread.sleep(SLEEP_TIME);
        onView(withClassName(endsWith("EditText"))).perform(typeText("pcr1234"));
        Thread.sleep(SLEEP_TIME);
        onView(withText("OK")).perform(click());
        Thread.sleep(SLEEP_TIME);
        // Navigate back to home screen
        onView(withContentDescription("Navigate up")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withContentDescription("Navigate up")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("Čekám na pátrání")).check(matches(isDisplayed()));
    }

    private void checkMapActivityNotConnected() throws InterruptedException {
        try {
        onView(withText("Připraven")).check(matches(isDisplayed()));
        onView(withText("Lokální stopa")).perform(click());
        Thread.sleep(SLEEP_TIME);
        onView(withText("Nejste připojeni, nemohu získat data o pozici.")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        } catch (NoMatchingViewException e) {
            onView(withText("V logu jsou méně než dvě pozice.")).check(matches(isDisplayed()));
            Thread.sleep(SLEEP_TIME);
        }


    }
    private void checkMapActivityConnected() throws InterruptedException {
        Thread.sleep(SLEEP_TIME);
        onView(withText("Stopy a pozice")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Lokální stopa")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Poslední pozice pátračů")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Stopy pátračů")).check(matches(isDisplayed()));
        Thread.sleep(SLEEP_TIME);
        onView(withText("Lokální stopa")).perform(click());
        Thread.sleep(SLEEP_TIME_LONG);
        onView(withText("Poslední pozice pátračů")).perform(click());
        Thread.sleep(SLEEP_TIME_MID);
        onView(withText("Stopy pátračů")).perform(click());
        Thread.sleep(SLEEP_TIME_MID);
    }

    private void writeMockAttachmentFile() throws IOException {
        File file = new File("/sdcard/DCIM/android_attachment.jpg");
        boolean sucess = file.createNewFile();
        byte[] data1={1,1,0,0};
        if(file.exists())
        {
            OutputStream fo = new FileOutputStream(file);
            fo.write(data1);
            fo.close();
        }
    }

    @Test
    public void api_gpx_lastTest() {

        String line;
        URL url = null;
        StringBuilder downloadedFile = new StringBuilder();
        try {
            url = new URL("http://gisak.vsb.cz/patrac/mserver.php?operation=getgpx_last&searchid=QA");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((line = in.readLine()) != null) {
                // do something with line
                downloadedFile.append(line);
                downloadedFile.append('\n');
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String[] expectedResults = {"<wpt lat=\"37.421998\" lon=\"-122.084\"><name>pcr1234</name><desc>SessionId: 5be026be7eedd</desc>",
                "<wpt lat=\"37.421998\" lon=\"-122.084\"><name>pcr1234</name><desc>SessionId: 5be02660d94d0</desc>"};
        for (String result : expectedResults) {
            assertThat(downloadedFile.toString(), containsString(result));
        }

    }
}