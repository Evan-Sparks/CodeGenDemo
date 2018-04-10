package com.demo;

import com.demo.db.MapDb;
import com.demo.spring.InterfaceComponentProvider;
import com.demo.types.markers.Cache;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to scan the classpath for Identifiable interfaces annotated with @Cache and generate a DataRepository
 * instance for each of them.
 */
public class DataRepositoryGenerator {
    public Map<Class, DataRepository> GenerateRepositories() {
        ClassPathScanningCandidateComponentProvider scanner = new InterfaceComponentProvider();
        scanner.addIncludeFilter(new AnnotationTypeFilter(Cache.class));

        MapDb dbs = new MapDb();
        Map<Class, DataRepository> dataRepositories = new HashMap<>();

        for (BeanDefinition bd : scanner.findCandidateComponents("com.demo")) {
            try {
                Class interfaceType = Class.forName(bd.getBeanClassName());
                DataRepository repository = new DataRepository(interfaceType, dbs);

                dataRepositories.put(interfaceType, repository);
            } catch (ClassNotFoundException | NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }

        }

        return dataRepositories;
    }
}
