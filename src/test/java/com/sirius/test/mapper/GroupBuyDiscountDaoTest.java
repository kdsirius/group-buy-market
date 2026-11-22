package com.sirius.test.mapper;

import com.sirius.mapper.IGroupBuyDiscountDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GroupBuyDiscountDaoTest {
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Test
    public void testQueryGroupBuyDiscountList() {
        log.info("testQueryGroupBuyDiscountList: {}", groupBuyDiscountDao.queryGroupBuyDiscountList());
    }
}
