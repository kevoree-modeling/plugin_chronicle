package org.kevoree.modeling.plugin;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;
import org.kevoree.modeling.scheduler.impl.DirectScheduler;

public class ChronicleTest {

    @Test
    public void test() {
        MetaModel metaModel = new MetaModel("MyMetaModel");
        final KMetaClass sensorMetaClass = metaModel.addMetaClass("Sensor");
        sensorMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        sensorMetaClass.addAttribute("value", KPrimitiveTypes.DOUBLE);
        sensorMetaClass.addRelation("siblings", sensorMetaClass, null);

        KMetaClass homeMetaClass = metaModel.addMetaClass("Home");
        homeMetaClass.addAttribute("name", KPrimitiveTypes.STRING);
        homeMetaClass.addRelation("sensors", sensorMetaClass, null);

        final KModel universe = metaModel.createModel(
                DataManagerBuilder
                        .create()
                        .withScheduler(new DirectScheduler())
                        .withContentDeliveryDriver(new ChroniclePlugin(100000))
                        .build()
        );

        universe.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KObject home = universe.universe(0).time(0).create(universe.metaModel().metaClassByName("Home"));
                home.set(home.metaClass().attribute("name"), "MainHome");

                KObject sensor = universe.universe(0).time(0).create(sensorMetaClass);
                sensor.set(sensor.metaClass().attribute("name"), "Sensor#1");

                home.addByName("sensors", sensor);

                universe.universe(0).time(0).json().save(home, new KCallback<String>() {
                    @Override
                    public void on(String s) {
                    }
                });
            }
        });
    }

}
