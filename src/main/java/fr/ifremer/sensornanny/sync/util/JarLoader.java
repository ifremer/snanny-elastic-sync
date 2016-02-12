package fr.ifremer.sensornanny.sync.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * Class that allow to scan jars in folder and classpathscan classes in jars
 * 
 * @author athorel
 *
 */
public class JarLoader {

    private static final Logger logger = Logger.getLogger(JarLoader.class.getName());

    /**
     * This method allow to discovers jars in a specific folder
     * 
     * @param folderPath folder path
     * @return list of jar url in a folder
     */
    public static URL[] discoverJars(String folderPath) {
        logger.info("Configure parsers in folder " + folderPath);
        if (StringUtils.isBlank(folderPath)) {
            throw new IllegalStateException("Unable to init parsers, the parameter parser.lib must be filled");
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) {
            throw new IllegalStateException("Unable to init parsers, the folder " + folderPath
                    + " doesn't exist or can't be read");
        }

        // Discover jar
        File[] jarsFiles = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (ArrayUtils.isEmpty(jarsFiles)) {
            throw new IllegalStateException("Unable to find jar archive parsers in the folder " + folderPath);
        }

        URL[] url = new URL[jarsFiles.length];
        int i = 0;
        for (File jar : jarsFiles) {
            url[i] = toURL(jar);
            i++;
        }

        return url;

    }

    /**
     * This method allow searching
     * 
     * @param urls urls to load in classpath
     * @param clazz clazz to search
     * @param packagePrefix package prefix to allow faster search
     * @return list of class that implements the <T> interface
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> scanForInterfaces(URL[] urls, Class<T> clazz, String packagePrefix) {
        // Load classes
        URLClassLoader parserClassLoader = URLClassLoader.newInstance(urls, clazz.getClassLoader());

        String urlStr = StringUtils.join(urls, ";");
        List<String> namesOfClassesImplementing = new FastClasspathScanner(packagePrefix).overrideClasspath(urlStr)
                .scan().getNamesOfClassesImplementing(clazz);

        List<T> items = new ArrayList<>();
        for (String className : namesOfClassesImplementing) {
            try {
                logger.info("Found parser " + className);
                items.add((T) parserClassLoader.loadClass(className).newInstance());
            } catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate class " + className, e);
            }
        }

        return items;
    }

    /**
     * Transform a file to an URL
     * 
     * @param file file to transform
     * @return url of the file
     */
    private static URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to load jars " + file, e);
        }
    }
}
