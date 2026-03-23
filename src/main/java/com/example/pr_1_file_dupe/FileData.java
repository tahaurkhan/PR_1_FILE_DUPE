package com.example.pr_1_file_dupe;

 public class FileData {

    private String name;
    private String type;
    private long size;
    private String path;

    public FileData(String name, String type, long size, String path) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.path = path;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public long getSize() { return size; }
    public String getPath() { return path; }
}