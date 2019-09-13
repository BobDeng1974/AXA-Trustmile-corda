package com.sidis.eas.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarEventStateTests extends SidisBaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    public static String dataJSONString() {
        return "{ \"some-field\" : \"some-value\" }";
    }

//    public static CarEventState create(@NotNull UniqueIdentifier id, @NotNull Party initiator, @NotNull String vin,
//                                       @NotNull Integer timestamp, @NotNull Long mileage, @NotNull Boolean accident,
//                                       Map<String, Object> data) {

    @Test
    public void test_create() {
        CarEventState carEvent = CarEventState.create(
                new UniqueIdentifier(),
                this.redCar.party,
                "42",
                1568211122,
                27000L,
                true,
                JsonHelper.convertStringToJson(dataJSONString()));
        Assert.assertEquals("car must be equal",
                this.redCar.party, carEvent.getInsuredCar());
    }

}
