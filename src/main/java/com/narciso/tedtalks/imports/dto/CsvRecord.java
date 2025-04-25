package com.narciso.tedtalks.imports.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvRecord {
    @CsvBindByName(column = "title", required = true)
    private String title;

    @CsvBindByName(column = "author", required = true)
    private String author;

    @CsvBindByName(column = "date", required = true)
    private String date;

    @CsvBindByName(column = "views", required = true)
    private String views;

    @CsvBindByName(column = "likes", required = true)
    private String likes;

    @CsvBindByName(column = "link", required = true)
    private String link;
}
