package com.prod.artchain.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Submission implements Serializable {
    private String paintingId;
    private String roundId;
    private String awardId;
    private int contestId;
    private String competitorId;
    private String description;
    private String title;
    private String imageUrl;
    private Date submissionDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Contest contest;
}
