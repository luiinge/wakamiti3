package es.iti.wakamiti.plugins.gherkin.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.nio.file.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.iti.wakamiti.api.Wakamiti;
import es.iti.wakamiti.core.WakamitiProperties;
import imconfig.Config;



class ITGherkinPlanAssembler {

    static {
        System.out.println("---------------------- using module ----------------------");
        System.out.println(ITGherkinPlanAssembler.class.getModule());
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "simpleScenario",
        "scenarioOutline",
        "background",
        "arguments"
    })
    void assembleSingleFeatureTests(String filename) throws IOException {

        System.out.println(filename);
        var conf = Config.factory().fromPairs(
            WakamitiProperties.REPOSITORY_TRANSIENT, "true",
            WakamitiProperties.RESOURCES_PATH, "src/test/resources/features/"+filename+".feature"
        );
        var wakamiti = Wakamiti.of(conf);
        var planID = wakamiti.buildPlan();
        var writer = new StringWriter();
        wakamiti.serialize(planID,writer);
        String assembledJson = writer.toString();
        System.out.println(assembledJson);
        var json = Files.readString(Path.of("src/test/resources/features/"+filename+".json"));
        assertEquals(json,assembledJson);
    }



    @Test
    void assembleRedefiningFeatureTest() throws IOException {
        var conf = Config.factory().fromPairs(
            WakamitiProperties.REPOSITORY_TRANSIENT, "true",
            WakamitiProperties.RESOURCES_PATH, "src/test/resources/features/redefining"
        );
        var wakamiti = Wakamiti.of(conf);
        var planID = wakamiti.buildPlan();
        var writer = new StringWriter();
        wakamiti.serialize(planID,writer);
        String assembledJson = writer.toString();
        var json = Files.readString(Path.of("src/test/resources/features/redefining/redefining_plan.json"));
        assertEquals(json,assembledJson);
    }




}
