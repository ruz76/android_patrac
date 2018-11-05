package cz.vsb.gis.ruz76.patrac.android.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by jencek on 2.11.18.
 */
@RunWith(AndroidJUnit4.class)
public class MapsActivityTest extends TestCase {

    @Rule
    public ActivityTestRule<MapsActivity> mActivityRule = new ActivityTestRule<>(
            MapsActivity.class);

    @Test
    public void testProcessResponse() throws Exception {
        //MapsActivity mapsActivity = new MapsActivity();
        mActivityRule.getActivity().processResponse(null);
    }

}