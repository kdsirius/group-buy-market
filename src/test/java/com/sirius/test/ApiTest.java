package com.sirius.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ApiTest {

    @Test
    public void test() {
        log.info("测试完成");
    }

    @Test
    public void test_split(){
        String testStr = ",2";
        String[] split = testStr.split(",");
        System.out.println(Arrays.toString(split));
        System.out.println(split.length);
    }

    @Test
    public void test_time(){
        System.out.println(new Date().getTime());
    }

}
