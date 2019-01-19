import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import java.util.Collections;
import java.util.Arrays;
import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class Randomizer extends Application {

    private ImageView view = new ImageView();
    private int imageIndex = 0;     // Index of the image the user is currently viewing
    private double screen_x;    // Width of the user's screen
    private double screen_y;    // Height of the user's screen
    private double button_width = 40.0;     // Width of the buttons used to navigate the images with (in px).

    @Override
    public void start(Stage stage) {
        HBox root = new HBox();

        // Setting the folder from which the pictures will be pulled and assigning the names of said pictures to a list
        System.out.print("Enter full path or name of directory: ");
        Scanner s = new Scanner(System.in);
        String folderName = s.nextLine();
        File folder = findDirectory(folderName, s);
        if (folder == null) {
            System.out.println("Directory not found");
            System.exit(0);
        }

        // Adding all files of filetype jpg (,jpeg) or png onto the ArrayList
        ArrayList<File> names = new ArrayList<>(Arrays.asList(folder.listFiles(
                    (a, b) ->   b.toLowerCase().endsWith(".jpg") ||
                                b.toLowerCase().endsWith(".png"))));
        if(names.size() == 0) {
            System.out.println("Directory contains no suitable images");
            System.exit(0);
        }
        Collections.shuffle(names);     // Shuffling the images to achieve pseudo-randomness in their order

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();     // Getting the dimensions of the user's screen. We want to maximize the use of screen-space.
        screen_x = screenSize.getWidth();
        screen_y = screenSize.getHeight();

        assignImage(new Image("file:" + folder.getPath() + "\\" + new File(names.get(imageIndex).getName())));      // Assigning the first image

        Button previous = new Button("<");  // Button for moving to previous image
        previous.setOnAction(event -> {
            previous_image(names, folder);
        });

        Button next = new Button(">");  // Button for moving to next image
        next.setOnAction(event -> {
           next_image(names, folder);
        });

        //Stylizing the buttons
        next.setFont(new Font("Calibri", 20.0));
        previous.setFont(new Font("Calibri", 20.0));
        next.setPrefHeight(screen_y);
        previous.setPrefHeight(screen_y);
        next.setMinWidth(button_width);
        previous.setMinWidth(button_width);

        // The StackPane contains the ImageView-object
        StackPane pane = new StackPane();
        pane.getChildren().add(view);
        pane.setAlignment(root, Pos.CENTER);
        pane.setPrefWidth(screen_x);

        root.getChildren()
                .addAll(previous, pane, next);

        Scene scene = new Scene(root, screen_x - (2 * button_width), screen_y);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {  // We also listen to keyboard inputs, giving the user the choice to navigate the images with their keyboard's arrow-keys.
            if (key.getCode() == KeyCode.LEFT) {
                previous_image(names, folder);
            } else if (key.getCode() == KeyCode.RIGHT) {
                next_image(names, folder);
            }
        });

        stage.setTitle(
                "Randomizer");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);

        stage.show();
    }

    // The method that is called whenever the user wishes to move to the previous image.
    private void previous_image(ArrayList<File> names, File folder) {
        if (imageIndex == 0) {
            return;
        }
        imageIndex -= 1;

        assignImage(new Image("file:" + folder.getPath() + "\\" + new File(names.get(imageIndex).getName())));
    }

    // The method that is called whenever the user wishes to move to the next image.
    private void next_image(ArrayList<File> names, File folder) {
        if (imageIndex == names.size() - 1) {
            return;
        }
        imageIndex += 1;

        assignImage(new Image("file:" + folder.getPath() + "\\" + new File(names.get(imageIndex).getName())));
    }

    // Calculates the desired dimensions of the given Image-object and assigns it to the program's default ImageView-object
    private void assignImage(Image image) {
        // Calculate the value by which the dimensions of the picture will be multiplied. The width will always be equal to or below screen_x - (2 * button_width) while the height will always be equal to or below screen_y, respectively.
        // The important part here is that the dimensions of the picture should not be altered even though its size most likely will.
        double x = image.getWidth();
        double y = image.getHeight();
        if (screen_y / y < (screen_x - (2 * button_width)) / x) {

            // If the multiplier of height is smaller than the multiplier of width...
            double multiplier = screen_y / y;

            view.setFitWidth(x * multiplier);
            view.setFitHeight(screen_y);
        } else {

            // If the multiplier of width is smaller than the multiplier of height...
            double multiplier = screen_x / x;

            view.setFitWidth(screen_x - (2 * button_width));
            view.setFitHeight(y * multiplier);
        }
        view.setImage(image);
    }

    // Goes through the entire C-drive and all of its sub-directories in search of folders with a name that matches the variable "toFind". Alternatively, if the toFind-variable is itself a path to a directory, we use it as is.
    // A windows-based system where the primary drive is called "C" is expected.
    private File findDirectory(String toFind, Scanner s) {
        try {
            File file = new File(toFind);
            if (file.isDirectory()) {
                return file;
            }
        } catch (Exception e) {
            System.out.println("Beginning search");
        }
        ArrayList<File> directories_found = new ArrayList<>();
        File root = new File("C:\\");

        // The directories-list contains only directories still under consideration for having the folder being searched for as a sub-directory
        List<File> directories = new ArrayList<>();
        directories.add(root);

        // The toBeAdded-list contains directories that will be searched if none of the folders in the directories-list prove to have what we're looking for
        List<File> toBeAdded = new ArrayList<>();
        boolean foundMoreDirectories = false;

        while (true) {
            for (File directory : directories) {
                for (File fileCurrent : directory.listFiles()) {

                    // Only directories that haven't been found yet are noted
                    if (fileCurrent.isDirectory() && !directories.contains(fileCurrent)) {

                        // At this point a new directory has been found, checks for whether or not it is usable follow
                        String pathName = directory + "\\" + fileCurrent.getName();
                        Path path = Paths.get(pathName);
                        if (Files.isReadable(path)) {
                            // Check for whether or not the directory that has been found is the one being looked for
                            if (fileCurrent.getName().toLowerCase().equals(toFind.toLowerCase())) {
                                while(true) {
                                    System.out.print("Found " + fileCurrent + ", do you wish to use this directory? (Y/N)\t");
                                    String dir_found_str = s.nextLine();
                                    if (dir_found_str.toLowerCase().equals("y")) {
                                        return fileCurrent;
                                    } else if (dir_found_str.toLowerCase().equals("n")) {
                                        directories_found.add(fileCurrent);
                                        break;
                                    }
                                }
                            }

                            // Even if the directory is not the correct one, it could still contain what we're after and is thus added to the toBeAdded-list
                            toBeAdded.add(fileCurrent);
                            foundMoreDirectories = true;
                        }
                    }
                }
            }

            // As long as the method finds new directories it will continue to look through them. Previously searched directories will not be searched again.
            if (foundMoreDirectories) {
                foundMoreDirectories = false;
                directories.clear();
                directories.addAll(toBeAdded);
                toBeAdded.clear();
            } else {
                break;
            }
        }

        switch(directories_found.size()) {
            case 0:
                return null;    //If no matching directory has been found the method returns null
            case 1:
                return directories_found.get(0);
        }
        System.out.println("Found the following directories: ");
        for(int dir_index = 0; dir_index < directories_found.size(); dir_index++) {
            System.out.println(dir_index + ": " + directories_found.get(dir_index).getPath());
        }

        while(true) {
            System.out.print("Which one do you wish to use? ");
            String input_str = s.nextLine();
            try {
                int input = Integer.parseInt(input_str);
                if (input >= 0 && input < directories_found.size()) {
                    return directories_found.get(input);
                }
            } catch (Exception e) {
                System.out.println("Invalid input...");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
