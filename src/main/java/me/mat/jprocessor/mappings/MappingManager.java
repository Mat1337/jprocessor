package me.mat.jprocessor.mappings;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.processor.MappingProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingManager {

    private final Map<String, Mapping> classMappings = new HashMap<>();

    private final Map<String, Mapping> reverseClassMappings = new HashMap<>();

    private final Map<String, List<FieldMapping>> fieldMappings = new HashMap<>();

    private final Map<String, List<MethodMapping>> methodMappings = new HashMap<>();

    private String currentClass;

    public MappingManager(MappingProcessor processor, File mappings) throws MappingLoadException {
        // if the mappings file does not exist alert the user
        if (!mappings.exists()) {
            throw new MappingLoadException(mappings.getAbsolutePath() + " does not exist");
        }

        // log to console that mappings are being loaded
        JProcessor.Logging.info("Loading the mappings");

        // load all the mappings
        processor.manager(this);
        try (BufferedReader reader = new BufferedReader(new FileReader(mappings))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processor.process(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // build the mapping data
        processor.build(classMappings, reverseClassMappings, fieldMappings, methodMappings);

        // log the loaded data to the console
        JProcessor.Logging.info("Loaded '%d' class mappings", classMappings.size());
        JProcessor.Logging.info("Loaded '%d' field mappings", fieldMappings.size());
        JProcessor.Logging.info("Loaded '%d' method mappings", methodMappings.size());
    }

    /**
     * Maps a class by its name and mapping
     *
     * @param name    name of the class
     * @param mapping mapping of the class
     */

    public void mapClass(String name, String mapping) {
        currentClass = mapping;

        Mapping currentMapping = new Mapping(name, mapping);
        classMappings.put(mapping, currentMapping);
        reverseClassMappings.put(name, currentMapping);
    }

    /**
     * Maps a field to the current class
     * by its name, mapping and return type
     *
     * @param name       name of the field that you want to map
     * @param mapping    mapping of the field that you want to map
     * @param returnType return type of the field that you want to map
     */

    public void mapField(String name, String mapping, String returnType) {
        List<FieldMapping> mappings = fieldMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new FieldMapping(name, mapping, returnType));
        fieldMappings.put(currentClass, mappings);
    }

    /**
     * Maps a method to the current class
     * by its name, mapping, return type and
     * the description
     *
     * @param name        name of the method that you want to map
     * @param mapping     mapping of the method that you want to map
     * @param returnType  return type of the method that you want to map
     * @param description description of the method that you want to map
     */

    public void mapMethod(String name, String mapping, String returnType, String description) {
        List<MethodMapping> mappings = methodMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new MethodMapping(name, mapping, returnType, description));
        methodMappings.put(currentClass, mappings);
    }

}
