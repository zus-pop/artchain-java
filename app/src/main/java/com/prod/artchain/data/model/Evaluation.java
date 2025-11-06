package com.prod.artchain.data.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Evaluation {
    private String id;
    private String paintingId;
    private String examinerId;
    private String examinerName;
    private int scoreRound1;
    private String feedback;
    private Date evaluationDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
