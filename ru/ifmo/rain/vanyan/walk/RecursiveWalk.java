package ru.ifmo.rain.vanyan.walk;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid input parameter. Read instruction and try again");
            return;
        }
        Path in, out;
        try {
            in = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path name of input file in program arguments " + args[0]);
            return;
        }
        try {
            out = Paths.get(args[1]);
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
        }
        catch (IOException e) {
            System.out.println("Exception while create parent directory" + args[1]);
            return;
        }
        catch (InvalidPathException e) {
            System.out.println("Invalid path name of output file in program arguments " + args[1]);
            return;

        }
        if (!Files.exists(in)) {
            System.out.println("Input file \"" + in.toString() + "\" does not exist");
            return;
        }


        try (BufferedWriter outWriter = Files.newBufferedWriter(out)) {
            Stream<String> lines = Files.lines(in);
            lines.forEach(x -> {
                Path inputFile;
                try {
                    inputFile = Paths.get(x);
                    if (Files.exists(inputFile) && Files.isDirectory(inputFile)) {
                        Stream<Path> paths = Files.walk(inputFile);
                        paths.filter(Files::isRegularFile).forEach(path -> writeHash(hash(path), path.toString(), outWriter));
                    } else {
                        writeHash(hash(inputFile), inputFile.toString(), outWriter);
                    }
                } catch (IOException e) {
                    System.out.println("Exception occurred while writing" + e.getMessage());
                } catch (InvalidPathException e) {
                    System.out.println("InvalidPathException during access to starting path for walking. Assumed that it's a file and wrote null-hash for this path. Exception " + "\"" + e.getMessage());
                    writeHash(0, x, outWriter);
                }
            });
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Exception occurred while writing");
        }
    }

    static private int hash(Path file) {
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            int h = 0x811c9dc5;
            int b;
            while ((b = is.read()) != -1) {
                h = (h * 0x01000193) ^ (b & 0xff);
            }
            return h;
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e.getMessage());
            return 0;
        } catch (IOException e) {
            System.out.println("Exception was thrown at the hash calculation reading state. File " + "\"" + file + "\" " + "couldn't be visited with I/O exception " + "\"" + e.getClass().getCanonicalName());
            return 0;
        }
    }

    static private void writeHash(int num, String file, BufferedWriter outWriter) {
        try {
            outWriter.write(String.format("%08x %s%n", num, file));
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Exception was thrown while writing the hash of the " + "\"" + file + "\" which is equal to" + num + ". Exception " + "\"" + e.getMessage());
        }
    }
}