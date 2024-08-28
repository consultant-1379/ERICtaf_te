package com.ericsson.cifwk.taf.allure;

import com.ericsson.cifwk.taf.annotations.TestId;
import org.testng.annotations.Test;

import static org.testng.FileAssert.fail;

public class CIP9384_Test {

    @Test
    @TestId(id = "sample_allure_sprint_test1", title = "Sample CIP9384 test 1")
    public void test1() {
        System.out.println("Running sample_allure_sprint_test1");
    }

    @Test
    @TestId(id = "sample_allure_sprint_test2", title = "Sample CIP9384 test 2")
    public void test2() {
        System.out.println("Running sample_allure_sprint_test2");
        fail();
    }

    @Test
    @TestId(id = "sample_allure_sprint_test3", title = "Sample CIP9384 test 3")
    public void test3() {
        System.out.println("Running sample_allure_sprint_test3");
    }

    @Test
    @TestId(id = "sample_allure_sprint_test4", title = "Sample CIP9384 test 4")
    public void test4() {
        System.out.println("Running sample_allure_sprint_test4");
        fail();
    }

}
