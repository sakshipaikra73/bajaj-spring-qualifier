package com.example.webhooksolver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String regNo;

    // store the final SQL query text
    @jakarta.persistence.Lob
    private String finalQuery;

    private Instant submittedAt;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getFinalQuery() { return finalQuery; }
    public void setFinalQuery(String finalQuery) { this.finalQuery = finalQuery; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
