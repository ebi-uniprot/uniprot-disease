//Using the plural for packages with homogeneous contents and the singular for packages with heterogeneous contents.
package uk.ac.ebi.uniprot.uniprotdisease;

import uk.ac.ebi.uniprot.uniprotdisease.services.DiseaseService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@SpringBootApplication
public class UniprotDiseaseApplication {
    private static final Logger LOG = LoggerFactory.getLogger(UniprotDiseaseApplication.class);

    public static void main(String[] args) {

        final List<String> argList =
                Stream.of(args).filter(s -> !s.equalsIgnoreCase("--stopserver")).collect(Collectors.toList());

        // user wants to import new data, Delete existing database
        if (!argList.isEmpty()) {
            deleteExistingDatabase();
        }

        //Starting spring application context
        final ApplicationContext context = SpringApplication.run(UniprotDiseaseApplication.class, args);
        final DiseaseService diService = context.getBean(DiseaseService.class);

        // Import only diseases and create new database
        if (argList.size() == 1) {
            // 1st parameter is to import the disease file
            diService.importDiseaseEntriesFromFileIntoDb(argList.get(0));
        }

        // Import Diseases, reference count and create new database
        if (argList.size() > 1) {
            //1st file is diseases, 2nd is reference
            diService.importDiseaseAndReferenceCountFromFilesIntoDb(argList.get(0), argList.get(1));
        }

        // Stop server if user just want to import data, using while creating docker image
        if (Stream.of(args).anyMatch(s -> s.equalsIgnoreCase("--stopserver"))) {
            ((ConfigurableApplicationContext) context).close();
            System.exit(0);
        }
    }

    private static void deleteExistingDatabase() {
        try {
            final Properties appProp = PropertiesLoaderUtils.loadAllProperties("application.properties");
            final String databaseUrl = appProp.getProperty("spring.mongodb.embedded.storage.database-dir");

            Path rootPath = null;
            if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
                rootPath = Paths.get(databaseUrl.trim());
            }

            // Start delete if only path exists
            if (rootPath != null && Files.exists(rootPath)) {
                LOG.info("Deleting Database from {}", rootPath);
                try (Stream<Path> paths = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)) {
                    paths.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }

        } catch (IOException e) {
            LOG.error("Deleting old database failed, but ignoring it and starting spring context");
            LOG.error("delete database failed due to ", e);
        }
    }
}