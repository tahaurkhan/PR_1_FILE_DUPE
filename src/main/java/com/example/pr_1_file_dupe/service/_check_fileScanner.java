package com.example.pr_1_file_dupe.service;

import java.util.List;

import com.example.pr_1_file_dupe.FileData;

public class _check_fileScanner {

	public _check_fileScanner() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileScanner scanner = new FileScanner();
		List<FileData> files = scanner.scanDirectory("/home/tahaur/Downloads");
		double file_count =0;
		for (FileData f : files) {
		    System.out.println(file_count++ + f.getPath());
		}
		System.out.println("Program is running ......!");
	}

}
