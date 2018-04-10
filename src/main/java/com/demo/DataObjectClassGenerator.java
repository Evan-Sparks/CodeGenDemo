package com.demo;

import com.demo.db.MapDb;
import com.demo.types.markers.Edge;
import javassist.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A generator object which takes an Identifiable interface and creates a concrete implementation.
 */
public class DataObjectClassGenerator {
    private static final String PROXY_SUFFIX = "_GENERATED_PROXY";

    private static final Pattern getterPattern = Pattern.compile("get(.*)");
    private static final Pattern setterPattern = Pattern.compile("set(.*)");

    public Class implement(Class interfaceType) throws CannotCompileException, NotFoundException {
        String proxyName = interfaceType.getName() + PROXY_SUFFIX;

        System.out.println("Generating class for " + interfaceType.getSimpleName());

        ClassPool pool = new ClassPool(true);
        CtClass cc = pool.makeClass(proxyName);
        cc.addInterface(pool.makeInterface(interfaceType.getName()));

        Map<String, FieldInfo> fieldInfos = new HashMap<>();

        // Find the fields the class needs from the interface's methods
        for (Method method: interfaceType.getMethods()) {
            String methodName = method.getName();
            boolean isEdge = method.isAnnotationPresent(Edge.class);
            Matcher getMatcher = getterPattern.matcher(methodName);
            Matcher setMatcher = setterPattern.matcher(methodName);

            String fieldName = null;
            Class fieldType = null;
            if (getMatcher.find()) {
                // Method is a getter
                fieldName = getMatcher.group(1);
                fieldType = method.getReturnType();
            } else if (setMatcher.find()) {
                // Method is a setter
                fieldName = setMatcher.group(1);
                fieldType = method.getParameterTypes()[0];
            }

            FieldInfo fieldInfo = fieldInfos.get(fieldName);
            if (fieldInfo == null) {
                fieldInfo = new FieldInfo(fieldType);
                fieldInfos.put(fieldName, fieldInfo);
            }
            if (isEdge) {
                fieldInfo.markAsEdge();
            }
            fieldInfos.put(fieldName, fieldInfo);

        }

        // Add the field and method implementations to the class
        addConstrutor(cc);
        for (Map.Entry<String, FieldInfo> entry : fieldInfos.entrySet()) {
            if (!entry.getValue().isEdge()) {
                addField(entry.getKey(), entry.getValue().getType(), cc);
                addGetter(entry.getKey(), entry.getValue().getType(), cc);
                addSetter(entry.getKey(), entry.getValue().getType(), cc);
            } else {
                addEdgeField(entry.getKey(), entry.getValue().getType(), cc);
                addEdgeGetter(entry.getKey(), entry.getValue().getType(), cc);
                addEdgeSetter(entry.getKey(), entry.getValue().getType(), cc);
            }
        }

        System.out.println("Successfully generated class for " + interfaceType.getSimpleName());
        return cc.toClass();
    }

    private String lowercaseName(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private void addField(String name, Class type, CtClass cc) throws CannotCompileException, NotFoundException {
        CtField f = new CtField(ClassPool.getDefault().get(type.getName()), lowercaseName(name), cc);
        cc.addField(f);
    }

    private void addGetter(String name, Class type, CtClass cc) throws CannotCompileException {
        String getterStr = "public " + type.getName() + " get" + name + "() { return " + lowercaseName(name) + ";}";
        CtMethod method = CtNewMethod.make(getterStr, cc);
        cc.addMethod(method);
    }

    private void addSetter(String name, Class type, CtClass cc) throws CannotCompileException {
        String setterStr = "public void set" + name + "(" + type.getName() + " val) { " + lowercaseName(name) + " = val;}";
        CtMethod method = CtNewMethod.make(setterStr, cc);
        cc.addMethod(method);
    }

    private void addEdgeField(String name, Class type, CtClass cc) throws CannotCompileException, NotFoundException {
        String keyFieldName = keyFieldName(name);
        String typeFieldName = typeFieldName(type);
        CtField keyField = new CtField(ClassPool.getDefault().get(String.class.getName()), keyFieldName, cc);
        cc.addField(keyField);
        CtField typeField = new CtField(ClassPool.getDefault().get(Class.class.getName()), typeFieldName, cc);
        cc.addField(typeField, CtField.Initializer.byExpr(type.getName() + ".class"));
    }

    private void addEdgeGetter(String name, Class type, CtClass cc) throws CannotCompileException {
        String keyFieldName = keyFieldName(name);
        String typeFieldName = typeFieldName(type);

        String getterStr = "public " + type.getName() + " get" + name + "() { return GENERATED_CLASS_DBS.getDb(" + typeFieldName + ").get(" + keyFieldName + ");}";
        CtMethod method = CtNewMethod.make(getterStr, cc);
        cc.addMethod(method);
    }

    private void addEdgeSetter(String name, Class type, CtClass cc) throws CannotCompileException {
        String keyFieldName = keyFieldName(name);

        String setterStr = "public void set" + name + "(" + type.getName() + " val) { " + keyFieldName + " = val.getId();}";
        CtMethod method = CtNewMethod.make(setterStr, cc);
        cc.addMethod(method);
    }

    private String typeFieldName(Class type) {
        return "GENERATED_TYPE_FIELD_" + type.getSimpleName();
    }

    private String keyFieldName(String name) {
        return "GENERATED_KEY_FIELD_" + name;
    }

    private void addConstrutor(CtClass cc) throws CannotCompileException, NotFoundException {
        CtField f = new CtField(ClassPool.getDefault().get(MapDb.class.getName()), "GENERATED_CLASS_DBS", cc);
        cc.addField(f);
        CtConstructor defaultConstructor = CtNewConstructor.make("public " + cc.getSimpleName()
                + "(" + MapDb.class.getName() + " dbs) { GENERATED_CLASS_DBS = dbs; }", cc);
        cc.addConstructor(defaultConstructor);
    }

    private static class FieldInfo {
        private final Class type;
        private boolean isEdge = false;

        private FieldInfo(Class type) {
            this.type = type;
        }

        public Class getType() {
            return type;
        }

        public boolean isEdge() {
            return isEdge;
        }

        public void markAsEdge() {
            isEdge = true;
        }
    }
}
