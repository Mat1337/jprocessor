package me.mat.jprocessor.mappings.generation.generator.impl;

import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryField;
import me.mat.jprocessor.jar.memory.MemoryLocalVariable;
import me.mat.jprocessor.jar.memory.MemoryMethod;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;

public class AlphabetMappingGenerator extends MappingGenerator {

    private final NameGenerator classNameGenerator = new NameGenerator();

    private NameGenerator fieldNameGenerator;
    private NameGenerator methodNameGenerator;
    private NameGenerator localVariableNameGenerator;

    @Override
    public String mapClass(String className, MemoryClass memoryClass) {
        this.fieldNameGenerator = new NameGenerator();
        this.methodNameGenerator = new NameGenerator();
        this.localVariableNameGenerator = new NameGenerator();
        return classNameGenerator.generate();
    }

    @Override
    public String mapField(String className, MemoryClass memoryClass, MemoryField memoryField) {
        return fieldNameGenerator.generate();
    }

    @Override
    public String mapMethod(String className, MemoryClass memoryClass, MemoryMethod memoryMethod) {
        this.localVariableNameGenerator = new NameGenerator();
        return methodNameGenerator.generate();
    }

    @Override
    public String mapLocalVariable(String className, MemoryClass memoryClass, MemoryMethod memoryMethod, MemoryLocalVariable localVariable) {
        return localVariableNameGenerator.generate();
    }

    private static final class NameGenerator {

        private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
        private static final int ALPHABET_LENGTH = ALPHABET.length() - 1;

        private String currentName = null;

        public String generate() {
            if (currentName == null) {
                currentName = String.valueOf(ALPHABET.charAt(0));
                return currentName;
            }

            char lastChar = getLastChar();
            boolean upperCase = Character.isUpperCase(lastChar);
            lastChar = Character.toLowerCase(lastChar);
            int index = ALPHABET.indexOf(lastChar);

            if (index == ALPHABET_LENGTH) {
                if (!upperCase) {
                    index = -1;
                    upperCase = true;
                } else {
                    currentName += ALPHABET.charAt(0);
                    return currentName;
                }
            }

            // clip the last character off the name
            currentName = currentName.substring(0, currentName.length() - 1);

            // append the next character
            char nextChar = ALPHABET.charAt(index + 1);
            if (upperCase) {
                nextChar = Character.toUpperCase(nextChar);
            }
            currentName += nextChar;

            // return the current name
            return currentName;
        }

        private char getLastChar() {
            return currentName.charAt(currentName.length() - 1);
        }

    }

}
