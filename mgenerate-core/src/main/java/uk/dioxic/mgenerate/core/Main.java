package uk.dioxic.mgenerate.core;

import org.apache.commons.cli.ParseException;
import uk.dioxic.mgenerate.core.exception.CliArgumentException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException, ParseException, CliArgumentException {

        CliOptions cli = new CliOptions(args);

        Writer writer = null;

        Long start = System.currentTimeMillis();

        if (cli.isConsoleOutput()) {
            writer = new OutputStreamWriter(System.out);
        }
        else if (cli.isSingleFileOuput()) {
            System.out.println("Writing to " + cli.getOutputPath());
            writer = Files.newBufferedWriter(cli.getOutputPath());
        }

        for (Template template : cli.getTemplates()) {

//            if (cli.isMultiFileOutput()) {
//                Path outputFile = cli.getOutputPath().resolve(template.getName());
//                System.out.println("Writing to " + outputFile);
//                writer = Files.newBufferedWriter(outputFile);
//            }
            try (PrintWriter pw = new PrintWriter(writer)) {
                Stream.generate(template::toJson)
                        .limit(cli.getNumber())
                        .forEach(pw::println);
            }
            if (cli.isMultiFileOutput()) {
                writer.close();
            }
        }

        if (cli.isSingleFileOuput()) {
            writer.close();
        }

        Long end = System.currentTimeMillis();

        Long speed = Double.valueOf(cli.getNumber().doubleValue() * 1000 / (end - start)).longValue();
        System.out.printf("Producting %s documents took %sms (%s docs/s)%n", cli.getNumber(), end - start, speed);
    }


}