package com.example.pr_1_file_dupe.service;

import java.util.List;
import java.util.Map;

import com.example.pr_1_file_dupe.DuplicateFinder;
import com.example.pr_1_file_dupe.FileData;

public class _check_fileScanner {

	public _check_fileScanner() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		
//		***************insert all file in List*************************
		
		
		FileScanner scanner = new FileScanner();
		List<FileData> files = scanner.scanDirectory("/home/tahaur/Downloads");
		double dupe_file_count =0;
//		for (FileData f : files) {
//		    System.out.println(file_count++ + f.getPath());
//		}
//		System.out.println("Program is running ......!");
//		
//
//		**********************Duplicates finder ******************************
		
		
		DuplicateFinder finder = new DuplicateFinder();

		Map<String, List<FileData>> result = finder.findDuplicates(files);
		Map<String, List<FileData>> duplicates = finder.getOnlyDuplicates(result);

		for (List<FileData> group : duplicates.values()) {
		    System.out.println(dupe_file_count++ + "Duplicate group:");
		    for (FileData f : group) {
		        System.out.println(f.getPath());
		    }
		}
	}

}
