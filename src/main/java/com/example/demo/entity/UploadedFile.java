package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mimeType;
    private String fileName;
    private String url;
    private Integer size;
}
