package com.sirius.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
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
        Date validStartTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(validStartTime);
        calendar.add(Calendar.MINUTE,10);
        System.out.println(calendar.getTime());
    }

}
