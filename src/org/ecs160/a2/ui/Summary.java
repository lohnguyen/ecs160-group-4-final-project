package org.ecs160.a2.ui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;

import org.ecs160.a2.models.Task;

public class Summary extends Container {

    // might move these to some other class if they need to be shared
    private static final Font nativeLight = Font.createTrueTypeFont("native:MainLight");
    private static final Font nativeRegular = Font.createTrueTypeFont("native:MainRegular");
    private static final Font nativeBold = Font.createTrueTypeFont("native:MainBold");

    // NOTE: this is temporary and will be changed to work with the
    // database later on!
    private static List<Task> taskList = new ArrayList<Task>();

    // some conversion factors
    private static long MILIS_TO_HOURS = 3600000L;

    
    private Container page1, page2;
    private Container taskContainer, sizeContainer, statsContainer; // labels

    public Summary () {
        super(new BoxLayout(BoxLayout.Y_AXIS));
        this.setScrollableY(true);

        // temporary Tasks for the summary
        taskList.add(new Task("Task 1"));
        taskList.add(new Task("Task 2"));
        taskList.get(0).setSize("M");
        taskList.get(0).start(LocalDateTime.of(2021, 2, 21, 5, 0));
        taskList.get(0).stop(LocalDateTime.of(2021, 2, 21, 7, 0));
        taskList.get(1).setSize("L");
        taskList.get(1).start(LocalDateTime.of(2021, 2, 22, 6, 0));
        taskList.get(1).stop(LocalDateTime.of(2021, 2, 23, 7, 0));

        // title
        this.add(createLabel("Summary", nativeBold, 0x000000, 8.0f));

        // Selection
        Container buttonContainer = new Container(new GridLayout(1, 2));
        Button page1Button = new Button ("Everything");
        page1Button.addActionListener((e) -> selectPageButtonAction(e));
        buttonContainer.add(page1Button);
        Button page2Button = new Button ("Tasks");
        page2Button.addActionListener((e) -> selectPageButtonAction(e));
        buttonContainer.add(page2Button);
        this.add(buttonContainer);

        // setup the different summary pages
        this.setupPage1();
        this.setupPage2();

        // call function on refresh (temporary, can have a better solution)
        this.addPullToRefresh(() -> updateVisibleContainers());
        this.updateVisibleContainers();

    }

    private void selectPageButtonAction (ActionEvent e) {
        Button button = (Button) e.getComponent();
        switch (button.getText()) {
            case "Everything":
                this.page1.setVisible(true);
                this.page2.setVisible(false);
                break;
            case "Tasks":
                this.page1.setVisible(false);
                this.page2.setVisible(true);
        }
    }

    private void setupPage1 () {
        this.page1 = new Container(new BoxLayout(BoxLayout.Y_AXIS));

        // Tasks
        page1.add(createLabel("Tasks", nativeBold, 0x000000, 5.5f));
        this.taskContainer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        page1.add(this.taskContainer);  

        // Sizes
        page1.add(createLabel("Sizes", nativeBold, 0x000000, 5.5f));
        this.sizeContainer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        page1.add(this.sizeContainer);  

        // Stats
        page1.add(createLabel("Statistics", nativeBold, 0x000000, 5.5f));
        this.statsContainer = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        page1.add(this.statsContainer);  

        this.page1.setVisible(true);
        this.add(page1);
    }

    private void setupPage2 () {
        this.page2 = new Container(new BoxLayout(BoxLayout.Y_AXIS));

        this.page2.setVisible(false);
        this.add(page2);
    }

    // create a label based on the specified parameters
    private Label createLabel (String labelText, Font style, int color, 
                                                             float fontSize) {
        Label label = new Label(labelText);

        int pixelSize = Display.getInstance().convertToPixels(fontSize);
        label.getAllStyles().setFont(style.derive(pixelSize, 
                                                  Font.STYLE_PLAIN));
        label.getAllStyles().setFgColor(color);

        return label;
    }


    // get a list of labels that correspond to amount given
    private List<Label> getLabelsToUpdate (Container container, 
                                           int labelCount) {
        int i;
        List<Label> returnLabels = new ArrayList<Label>();

        // loop through the task list, adding new labels if necessary
        for (i = 0; i < labelCount; i++) {
            Label label;

            if (i < container.getComponentCount()) {
                label = (Label) container.getComponentAt(i);
            } else {
                label = createLabel("", nativeLight, 0x000000, 3.0f);
                container.add(label);
            }

            returnLabels.add(label);
        }

        // remove the extra labels from the component
        while (i < container.getComponentCount()) {
            Component extraLabel = container.getComponentAt(i);
            container.removeComponent(extraLabel);
        }

        return returnLabels;
    }

    // Updates task labels in the Tasks section
    // NOTE: could be more effecient with callbacks
    private void updateTaskLabels () {
        List<Label> labels = getLabelsToUpdate(this.taskContainer, 
                                               taskList.size());
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i); // update the label w/ its
            Label label = labels.get(i); // corresponding task
            label.setText(" - " + (task.getTotalTime() / MILIS_TO_HOURS) +
                          " hours total for " + task.getTitle());
        }
    }

    // Updates size labels in the Size section
    private void updateSizeLabels () {
        // use a map to keep track of the current totals for the sizes
        Map<String, Long> sizeStatsMap = new HashMap<String, Long>();

        // add up totals for the different sizes
        for (Task task : taskList) {
            if (task.getSize().length() == 0) continue; // no empty
            long sizeTime = task.getTotalTime();
            if (sizeStatsMap.containsKey(task.getSize())) {
                sizeTime += sizeStatsMap.get(task.getSize());
            }
            sizeStatsMap.put(task.getSize(), sizeTime);
        }

        Object[] availableSizes = sizeStatsMap.keySet().toArray();
        List<Label> labels = getLabelsToUpdate(this.sizeContainer, 
                                               sizeStatsMap.keySet().size());
        for (int i = 0; i < availableSizes.length; i++) {
            String size = (String) availableSizes[i];
            Label label = labels.get(i);
            label.setText(" - " + (sizeStatsMap.get(size) / MILIS_TO_HOURS) +
                          " hours total for " + size);
        }
    }

    // Update labels in the Statistics section
    // NOTE: can lessen calls by making the amount of labels constant
    private void updateStatsLabels () {
        List<Label> labels = getLabelsToUpdate(this.statsContainer, 3);
        long min = -1L, average = -1L, max = -1L;

        // calculate the stats
        for (Task task : taskList) {
            long taskTime = task.getTotalTime();
            if (min < 0 || min > taskTime) min = taskTime;
            if (max < 0 || max < taskTime) max = taskTime;
            average += taskTime;
        }
        average /= taskList.size();

        // update the constant labels
        labels.get(0).setText(" - " + (min / MILIS_TO_HOURS) + 
                              " hours minimum");
        labels.get(1).setText(" - " + (max / MILIS_TO_HOURS) + 
                              " hours maximum");
        labels.get(2).setText(" - " + (average / MILIS_TO_HOURS) + 
                              " hours average");
    }

    // called whenever the labels need updating
    // TODO: onload? on refresh?
    public void updateVisibleContainers () {
        if (taskList.size() > 0) {
            this.updateTaskLabels();
            this.updateSizeLabels();
            this.updateStatsLabels();
        }
    }

    // definitely gonna change this, unneeded since this class is a container
    public Container get () {
        return this;
    }
}