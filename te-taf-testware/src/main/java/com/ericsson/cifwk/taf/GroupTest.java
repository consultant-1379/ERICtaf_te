package com.ericsson.cifwk.taf;

import org.testng.annotations.Test;

public class GroupTest extends TafTestBase {

    @Test(groups={"group1"})
    public void group1_test1() {
        System.out.println("group1_test1");
    }

    @Test(groups={"group1"})
    public void group1_test2() {
        System.out.println("group1_test2");
    }

    @Test(groups={"group2"})
    public void group2_test1() {
        System.out.println("group2_test1");
    }

    @Test(groups={"group1", "group2"})
    public void groups12_test1() {
        System.out.println("group2_test2");
    }

    @Test
    public void grouplessTest() {
        System.out.println("grouplessTest");
    }

}
