package me.mat.jprocessor.jar;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.util.JarUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;

public class MemoryJar {

    public final Map<String, MemoryClass> classes = new HashMap<>();

    public final Map<String, MemoryResource> resources = new HashMap<>();

    public MemoryJar(File file) {
        // log to console that the jar's classes are loading into the memory
        JProcessor.Logging.info("Loading '%s' classes into memory", file.getName());

        // load all the classes into the memory
        JarUtil.load(file).forEach(classNode -> classes.put(classNode.name, new MemoryClass(classNode)));

        // setup the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.initialize(classes));

        // log to console how many classes were loaded
        JProcessor.Logging.info("Loaded '%d' classes into memory", classes.size());

        // log to console that the jar's resources are loading into the memory
        JProcessor.Logging.info("Loading '%s' resources into memory", file.getName());

        // load all the resources
        JarUtil.loadResource(file, resources);

        // log to console how many resources were loaded into memory
        JProcessor.Logging.info("Loaded '%d' resources into memory", resources.size());

        // log to console that the
        JProcessor.Logging.info("Setting up the class hierarchy");

        // build the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.findOverrides(memoryClass.superClass));
    }

    public void reMap(MappingManager mappingManager) {
        SimpleRemapper remapper = new SimpleRemapper(mappingManager.getMappings());
        classes.forEach((className, memoryClass) -> {
            ClassNode mappedNode = new ClassNode();
            ClassRemapper adapter = new ClassRemapper(mappedNode, remapper);

            memoryClass.classNode.accept(adapter);
            memoryClass.classNode = mappedNode;
        });
    }

    /**
     * Creates a class in the memory
     * and loads it into the jar
     *
     * @param version    version of java that this will target
     * @param access     access of the class
     * @param name       name of the class
     * @param sig        signature of the class
     * @param superName  name of the parenting class
     * @param interfaces array of interfaces that the class has
     * @return {@link MemoryClass}
     */

    public MemoryClass createClass(int version, int access, String name,
                                   String sig, String superName, String[] interfaces) {
        return createClass(Opcodes.ASM9, version, access, name, sig, superName, interfaces);
    }

    /**
     * Creates a class in the memory
     * and loads it into the jar
     *
     * @param api        version of the ASM api that you want to use
     * @param version    version of java that this will target
     * @param access     access of the class
     * @param name       name of the class
     * @param sig        signature of the class
     * @param superName  name of the parenting class
     * @param interfaces array of interfaces that the class has
     * @return {@link MemoryClass}
     */

    public MemoryClass createClass(int api, int version, int access,
                                   String name, String sig, String superName,
                                   String[] interfaces) {
        ClassNode classNode = new ClassNode(api);
        classNode.visit(version, access, name, sig, superName, interfaces);
        classNode.visitSource(name + ".java", null);
        classNode.visitEnd();

        MemoryClass memoryClass = new MemoryClass(classNode);
        classes.put(name, memoryClass);

        memoryClass.initialize(classes);
        memoryClass.findOverrides(memoryClass.superClass);

        return memoryClass;
    }

    /**
     * Saves the jar from memory
     * to a file on the disk
     *
     * @param file file that you want to save to
     */

    public void save(File file) {
        // log to console that the jar from memory is being saved to a file
        JProcessor.Logging.info("Saving the jar from memory to '%s'", file.getAbsolutePath());

        // create the jar output stream
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(file.toPath()))) {

            // alert the user that classes are being written
            JProcessor.Logging.info("Writing %d classes...", classes.size());

            // loop through all the class nodes and write them to the stream
            classes.forEach((name, memoryClass) -> memoryClass.write(out));

            // alert the user that classes are finished writing
            JProcessor.Logging.info("Finished writing classes");

            // alert the user that resources are being saved
            JProcessor.Logging.info("Saving %d resources...", resources.size());

            // loop through all the resources
            resources.forEach((name, resource) -> resource.write(out, name));

            // alert the user that classes are finished writing
            JProcessor.Logging.info("Finished saving resources");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
