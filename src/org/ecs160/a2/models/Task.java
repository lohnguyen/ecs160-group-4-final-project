package org.ecs160.a2.models;

import com.codename1.io.Externalizable;
import com.codename1.io.Util;
import org.ecs160.a2.utils.Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task implements Externalizable {

    public static final String OBJECT_ID = "Task";
    public static final String COUNTER_ID = "TaskCounter";
    public static final List<String> sizes = Arrays.asList("None", "S", "M", "L", "XL");

    private int id;
    private String title, description, size;
    private long totalTime; // total time spent (excluding in progress)
    private List<TimeSpan> timeSpans;
    private List<String> tags;

    // allow for the construction of a Task based on a title and description
    public Task(String title, String description) {
        // basic task internals
        this.title = title;
        this.description = description;
        this.size = "None";

        // task time internals
        this.totalTime = 0L;
        this.timeSpans = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // allow for the construction of a Task based on a title
    public Task(String title) {
        this(title, "None");
    }

    // used by AddNewTask, generate id
    public Task(String title, String desc, String size, List<String> tags) {
        this.id = Database.generateID(COUNTER_ID);
        this.title = title;
        this.description = desc;
        this.size = size;
        this.totalTime = 0L;
        this.timeSpans = new ArrayList<>();
        this.tags = tags;
    }

    public Task() {
        this("Task");
    }

    // for the basic task internals
    public int getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSize() {
        return this.size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // for the time internals
    public long getTotalTime() {
        return this.totalTime;
    }

    // NOTE: may remove getTimeSpans in favor of wrapper functions for the list
    public List<TimeSpan> getTimeSpans() {
        return this.timeSpans;
    }

    public List<String> getTags() {
        return this.tags;
    }

    /**
     * Gets the most recently added time span/window
     *
     * @return The TimeSpan object of the most recently added time span
     */
    public TimeSpan getMostRecentTimeSpan() {
        if (timeSpans.size() == 0) return null;
        else return this.timeSpans.get(timeSpans.size() - 1);
    }

    /**
     * Checks whether the most recent time span is currently in progress
     *
     * @return Boolean that indicates whether the Task is running
     */
    public boolean isInProgress() {
        TimeSpan currentTimeSpan = this.getMostRecentTimeSpan();
        return currentTimeSpan != null && currentTimeSpan.isRunning();
    }

    /**
     * Starts the task according to the given LocalDateTime
     *
     * @param startTime The start time of the new TimeSpan
     */
    public void start(LocalDateTime startTime) {
        TimeSpan currentTimeSpan = this.getMostRecentTimeSpan();
        // task not running

        if (currentTimeSpan == null || (currentTimeSpan != null && 
                                        !currentTimeSpan.isRunning())) {

            this.timeSpans.add(new TimeSpan(startTime, null));
        } else {
            // TODO throw a custom exception if something is running
        }
    }

    /**
     * Starts the task at the current system time
     */
    public void start() {
        this.start(LocalDateTime.now());
    }

    /**
     * Stops the task according to the given LocalDateTime
     *
     * @param stopTime The stop time of the currently running TimeSpan
     */
    public void stop(LocalDateTime stopTime) {
        TimeSpan currentTimeSpan = this.getMostRecentTimeSpan();
        if (currentTimeSpan != null && currentTimeSpan.isRunning()) {
            currentTimeSpan.stopSpan(stopTime);
            this.totalTime = this.calculateTotalTime().toMillis();
        } else {
            // TODO throw a custom exception if nothing is running
        }
    }

    /**
     * Stops the task at the current system time
     */
    public void stop() {
        this.stop(LocalDateTime.now());
    }

    // NOTE: could be public, or called every time total time is
    private Duration calculateTotalTime() {
        return TimeSpan.getTotalDuration(this.timeSpans);
    }

    /*
     * Reference: calculate time difference from milliseconds
     * https://stackoverflow.com/questions/4142313/convert-timestamp-in-
     * milliseconds-to-string-formatted-time-in-java/16520928#16520928
     */
    public String getTotalTimeStr() {
        long second = totalTime / 1000 % 60;
        long minute = totalTime / (1000 * 60) % 60;
        long hour = totalTime / (1000 * 60 * 60);

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void externalize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        Util.writeUTF(title, out);
        Util.writeUTF(description, out);
        Util.writeUTF(size, out);
        out.writeLong(totalTime);
        Util.writeObject(tags, out);
        Util.writeObject(timeSpans, out);
    }

    @Override
    public void internalize(int version, DataInputStream in) throws IOException {
        id = in.readInt();
        title = Util.readUTF(in);
        description = Util.readUTF(in);
        size = Util.readUTF(in);
        totalTime = in.readLong();
        tags = (List<String>) Util.readObject(in);
        timeSpans = (List<TimeSpan>) Util.readObject(in);
    }

    @Override
    public String getObjectId() {
        return OBJECT_ID;
    }
}
