package com.netcall.test;

import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallHttps;
import com.netcall.core.NetCallProc;
import com.netcall.test.call.CallTest;
import com.netcall.test.call.CallTestCache;

import org.junit.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testAnnotation() {
        CallTest callTest = new CallTest("");
        Inherited inherited = callTest.getClass().getAnnotation(Inherited.class);
        CallGet callData = callTest.getClass().getAnnotation(CallGet.class);
        NetCallProc callProc = new NetCallProc(callTest);
//        callTest.call();
        System.out.println("~~~~" + inherited);
        System.out.println("~~~~" + callTest.getClass().isAnnotationPresent(Target.class));
        System.out.println("~~~~" + callTest.getClass().isAnnotationPresent(CallGet.class));
        System.out.println("~~~~" + callTest.getClass().getAnnotation(Target.class));
        System.out.println("~~~~" + callProc.getCallMethod());

        CallTestCache callTestCache = new CallTestCache("Yes");
        System.out.println("~~~~~~~~~~ " + callTestCache.getClass().isAnnotationPresent(CallGet.class));
        System.out.println("~~~~~~~~~~ " + (callTestCache.getClass().getAnnotation(CallGet.class) != null));
        System.out.println("~~~~~~~~~~ " + callTestCache.getClass().isAnnotationPresent(CallHttps.class));
    }
}