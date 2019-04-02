package info.alexhawley;

import com.kodehelp.sftp.ListRecursiveFolderSFTP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.exit;

public class Main {

    String host = "";
    String Username = "";
    String Password = "";
    String RemoteDir = "";
    String LocalDir = "";

    public static String folderSearchTerm = "";
    public static String fileSearchTerm = "";
    public static List<String> uniqueFolderPaths = new ArrayList<String>();

    int port = 22;

    public static void main(String[] args) {

        Main main = new Main();
        main.loadConfig();

        if (args.length > 0) {
            folderSearchTerm = args[0];
        }
        if (args.length > 1) {
            fileSearchTerm = args[1];
        }

        if (args.length < 2) {
            System.out.println("Please pass an argument for the FolderSearchTerm, as well as FileSearchTerm ");
            exit(0);
        }
        if (args[0].equals("-h")) {
            System.out.println("Usage: " + "\n\t" + "java -jar checkFilePaths.jar <folderSearchTerm> <fileSearchTerm>");
            System.out.println("Help: " + "\n\t" + "Checks all file paths in a folder by a search term, and then searches the content of those files for another search term.");
            System.out.println("\t" + "<folderSearchTerm> - Search Term for file paths in intial folder");
            System.out.println("\t" + "<fileSearchTerm> - Search Term for content of all files found in the first search");
            exit(1);
        }

        main.deleteDirectory(new File("C:\\Temp\\CheckFilePath"));

        File dir = new File("C:\\Temp\\CheckFilePath");

        dir.mkdir();

        ListRecursiveFolderSFTP Files = new ListRecursiveFolderSFTP(main.host, main.Username, main.Password, main.RemoteDir, main.LocalDir, main.port);

        Collections.sort(uniqueFolderPaths);

        boolean hasWritten = false;

        boolean doFind = !(Main.fileSearchTerm.contains("!"));
        Main.fileSearchTerm = Main.fileSearchTerm.replace("!", "");
        for (String filePath: uniqueFolderPaths) {
            BufferedReader reader;

            boolean didFind = false;
            try {
                reader = new BufferedReader(new FileReader(filePath));
                String line = reader.readLine();
                if (line != null) {
                    if ((line.contains(Main.fileSearchTerm))) {
                        didFind = true;
                    }
                }
                while (line != null) {
                    line = reader.readLine();
                    if (line != null) {
                        if ((line.contains(Main.fileSearchTerm))) {
                            didFind = true;
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ((doFind && didFind) || (!doFind && !didFind)) {

                try  {

                    if (!hasWritten) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Temp\\check_file_paths_output.txt"));
                        writer.write(filePath.replace(main.LocalDir, "") + "\n");
                        writer.close();
                        System.out.println(filePath.replace(main.LocalDir, ""));
                        hasWritten = true;
                    } else {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Temp\\check_file_paths_output.txt", true));
                        writer.append(filePath.replace(main.LocalDir, "") + "\n");
                        System.out.println(filePath.replace(main.LocalDir, ""));
                        writer.close();
                    }

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void loadConfig() {

        String file = "C:\\Temp\\check_file_paths_config.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                String[] lineArr = line.split("\t");
                if (lineArr.length > 1) {
                    switch (lineArr[0])
                    {
                        case "host":
                            this.host = lineArr[1];
                            break;
                        case "username":
                            this.Username = lineArr[1];
                            break;
                        case "password":
                            this.Password = lineArr[1];
                            break;
                        case "remote_directory":
                            this.RemoteDir = lineArr[1];
                            break;
                        case "local_directory":
                            this.LocalDir = lineArr[1];
                            break;
                        case "port":
                            this.port = Integer.parseInt(lineArr[1]);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
